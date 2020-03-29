(ns browser.core
  (:require ["regl" :as r]
            [browser.stubs :as stubs]
            [clojure.string :as string]
            [cljs.reader :as reader]
            ["codemirror/lib/codemirror" :as cm]
            [cljs.js])
  (:use [cljs.core :only [eval]]
        [hydra.core :only [render]]
        [hydra.filters :only
         [oc from to diff
          swizzleRGB swizzleRBG swizzleGRB swizzleGBR swizzleBGR swizzleBRG
          shape
          color-scale mod-hsv
          scale-x scroll
          hsv->rgb warp polarize cells center pulsate invert haze pride threshold rotate pixelate scale osc shift-hsv posterize]]))

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
              (flatten ["precision highp float;"
                        "#define PI 3.1415926535897932384626433832795"
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

(def sin (.-sin js/Math))
(def cosin (.-cos js/Math))
(def tan (.-tan js/Math))

(defn now [] (/ (.getTime (js/Date.)) 1000))
(def start-time (+ (now) 5))
(defn ts [minutes seconds] (+ start-time seconds (* minutes 60)))

(defn mix [x1 x2 y1 y2 x]
  (cond
    (< x x1) y1
    (> x x2) y2
    :else (+ (* y2 (/ (- x x1) (- x2 x1)))
             (* y1 (/ (- x2 x) (- x2 x1))))))

(defn fade [t-start t-end smooth from to]
  (let [n (now)]
    (if
     (< n t-start)
      (mix (- t-start smooth) t-start          from to   n)
      (mix t-end              (+ t-end smooth) to   from n))))




(def warp-amount
  (partial fade (ts 2 53) (ts 3 18) 10 0 3))
(defn co [] (/ (mod (now) 4) 4))
(defn co2 [] (/ (mod (+ 2 (now)) 4) 4))



(def diamond-size
  (partial fade (ts 0 3) (ts 2 30) 3 1.5 1.05))

(def osc-amplitude
  (partial fade (ts 0 27) (ts 4 10) 12 0 1))

(def sat
  (partial fade (ts 0 55) (ts 3 55) 5 0 1))

(defn hue []
  (fade (ts 1 15) (ts 2 40) 2 0.8 (mod (/ (now) 12) 1)))

(def pulse-modifier (partial fade (ts 0 38) (ts 3 55) 2 0 1))

(defn pulse-1-size []
  (* (pulse-modifier) 4 (- 1 (co))))
(defn pulse-1-color []
  (let [x (co)]
    [x x x]))
(defn pulse-2-size []
  (* (pulse-modifier) 4 (- 1 (co2))))
(defn pulse-2-color []
  (let [x (co2)]
    [x x x]))
(defn haze-speed []
  1)

(def show-src
  (->
   (osc {} [osc-amplitude])
   (mod-hsv hue sat)

   (shape 50 diamond-size 0.2)

   (posterize 4)

 ; -- 2 -- remove above 
   (shape 3 pulse-1-size 0 pulse-1-color)
   (shape 3 pulse-2-size 0 pulse-2-color)

   (color-scale 0.4)


 ;  (haze haze-speed)

 ; -- leave alone
   (scale (* 0.506 3.14159))
   (rotate 0.75)
   (scale-x 5)
   (scroll :-0.25*time)
   (warp warp-amount)
   polarize
   render))

(defn test-shader [regl show]
  (let [rendered (hydra->browser regl show)]
    {:source rendered
     :uniforms (:u show)}))

(defn frame [draw vals]
  (.clear @regl (clj->js {:color [0 0 0 0] :depth 1}))
  (draw (clj->js vals)))

(defn prepare! []
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
