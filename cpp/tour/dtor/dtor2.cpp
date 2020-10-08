#include <iostream>
class Foo {
public:
	~Foo() {
		std::cout << "Foo dtor" << std::endl;
	}
private:
	int* a;
};

int main() {
	Foo f;
}
