#include <iostream>
// This is an example of function overriding.
// B isn't inheriting A's foo here.
class A {
public:
	void foo() {
		std::cout << "A" << std::endl;
	}
	void bar() {
		std::cout << "A bar" << std::endl;
	}
};

class B: public A {
public:
	void foo() {
		std::cout << "B" << std::endl;
	}
};

int main(){
	B b;
	b.foo();
	b.bar();
	A a = b;
	a.foo();
}
