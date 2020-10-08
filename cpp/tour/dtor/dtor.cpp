#include <iostream>
class Foo {
public:
	~Foo() {
		std::cout << "Foo dtor" << std::endl;
	}
};

class Bar {
private:
	Foo f; // When Bar is destructed, Foo's destructor is called.
};

int main() {
	Bar b;
}

