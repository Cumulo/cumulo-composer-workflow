
(ns app.vm (:require [app.config :as config]))

(defn get-view-model [store local-store]
  {:local local-store, :site config/site, :store store})

(defn on-action [d! op param options view-model]
  (when config/dev? (println "Action" op param (pr-str options)))
  (case op
    :local/username (d! :local-mutate {:path [:username], :value (:value options)})
    :local/password (d! :local-mutate {:path [:password], :value (:value options)})
    :signup
      (let [local (:local view-model)]
        (d! :user/sign-up [(:username local) (:password local)]))
    :login
      (let [local (:local view-model)]
        (d! :user/log-in [(:username local) (:password local)]))
    :profile (d! :router/change {:name :profile})
    :home (d! :router/change {:name :home})
    (do (println "Unknown op:" op))))
