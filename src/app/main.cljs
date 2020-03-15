(ns app.main
  (:require [browser.core :as browser]))

(defn main! []
  (set! (.-onload js/window) browser/prepare!))

(defn reload! []
  (browser/reload!))
