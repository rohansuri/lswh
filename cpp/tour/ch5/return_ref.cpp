#include <iostream>
int& foo() {
	int x = 1;
	return x;
}

int main() {
	int x = foo();
	std::cout << x << std::endl;
}
