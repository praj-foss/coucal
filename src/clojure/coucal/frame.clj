(ns coucal.frame
  (:import (coucal.util ReadUtil)
           (java.io InputStream)))

;; Frame id
(defn read-id [stream]
  (ReadUtil/frameId stream))

;; Album title
(defn- read-album [stream size]
  {:album (ReadUtil/text stream size)})

;; Beats per minute
(defn- read-bpm [stream size]
  {:bpm (Integer/parseInt (ReadUtil/text stream size))})

;; Composers
(defn- read-composers [stream size]
  {:composers (ReadUtil/text stream size)})

;; Genre
(defn- read-genre [stream size]
  {:genre (ReadUtil/text stream size)})

;; Copyright message
(defn- read-copyright [stream size]
  {:copyright (ReadUtil/text stream size)})

;; Song title
(defn- read-title [stream size]
  {:title (ReadUtil/text stream size)})

;; Track number
(defn- read-track-num [stream size]
  {:track-num (ReadUtil/text stream size)})

;; Year of recording
(defn- read-year [stream size]
  {:year (Integer/parseInt (ReadUtil/text stream size))})

;; Content readers for supported frames
(def ^:private readers
  {"TALB" read-album
   "TBPM" read-bpm
   "TCOM" read-composers
   "TCON" read-genre
   "TCOP" read-copyright
   "TIT2" read-title
   "TRCK" read-track-num
   "TYER" read-year})

;; Skip by given bytes
(defn- skip [stream bytes]
  (ReadUtil/skip stream bytes))

;; Read supported content, skip otherwise
(defn read-or-skip
  [^InputStream stream ^String id size]
  ((get readers id skip) stream size))
