#include<iostream>

// Auto can deduce return types as well.
constexpr auto calculate_pi() {
	return 22/7.0;
}

constexpr auto pi = calculate_pi();

int main() {
	const auto radius = 1.5;
	const auto area = pi * radius * radius;
	std::cout << area;
}
