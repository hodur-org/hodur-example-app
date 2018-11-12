(ns hodur-example-app.resolvers)

(defn transform-email
  [v]
  (clojure.string/lower-case v))

(defn build-employee-name-search-where
  [{:keys [where args]} placeholder {:keys [nameSearch] :as args-in}]
  (let [where-expr '[[?e :employee/first-name ?first-name]
                     [?e :employee/last-name ?last-name]
                     (or-join [?term ?term-up ?first-name ?last-name]
                              (and [(<= ?term ?first-name)]
                                   [(< ?first-name ?term-up)])
                              (and [(<= ?term ?last-name)]
                                   [(< ?last-name ?term-up)]))]
        term (str nameSearch "a")
        term-up (str nameSearch "z")]
    {:where (concat where where-expr)
     :args (assoc args '?term term '?term-up term-up)}))

(defn ^:private employee-full-name-resolvers
  [ctx args {:keys [:firstName :lastName] :as resolved-value}]
  (str firstName " " lastName))

(defn resolvers []
  {:employee/full-name-resolver employee-full-name-resolvers})
