(ns coucal.frame
  (:require [clojure.string :as st])
  (:import  (coucal.util ReadUtil)
            (java.io InputStream)))

;; Frame id
(defn read-id [stream]
  (ReadUtil/frameId stream))

(defn- split-slash [^String text]
  (st/split text #"/"))

(defn- parse-int [^String text]
  (Integer/parseInt text))

(defn- skip [stream bytes]
  (ReadUtil/skip stream bytes))

;; Supported string frames
;; Maps frame id to a keyword and a string adapter
(def ^:private string-frames
  {"TALB" [:album]
   "TBPM" [:bpm parse-int]
   "TCOM" [:composers split-slash]
   "TCON" [:genre]
   "TCOP" [:copyright]
   "TDAT" [:date]
   "TDLY" [:delay parse-int]
   "TENC" [:encoded-by]
   "TEXT" [:lyricists split-slash]
   "TFLT" [:file-type]
   "TIME" [:time]
   "TIT1" [:group]
   "TIT2" [:title]
   "TIT3" [:subtitle]
   "TKEY" [:initial-key]
   "TLAN" [:languages]
   "TLEN" [:length parse-int]
   "TMED" [:media-type]
   "TOAL" [:original-album]
   "TOFN" [:original-filename]
   "TOLY" [:original-lyricists split-slash]
   "TOPE" [:original-artists split-slash]
   "TORY" [:original-year parse-int]
   "TOWN" [:owner]
   "TPE1" [:artists split-slash]
   "TPE2" [:band]
   "TPE3" [:conductor]
   "TPE4" [:modified-by]
   "TPOS" [:part-num]
   "TPUB" [:publisher]
   "TRCK" [:track-num]
   "TRDA" [:recording-dates]
   "TRSN" [:radio-station]
   "TRSO" [:radio-owner]
   "TSIZ" [:audio-size parse-int]
   "TSRC" [:isrc]
   "TSEE" [:settings]
   "TYER" [:year parse-int]})

;; Read supported content, skip otherwise
(defn read-or-skip
  [^InputStream stream id size adapters]
  (condp contains? id
    string-frames
      (let [[fr default] (get string-frames id)
            content      (ReadUtil/text stream size)]
        {fr (try
              ((or (fr adapters) (:rest adapters) default identity)
               content)
              (catch Exception _
                (throw (ex-info
                         (str "Failed to adapt content of " id)
                         {:frame fr :content content}))))})
    (skip stream size)))
