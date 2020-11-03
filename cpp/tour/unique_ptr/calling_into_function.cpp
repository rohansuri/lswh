#include<memory>
void foo(std::unique_ptr<int> u){}

int main() {

	// calling_into_function.cpp:7:6: error: call to implicitly-deleted copy constructor of 'std::unique_ptr<int>'
	auto x = std::make_unique<int>(1);
	foo(x);
}
