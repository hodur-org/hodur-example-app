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
            [hodur-example-app.graphql :as graphql]
            [hodur-example-app.resolvers :as resolvers]
            [hodur-example-app.schemas :as schemas]
            [hodur-lacinia-datomic-adapter.core :as hodur-adapter]
            [hodur-lacinia-schema.core :as hodur-lacinia]))

(def meta-db (engine/init-schema schemas/shared
                                 schemas/lacinia-pagination
                                 schemas/lacinia-query))

(def lacinia-schema (hodur-lacinia/schema meta-db))

(def datomic-schema (hodur-datomic/schema meta-db))

(def cfg {:server-type :ion
          :region "us-east-2"
          :system "datomic-cloud-luchini"
          :query-group "datomic-cloud-luchini"
          :endpoint "http://entry.datomic-cloud-luchini.us-east-2.datomic.net:8182/"
          :proxy-port 8182})

(def compiled-schema
  (-> lacinia-schema
      (lacinia-util/attach-resolvers (resolvers/resolvers))
      (hodur-adapter/attach-resolvers meta-db)
      lacinia-schema/compile))



(defn ^:private graphql-ion-handler [payload]
  (graphql/interceptor payload compiled-schema nil))

(def graphql-ion
  (api-gateway/ionize graphql-ion-handler))
