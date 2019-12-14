(ns coucal.core
  (:require [clojure.java.io :as io])
  (:import  (coucal.util BitUtil)
            (java.io InputStream)))

(defn- read-version
  [stream]
  {:major (.read stream)
   :minor (.read stream)})

(defn- read-flags
  [stream]
  (let [flags    (.read stream)
        when-bit #(bit-test flags %)]
    (cond-> #{}
            (when-bit 5) (conj :experimental?)
            (when-bit 6) (conj :extended?)
            (when-bit 7) (conj :unsynchronised?))))

(defn- read-size
  [stream]
  (->> #(.read stream)
       (repeatedly 4)
       (map int)
       (int-array)
       (BitUtil/unsynchsafe)))

(defn- read-header
  [^InputStream stream]
  (if-not (->> #(.read stream)
               (repeatedly 3)
               (map char)
               (= [\I \D \3]))
    (throw (ex-info "Invalid ID3v2 tag" {:cause "Magic number not found"}))
    (-> {}
        (assoc :version (read-version stream))
        (assoc :flags (read-flags stream))
        (assoc :size (read-size stream)))))

(defn read-tag
  [path]
  (with-open [stream (io/input-stream path)]
    (read-header stream)))
