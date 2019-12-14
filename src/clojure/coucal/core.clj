(ns coucal.core
  (:require [clojure.java.io :as io])
  (:import  (coucal.util ReadUtil)
            (java.io InputStream)))

;; Aliases
(defn- read-unsigned-int  [stream] (ReadUtil/unsignedInt stream))
(defn- read-synchsafe-int [stream] (ReadUtil/synchsafeInt stream))
(defn- read-str [stream size] (ReadUtil/string stream size))

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

(defn- extend-header
  [header stream]
  (if (:extended? (:flags header))
    (-> header
        (update :remaining -
                (+ 4 (read-unsigned-int stream)))           ;; Cut extended header size
        (update :flags into (read-extra-flags stream))
        (update :remaining + (read-unsigned-int stream))    ;; Add padding size
        (conj (if (:crc? (:flags header))
                {:crc (read-unsigned-int stream)})))
    header))

(defn- read-header
  [^InputStream stream]
  (if (= "ID3" (read-str stream 3))
    (-> {}
        (assoc :version (read-version stream))
        (assoc :flags (read-flags stream))
        (assoc :remaining (read-synchsafe-int stream))
        (extend-header stream))
    (throw (ex-info "Invalid ID3v2 tag"
                    {:cause "Magic number not found"}))))

(defn- read-frames
  [tag stream]
  (if-not (> (:remaining tag) 0)
    tag
    (let [id   (read-str stream 4)
          size (read-unsigned-int stream)
          _    (.skip stream (+ 2 size))]
      (-> tag
          (update :frames conj id)
          (update :remaining - (+ 10 size))
          (recur stream)))))

(defn read-tag
  [path]
  (with-open [stream (io/input-stream path)]
    (-> (read-header stream)
        (assoc :frames [])
        (read-frames stream)
        (dissoc :remaining))))
