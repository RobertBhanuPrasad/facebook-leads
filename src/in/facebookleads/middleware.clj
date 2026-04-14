(ns in.facebookleads.middleware
  (:require [com.biffweb :as biff]
            [muuntaja.middleware :as muuntaja]
            [ring.middleware.anti-forgery :as csrf]
            [ring.middleware.defaults :as rd]))

(defn wrap-signed-in [handler]
  (fn [{:keys [session] :as ctx}]
    ;; For demonstration purposes, if session is empty, we set a mock user ID
    ;; In a real app, this would be handled by the login flow
    (let [session (or session {:uid #uuid "00000000-0000-0000-0000-000000000001"})]
      (handler (assoc ctx :session session)))))

(defn wrap-site-defaults [handler]
  (-> handler
      biff/wrap-render-rum
      biff/wrap-anti-forgery-websockets
      csrf/wrap-anti-forgery
      biff/wrap-session
      muuntaja/wrap-params
      muuntaja/wrap-format
      (rd/wrap-defaults (-> rd/site-defaults
                            (assoc-in [:security :anti-forgery] false)
                            (assoc-in [:responses :absolute-redirects] true)
                            (assoc :session false)
                            (assoc :static false)))))

(defn wrap-base-defaults [handler]
  (-> handler
      biff/wrap-https-scheme
      biff/wrap-resource
      biff/wrap-internal-error
      biff/wrap-log-requests))
