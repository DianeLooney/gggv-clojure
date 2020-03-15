(ns browser.core
  (:require ["regl" :as r]
            [clojure.string :as string])
  (:use [hydra.core :only [render]]
        [hydra.filters :only [osc]]))

(def regl (atom nil))

(def base-vert "precision mediump float;
                attribute vec2 position;
                void main () {
                  gl_Position = vec4(position, 0, 1);
                }")

(def base-frag "precision mediump float;
                uniform vec4 color;
                void main () {
                  gl_FragColor = color;
                }")

(def base-position [[-1 -1] [1 -1] [1 1]
                    [-1 -1] [1 1] [-1 1]])

(defn base-uniforms [regl] {:scaleX (.prop @regl "scaleX")
                            :scaleY (.prop @regl "scaleY")
                            :time   (.prop @regl "time")})

(defn hydra->browser [regl h]
  (println h)
  (let [vert base-vert
        position base-position
        count 6
        frag (string/join
              "\n"
              (flatten ["precision mediump float;"
                        "struct Pixel { vec2 xy; vec4 color; };"
                        "uniform float time;"
                        "uniform float scaleX;"
                        "uniform float scaleY;"
                        (vals (:t h))
                        "void main() {"
                        "  vec2 ftc = gl_FragCoord.xy / vec2(scaleX, scaleY);"
                        "  ftc = ftc - vec2(0.5);"
                        "  ftc = ftc * vec2(scaleX / scaleY, 1);"
                        (str "  gl_FragColor = " (:s h) ".color;")
                        "}"]))
        uniforms (base-uniforms regl)]
    {:frag frag
     :vert vert
     :attributes {:position base-position}
     :count 6
     :uniforms uniforms}))

(defn test-shader [regl]
  (clj->js
   (hydra->browser
    regl
    (->
     (osc {} [])
     render))))

(defn frame [draw vals]
  (.clear @regl (clj->js {:color [0 0 0 0] :depth 1}))
  (println vals)
  (draw (clj->js vals)))

(defn test-it! []
  (set! (.-arr js/document) (clj->js [1 0 1 1]))
  (let [draw (@regl (test-shader regl))]
    (.frame
     @regl
     (fn [v]
       (let [time (.-time v)
             scaleX (.-drawingBufferWidth v)
             scaleY (.-drawingBufferHeight v)]
         (frame draw {"time" time
                      "scaleX" scaleX
                      "scaleY" scaleY}))))))

(defn prepare! []
  (swap! regl #(r))
  (test-it!))

(defn reload! []
  (swap! regl #(r)))
