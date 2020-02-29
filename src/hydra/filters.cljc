(ns hydra.filters
  (:use [hydra.core :only (colorize geometry recolor)]))

(def from
  (partial colorize :from [:storage]
           "vec4 from(vec2 xy, layout(rgba8) image2D storage) { return imageLoad(storage, ivec2(xy * vec2(windowWidth, windowHeight))); }"))

(def osc
  (partial colorize :osc []
           "vec4 osc(vec2 xy) { return vec4(.5 * (1 + cos(xy)), 0, 1); }"))

(def pulsate
  (partial geometry :pulsate [1 1]
           "vec2 pulsate(vec2 xy, float r, float ex) {
             vec2 p = cToP(xy);
             p.x -= 3 * pow(abs( cos(p.x - time*r)), ex);
             return pToC(p);
            }"))

(def scale
  (partial geometry :scale [20]
           "vec2 scale(vec2 xy, float r) { return xy * vec2(r); }"))

(def pixelate
  (partial geometry :pixelate [40]
           "vec2 pixelate(vec2 xy, float n) { return floor(xy * n) / n; }"))

(def rotate
  (partial geometry :rotate [0]
           "vec2 rotate(vec2 xy, float theta) { return pToC(cToP(xy) + vec2(0, 2*PI*theta)); }"))

(def shift-hsv
  (partial recolor :shiftHsv [0.5 0.5 0.5]
           "vec4 shiftHsv(vec4 c, float h, float s, float v) {
              vec3 x = rgb2hsv(c.rgb);
              return vec4(hsv2rgb(fract(x + vec3(h, s, v))), 1);
            }"))

(def pride
  (partial recolor :pride [[0 0 0] [0.5 0.5 0.5] [1 1 1] 0.333 0.333 0.333]
           "vec4 pride(vec4 c, vec3 c1, vec3 c2, vec3 c3, float w1, float w2, float w3) {
              c1 = hsv2rgb(c1);
              c2 = hsv2rgb(c2);
              c3 = hsv2rgb(c3);
              float x = rgb2hsv(c.rgb).z * (w1 + w2 + w3);
              if (x<w1) return vec4(c1, 1);
              if (x<w1 + w2) return vec4(c2, 1);
              return vec4(c3, 1);
            }"))

(def haze
  (partial recolor :haze [0.01 :storage]
           "vec4 haze(vec4 c, float speed, layout(rgba8) image2D store) {
              vec4 p = imageLoad(store, iftc);
              c = p + clamp(c-p, -vec4(speed), vec4(speed));
              imageStore(store, iftc, c);
              return vec4(c.rgb, c.a);
            }"))

(def invert
  (partial recolor :invert []
           "vec4 invert(vec4 c) {
              return vec4(1 - c.rgb, c.a);
            }"))

(def threshold
  (partial recolor :threshold []
           "vec4 threshold(vec4 c) {
              if (length(c.rgb) > 1) return c;
              return vec4(0);
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
(def oc
  (partial recolor :setOutputColor []
           "vec4 setOutputColor(vec4 c) { outputColor = c; return c; }"))

(def store
  (partial recolor :store [:storage]
           "vec4 store(vec4 c, layout(rgba8) image2D storage) {
              imageStore(storage, iftc, c);
              return c;
            }"))

(defn to [chain name] (store chain {:storage name}))
