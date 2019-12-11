(ns coucal.core
  (:require [org.clojars.smee.binary.core :as bin]
            [clojure.java.io :as io])
  (:import  [coucal.util BitUtil]))

(def :synchsafe
  (bin/compile-codec :int-be #(BitUtil/synchsafe %) #(BitUtil/unsynchsafe %)))

(def ^:private header
  (bin/ordered-map
    :magic-number (bin/string "ISO-8859-1" :length 3) ;; "ID3"
    :version      (bin/ordered-map
                    :major :byte
                    :minor :byte)
    :flags        (bin/bits [nil nil nil nil
                             :footer?
                             :experimental?
                             :extended-header?
                             :unsynchronised?])
    :tag-size     :synchsafe))

(def ^:private mp3-codec (bin/compile-codec [header]))

(defn read-mp3
  [path]
  (with-open [in (io/input-stream path)]
    (bin/decode mp3-codec in)))
