
(ns app.vm (:require [app.config :as config]))

(defn get-view-model [store states] {:states states, :site config/site, :store store})

(def state-header
  {:init (fn [props state] state),
   :update (fn [d! op context options state m!]
     (case op
       :home (d! :router/change {:name :home})
       :profile (d! :router/change {:name :profile})
       (println "Unhandled op:" op)))})

(def state-login
  {:init (fn [props state] (or state {:username "", :password ""})),
   :update (fn [d! op context options state mutate!]
     (case op
       :username (mutate! (assoc state :username (:value options)))
       :password (mutate! (assoc state :password (:value options)))
       :signup
         (let [login-pair [(:username state) (:password state)]]
           (d! :user/sign-up login-pair)
           (.setItem js/localStorage (:storage-key config/site) login-pair))
       :login
         (let [login-pair [(:username state) (:password state)]]
           (d! :user/log-in login-pair)
           (.setItem js/localStorage (:storage-key config/site) login-pair))
       (println "Unhandled op:" op)))})

(def state-offline
  {:init (fn [props state] state),
   :update (fn [d! op context options state m!]
     (case op :reconnect (d! :effect/connect nil) (println "not handled op" op)))})

(def state-profile
  {:init (fn [props state] state),
   :update (fn [d! op context options state m!]
     (case op
       :logout
         (do (d! :user/log-out nil) (.removeItem js/localStorage (:storage-key config/site)))
       (println "Unhandled op" op)))})

(def state-workspace
  {:init (fn [props state] state),
   :update (fn [d! op context options state mutate!] (case op (println "Unhandled op" op)))})

(def states-manager
  {"offline" state-offline,
   "login" state-login,
   "workspace" state-workspace,
   "profile" state-profile,
   "header" state-header})

(defn on-action [d! op context options view-model states]
  (when config/dev? (println "Action" op context (pr-str options)))
  (let [param (:param options)
        template-name (:template-name context)
        state-path (:state-path context)
        mutate! (fn [x] (d! :states [state-path x]))
        this-state (get-in states (conj state-path :data))]
    (if (contains? states-manager template-name)
      (let [action-handler (get-in states-manager [template-name :update])
            state-fn (get-in states-manager [template-name :init])
            state (if (fn? state-fn) (state-fn (:data context) this-state) this-state)]
        (action-handler d! op context options state mutate!))
      (println "Unhandled template:" template-name))))
