(ns in.facebookleads.facebook
  (:require [com.biffweb :as biff]
            [clj-http.client :as http]
            [jsonista.core :as j]
            [in.facebookleads.ui :as ui]
            [in.facebookleads.middleware :as mid]
            [xtdb.api :as xt]))

(defn fb-auth-url [{:keys [biff/secret] :as ctx}]
  (let [client-id (secret :facebook/client-id)
        redirect-uri (or (:facebook/redirect-uri ctx) "http://localhost:8080/facebook/callback")]
    (str "https://www.facebook.com/v19.0/dialog/oauth?"
         (clojure.string/join
          "&"
          (for [[k v] {:client_id client-id
                       :redirect_uri redirect-uri
                       :scope "public_profile,email,pages_show_list,pages_read_engagement,pages_manage_ads,leads_retrieval,pages_manage_metadata"}]
            (str (name k) "=" v))))))

(defn auth-page [ctx]
  (ui/page
   ctx
   [:div.max-w-md.mx-auto.mt-12.bg-white.rounded-3xl.shadow-xl.overflow-hidden.border.border-slate-100
    [:div.bg-blue-600.px-8.py-12.text-center
     [:div.text-5xl.mb-4 "🏠"]
     [:h2.text-2xl.font-bold.text-white "Integrate Facebook"]]
    [:div.px-8.py-10.text-center
     [:p.text-slate-600 "To start capturing leads, we need your permission to access your Facebook Pages and Lead Ads data."]
     [:a.mt-8.block.w-full.bg-blue-600.text-white.font-bold.py-4.rounded-xl.shadow-lg.shadow-blue-200.hover:bg-blue-700.transition-all
      {:href (fb-auth-url ctx)} "Connect with Facebook"]
     [:p.mt-6.text-xs.text-slate-400 "Official Meta Integration using Graph API v19.0"]]]))

