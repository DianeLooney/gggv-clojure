(ns gggv.runtime
  (:use [gggv.core :only (osc hash->osc)])
  (:require [clojure.string :as string]
            [hydra.core :as h]
            [lumo.core]))


(println "#Started")

(defn _t [] (/ (.getTime (js/Date.)) 1000))
(def t (_t))
(js/setInterval #(def t (_t)) 1)

(def node-osc (js/require "node-osc"))
(def Client (.-Client node-osc))
(def Message (.-Message node-osc))
(def client (Client. "127.0.0.1" 4200))
(defn send-raw [msg]
  ;(println msg)
  (.send client (clj->js msg)))
(defn send [msg]
  (cond
    (nil? msg) nil
    (fn? msg) (js/setInterval #(send-raw (msg)) (/ 1000 60))
    :else  (send-raw msg)))

(def queue (atom []))
(defn push-osc [& msgs] (swap! queue concat msgs))


(defn send-next [] (swap! queue (fn [q] (send (first q)) (rest q))))
(js/setInterval send-next 5)

(def write-file-sync (.-writeFileSync (js/require "fs")))
(defn hydra [& steps]
  (let [n (gensym "frag")
        datas (map h/render steps)
        call-strs (map :s datas)
        uniforms (apply merge (map :u datas))
        ts (apply merge (map :t datas))
        glsl (string/join
              "\n"
              (flatten [(vals ts)
                        "void main() {"
                        (map #(str % ";") call-strs)
                        "}"]))
        geometry [-1 -1 0 0 0 0
                  +1 -1 0 1 0 0
                  -1 +1 0 0 1 0
                  +1 -1 0 1 0 0
                  -1 +1 0 0 1 0
                  +1 +1 0 1 1 0]
        particle-count 1
        width 1
        height 1
        filename (str "/tmp/gggv/" n ".glsl")]
    (write-file-sync filename glsl)
    {:source :shader
     :program n
     :vert "shaders/vert/default.glsl"
     :geom "shaders/geom/default.glsl"
     :frag filename
     :mag "NEAREST"
     :geometry geometry
     :pc particle-count
     :width width
     :height height
     :name n
     :inputs  (flatten (map :i datas))
     :storage (apply clojure.set/union (map :r datas))
     :uniforms uniforms}))

(defn mag-linear [input]
  (merge input {:mag "LINEAR"}))
