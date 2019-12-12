(ns coucal.core
  (:require [coucal.codec :as codec]
            [clojure.java.io :as io]
            [org.clojars.smee.binary.core :refer [decode]]))

(defn read-tag
  [path]
  (with-open [in (io/input-stream path)]
    (decode codec/tag in)))
