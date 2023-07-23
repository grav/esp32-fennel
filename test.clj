(require '[clojure.string :as str])

(def device "/dev/ttyUSB0")

(defn str->writelines 
  ([s]
   (str->writelines
     200 s))
  ([max-length s]
   (concat 
    (->> s
      (partition-all max-length)
      (map (fn [cs]
             ;; bracket levels -- see https://www.lua.org/manual/5.4/manual.html#3.1 
             ;; search for 'long brackets'
             (format "io.write([===[%s]===])" (apply str cs)))))
      
    ["io.write([[\n]])"])))
             
        
                             

(comment
  (str->writelines 3 "hello world")) 

(defn spit-esp [f s]
  (doseq [l (->> (concat
                  [(format "file=io.open(\"%s\",\"w\")" f)
                   "io.output(file)"]
                  (->> (str/split-lines s)
                       #_(map #(str/replace % "'" "\\'"))     
                       #_(map #(str/replace % "\\n" "_"))
                       (mapcat str->writelines))
                  ["io.close(file)"
                   "return 'done'"]))]
    (spit device (str l "\n")))
        
 (comment
   (spit-esp "init.lua" "print(\"Hello from lua\" ) "))

 (comment
   (let [s (->> (slurp "fennel.lua")
                str/split-lines
                #_(drop 398)
                #_(take 1))]
     #_(->> (map count s)
            sort
            reverse
            (take 10))

     (spit-esp "fennel.lua" (str/join "\n" s))))

 
 (comment
   (let [s (->> (slurp "test.lua"))]
     (spit-esp "init.lua" s)))        
 
 (comment
   (->> (slurp "test.lua")
        (str/split-lines)
        (map #(str/replace % "\\n" "v")))))

  
