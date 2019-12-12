(ns coucal.codec
  (:require [org.clojars.smee.binary.core :as bin])
  (:import  [coucal.util BitUtil]))

(def ^:private synchsafe
  (bin/compile-codec :int-be #(BitUtil/synchsafe %) #(BitUtil/unsynchsafe %)))

(def header
  (bin/ordered-map
    :magic-number (bin/constant (bin/string "ISO-8859-1" :length 3) "ID3")
    :version      (bin/ordered-map
                    :major :byte
                    :minor :byte)
    :flags        (bin/bits [nil nil nil nil
                             :footer?
                             :experimental?
                             :extended-header?
                             :unsynchronised?])
    :tag-size     synchsafe))

(def tag (bin/compile-codec [header]))
