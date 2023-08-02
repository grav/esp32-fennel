# esp32-fennel

WIP

Aim: Compile fennel code to Lua, send to ESP32 (with NodeMCU) to evaluate, and receive back result.

## Set up and test

```
# set baud rate
$ stty -F /dev/ttyUSB0 115200
# disable flow control
$ stty -F /dev/ttyUSB0 -crtscts
# start listening to output
$ cat /dev/ttyUSB0
# send some lua code
$ echo -e "do local x=4\nprint(x+2)\nend\n" > /dev/ttyUSB0
```

Sometimes the `cat` command goes south and loops forever. Then it seems to help to connect with screen:

```
$ screen /dev/ttyUSB0 115200
```
then send `<ctrl+a d>` to disconnect,

and finally kill screen with 
```
$ killall screen
```

## Send Fennel program to the device:

```
$ fennel --compile blinkled.fnl  | ./wrap_do.sh | tee /dev/ttyUSB0
```

The `wrap_do` part is because of how local scope works with interactive mode, see https://www.lua.org/pil/4.2.html

## Getting Clojure in the mix

Might want to code up a "repl" with Clojure.

Listening for output from Clojure

```
$ clj -e '(with-open [rdr (clojure.java.io/reader "/dev/ttyUSB0")] (doall (map println (line-seq rdr))))'
```

Sending some code with Clojure:

```clj
user=> (spit "/dev/ttyUSB0" "do\nlocal x=5\nprint(x+3)\nend\n")
```

## Pretty-printing tables
We need to serialize data when sending it from the ESP32 to the client.
Lua by default does not pretty-print tables, so they'll just be shown as

```lua
> {foo = 42, bar = {baz = {1, 2, "3"}}}
table: 0x600002881980
```

That's no good. Fennel's repl works in another way:
```
>> {:bar {:baz [1 2 "3"]} :foo 42}
{:bar {:baz [1 2 "3"]} :foo 42}
```

One thought was to use [antifennel](https://git.sr.ht/~technomancy/antifennel) to convert the resulting Lua expression back to fennel, and then pretty-print it,
but that won't work because:
1. we still need to pretty-print the result in Lua to send it to antifennel
2. evaluating the code in Fennel won't pretty-print it, but just behave exactly like Lua, eg print something like "`table: 0x600002881980`".

However, Fennel has a specific function for turning a map into a string:
```
>> (local fennel (require :fennel))
>> (fennel.view {:foo 42})
"{:foo 42}"
```
So, do we still need to run the whole Fennel compiler on the ESP32 to do this? Nope, we are so lucky that there's a separate (albeit older) version of the function both in [Fennel](https://git.sr.ht/~technomancy/fennel/tree/3dbee7d40bef802dcf58a07f2daea1db17e59dca/item/fennelview.fnl) but also in [Lua](https://git.sr.ht/~technomancy/fennel/tree/3dbee7d40bef802dcf58a07f2daea1db17e59dca/item/fennelview.lua)

So 
```
$ curl -O https://git.sr.ht/~technomancy/fennel/tree/3dbee7d40bef802dcf58a07f2daea1db17e59dca/item/fennelview.lua
$  lua -e "print((require 'fennelview')(load(\"return {foo = 42, bar = {baz = {1, 2, '3'}}}\")()))"
{
  :bar {
    :baz [1 2 "3"]
  }
  :foo 42
}
```

Now we just need to figure out how to evaluate the code in the correct context.
