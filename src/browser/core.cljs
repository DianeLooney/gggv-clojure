(ns browser.core
  (:require ["regl" :as r]
            [browser.stubs :as stubs]
            [clojure.string :as string]
            [cljs.reader :as reader]
            ["codemirror/lib/codemirror" :as cm]
            [cljs.js])
  (:use [cljs.core :only [eval]]
        [hydra.core :only [render]]
        [hydra.filters :only [osc
                              invert shift-hsv
                              polarize pulsate warp]]))

(defn fake-eval [form]
  (let [st (cljs.js/empty-state)]
    (cljs.js/compile-str st form "asdf" {:ns (find-ns cljs.core)} println)))

(let [eval *eval*
      st (cljs.js/empty-state)]
  (set! *eval*
        (fn [form]
          (binding [cljs.env/*compiler* st
                    *ns* (find-ns cljs.analyzer/*cljs-ns*)
                    cljs.js/*eval-fn* cljs.js/js-eval]
            (eval form)))))

(def regl
  (atom nil))

(def base-vert "precision highp float;
                attribute vec2 position;
                void main () {
                  gl_Position = vec4(position, 0, 1);
                }")

(def base-frag "precision highp float;
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
                        (apply str stubs/all)
                        (vals (:t h))
                        "void main() {"
                        "  vec2 ftc = gl_FragCoord.xy / vec2(scaleX, scaleY);"
                        "  ftc = ftc - vec2(0.5);"
                        "  ftc = ftc * vec2(scaleX / scaleY, 1);"
                        (str "  gl_FragColor = " (:s h) ".color;")
                        "}"]))
        uniforms (merge (base-uniforms regl)
                        (into
                         {}
                         (map (fn [key] [key (.prop @regl (name key))]) (keys (:u h)))))]
    {:frag frag
     :vert vert
     :attributes {:position base-position}
     :count 6
     :uniforms uniforms}))

(def show-src
  (->
   (osc {} [])
   (pulsate (fn [v] (.-time v)) 100)
   ;warp
   ;(shift-hsv 0.1 0.7 0.9)
   invert
   render))

(defn test-shader [regl show]
  (let [rendered (hydra->browser regl show)]
    {:source rendered
     :uniforms (:u show)}))

(defn frame [draw vals]
  (.clear @regl (clj->js {:color [0 0 0 0] :depth 1}))
  (draw (clj->js vals)))

(defn prepare! []
  (fake-eval "
    (ns cljs.core
      (:require-macros [cljs.js :refer [dump-core]]
                       [cljs.env.macros :as env]))
    (-> 1 (+ 2))")

  (swap! regl #(r (.getElementById js/document "main")))
  (set! (.-reglCtx js/document) @regl)
  (set! (.-arr js/document) (clj->js [1 0 1 1]))
  (let [shader (test-shader regl show-src)
        source (:source shader)
        uniforms (:uniforms shader)
        draw (@regl (clj->js source))]
    (.frame
     @regl
     (fn [v]
       (let [time   (.-time v)
             scaleX (.-drawingBufferWidth v)
             scaleY (.-drawingBufferHeight v)]
         (frame
          draw
          (merge
           {:time time, :scaleX scaleX, :scaleY scaleY}
           (into {}
                 (map
                  (fn [[name val]]
                    [name (if (fn? val) (val v) val)])
                  uniforms)))))))))

(defn reload! []
  (println (.-reglCtx js/document))
  (swap! regl #(.-reglCtx js/document))
  (println @regl))
