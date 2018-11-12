(ns hodur-example-app.resolvers)

(defn ^:private employee-full-name-resolvers
  [ctx args {:keys [:firstName :lastName] :as resolved-value}]
  (str firstName " " lastName))

(defn resolvers []
  {:employee/full-name-resolver employee-full-name-resolvers})
