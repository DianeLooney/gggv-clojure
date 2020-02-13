(ns hydra.core-test
  (:require [clojure.test :refer :all]
            [hydra.core :as h]))

(deftest mix-args
  (are [x y z] (= x (h/mix-args y z))
    []      []   []
    []      [:a] []
    [:a :b] []   [:a :b]
    [:c :b] [:c] [:a :b]))

(deftest colorize
  (is (= (h/colorize "bananas" [:a :b] "glsl" [])
         [{:name "bananas", :decl "glsl", :args '(:a :b)}]))

  (testing "partials"
    (let [p (partial h/colorize "bananas" [:a :b] "glsl")]
      (are [x y] (= x y)
        (p [])   [{:name "bananas", :decl "glsl", :args '(:a :b)}]
        (p [:c]) [{:name "bananas", :decl "glsl", :args '(:c :b)}]))))

(deftest geometry
  (is (= (h/geometry "apples" [:a :b] "glsl" [:pipe] :c)
         [{:name "apples", :decl "glsl", :args [:c :b]}, :pipe]))

  (testing "partials"
    (let [p (partial h/geometry "apples" [:a :b] "glsl")]
      (is (= (-> [:pipe] (p :c)) [{:name "apples", :decl "glsl", :args [:c :b]}, :pipe]))
      (is (= (-> [:pipe] p)      [{:name "apples", :decl "glsl", :args [:a :b]}, :pipe])))))

(deftest recolor
  (is (= (h/recolor "carrots" [:a :b] "glsl" [:pipe] :c)
         [:pipe
          {:name "carrots"
           :decl "glsl"
           :args [:c :b]}])))

(deftest example
  (testing "steps get reordered correctly"
    (let [a (partial h/colorize "apples"    [:a :b] "glsl a")
          b (partial h/geometry "bananas"   [:c :d] "glsl b")
          c (partial h/geometry "carrots"   [:e :f] "glsl c")
          d (partial h/recolor  "dumplings" [:g :h] "glsl d")

          ex1 (-> (a []) d b c)
          ex2 (-> (a []) b d c)
          ex3 (-> (a []) b c d)

          fixture [{:name "carrots",   :decl "glsl c", :args [:e :f]}
                   {:name "bananas",   :decl "glsl b", :args [:c :d]}
                   {:name "apples",    :decl "glsl a", :args [:a :b]}
                   {:name "dumplings", :decl "glsl d", :args [:g :h]}]]

      (are [x y] (= x y)
        ex1 fixture
        ex2 fixture
        ex3 fixture))))

(deftest render
  (is (= (h/render [{:name "apples", :args [420 69], :decl "glsl a"}])
         {:s "apples(ftc, uniform444, uniform445)"
          :u {'uniform444 420, 'uniform445 69}
          :t {"apples" "glsl a"
              'uniform444 "uniform float uniform444;"
              'uniform445 "uniform float uniform445;"}})))
