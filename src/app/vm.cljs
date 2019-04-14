
(ns app.vm (:require [app.config :as config]))

(defn get-view-model [store local-store]
  (-> store (assoc :local local-store) (assoc :site config/site)))

(defn on-action [d! op param options view-model]
  (when config/dev? (println "Action" op param (pr-str options)))
  (case op (do (println "Unknown op:" op))))
