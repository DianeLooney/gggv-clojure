(ns hydra.core
  (:gen-class))


(def prelude (atom ""))

(defn mix-args [args base]
  (concat (take (count base) args)
          (nthrest base (count args))))

(defn colorizer [name base]
  (fn [pipe & args]
    {:func name
     :args (mix-args args base)}))
