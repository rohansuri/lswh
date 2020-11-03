#include<iostream>
class Foo {

public:

	Foo(Foo f) {
		std::cout << "ctor called" << std::endl;
	} // not a copy ctor since doesn't take reference? infact would invoke default copy ctor?

};

int main() {
	Foo f;
	Foo f2 = f;
}
