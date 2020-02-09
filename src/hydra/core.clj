(ns hydra.core
  (:gen-class))

(defn mix-args [args base]
  (concat (take (count base) args)
          (nthrest base (count args))))

(defn colorize [name base glsl args]
  [{:name name, :decl glsl, :args (mix-args args base)}])

(defn geometry [name base glsl pipe & args]
  (concat [{:name name, :decl glsl, :args (mix-args args base)}] pipe))

(defn recolor [name base glsl pipe & args]
  (concat pipe [{:name name, :decl glsl, :args (mix-args args base)}]))

(defn render [v]
  (cond
    (nil? v) {:s "ftc", :u {}}
    (not (coll? v)) (let [k (gensym 'uniform)] {:s k, :u {k v}})
    :else (let [step  (last v)
                chain (render (butlast v))
                args  (map render (:args step))]
            {:s (apply str (flatten [(:name step) "(" (:s chain)
                                     (map #(list ", " (:s %)) args)
                                     ")"]))
             :u (apply merge (map :u args))})))
