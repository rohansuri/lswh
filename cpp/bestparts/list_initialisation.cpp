#include<array>

template<typename T>
std::array<T, 1> get_data(T v1) {
	return {v1};
}

template<typename T>
std::array<T, 2> get_data(T v1, T v2) {
	return {v1, v2};
}

template<typename T>
std::array<T, 3> get_data(T v1, T v2, T v3) {
	return {v1, v2, v3};
}

int main() {
	auto d1 = get_data<int>(1);
	auto d2 = get_data<int>(1, 2);
	auto d3 = get_data<int>(1, 2, 3);
}
