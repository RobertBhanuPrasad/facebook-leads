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
          [:lead/form-id     :string]
          [:lead/page-id     :string]
          [:lead/name        :string]
          [:lead/email       :string]
          [:lead/phone       {:optional true} :string]
          [:lead/created-at  inst?]
          [:lead/created-by  :user/id]]})

(def module
  {:schema schema})
