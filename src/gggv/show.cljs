(ns gggv.show
  (:use [clojure.set   :only (union)]
        [gggv.runtime  :only (hydra t mag-linear out)]
        [gggv.core     :only (shader shader-gen)]
        [hydra.core    :only (colorize geometry recolor render)]
        [hydra.filters :only (oc from to pulsate invert haze pride threshold rotate pixelate scale osc shift-hsv posterize)]))

(def mix-layers (shader {:f "mix.layers"} {}))
(def highlights (shader {:f "highlights"} {}))

(def sin (.-sin js/Math))
(def cosin (.-cos js/Math))
(def tan (.-tan js/Math))

(-> 
    (osc {})
    (scale 54)
    (rotate 0.5)
    (pulsate 1 10.5)
    (pride [1 1 1] [0.6 0.6 0.6] [0.3 0.3 0.3]
           1 1 1)
    (scale #(mod (* 4 t) 20))
    oc
    hydra
    out)