(defn callback-handler [{:keys [params session biff.xtdb/node biff/secret] :as ctx}]
  (let [code (:code params)
        client-id (secret :facebook/client-id)
        client-secret (secret :facebook/client-secret)
        redirect-uri (or (:facebook/redirect-uri ctx) "http://localhost:8080/facebook/callback")
        token-response (http/post "https://graph.facebook.com/v19.0/oauth/access_token"
                                  {:form-params {:client_id client-id
                                                 :client_secret client-secret
                                                 :redirect_uri redirect-uri
                                                 :code code}
                                   :throw-exceptions false})
        body (j/read-value (:body token-response))
        token (get body "access_token")]
    (if token
      (let [user-id (or (:uid session) #uuid "00000000-0000-0000-0000-000000000001")
            tx (xt/submit-tx node [[::xt/put {:xt/id user-id
                                              :user/id user-id
                                              :user/facebook-token token
                                              :user/email "admin@example.com"
                                              :user/joined-at (java.util.Date.)}]])]
        (xt/await-tx node tx)
        {:status 303
         :headers {"location" "/facebook/pages"}})
      (ui/page
       ctx
       [:div.max-w-md.mx-auto.mt-12.bg-white.rounded-3xl.shadow-xl.overflow-hidden.border.border-slate-100
        [:div.bg-red-600.px-8.py-12.text-center
         [:div.text-5xl.mb-4 "❌"]
         [:h2.text-2xl.font-bold.text-white "Connection Failed"]]
        [:div.px-8.py-10.text-center
         [:p.text-slate-600 "Facebook was unable to verify the connection."]
         [:p.mt-4.text-sm.text-red-500 (get-in body ["error" "message"] "Unknown connection error")]
         [:a.mt-8.block.w-full.bg-blue-600.text-white.font-bold.py-4.rounded-xl.shadow-lg.hover:bg-blue-700.transition-all
          {:href "/facebook"} "Try Again"]]]))))

(defn pages-page [{:keys [biff.xtdb/node session] :as ctx}]
  (let [user-id (or (:uid session) #uuid "00000000-0000-0000-0000-000000000001")
        db (xt/db node)
        user (xt/entity db user-id)
        token (:user/facebook-token user)]
    (println "DEBUG pages-page user-id:" user-id)
    (println "DEBUG pages-page user found:" (not (nil? user)))
    (println "DEBUG pages-page token found:" (not (nil? token)))
    (if-not token
      (auth-page ctx)
      (let [response (http/get "https://graph.facebook.com/v19.0/me/accounts"
                               {:query-params {:access_token token}})
            pages (get (j/read-value (:body response)) "data")]
        (ui/page
         ctx
         [:div.max-w-2xl.mx-auto
          [:div.mb-8
           [:h2.text-3xl.font-extrabold.text-slate-900 "Select Your Page"]
           [:p.text-slate-600 "Choose the Facebook Page you want to capture leads from."]]
          [:div.space-y-4
           (for [page pages]
             [:div.flex.items-center.justify-between.p-6.bg-white.rounded-2xl.border.border-slate-100.shadow-sm.hover:shadow-md.transition-all
              [:div
               [:span.font-bold.text-slate-900 (get page "name")]
               [:p.text-xs.text-slate-400 (str "ID: " (get page "id"))]]
              (biff/form
               {:action "/facebook/select-page"}
               [:input {:type "hidden" :name "page-id" :value (get page "id")}]
               [:input {:type "hidden" :name "page-access-token" :value (get page "access_token")}]
               [:button.text-blue-600.font-bold.hover:text-blue-700 "Select Page"])])]])))))

(defn select-page-handler [{:keys [params session biff.xtdb/node] :as ctx}]
  (let [page-id (:page-id params)
        page-token (:page-access-token params)
        user-id (or (:uid session) #uuid "00000000-0000-0000-0000-000000000001")]
    (xt/submit-tx node [[::xt/put (merge (xt/entity (xt/db node) user-id)
                                         {:xt/id user-id
                                          :user/facebook-page-id page-id
                                          :user/facebook-page-token page-token})]])
    {:status 303
     :headers {"location" "/leads"}}))

(defn webhook-get [{:keys [params biff/secret]}]
  (let [verify-token (secret :facebook/verify-token)
        mode (get params "hub.mode")
        token (get params "hub.verify_token")
        challenge (get params "hub.challenge")]
    (if (and (= mode "subscribe") (= token verify-token))
      {:status 200
       :body challenge}
      {:status 403
       :body "Forbidden"})))

(defn fetch-lead-details [lead-id page-token]
  (let [response (http/get (str "https://graph.facebook.com/v19.0/" lead-id)
                           {:query-params {:access_token page-token}})]
    (j/read-value (:body response))))

(defn webhook-post [{:keys [params biff/db biff.xtdb/node] :as ctx}]
  (let [entry (first (get params "entry"))
        changes (get entry "changes")
        change (first changes)
        value (get change "value")
        leadgen-id (get value "leadgen_id")
        page-id (get value "page_id")]
    (println "DEBUG webhook received leadgen-id:" leadgen-id "page-id:" page-id)
    (when leadgen-id
      (let [user (first (biff/q db '{:find (pull u [*])
                                     :in [?page-id]
                                     :where [[u :user/facebook-page-id ?page-id]]}
                                 (str page-id))) ;; Ensure it's a string
            _ (println "DEBUG webhook found user for page:" (not (nil? user)))
            page-token (:user/facebook-page-token user)]
        (when page-token
          (let [details (fetch-lead-details leadgen-id page-token)
                _ (println "DEBUG fetched lead details for form:" (get details "form_id"))
                field-data (get details "field_data")
                find-field (fn [name] (some #(when (= (get % "name") name) (first (get % "values"))) field-data))
                lead-name (or (find-field "full_name") (find-field "first_name"))
                lead-email (find-field "email")
                lead-phone (find-field "phone_number")]
            (xt/submit-tx node [[::xt/put {:xt/id (java.util.UUID/randomUUID)
                                           :lead/form-id (get details "form_id")
                                           :lead/page-id (str page-id)
                                           :lead/name (or lead-name "Unknown")
                                           :lead/email (or lead-email "Unknown")
                                           :lead/phone lead-phone
                                           :lead/created-at (java.util.Date.)
                                           :lead/created-by (:xt/id user)}]])))))
    {:status 200
     :body "EVENT_RECEIVED"}))

(def module
  {:routes [["/facebook" {:middleware [mid/wrap-signed-in]}
             ["" {:get auth-page}]
             ["/callback" {:get callback-handler}]
             ["/pages" {:get pages-page}]
             ["/select-page" {:post select-page-handler}]]
            ["/webhook/facebook" {:get webhook-get
                                  :post webhook-post}]]})
