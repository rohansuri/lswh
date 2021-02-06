g++ -c -fpic dynamic_lib.cpp
g++ -I. -L. -shared dynamic_lib.o -o libdynamic_lib.so -lstatic_lib
