#include <memory>
#include <iostream>
class Foo {
public:
	void call() {
		std::cout << "inside function Foo" << std::endl;
	}
};

int main() {
	std::unique_ptr<Foo> ptr = std::make_unique<Foo>();
	std::unique_ptr<Foo> ptr2 = std::move(ptr);
	ptr->call(); // ptr can still be used after being moved? Then what's the point of unique_ptr?
	// https://stackoverflow.com/questions/38027402/unique-pointer-still-holds-the-object-after-moving
	// ptr is reset and is set to nullptr. It works because runtime doesn't check for calling methods on nullptrs.
	// Try another example that accesses state of the object and it should fail.
	ptr2->call();
}
