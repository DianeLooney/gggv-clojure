(ns hydra.filters
  (:use [hydra.core :only (colorize geometry recolor)]))

(def pulsate
  (partial geometry :pulsate [1 1]
           "vec2 pulsate(vec2 xy, float r, float ex) {
             vec2 p = cToP(xy);
             p.x += pow(.5 * (1.2 + sin(p.x * r + 0) / r), ex);
             return pToC(p);
            }"))

(def scale
  (partial geometry :scale [20]
           "vec2 scale(vec2 xy, float r) { return 1 + xy * vec2(r); }"))

(def osc
  (partial colorize :osc []
           "vec4 osc(vec2 xy) { return .5 * (1 + vec4(sin(xy), 0, 1)); }"))

(def shift-hsv
  (partial recolor :shiftHsv [0.5 0.5 0.5]
           "vec4 shiftHsv(vec4 c, float h, float s, float v) {
              vec3 x = rgb2hsv(c.rgb);
              x = fract(x + vec3(h, s, v));
              return vec4(hsv2rgb(x.rgb), c.a);
            }"))

(def posterize
  (partial recolor :posterize [5 0.9]
           "vec4 posterize(vec4 c, float bins, float alpha) {
              c = pow(c, vec4(alpha));
              c *= bins;
              c = floor(c);
              c /= bins;
              c = pow(c, vec4(1. / alpha));
              return c;
            }"))

