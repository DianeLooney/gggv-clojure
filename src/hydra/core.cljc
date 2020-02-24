(ns hydra.core)

(defn mix-args [args base]
  (concat (take (count base) args)
          (nthrest base (count args))))

(def colorize
  (fn
    ([name base glsl]              [{:name name, :decl glsl, :args [], :sources []}])
    ([name base glsl args]         [{:name name, :decl glsl, :args (mix-args args base), :sources []}])
    ([name base glsl sources args] [{:name name, :decl glsl, :args (mix-args args base), :sources sources}])))

(defn geometry [name base glsl pipe & args]
  (concat [{:name name, :decl glsl, :args (mix-args args base)}] pipe))

(defn recolor [name base glsl pipe & args]
  (concat pipe [{:name name, :decl glsl, :args (mix-args args base)}]))

(defn all? [f arr] 
  (if (empty? arr) true
      (and (f (first arr)) (all? f (rest arr)))))

(defn vec4? [v]
  (and (coll? v)
       (= 3 (count v))
        (all? number? v)))

(defn vec3? [v]
  (and (coll? v)
       (= 3 (count v))
        (all? number? v)))

(defn render [v]
  (cond
    (nil? v) {:s "ftc", :u {}, :t {}, :r #{}}
    (vec3? v) (let [k (gensym 'u)]
                {:s k
                 :u {k v}
                 :t {k (str "uniform vec3 " k ";")}
                 :r #{}})
    (vec4? v) (let [k (gensym 'u)]
                {:s k
                 :u {k v}
                 :t {k (str "uniform vec4 " k ";")}
                 :r #{}})
    (coll? v) (let [step  (last v)
                    chain (render (butlast v))
                    args  (map render (:args step))]
                {:i (concat (:i chain) (:sources step))
                 :s (apply str (flatten [(name (:name step)) "(" (:s chain)
                                         (map #(list ", " (:s %)) args)
                                         ")"]))
                 :u (merge (:u chain) (apply merge (map :u args)))
                 :t (merge {(:name step) (:decl step)} (:t chain) (apply merge (map :t args)))
                 :r (clojure.set/union (:r chain) (apply clojure.set/union (map :r args)))})
    (= v :storage) (let [k (gensym 'storage)]
                     {:s k
                      :t {k (str "layout(rgba8) uniform image2D " k ";")}
                      :r #{k}})
    :else (let [k (gensym 'u)]
            {:s k
             :u {k v}
             :t {k (str "uniform float " k ";")}
             :r #{}})))
