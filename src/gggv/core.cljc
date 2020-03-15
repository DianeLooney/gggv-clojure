(ns gggv.core)

(defn osc [endpoint & data]
  (if (some fn? data)
    (fn [] {:address endpoint, :args (flatten (map #(if (fn? %) (%) %) data))})
    {:address endpoint, :args (flatten data)}))

(defn uniform->osc [shader name value]
  (osc "/source.shader/set/uniform1f" shader name value))

(defn geometry->osc [name data]
  (apply osc (flatten ["/source.shader/set/geometry" name data])))

(defn shader->osc [data]
  [(map hash->osc (:inputs data))
   (osc "/program/watch" (:program data) (:vert data) (:geom data) (:frag data))
   (osc "/source.shader/create" (:name data))
   (osc "/source.shader/set/program" (:name data) (:program data))
   (osc "/source/set/magfilter" (:name data) (:mag data))
   (osc "/source.shader/set/dimensions" (:name data) (:width data) (:height data))
   (osc "/source.shader/set/drawcount" (:name data) (:pc data))
   (geometry->osc (:name data) (:geometry data))
   (map #(osc "/source.shader/add/storage" (:name data) %) (:storage data))
   (map-indexed #(osc "/source.shader/set/input" (:name data) %1 (:name %2)) (:inputs data))
   (map #(uniform->osc (:name data) %1 (get (:uniforms data) %1)) (keys (:uniforms data)))])

(defn ffvideo->osc [data & timescale]
  [(osc "/source.ffvideo/create" (:name data) (:path data))
   (osc "/source.ffvideo/set/timescale" (:name data) (if (nil? timescale) 1 timescale))])

(defn fft->osc [data] [(osc "/source.fft/create" (:name data))
                       (osc "/source.fft/scale" (:name data) (:scale data))])

(defn hash->osc [data]
  (flatten
   (case (:source data)
     :shader  (shader->osc data)
     :ffvideo (ffvideo->osc data)
     :fft     (fft->osc data)
     [])))

(defn shader-fn [n base-uniforms s specific-uniforms]
  (let [name (gensym)
        vert (or (:v n) "default")
        geom (or (:g n) "default")
        frag (or (:f n) "default")
        vert-path  (or (:vp n) (str "shaders/vert/" vert ".glsl"))
        geom-path  (or (:gp n) (str "shaders/geom/" geom ".glsl"))
        frag-path  (or (:fp n) (str "shaders/frag/" frag ".glsl"))
        inputs     (if (nil? s) [] (flatten [s]))
        inputNames (map #(:name %1) inputs)
        geometry   (or (:geometry specific-uniforms)
                       (:geometry base-uniforms)
                       [-1 -1 0 0 0 0
                        +1 -1 0 1 0 0
                        -1 +1 0 0 1 0
                        +1 -1 0 1 0 0
                        -1 +1 0 0 1 0
                        +1 +1 0 1 1 0])
        particle-count (or (:pc specific-uniforms)
                           (:pc base-uniforms)
                           1)
        width (or (:w specific-uniforms)
                  (:w base-uniforms)
                  1)
        height (or (:h specific-uniforms)
                   (:h base-uniforms)
                   1)
        uniforms (dissoc (merge base-uniforms specific-uniforms)
                         :geometry :w :h :pc)]
    {:source :shader
     :program name
     :vert vert-path
     :geom geom-path
     :frag frag-path
     :mag "NEAREST"
     :geometry geometry
     :pc particle-count
     :width width
     :height height
     :name name
     :inputs inputs
     :uniforms uniforms}))

(defn shader-gen [n base-uniforms]
  (fn [& [specific-uniforms]] (shader-fn n base-uniforms [] specific-uniforms)))

(defn shader [n base-uniforms]
  (fn [s & [specific-uniforms]] (shader-fn n base-uniforms s specific-uniforms)))

(defn out [data]
  (let [messages (hash->osc data)
        suffix   (osc "/source.shader/set/input" "window" 0 (:name data))]
    (apply push-osc (flatten [messages suffix]))))
