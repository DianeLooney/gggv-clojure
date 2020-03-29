(ns gggv.show
  (:require [gggv.core :as gggv])
  (:use [clojure.set   :only [union]]
        [gggv.core     :only [shader shader-gen hash->osc]]
        [hydra.core    :only [colorize geometry recolor render]]
        [hydra.filters :only
         [oc from to diff
          swizzleRGB swizzleRBG swizzleGRB swizzleGBR swizzleBGR swizzleBRG
          shape
          color-scale mod-hsv
          scale-x scroll
          hsv->rgb warp polarize cells center pulsate invert haze pride threshold rotate pixelate scale osc shift-hsv posterize]]
        [gggv.runtime  :only [hydra t mag-linear push-osc]]))

(defn out [data]
  (let [messages (hash->osc data)
        suffix   (gggv/osc "/source.shader/set/input" "window" 0 (:name data))]
    (apply push-osc (flatten [messages suffix]))))

(def sin (.-sin js/Math))
(def cosin (.-cos js/Math))
(def tan (.-tan js/Math))

(defn now [] (/ (.getTime (js/Date.)) 1000))
(js/setInterval #(println (- (now) start-time)) 100)
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
(defn co [] (/ (mod t 4) 4))
(defn co2 [] (/ (mod (+ 2 t) 4) 4))



(def diamond-size
  (partial fade (ts 0 3) (ts 2 30) 3 1.5 1.05))

(def osc-amplitude
  (partial fade (ts 0 27) (ts 4 10) 12 0 1))

(def sat
  (partial fade (ts 0 55) (ts 3 55) 5 0 1))

(defn hue []
  (fade (ts 1 15) (ts 2 40) 2 0.8 (mod (/ t 12) 1)))

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

(->
 (osc {} [osc-amplitude])
 (mod-hsv hue sat)

 (shape 50 diamond-size 0.2)

 (posterize 4)

 ; -- 2 -- remove above 
 (shape 3 pulse-1-size 0 pulse-1-color)
 (shape 3 pulse-2-size 0 pulse-2-color)

 (color-scale 0.4)


 (haze haze-speed)

 ; -- leave alone
 (scale (* 0.506 3.14159))
 (rotate 0.75)
 (scale-x 5)
 (scroll :-0.25*time)
 (warp warp-amount)
 polarize
 center
 oc
 hydra
 out)
