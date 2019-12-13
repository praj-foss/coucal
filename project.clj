(defproject coucal "0.0.1-SNAPSHOT"
  :description  "A Clojure library for parsing ID3 tags"
  :url          "https://github.com/praj-foss/coucal"
  :license      {:name "MIT License"
                 :url "http://opensource.org/licenses/MIT"}

  :dependencies [[org.clojure/clojure  "1.8.0"]]

  :source-paths      ["src/clojure"]
  :java-source-paths ["src/java"]
  :javac-options     ["-target" "1.8" "-source" "1.8"])
