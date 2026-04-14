(ns in.facebookleads.leads
  (:require [com.biffweb :as biff]
            [in.facebookleads.ui :as ui]
            [in.facebookleads.middleware :as mid]
            [xtdb.api :as xt]))

(defn leads-page [{:keys [biff/db session] :as ctx}]
  (let [leads (biff/q db '{:find (pull l [*])
                           :in [?user]
                           :where [[l :lead/created-by ?user]]
                           :order-by [[l :lead/created-at :desc]]}
                      (:uid session))]
    (ui/page
     ctx
     [:div
      [:div.flex.justify-between.items-end.mb-8
       [:div
        [:h2.text-3xl.font-extrabold.text-slate-900 "Recent Leads"]
        [:p.mt-1.text-slate-500 "View and manage leads captured from your connected Facebook Page."]]
       [:a.px-4.py-2.bg-white.text-blue-600.font-bold.rounded-xl.border.border-blue-100.hover:bg-blue-50.transition-all
        {:href "/facebook/pages"} "Settings"]]
      
      [:div.mt-8.bg-white.rounded-3xl.shadow-sm.border.border-slate-100.overflow-hidden
       [:table.min-w-full.divide-y.divide-slate-100
        [:thead.bg-slate-50
         [:tr
          [:th.p-6.text-left.text-xs.font-bold.text-slate-500.uppercase.tracking-wider "Lead Name"]
          [:th.p-6.text-left.text-xs.font-bold.text-slate-500.uppercase.tracking-wider "Contact Info"]
          [:th.p-6.text-left.text-xs.font-bold.text-slate-500.uppercase.tracking-wider "Created At"]
          [:th.p-6.text-left.text-xs.font-bold.text-slate-500.uppercase.tracking-wider "Action"]]]
        [:tbody.divide-y.divide-slate-100
         (for [lead leads]
           [:tr.hover:bg-slate-50.transition-colors
            [:td.p-6
             [:div.text-sm.font-bold.text-slate-900 (:lead/name lead)]
             [:div.text-xs.text-slate-400 (str "Form: " (:lead/form-id lead))]]
            [:td.p-6
             [:div.text-sm.text-slate-600 (:lead/email lead)]
             [:div.text-xs.text-slate-400 (:lead/phone lead)]]
            [:td.p-6.text-sm.text-slate-500
             (str (:lead/created-at lead))]
            [:td.p-6
             [:button.text-blue-600.font-medium.hover:underline "View Details"]]])]]]
      
      (when (empty? leads)
        [:div.mt-16.text-center
         [:div.text-6xl.mb-4 "📥"]
         [:h3.text-xl.font-bold.text-slate-900 "No leads yet"]
         [:p.mt-2.text-slate-500 "Submit a test lead using the FB Lead Ads Testing Tool to see it here."]
         [:a.mt-6.inline-block.text-blue-600.font-bold.hover:underline 
          {:href "https://developers.facebook.com/tools/lead-ads-testing" :target "_blank"} 
          "Open Testing Tool →"]])])))

(def module
  {:routes [["/leads" {:middleware [mid/wrap-signed-in]}
             ["" {:get leads-page}]]]})
