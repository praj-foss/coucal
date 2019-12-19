(ns coucal.core
  (:require [coucal.data :as d]
            [coucal.frame :as f])
  (:import  (java.io InputStream)))

(defn- read-flags
  [stream]
  (let [flag-byte (d/byte stream)]
    (cond-> #{}
            (bit-test flag-byte 5) (conj :experimental?)
            (bit-test flag-byte 6) (conj :extended?)
            (bit-test flag-byte 7) (conj :unsynchronised?))))

(defn- read-extra-flags
  [stream]
  (let [flag-byte (d/byte stream)
        _         (d/skip stream 1)]                        ;; TODO: Make use
    (cond-> []
            (bit-test flag-byte 7) (conj :crc?))))

(defn- read-header
  [parse-map]
  (let [stream (:stream parse-map)]
    (if (= "ID3" (d/text stream 3))
      (-> parse-map
          (assoc-in [:tag :version]
                    (str "2." (d/byte stream) "." (d/byte stream)))
          (assoc :flags (read-flags stream))
          (assoc :remaining (d/ssafe stream))
          (as-> pm (assoc-in pm [:tag :size] (:remaining pm))))
      (throw (ex-info "Invalid ID3v2 tag"
                      {:cause "Magic number not found"})))))

(defn- read-extended-header
  [parse-map]
  (if (:extended? (:flags parse-map))
    (let [stream (:stream parse-map)]
      (-> parse-map
          (update :remaining -
                  (+ 4 (d/uint stream)))                    ;; Cut extended header size
          (update :flags into (read-extra-flags stream))
          (update :remaining - (d/uint stream))             ;; Cut padding size
          (conj (if (:crc? (:flags parse-map))
                  {:crc (d/uint stream)}))))
    parse-map))

(defn- read-frames
  [parse-map contents]
  (loop [pm (assoc-in parse-map [:tag :frames] {})]
    (let [stream (:stream pm)
          id     (d/frame-id stream)]
      (if (or (<= (:remaining pm) 0) (nil? id))
        pm
        (let [size (d/uint stream)
              _    (d/skip stream 2)]                       ;; TODO: Make use
          (-> pm
              (update-in [:tag :frames] conj
                         (f/read-or-skip stream id size contents))
              (update :remaining - (+ size 10))
              (recur)))))))

(defn read-tag
  ([^InputStream stream]
   (read-tag stream nil))
  ([^InputStream stream contents]
   (-> {:stream stream :tag {}}
       (read-header)
       (read-extended-header)
       (read-frames contents)
       (:tag))))
