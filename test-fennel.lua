-- To create `fennel.lua` lib, just run `make` in Fennel git repo
local fennel = require("fennel")
local script = "(+ 2 2)"
local scriptcompiled = fennel.compileString(script)

-- prints `return (2 + 2)
print(scriptcompiled)

-- prints the function corresponding to the above
print(load(scriptcompiled))

-- prints the result of evaluating the function
print(load(scriptcompiled)())

