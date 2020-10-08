#include <memory>
#include <iostream>
class Foo {
public:
	void call() {
		std::cout << "inside function Foo" << std::endl;
		std::cout << "x=" << x << std::endl;
	}
private:
	int x = 5;
};

int main() {
	std::unique_ptr<Foo> ptr = std::make_unique<Foo>();
	std::unique_ptr<Foo> ptr2 = std::move(ptr);
	ptr->call(); 
	// /bin/bash: line 1: 19281 Segmentation fault: 11  ./a.out
}
