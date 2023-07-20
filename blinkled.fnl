(local m {:pins {:ESP32_DOIT 2 :ESP32_TTGO 13 :ESP32_Wemos 16}
          :value 0
          :duration 200
          :ctr 0})       
          
(fn toggle-led []
  (if (= m.value 0)
    (set m.value 1)
    (set m.value 0))
  (each [key pin (pairs m.pins)]
    (gpio.write pin m.value)))

(print "Initializing pins")
(each [key pin (pairs m.pins)]
  (print "Initting LED pin for ESP32 device: " key "pin:" pin)
  (gpio.config {:dir gpio.OUT :gpio [pin]}))
  
(set m.mytimer (tmr.create))

(m.mytimer:alarm m.duration tmr.ALARM_AUTO toggle-led)

