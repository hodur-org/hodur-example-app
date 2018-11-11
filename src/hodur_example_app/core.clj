(ns hodur-example-app.core
  (:require [com.walmartlabs.lacinia :as lacinia]
            [com.walmartlabs.lacinia.parser :as l-parser]
            [com.walmartlabs.lacinia.schema :as l-schema]
            [com.walmartlabs.lacinia.util :as l-util]
            [datomic.client.api :as datomic]
            [hodur-datomic-schema.core :as datomic-plugin]
            [hodur-engine.core :as engine]
            [hodur-example-app.schemas :as schemas]
            [hodur-lacinia-datomic-adapter.core :as adapter]
            [hodur-lacinia-schema.core :as lacinia-plugin]))

(def meta-db (engine/init-schema schemas/shared))

(def lacinia-schema (lacinia-plugin/schema meta-db))

(def datomic-schema (datomic-plugin/schema meta-db))

(def cfg {:server-type :ion
          :region "us-east-2"
          :system "datomic-cloud-luchini"
          :query-group "datomic-cloud-luchini"
          :endpoint "http://entry.datomic-cloud-luchini.us-east-2.datomic.net:8182/"
          :proxy-port 8182})
