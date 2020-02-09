(ns hydra.core-test
  (:require [clojure.test :refer :all]
            [hydra.core :refer :all]))

(deftest test-mix-args
  (are [x y z] (= x (mix-args y z))
    []      []   []
    []      [:a] []
    [:a :b] []   [:a :b]
    [:c :b] [:c] [:a :b]))

(deftest test-colorize
  (is (= (colorize "bananas" [:a :b] "glsl" [])
         [{:name "bananas"
           :decl "glsl"
           :args '(:a :b)}])))

(deftest test-partial-colorize
  (let [p (partial colorize "bananas" [:a :b] "glsl")]
    (are [x y] (= x y)
      (p []) [{:name "bananas"
            :decl "glsl"
            :args '(:a :b)}]
      (p [:c]) [{:name "bananas"
               :decl "glsl"
               :args '(:c :b)}])))
