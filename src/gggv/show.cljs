(ns gggv.show
  (:use [gggv.runtime :only (hydra t mag-linear out)]
        [gggv.core :only (shader shader-gen)]
        [hydra.core :only (colorize geometry recolor render)]
        [hydra.filters :only (pulsate scale osc shift-hsv posterize)]))

(def mix-layers (shader {:f "mix.layers"} {}))

(def sin (.-sin js/Math))
(def cosin (.-cos js/Math))

(def background (-> (osc {})
                    (scale #(+ 20 (* 5 (cosin t))))
                    (pulsate #(* 10 (sin t)) 2.5)
                    (shift-hsv #(mod (/ t 30) 1) #(mod (/ t 20) 1) #(mod (/ t 25) 1))
                    (posterize 10 1)
                    hydra
                    out))
