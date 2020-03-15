(ns hydra.filters
  (:use [hydra.core :only (colorize geometry recolor)]))

(def from
  (partial colorize :from [:storage]
           "vec4 from(vec2 xy, layout(rgba8) image2D storage) {
              vec2 _xy = fract(xy);
              return imageLoad(
                storage,
                ivec2(_xy*vec2(windowWidth, windowHeight))
              );
            }"))

(def osc
  (partial colorize :osc []
           "Pixel osc(Pixel p) {
              p.color = vec4(.5 * (1 + cos(p.xy)), 0, 1);
              return p;
            }"))

(def pulsate
  (partial geometry :pulsate [1 0]
           "Pixel pulsate(Pixel p, float offset, float amplitude) {
              p.xy = cToP(p.xy);
              p.xy.x += 20*sin(p.xy.x + time/9);
              return p;
            }"))

(def polarize
  (partial geometry :polarize []
           "Pixel polarize(Pixel p) {
            p.xy = cToP(p.xy);
            return p;
          }"))

(def center
  (partial geometry :center []
           "Pixel center(Pixel p) {
              p.xy = (0.5 - p.xy)*vec2(windowWidth/windowHeight, 1);
              return p;
            }"))

(def hsv->rgb
  (partial recolor :hsvToRgb []
           "Pixel hsvToRgb(Pixel p) {
              p.color = vec4(hsv2rgb(p.color.rgb), p.color.a);
              return p;
            }"))

(defn swiz [pattern]
  (partial recolor
           (keyword (str "swizzle" pattern))
           []
           (str "Pixel swizzle" pattern "(Pixel p) {
                   p.color.rgb = p.color." pattern ";
                   return p;
                 }")))

(def swizzleRGB (swiz "rgb"))
(def swizzleRBG (swiz "rbg"))
(def swizzleGRB (swiz "grb"))
(def swizzleGBR (swiz "gbr"))
(def swizzleBGR (swiz "bgr"))
(def swizzleBRG (swiz "brg"))

(def diff
  (partial recolor :diff [1 :storage]
           "vec4 diff(vec4 c, float amp, layout(rgba8) image2D store) {
              vec4 prev = imageLoad(store, iftc);
              vec4 diff = vec4(c.rgb - prev.rgb, 1);
              imageStore(store, iftc, c);
              diff.rgb = mod(diff.rgb, vec3(1));
              if(diff.r > 0.5) diff.r = 0;
              if(diff.g > 0.5) diff.g = 0;
              if(diff.b > 0.5) diff.b = 0;
              return diff*20;
            }"))

(def cells
  (partial colorize :cells [0]
           "Pixel cells(Pixel p, float seed) {
              p.xy = cellular(p.xy, seed);
              p.color = vec4(vec3(0.1 + p.xy.y - p.xy.x), 1);
              return p;
            }"))

(def warp
  (partial geometry :warp []
           "Pixel warp(Pixel p) {
              p.xy = atan(p.xy);
              return p;
            }"))

(def scale
  (partial geometry :scale [20]
           "Pixel scale(Pixel p, float r) {
              p.xy = p.xy * vec2(r);
              return p;
            }"))

(def pixelate
  (partial geometry :pixelate [40]
           "Pixel pixelate(Pixel p, float n) {
              p.xy = floor(p.xy * n) / n;
              return p;
            }"))

(def rotate
  (partial geometry :rotate [0]
           "Pixel rotate(Pixel p, float theta) {
              p.xy = pToC(cToP(p.xy) + vec2(0, 2*PI*theta));
              return p;
            }"))

(def shift-hsv
  (partial recolor :shiftHsv [0.5 0.5 0.5]
           "Pixel shiftHsv(Pixel p, float h, float s, float v) {
              vec3 x = rgb2hsv(p.color.rgb);
              p.color = vec4(hsv2rgb(fract(x + vec3(h, s, v))), 1);
              return p;
            }"))

(def pride
  (partial recolor :pride [[0 0 0] [0.5 0.5 0.5] [1 1 1] 0.333 0.333 0.333]
           "Pixel pride(Pixel p, vec3 c1, vec3 c2, vec3 c3, float w1, float w2, float w3) {
              c1 = hsv2rgb(c1);
              c2 = hsv2rgb(c2);
              c3 = hsv2rgb(c3);
              float x = rgb2hsv(p.color.rgb).b * (w1 + w2 + w3);
              if (x<w1) {
                p.color = vec4(c1, 1);
              } else if (x<w1 + w2) {
                p.color = vec4(c2, 1);
              } else {
                p.color = vec4(c3, 1);
              }
              return p;
            }"))

(def haze
  (partial recolor :haze [0.01 :storage]
           "Pixel haze(Pixel p, float speed, layout(rgba8) image2D store) {
              vec4 v = imageLoad(store, iftc);
              p.color = v + clamp(p.color-v, -vec4(speed), vec4(speed));
              imageStore(store, iftc, p.color);
              return p;
            }"))

(def invert
  (partial recolor :invert []
           "Pixel invert(Pixel p) {
              p.color = vec4(1 - p.color.rgb, p.color.a);
              return p;
            }"))

(def threshold
  (partial recolor :threshold []
           "vec4 threshold(vec4 c) {
              if (length(c.rgb) > 1) return c;
              return vec4(0);
            }"))

(def posterize
  (partial recolor :posterize [5 0.9]
           "Pixel posterize(Pixel p, float bins, float alpha) {
              p.color = pow(p.color, vec4(alpha));
              p.color *= bins;
              p.color = floor(p.color);
              p.color /= bins;
              p.color = pow(p.color, vec4(1. / alpha));
              return p;
            }"))
(def oc
  (partial recolor :setOutputColor []
           "Pixel setOutputColor(Pixel p) {
              outputColor = p.color;
              return p;
            }"))

(def store
  (partial recolor :store [:storage]
           "Pixel store(Pixel p, layout(rgba8) image2D storage) {
              imageStore(storage, iftc, p.color);
              return c;
            }"))

(defn to [chain name] (store chain {:storage name}))
