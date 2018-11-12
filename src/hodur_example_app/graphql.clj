(ns hodur-example-app.graphql
  (:require [camel-snake-kebab.core :refer [->kebab-case-keyword]]
            [camel-snake-kebab.extras :refer [transform-keys]]
            [cheshire.core :as cheshire]
            [com.walmartlabs.lacinia :as lacinia]))

(def ^:private cors-headers
  {"Access-Control-Allow-Origin"  "*"
   "Access-Control-Allow-Headers" "*"
   "Access-Control-Allow-Methods" "DELETE,GET,HEAD,OPTIONS,PATCH,POST,PUT"})

(defn ^:private respond [status body]
  {:status  status
   :headers (merge {"Content-Type" "application/json"} cors-headers)
   :body    (cheshire/generate-string body)})

(defn ^:private respond-ok [body]
  (respond 200 body))

(defn ^:private respond-user-error [error]
  (respond 400 {:error error}))

(defn ^:private respond-server-error [ex]
  (respond 500 {:error {:cause (ex-data ex)}}))

(defn ^:private is-graphql-request? [headers]
  (= "application/graphql" (get headers :content-type)))

(defn ^:private is-json-request? [headers]
  (= "application/json" (get headers :content-type)))

(defn ^:private valid-headers? [headers]
  (or
   (is-graphql-request? headers)
   (is-json-request? headers)))

(defn ^:private valid-request?
  "Returns true if proper content type is set and there is query to execute."
  [headers body]
  (and (valid-headers? headers) (not (nil? body))))

(defn ^:private run-json-query
  "Runs the query which was passed as application/json content type (from Graphqurl for example).
  JSON body of this request is in format: { query: \"\", variables: \"\" }"
  [compiled-schema body context]
  (let [parsed-body (cheshire/parse-string body true)
        query (:query parsed-body)
        variables (:variables parsed-body)]
    (lacinia/execute compiled-schema query variables context)))

(defn ^:private get-response [{:keys [headers body]}
                              compiled-schema context]
  (let [transformed-headers (transform-keys ->kebab-case-keyword headers)]
    (if (valid-request? transformed-headers body)
      ;; adding headers to context to be used for feature flag override
      (let [context' (merge context {:request {:headers headers}})]
        (if (is-graphql-request? transformed-headers)
          ;; we are supporting both "raw" GraphQL queries inside of request body
          ;; and Graphqurl json request body
          (respond-ok (lacinia/execute compiled-schema body nil context'))
          (respond-ok (run-json-query compiled-schema body context'))))
      ;; request headers and body is included in response for debugging purposes
      ;; we can remove it later if needed
      (respond-user-error {:message "Invalid request." :headers transformed-headers :body body}))))
(defn ^:private respond-to-query [{:keys [headers body] :as payload}
                                  compiled-schema context]
  (try
    (let [body' (some-> body slurp)]
      (get-response (assoc payload :body body')
                    compiled-schema context))
    (catch Exception e
      (respond-server-error e))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn interceptor [{:keys [request-method headers body] :as payload}
                   compiled-schema context]
  (if (= request-method :options)
    (respond-ok "")
    (respond-to-query payload compiled-schema context)))
