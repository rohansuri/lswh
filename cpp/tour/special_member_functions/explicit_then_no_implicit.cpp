#include<utility>
// If we declare any of the special members explicility
// then we don't get any of the implicit ones?
// In that case, does the compiler enforce us to implement all?

class Foo {
public:
	Foo(){}
	Foo(const Foo& f) {}
};

int main() {
	Foo f1;
	Foo f2;
	f2 = std::move(f1);
}
