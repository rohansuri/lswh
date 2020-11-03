#include<array>

// NOTE: the template arguments need not be of the same type.
// In this case, we'll get a compile error if we pass different types because
// std::array would expect the list initialisation to have the same types.
// The type compiler will expect for all params depends on the first arg given here.
template<typename VT, typename ... Params>
std::array<VT, sizeof ...(Params)+1> get_data(VT v1, Params ... params) { // Function parameter pack.
	// Parameter pack expansion.
	return {v1, params...};
}

int main() {
	// error: type 'double' cannot be narrowed to 'int' in initializer list [-Wc++11-narrowing]
	// std::array<int, 2> d =  get_data(1, 2.3);
	// error: cannot initialize an array element of type 'int' with an lvalue of type 'const char *'
	// auto d2 = get_data(1, "hi");
}
 
