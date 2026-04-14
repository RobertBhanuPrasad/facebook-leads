(ns in.facebookleads.home
  (:require [com.biffweb :as biff]
            [in.facebookleads.ui :as ui]))

(defn home-page [ctx]
  (ui/page
   ctx
   [:div.flex.flex-col.items-center.justify-center.py-20
    [:div.text-center
     [:h1.text-5xl.font-extrabold.tracking-tight.text-slate-900
      "Maximize Your " [:span.text-blue-600 "Facebook Leads"]]
     [:p.mt-6.text-xl.text-slate-600.max-w-2xl
      "Capture, track, and manage your Facebook Lead Ads in real-time. Boost your conversion rates with instant notifications and a centralized dashboard."]
     [:div.mt-10.flex.gap-4.justify-center
      [:a.px-8.py-4.bg-blue-600.text-white.font-bold.rounded-2xl.shadow-xl.shadow-blue-200.hover:bg-blue-700.transition-all.text-lg
       {:href "/facebook"} "Connect Leads Now"]
      [:a.px-8.py-4.bg-white.text-slate-700.font-bold.rounded-2xl.border.border-slate-200.hover:bg-slate-50.transition-all.text-lg
       {:href "/leads"} "View Dashboard"]]]
    
    [:div.mt-24.grid.grid-cols-1.md:grid-cols-3.gap-8
     (for [[title desc icon bg] 
           [["Real-time Capture" "Instant lead synchronization via Facebook Webhooks." "⚡" "bg-amber-50 text-amber-600"]
            ["Secure Storage" "Reliable lead persistence in a high-performance XTDB database." "🔒" "bg-blue-50 text-blue-600"]
            ["Modern Dashboard" "Clean, intuitive interface powered by HTMX and Biff." "📊" "bg-emerald-50 text-emerald-600"]]]
       [:div.p-8.bg-white.rounded-3xl.border.border-slate-100.shadow-sm.hover:shadow-md.transition-all
        [:div.w-12.h-12.flex.items-center.justify-center.rounded-xl.text-2xl.mb-6 {:class bg} icon]
        [:h3.text-xl.font-bold.text-slate-900 title]
        [:p.mt-3.text-slate-600 desc]])]]))

(def module
  {:routes [["/" {:get home-page}]]})
