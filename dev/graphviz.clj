(ns graphviz
  (:require [clojure.java.shell :as shell]
            [hodur-engine.core :as engine]
            [hodur-example-app.schemas :as schemas]
            [hodur-graphviz-schema.core :as hodur-graphviz]))

(def meta-db (engine/init-schema schemas/shared
                                 schemas/lacinia-pagination
                                 schemas/lacinia-query))

(defn ^:private render-png-from-dot
  "Convert a DOT content into a PNG file"
  [content png-filename]
  (shell/sh "dot"
            "-Tpng"
            "-o" png-filename
            :in content))

(defn -main
  "Generates PNG files from hodur models"
  [& args]
  (-> (hodur-graphviz/schema meta-db)
      (render-png-from-dot (str "docs/diagrams/model.png")))
  (System/exit 0))
