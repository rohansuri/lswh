#include<iostream>
class Foo {};
int main() {
	Foo f;
	int* x = (int*)(&f); // C style cast doesn't raise an error neither at compile time nor runtime. 
	std::cout << *x << std::endl;


	x = static_cast<int*>(&f); // Raises error at compile time.
	return 0;
}
