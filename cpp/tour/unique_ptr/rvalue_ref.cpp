#include<memory>


void foo(std::unique_ptr<int>&& x){}

int main() {
	foo(std::make_unique<int>(2));
}
