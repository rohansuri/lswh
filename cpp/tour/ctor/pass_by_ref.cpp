// Does pass by ref invoke copy ctor?
// NO
#include<iostream>
class Foo {
public:
	Foo() {}
	Foo(const Foo& f) {
		std::cout << "copy ctor called " << std::endl;
	}

};


void foo(Foo& f) {
}

int main() {
	Foo f1;
	foo(f1);
}
