# esp32-fennel

WIP

Aim: Compile fennel code to Lua, send to ESP32 (with NodeMCU) to evaluate, and receive back result.

## Set up and test

```
# set baud rate
$ stty -F /dev/ttyUSB0 115200
# disable flow control
$ stty -F /dev/ttymxc2 -crtscts
# start listening to output
$ cat /dev/ttyUSB0
# send some lua code
$ echo -e "do local x=4\nprint(x+2)\nend\n" > /dev/ttyUSB0
```

## Send Fennel program to the device:

```
$ fennel --compile blinkled.fnl  | ./wrap_do.sh | tee /dev/ttyUSB0
```

## Getting Clojure in the mix

Listening for output from Clojure

```
$ clj -e '(with-open [rdr (clojure.java.io/reader "/dev/ttyUSB0")] (doall (map println (line-seq rdr))))'
```

Sending some code with Clojure:

```clj
user=> (spit "/dev/ttyUSB0" "do\nlocal x=5\nprint(x+3)\nend\n")
```

