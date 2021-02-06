The example here links a static library (libstatic_lib.a) twice:
* Once into a dynamic library (libdynamic_lib.so)
* Once into main executable (main)

By definition of static library, the code in it gets baked right into the target being linked. This causes any static variables to have their own independent lifetimes in each of those targets. See [this](https://stackoverflow.com/questions/59755314/will-static-mapvariables-be-freed-multiple-times-if-the-static-library-which-c?noredirect=1&lq=1).

A below is a static local variable defined in libstatic_lib.a and it gets constructed twice, one for main's linking and another for libdynamic_lib.so's linking.

### Output of program
calling static_lib in main   
A ctor   
calling dynamic_lib in main   
dynamic_lib calling static_lib   
A ctor   
A dtor   
A dtor  

Also [see](https://stackoverflow.com/questions/26547454/static-variable-is-initialized-twice)
