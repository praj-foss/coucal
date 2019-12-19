(ns coucal.frame
  (:require [coucal.data :as d]
            [clojure.string :as s])
  (:import  (java.io InputStream)))

(def ^:private slashed
  (comp #(s/split % #"/") d/text))

;; Maps supported frame ids to keywords and content readers
(def ^:private frames
  ;; Texts
  {"TALB" [:album d/text]
   "TBPM" [:bpm d/num-string]
   "TCOM" [:composers slashed]
   "TCON" [:genre d/text]
   "TCOP" [:copyright d/text]
   "TDAT" [:date d/text]
   "TDLY" [:delay d/num-string]
   "TENC" [:encoded-by d/text]
   "TEXT" [:lyricists slashed]
   "TFLT" [:file-type d/text]
   "TIME" [:time d/text]
   "TIT1" [:group d/text]
   "TIT2" [:title d/text]
   "TIT3" [:subtitle d/text]
   "TKEY" [:initial-key d/text]
   "TLAN" [:languages d/text]
   "TLEN" [:length d/num-string]
   "TMED" [:media-type d/text]
   "TOAL" [:original-album d/text]
   "TOFN" [:original-filename d/text]
   "TOLY" [:original-lyricists slashed]
   "TOPE" [:original-artists slashed]
   "TORY" [:original-year d/num-string]
   "TOWN" [:owner d/text]
   "TPE1" [:artists slashed]
   "TPE2" [:band d/text]
   "TPE3" [:conductor d/text]
   "TPE4" [:modified-by d/text]
   "TPOS" [:part-num d/text]
   "TPUB" [:publisher d/text]
   "TRCK" [:track-num d/text]
   "TRDA" [:recording-dates d/text]
   "TRSN" [:radio-station d/text]
   "TRSO" [:radio-owner d/text]
   "TSIZ" [:audio-size d/num-string]
   "TSRC" [:isrc d/text]
   "TSEE" [:settings d/text]
   "TYER" [:year d/num-string]

   ;; URL links
   "WCOM" [:url-commercial d/text]
   "WCOP" [:url-copyright d/text]
   "WOAF" [:url-audio d/text]
   "WOAR" [:url-artist d/text]
   "WOAS" [:url-source d/text]
   "WORS" [:url-radio d/text]
   "WPAY" [:url-payment d/text]
   "WPUB" [:url-publisher d/text]})

;; Read supported content, skip otherwise
(defn read-or-skip
  [^InputStream stream id size contents]
  (if (contains? frames id)
    (let [[frame default] (get frames id)
          content         (or (frame contents)
                              (:rest contents)
                              default)]
      (try
        {frame (content stream size)}
        (catch Exception _
          (throw (ex-info
                   "Failed to read frame content"
                   {:id id :frame frame})))))
    (d/skip stream size)))
