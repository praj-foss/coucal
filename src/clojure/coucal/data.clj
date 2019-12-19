(ns coucal.data
  (:import (coucal.util ReadUtil)
           (java.io InputStream)))

(defn skip [stream size]
  (ReadUtil/skip stream size))

(defn frame-id [stream]
  (ReadUtil/frameId stream))

(defn ssafe [stream]
  (ReadUtil/synchsafeInt stream))

(defn uint [stream]
  (ReadUtil/unsignedInt stream))

(defn byte [stream]
  (.read ^InputStream stream))

(defn text [stream size]
  (ReadUtil/text stream size))

(defn num-string [stream size]
  (Integer/parseInt (ReadUtil/text stream size)))
