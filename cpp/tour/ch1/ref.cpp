#include <iostream>
int main() {
	int x = 1, y = 2;
	int& p = x;
	// Assigning the reference doesn't assign to the reference but to the referenced object.
	// So here x's value comes 2.
	p = y;
	std::cout << x; // 2
}
