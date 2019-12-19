coucal
======
A Clojure library for parsing ID3 tags from MP3 files.
It only supports ID3v2.3 tags currently, which is also the
most popular version as of writing this. Support for other
versions will be coming soon. 

It's a fun project created while I was learning Clojure.
A lot more features have been planned including support
for writing out tags. Feel free to submit an issue or a
PR if you feel like it.
:relaxed:

### Examples
```clojure
;; First
(require '[coucal.core :as cou]
         '[clojure.java.io :as io])

;; Read from MP3
(with-open [mp3 (io/input-stream "The Calling.mp3")]
  (cou/read-tag mp3))
=>
{:version "2.3.0",
 :size 4342,
 :frames {:genre "(17)Rock",
          :track-num "7",
          :publisher "Exzel Music Co.",
          :composers ["Rick Costello"],
          :band "Angelwing",
          :title "The Calling [Nymph Mix]",
          :year 2001,
          :length 199866,
          :artists ["Angelwing"],
          :album "The Nymphaeum"}}
```

## License
This project is licensed under the MIT License. 
Please see the [LICENSE](LICENSE) file for more information.
