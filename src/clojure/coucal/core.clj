(ns coucal.core
  (:require [clojure.java.io :as io])
  (:import  [coucal.util BitUtil]))

(defn- read-id3
  [stream-map]
  (let [head  (->> #(.read (:stream stream-map))
                   (repeatedly 3)
                   (map char)
                   (apply str))]
    (if (= head "ID3")
      stream-map
      (throw (ex-info "Invalid ID3v2 header"
                      {:starts-with head})))))

(defn- read-version
  [stream-map]
  (let [major     (.read (:stream stream-map))
        revision  (.read (:stream stream-map))]
    (if (and (< major  0xFF) (< revision 0xFF))
      (assoc-in stream-map [:result :version]
                (str "2." major "." revision))
      (throw (ex-info "Invalid ID3v2 version"
                      {:major major :revision revision})))))

(defn- read-flags
  [stream-map]
  (let [flags     (.read (:stream stream-map))
        test-bit  #(bit-test flags %)]
    (assoc-in stream-map [:result :flags]
              {:unsynchronised  (test-bit 7)
               :extended-header (test-bit 6)
               :experimental    (test-bit 5)})))

(defn- read-size
  [stream-map]
  ;; TODO: Implement this
  stream-map)

(defn read-tags
  [path]
  (with-open [stream (io/input-stream path)]
    (-> {:stream stream :result {}}
        (read-id3)
        (read-version)
        (read-flags)
        (read-size)
        (:result))))

