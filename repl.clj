(ns repl
  (:require [clojure.string :as str]
            [clojure.java.io]))

;; usually the device name in Linux
(def device "/dev/ttyUSB0")

;; max string length in lua
;; found via experimentation
(def max-string-length 4096)

(defn str->writelines 
  "Convert a string to an array of `io.write` statements.
  Break up the string in multiple statements if it exceeds max-length"
  ([s]
   (str->writelines
     200
     s))

  ([max-length s]
   (assert (<= (count s) max-string-length))
   (loop [[p & ps] (partition-all max-length s)
          res []]
     (let [s (format "io.write([=====[%s%s]=====])" 
                     (apply str p)
                     (if (nil? ps) "\\n" ""))]
       (if (nil? ps)
         (concat res [s])
         (recur ps (concat res [s])))))))
      
(comment
  (str->writelines 30 "hello world")) 

(defn eval-lua [s]
  (let [s' (format "do\n%s\nend\n" s)]
    (doseq [l (str/split-lines s')]
      (spit device (str l "\n")))))


(defn spit-esp [f s]
  (eval-lua (format "
    file=io.open(\"%s\",\"w\")
    io.output(file)
    io.close(file)" f))
  (doseq [p (->> (str/split-lines s)
                 (partition-all 200))
          :let [_ (Thread/sleep 10000)]
          l (->> (concat
                  [(format "file=io.open(\"%s\",\"a\")" f)
                   "io.output(file)"]
                  (->> p
                       (mapcat str->writelines))
                  ["io.close(file)"
                   ;; check length of file
                   "do"
                   "local ctr = 0"
                   (format "for _ in io.lines'%s' do" f)
                   "  ctr = ctr + 1"
                   " end"
                   "print(ctr)"
                   "end"]))]
    (spit device (str l "\n")))
        
 (comment
   (spit-esp "init.lua" "print(\"Hello from lua\" ) ")))

(comment
  (future (with-open [rdr (clojure.java.io/reader "/dev/ttyUSB0")] 
            (doall (map println (line-seq rdr))))))

(defn copy-file 
  ([f]
   (copy-file f f))
  ([f t]
   (->> (slurp f)
        (str/split-lines)
        #_(drop 5670) 
        #_(take 10)
        (str/join "\n")
        (spit-esp t))))

(comment
  (do
    (let [s (apply str (repeat 4097 "x"))]
      (spit "test.txt" s))
    (let [s (->> (slurp "test.txt"))]
     (spit-esp "test.txt" s))))        
 
(comment
  (->> (slurp "fennel.lua")
       (str/split-lines)
       (map-indexed (fn [idx s]
                      [idx (count s)]))
       (sort-by last)
       reverse
       (take 10)))     
       

(comment 
  (eval-lua "
local ctr = 0
for _ in io.lines'test-fennel.lua' do
  print(_)
  ctr = ctr + 1
end
print(ctr) "))

(comment
  (eval-lua "local x=4
            return 2+x"))

(comment
  (eval-lua "return loadfile('test-fennel.lua')"))

(comment
  (eval-lua "print(node.heap())"))
