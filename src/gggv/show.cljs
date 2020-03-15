(ns gggv.show
  (:use [clojure.set   :only (union)]
        [gggv.runtime  :only (hydra t mag-linear out)]
        [gggv.core     :only (shader shader-gen)]
        [hydra.core    :only (colorize geometry recolor render)]
        [hydra.filters :only
         [oc from to diff
          swizzleRGB swizzleRBG swizzleGRB swizzleGBR swizzleBGR swizzleBRG
          hsv->rgb warp polarize cells center pulsate invert haze pride threshold rotate pixelate scale osc shift-hsv posterize]]))

(def mix-layers (shader {:f "mix.layers"} {}))
(def highlights (shader {:f "highlights"} {}))

(def sin (.-sin js/Math))
(def cosin (.-cos js/Math))
(def tan (.-tan js/Math))

(->
 (osc {} [])
 (pulsate :-2*time 1)
 warp
 ; (pride [0 0 0]
 ;        [#(mod (+ 0.0 (/ t 20)) 1) 0.8 1]
 ;        [#(mod (+ 0.3 (/ t 20)) 1) 0.8 0.5]
 ;        1 1 1)
 (shift-hsv #(/ (mod (* 3 t) 7) 7) 0.7 0.9)
 swizzleGBR
 invert
 oc
 center
 hydra
 out)
