// Use constexpr to make a compile time constant.
// const is for constants at runtime.
#include<iostream>

constexpr double calculate_pi() {
	return 22/7.0;
}

constexpr double pi = calculate_pi();

int main() {
	const double radius = 1.5;
	const double area = pi * radius * radius;
	std::cout << area;
}
