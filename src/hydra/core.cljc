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

(defn prepare-vec-uniform [v]
  (if (some fn? v)
      (fn [] (map #(if (fn? %) (%) %) v))
      v))

(defn storage? [v]
  (or (= v :storage)
      (:storage v)))

(defn vec4? [v]
  (and (coll? v)
       (= 3 (count v))
       (all? #(number? (if (fn? %) (%) %)) v)))

(defn vec3? [v]
  (and (coll? v)
       (= 3 (count v))
       (all? #(number? (if (fn? %) (%) %)) v)))

(defn external-symbol? [v]
  (and (not (storage? v))
       (or (symbol? v)
           (keyword? v))))

(defn uniform? [v]
  (or (storage? v)
      (number? v)
      (vec3? v)
      (fn? v)))

(defn standardize-uniform [v]
  (cond (= v :storage) {:name (gensym 'storage), :kind :image2D}
        (symbol? v)    {:name (name v)}
        (keyword? v)   {:name (name v)}
        (:storage v)   {:name (name (:storage v)), :kind :image2D}
        (number? v)    {:name (gensym 'u),         :kind :float, :value v}
        (:float v)     {:name (name (:float v)),   :kind :float, :value (:value v)}
        (vec3? v)      {:name (gensym 'u),         :kind :vec3,  :value (prepare-vec-uniform v)}
        (:vec3 v)      {:name (name (:vec3 v)),    :kind :vec3,  :value (prepare-vec-uniform (:value v))}
        (vec4? v)      {:name (gensym 'u),         :kind :vec4,  :value (prepare-vec-uniform (prepare-vec-uniform v))}
        (:vec4 v)      {:name (name (:ve4 v)),     :kind :vec4,  :value (prepare-vec-uniform (:value v))}
        (fn? v)        (merge (standardize-uniform (v)) {:value v})))

(defn render [v]
  (cond
    (nil? v) {:s "ftc", :u {}, :t {}, :r #{}}
    (uniform? v) (let [u (standardize-uniform v)
                       named (:name u)
                       kind  (:kind u)
                       value (:value u)]
                   {:s named
                    :t (if kind  {named (str (if (storage? v) "layout(rgba8) ") "uniform " (name kind) " " named ";")} {})
                    :u (if (nil? value) {} {named value})
                    :r (if (storage? v) #{named})})
    (external-symbol? v) {:s (name v)}
    (coll? v) (let [step  (last v)
                    chain (render (butlast v))
                    args  (map render (:args step))]
                {:i (concat (:i chain) (:sources step))
                 :s (apply str (flatten [(name (:name step)) "(" (:s chain)
                                         (map #(list ", " (:s %)) args)
                                         ")"]))
                 :u (merge (:u chain) (apply merge (map :u args)))
                 :t (merge {(:name step) (:decl step)} (:t chain) (apply merge (map :t args)))
                 :r (clojure.set/union (:r chain) (apply clojure.set/union (map :r args)))})))
