(ns in.facebookleads.schema)

(def schema
  {:user/id :uuid
   :user    [:map {:closed true}
             [:xt/id           :user/id]
             [:user/email      :string]
             [:user/joined-at  inst?]
             [:user/facebook-token {:optional true} :string]
             [:user/facebook-page-id {:optional true} :string]
             [:user/facebook-page-token {:optional true} :string]]

   :lead [:map
          [:xt/id            :uuid]
          [:lead/facebook-lead-id {:optional true} :string]
          [:lead/form-id     :string]
          [:lead/form-name   {:optional true} :string]
          [:lead/page-id     {:optional true} :string]
          [:lead/ad-id       {:optional true} :string]
          [:lead/ad-name     {:optional true} :string]
          [:lead/campaign-id {:optional true} :string]
          [:lead/campaign-name {:optional true} :string]
          [:lead/platform    {:optional true} :string]
          [:lead/source      {:optional true} :string]
          [:lead/name        :string]
          [:lead/email       :string]
          [:lead/phone       {:optional true} :string]
          [:lead/created-at  inst?]
          [:lead/created-by  {:optional true} :user/id]]})

(def module
  {:schema schema})
