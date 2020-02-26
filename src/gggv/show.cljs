(ns gggv.show
  (:use [clojure.set   :only (union)]
        [gggv.runtime  :only (hydra t mag-linear out)]
        [gggv.core     :only (shader shader-gen)]
        [hydra.core    :only (colorize geometry recolor render)]
        [hydra.filters :only (pulsate invert haze pride threshold rotate pixelate scale osc shift-hsv posterize)]))

(def mix-layers (shader {:f "mix.layers"} {}))
(def highlights (shader {:f "highlights"} {}))

(def sin (.-sin js/Math))
(def cosin (.-cos js/Math))
(def tan (.-tan js/Math))

(-> (osc {})
    (scale)
    (rotate 0.5)
    (pulsate 1 10.5)
    ;invert
    haze
    (pride {:vec3 'midiHSV3} {:vec3 'midiHSV4} {:vec3 'midiHSV5}
           {:float 'midiV3} {:float 'midiV4} {:float 'midiV5})
    hydra
    out)
