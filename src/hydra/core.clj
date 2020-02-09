(ns hydra.core
  (:gen-class))

(defn mix-args [args base]
  (concat (take (count base) args)
          (nthrest base (count args))))

(defn colorize [name base glsl args]
  [{:name name
    :decl glsl
    :args (mix-args args base)}])
