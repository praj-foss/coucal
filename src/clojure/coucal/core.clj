(ns coucal.core
  (:require [clojure.java.io :as io])
  (:import  (coucal.util ReadUtil)
            (java.io InputStream)))

;; Aliases
(defn- read-unsigned-int  [stream] (ReadUtil/unsignedInt stream))
(defn- read-synchsafe-int [stream] (ReadUtil/synchsafeInt stream))
(defn- read-str [stream size] (ReadUtil/string stream size))
(defn- read-frame-id [stream] (ReadUtil/frameId stream))

(defn- read-version
  [stream]
  {:major (.read stream)
   :minor (.read stream)})

(defn- read-flags
  [stream]
  (let [flag-byte (.read stream)]
    (cond-> #{}
            (bit-test flag-byte 5) (conj :experimental?)
            (bit-test flag-byte 6) (conj :extended?)
            (bit-test flag-byte 7) (conj :unsynchronised?))))

(defn- read-extra-flags
  [stream]
  (let [flag-byte (.read stream)
        _         (.read stream)]                           ;; TODO: Make use
    (cond-> []
            (bit-test flag-byte 7) (conj :crc?))))

(defn- read-header
  [parse-map]
  (let [stream (:stream parse-map)]
    (if (= "ID3" (read-str stream 3))
      (-> parse-map
          (assoc :version (read-version stream))
          (assoc :flags (read-flags stream))
          (assoc :remaining (read-synchsafe-int stream)))
      (throw (ex-info "Invalid ID3v2 tag"
                      {:cause "Magic number not found"})))))

(defn- read-extended-header
  [parse-map]
  (if (:extended? (:flags parse-map))
    (let [stream (:stream parse-map)]
      (-> parse-map
          (update :remaining -
                  (+ 4 (read-unsigned-int stream)))           ;; Cut extended header size
          (update :flags into (read-extra-flags stream))
          (update :remaining - (read-unsigned-int stream))    ;; Cut padding size
          (conj (if (:crc? (:flags parse-map))
                  {:crc (read-unsigned-int stream)}))))
    parse-map))

(defn- read-frames
  [parse-map]
  (let [stream (:stream parse-map)
        id     (read-frame-id stream)]
    (if (or (<= (:remaining parse-map) 0)
            (nil? id))
      parse-map
      (let [size (read-unsigned-int stream)
            _    (.skip stream (+ size 2))]
        (-> parse-map
            (update :frames conj id)
            (update :remaining - (+ size 10))
            (recur))))))

(defn read-tag
  [path]
  (with-open [^InputStream stream (io/input-stream path)]
    (-> {:stream stream :frames []}
        (read-header)
        (read-extended-header)
        (read-frames)
        (:frames))))
