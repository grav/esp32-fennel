(require '[clojure.string :as str])

(def device "/dev/ttyUSB0")

(defn str->writelines 
  ([s]
   (str->writelines
     200 s))
  ([max-length s]
   (loop [[p & ps] (partition-all max-length s)
          res []]
     (let [s (format "io.write([===[%s%s]===])" 
                     (apply str p)
                     (if (nil? ps) "\\\n" ""))]
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
                   "do"
                   "local ctr = 0"
                   "for _ in io.lines'fennel.lua' do"
                   "  ctr = ctr + 1"
                   " end"
                   "print(ctr)"
                   "end"]))]
    (spit device (str l "\n")))
        
 (comment
   (spit-esp "init.lua" "print(\"Hello from lua\" ) "))

 (comment
   (let [s (->> (slurp "fennel.lua")
                str/split-lines)]

     (spit-esp "fennel.lua" (str/join "\n" s))))

 
 (comment
   (let [s (->> (slurp "test.lua"))]
     (spit-esp "init.lua" s)))        
 
 (comment
   (->> (slurp "test.lua")
        (str/split-lines)
        (map #(str/replace % "\\n" "v")))))

(comment 
  (eval-lua "
local ctr = 0
for _ in io.lines'fennel.lua' do
  ctr = ctr + 1
end
print(ctr) "))

