# Create object file
g++ -c static_lib.cpp -o static_lib.o
# Create archive i.e. static library
ar ru libstatic_lib.a static_lib.o
# ??
ranlib libstatic_lib.a
