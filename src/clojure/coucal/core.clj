(ns coucal.core
  (:require [clojure.java.io :as io])
  (:import  (coucal.util ReadUtil)
            (java.io InputStream)))

;; Aliases
(defn- read-unsigned-int  [stream] (ReadUtil/unsignedInt stream))
(defn- read-synchsafe-int [stream] (ReadUtil/synchsafeInt stream))
(defn- read-text [stream size] (ReadUtil/text stream size))
(defn- read-frame-id [stream] (ReadUtil/frameId stream))
(defn- skip [stream size] (.skip stream size))

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
    (if (= "ID3" (read-text stream 3))
      (-> parse-map
          (assoc :version
                 (str "2." (.read stream) "." (.read stream)))
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
                  (+ 4 (read-unsigned-int stream)))         ;; Cut extended header size
          (update :flags into (read-extra-flags stream))
          (update :remaining - (read-unsigned-int stream))  ;; Cut padding size
          (conj (if (:crc? (:flags parse-map))
                  {:crc (read-unsigned-int stream)}))))
    parse-map))

;; Supported frames
(def supported
  {"TIT2" [:title read-text]
   "TYER" [:year (comp #(Integer/parseInt %) read-text)]
   "TPE1" [:artist read-text]
   "COMM" [:comments read-text]
   "TALB" [:album read-text]
   "TENC" [:encoded-by read-text]
   "TPUB" [:publisher read-text]
   "TCON" [:genre read-text]
   "TCOP" [:copyright read-text]})
;; TODO: Custom type converters

(defn- read-frames
  [parse-map]
  (let [stream (:stream parse-map)
        id     (read-frame-id stream)]
    (if (or (<= (:remaining parse-map) 0)
            (nil? id))
      parse-map
      (let [size    (read-unsigned-int stream)
            _       (skip stream 2)                         ;; Ignore flags
            frame   (get-in supported [id 0])
            content ((or (get-in supported [id 1])
                          skip) stream size)]
        (-> parse-map
            (update :frames conj (and frame {frame content}))
            (update :remaining - (+ size 10))
            (recur))))))

(defn read-tag
  [path]
  (with-open [^InputStream stream (io/input-stream path)]
    (-> {:stream stream :frames {}}
        (read-header)
        (read-extended-header)
        (read-frames)
        (dissoc :stream :remaining))))
