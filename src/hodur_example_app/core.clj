(ns hodur-example-app.core
  (:require [cheshire.core :as cheshire]
            [com.walmartlabs.lacinia :as lacinia]
            [com.walmartlabs.lacinia.parser :as lacinia-parser]
            [com.walmartlabs.lacinia.schema :as lacinia-schema]
            [com.walmartlabs.lacinia.util :as lacinia-util]
            [datomic.client.api :as datomic]
            [datomic.ion.lambda.api-gateway :as api-gateway]
            [hodur-datomic-schema.core :as hodur-datomic]
            [hodur-engine.core :as engine]
            [hodur-example-app.dataset :as dataset]
            [hodur-example-app.graphql :as graphql]
            [hodur-example-app.resolvers :as resolvers]
            [hodur-example-app.schemas :as schemas]
            [hodur-lacinia-datomic-adapter.core :as hodur-adapter]
            [hodur-lacinia-schema.core :as hodur-lacinia]))

(def ^:private cfg {:server-type :ion
                    :region "us-east-2"
                    :system "transactor"
                    :query-group "transactor"
                    :endpoint "http://entry.transactor.us-east-2.datomic.net:8182/"
                    :proxy-port 8182})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Hodur initialization
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:private meta-db (engine/init-schema schemas/shared
                                           schemas/lacinia-pagination
                                           schemas/lacinia-query))

(def ^:private lacinia-schema (hodur-lacinia/schema meta-db))

(def ^:private datomic-schema (hodur-datomic/schema meta-db))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Datomic initialization
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:private get-client
  "This function will return a local implementation of the client
  interface when run on a Datomic compute node. If you want to call
  locally, fill in the correct values in the map."
  (memoize #(datomic/client cfg)))

(defn ^:private ensure-db
  "Ensure that a database named db-name exists. Returns connection"
  [client db-name]
  (let [client (get-client)]
    (datomic/create-database client {:db-name db-name})
    (datomic/connect client {:db-name db-name})))

(defn ^:private ensure-tx-data
  "Ensure that the db in the connection conn has the applied tx-data. Returns connection"
  [conn tx-data]
  (datomic/transact conn {:tx-data tx-data})
  conn)

(def ^:private get-connection
  (memoize #(-> (get-client)
                (ensure-db "hodur-test")
                (ensure-tx-data datomic-schema)
                (ensure-tx-data dataset/seed))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Lacinia initialization
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:private compiled-schema
  (-> lacinia-schema
      (lacinia-util/attach-resolvers (resolvers/resolvers))
      (hodur-adapter/attach-resolvers meta-db)
      lacinia-schema/compile))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Ion entrypoint
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn ^:private graphql-ion-handler [payload]
  (graphql/interceptor payload
                       compiled-schema
                       {:db (datomic/db (get-connection))}))

(def graphql-ion
  (api-gateway/ionize graphql-ion-handler))


#_(lacinia/execute compiled-schema "
{
  employee(email: \"tl@work.co\") {
    firstName
  }
}"
                   nil {:db (datomic/db (get-connection))})
