(ns in.facebookleads.google-sheets
  (:require [com.biffweb :as biff]
            [jsonista.core :as j]
            [xtdb.api :as xt]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log])
  (:import [com.google.api.client.googleapis.javanet GoogleNetHttpTransport]
           [com.google.api.client.json.gson GsonFactory]
           [com.google.api.services.sheets.v4 Sheets$Builder SheetsScopes]
           [com.google.auth.oauth2 ServiceAccountCredentials]
           [com.google.auth.http HttpCredentialsAdapter]))

(def ^:private json-factory (GsonFactory/getDefaultInstance))

(defn- create-sheets-service []
  (let [credentials-file (io/resource "google-service-account.json")]
    (if-not credentials-file
      (do
        (log/error "Google service account JSON file not found in resources/")
        nil)
      (let [http-transport (GoogleNetHttpTransport/newTrustedTransport)
            credentials (.createScoped
                         (ServiceAccountCredentials/fromStream (io/input-stream credentials-file))
                         [SheetsScopes/SPREADSHEETS_READONLY])]
        (-> (Sheets$Builder. http-transport json-factory (HttpCredentialsAdapter. credentials))
            (.setApplicationName "facebook-leads-crm")
            (.build))))))

(defn fetch-sheet-rows [sheet-id sheet-name]
  (if-let [service (create-sheets-service)]
    (try
      (let [range (str "'" sheet-name "'!A:Z")
            _ (log/info "Fetching sheet range:" range)
            response (-> service
                         (.spreadsheets)
                         (.values)
                         (.get sheet-id range)
                         (.execute))
            values (.getValues response)]
        (if values
          (map vec values)
          []))
      (catch Exception e
        (log/error e "Error fetching Google Sheet rows")
        []))
    (do
      (log/error "Sheets service not initialized")
      [])))

(defn parse-rows [rows]
  (when (seq rows)
    (let [header (map keyword (first rows))
          data (rest rows)]
      (for [row data]
        (zipmap header row)))))

(defn- parse-date [date-str]
  (try
    (if (string? date-str)
      (let [s (clojure.string/replace date-str " " "T")
            s (if (not (re-find #"[Z+-]" s)) (str s "Z") s)]
        (java.util.Date/from (java.time.Instant/parse s)))
      (java.util.Date.))
    (catch Exception _
      (java.util.Date.))))

(defn sync-google-leads [{:keys [biff.xtdb/node] :as ctx}]
  (let [sheet-id (:google/sheet-id ctx)
        sheet-name (:google/sheet-name ctx)]
    (if (or (empty? sheet-id) (empty? sheet-name))
      (do
        (log/error "Google Sheet ID or Name not configured")
        {:success false :error "Config missing"})
      (do
        (log/info "Starting Google Sheets sync for ID:" sheet-id "Name:" sheet-name)
        (let [rows (fetch-sheet-rows sheet-id sheet-name)
              leads (parse-rows rows)
              db (xt/db node)
              new-leads (filter (fn [l]
                                  (and (= "CREATED" (some-> (:lead_status l) clojure.string/trim))
                                       (empty? (biff/q db '{:find (pull l [:xt/id])
                                                            :in [?id]
                                                            :where [[l :lead/facebook-lead-id ?id]]}
                                                       (str (:id l))))))
                                leads)]
          (log/info "Fetched" (count leads) "leads from sheet." (count new-leads) "are new.")
          (doseq [l new-leads]
            (xt/submit-tx node [[::xt/put {:xt/id (java.util.UUID/randomUUID)
                                           :lead/facebook-lead-id (str (:id l))
                                           :lead/name (or (:full_name l) "Unknown")
                                           :lead/email (or (:email l) "Unknown")
                                           :lead/form-id (str (:form_id l))
                                           :lead/form-name (str (:form_name l))
                                           :lead/platform (str (:platform l))
                                           :lead/ad-id (str (:ad_id l))
                                           :lead/ad-name (str (:ad_name l))
                                           :lead/campaign-id (str (:campaign_id l))
                                           :lead/campaign-name (str (:campaign_name l))
                                           :lead/created-at (parse-date (:created_time l))
                                           :lead/source "google-sheet"}]]))
          {:success true
           :imported (count new-leads)})))))

(defn sync-handler [ctx]
  (let [result (sync-google-leads ctx)]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (j/write-value-as-string result)}))

(def module
  {:routes [["/sync/google-leads" {:post sync-handler}]]
   :tasks [{:task sync-google-leads
            :schedule #(iterate (fn [t] (biff/add-seconds t 60)) (java.util.Date.))}]})

