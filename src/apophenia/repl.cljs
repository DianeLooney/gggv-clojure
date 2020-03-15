(ns apophenia.repl
  (:require [apophenia.core :refer [make bpm => env-over env-linear env-static status clock-now]]))

(def node-osc (js/require "node-osc"))
(def Client (.-Client node-osc))
(def Message (.-Message node-osc))
(def client (Client. "127.0.0.1" 4200))
(defn send [msg]
  ;(println msg)
  (.send client (clj->js msg)))

(def x 1)
(def _ 0.1)

(def c (make))
(bpm c (clock-now) 90)
;(=> c :a [1 0 1 0 1 0 0 0])
;(=> c :b {} [1 1 1 1])
(=> c :a {:gain 4} [x])
(=> c :b {:gain 2} [x])
(=> c :c {:gain 1} [x])
(=> c :d {:gain 1} [1 2 3 4 5 6 7 8])
;(doall (map println (dump c)))

(defn spew []
  (let [state (status c (clock-now))]
    (doall (map (fn [[k v]]
             ;(println  k  v)
             (send {:address "/source.shader/set.global/uniform1f", :args [(name k) v]})
             ) state))))

(spew)
(js/setInterval spew (/ 1000 60))