(ns in.facebookleads
  (:require [com.biffweb :as biff]
            [in.facebookleads.home :as home]
            [in.facebookleads.facebook :as facebook]
            [in.facebookleads.leads :as leads]
            [in.facebookleads.schema :as schema]
            [in.facebookleads.middleware :as mid]
            [in.facebookleads.ui :as ui]
            [clojure.tools.logging :as log])
  (:gen-class))

(def modules
  [home/module
   facebook/module
   leads/module
   schema/module])

(def routes ["" {:middleware [mid/wrap-site-defaults]}
             (mapcat :routes modules)])

(def handler (-> (biff/reitit-handler {:routes routes})
                 mid/wrap-base-defaults))

(def initial-system
  {:biff/modules #'modules
   :biff/handler #'handler
   :biff.xtdb/dir "storage/xtdb"
   :biff.middleware/on-error #'ui/on-error})

(defonce system (atom {}))

(def components
  [biff/use-aero-config
   biff/use-xtdb
   biff/use-jetty])

(defn start []
  (let [new-system (reduce (fn [system component]
                             (component system))
                           initial-system
                           components)]
    (reset! system new-system)
    (log/info "System started at" (:biff/base-url new-system))
    new-system))

(defn -main []
  (start))
