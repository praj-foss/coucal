(ns coucal.core
  (:require [clojure.java.io :as io])
  (:import  (coucal.util ReadUtil)
            (java.io InputStream)))

;; Aliases
(defn- read-unsigned-int  [stream] (ReadUtil/unsignedInt stream))
(defn- read-synchsafe-int [stream] (ReadUtil/synchsafeInt stream))

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

(defn- read-extra-flags
  [stream]
  (let [flags-1  (.read stream)
        _        (.read stream)                             ;; TODO: Make use
        when-bit #(bit-test %1 %2)]
    (cond-> #{}
            (when-bit flags-1 7) (conj :crc?))))

(defn- read-extended
  [stream]
  (as-> {} extended
        (assoc extended :size (read-unsigned-int stream))
        (assoc extended :flags (read-extra-flags stream))
        (assoc extended :padding (read-unsigned-int stream))
        (conj extended
              (if (:crc? (:flags extended))
                {:crc (read-unsigned-int stream)}))))

(defn- read-header
  [^InputStream stream]
  (if-not (->> #(.read stream)
               (repeatedly 3)
               (map char)
               (= [\I \D \3]))
    (throw (ex-info "Invalid ID3v2 tag"
                    {:cause "Magic number not found"}))
    (as-> {} header
          (assoc header :version (read-version stream))
          (assoc header :flags (read-flags stream))
          (assoc header :size (read-synchsafe-int stream))
          (conj header
                (if (:extended? (:flags header))
                  (select-keys (read-extended stream)
                               [:padding :crc]))))))

(defn read-tag
  [path]
  (with-open [stream (io/input-stream path)]
    (read-header stream)))
