(ns in.facebookleads.ui
  (:require [com.biffweb :as biff]
            [rum.core :as rum]))

(defn page [ctx & body]
  (biff/base-html
   (assoc ctx 
          :base/head [[:link {:rel "stylesheet" :href "https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap"}]
                      [:script {:src "https://cdn.tailwindcss.com"}]
                      [:style "body { font-family: 'Inter', sans-serif; }"]]
          :base/title "Facebook Leads CRM")
   [:div.min-h-screen.bg-slate-50
    [:nav.bg-white.border-b.border-slate-100.sticky.top-0.z-50
     [:div.max-w-5xl.mx-auto.px-4.h-16.flex.items-center.justify-between
      [:a.text-xl.font-bold.text-slate-900 {:href "/"} "Facebook Leads " [:span.text-blue-600 "CRM"]]
      [:div.flex.gap-6
       [:a.text-sm.font-medium.text-slate-600.hover:text-blue-600 {:href "/leads"} "Leads"]
       [:a.text-sm.font-medium.text-slate-600.hover:text-blue-600 {:href "/facebook"} "Settings"]]]]
    [:div.max-w-5xl.mx-auto.px-4.py-8
     body]]))

(defn on-error [{:keys [status] :as ctx}]
  {:status status
   :headers {"content-type" "text/html"}
   :body (rum/render-static-markup
          (page ctx
                [:h1.text-2xl.font-bold
                 (if (= status 404)
                   "Page not found."
                   "Something went wrong.")]))})
