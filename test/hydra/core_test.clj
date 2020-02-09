(ns hydra.core-test
  (:require [clojure.test :refer :all]
            [hydra.core :refer :all]))

(deftest test-mix-args
  (are [x y z] (= x (mix-args y z))
    []      []   []
    []      [:a] []
    [:a :b] []   [:a :b]
    [:c :b] [:c] [:a :b]))
