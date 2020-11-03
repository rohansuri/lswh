// Lifetime of class members
#include <iostream>
class A {
public:
	A() {
		std::cout << "A ctor" << std::endl;
	}

	~A() {
		std::cout << "A dctor" << std::endl;
	}
	
	int value;
};


class B {
public:
	B() {
		std::cout << "B ctor" << std::endl;
	}

	~B() {
		std::cout << "B dctor" << std::endl;
	}

	A a; // zero value of A.
};

int main() {

	B b;
	std::cout << "b.a.value = " << b.a.value << std::endl;	
}
