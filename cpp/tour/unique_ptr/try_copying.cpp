#include <memory>
class Foo {
};

int main() {
	std::unique_ptr<Foo> ptr = std::make_unique<Foo>();
	std::unique_ptr<Foo> ptr2 = ptr;
}
