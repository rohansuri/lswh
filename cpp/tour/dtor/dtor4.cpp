#include <iostream>
class Bar {
public:
	~Bar() {
		std::cout << "Bar dtor" << std::endl;
	}
};

class Foo {
public:
	Foo():b(new Bar()){}
	~Foo() {
		std::cout << "Foo dtor" << std::endl;
	}
private:
	Bar *b;
};

int main() {
	Foo f;
	// Bar dtor not called.
}
