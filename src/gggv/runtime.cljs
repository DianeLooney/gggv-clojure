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
  (println msg)
  (.send client (clj->js msg)))
(defn send [msg]
  (cond
   (nil? msg) nil
   (fn? msg) (js/setInterval #(send-raw (msg) 17))
   :else  (send-raw msg)))

(def queue (atom []))
(defn push-osc [& msgs] (swap! queue concat msgs))


(defn send-next [] (swap! queue (fn [q] (send (first q)) (rest q))))
(js/setInterval send-next 5)

(def write-file-sync (.-writeFileSync (js/require "fs")))
(defn hydra [steps]
  (let [n (gensym "frag")
        data (h/render steps)
        call-str (:s data)
        uniforms (:u data)
        glsl (string/join
              "\n"
              (flatten [(vals (:t data))
                        "void main() {"
                        (str "outputColor = " call-str ";")
                        "};"]))
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
     :inputs inputs
     :uniforms uniforms}))

(defn mag-linear [input]
  (merge input {:mag "LINEAR"}))

(defn out [data]
  (let [messages (hash->osc data)
        suffix   (osc "/source.shader/set/input" "window" 0 (:name data))]
    (apply push-osc (flatten [messages suffix]))))
