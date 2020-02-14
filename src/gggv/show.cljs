(ns gggv.show
  (:use [gggv.runtime :only (hydra t mag-linear out)]
        [gggv.core :only (shader shader-gen)]
        [hydra.core :only (colorize geometry recolor render)]
        [hydra.filters :only (pulsate rotate pixelate scale osc shift-hsv posterize)]))

(def mix-layers (shader {:f "mix.layers"} {}))

(def sin (.-sin js/Math))
(def cosin (.-cos js/Math))

(def background (-> (osc {})
                    (scale)
                    (rotate 0.5)
                    (pulsate 1 10.5)
                    (posterize 10 1)
                    (rotate #(mod t 6.28))
                    hydra
                    out))
