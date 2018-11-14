(ns hodur-example-app.visualizer
  (:require [hodur-engine.core :as engine]
            [hodur-example-app.schemas :as schemas]
            [hodur-visualizer-schema.core :as visualizer]))
(def meta-db
  (engine/init-schema schemas/shared
                      schemas/lacinia-pagination
                      schemas/lacinia-query))

(-> meta-db
    visualizer/schema
    visualizer/apply-diagram!)
