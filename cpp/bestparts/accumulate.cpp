#include<numeric>
#include<iostream>
#include<vector>


template<typename Value_Type>
std::vector<Value_Type> get_data(Value_Type v1, Value_Type v2, Value_Type v3) {
	std::vector<Value_Type> v;
	v.push_back(v1);
	v.push_back(v2);
	v.push_back(v3);
	return v;
}	

// So how is std accumulate working?
// It must've defined the default accumulate functions for all std types?
// Or maybe expect that our Value_Type has operator+ defined on it?
// How can we enforce that the operator+ is defined during compile time?
// For all types that call into sum?
template<typename Value_Type>
Value_Type sum(const std::vector<Value_Type>& v) {
	return std::accumulate(v.begin(), v.end(), Value_Type());
}

int main() {
	auto d = get_data<int>(1, 2, 3);
	auto d2 = get_data<std::string>("hi", "bye", "yo");
	
	std::cout << sum(d) << std::endl;	
	std::cout << sum(d2) << std::endl;	
}
