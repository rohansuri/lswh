#include<iostream>
int& foo() {
	int x = 1;
	return x;
}

int main() {
	std::cout << foo() << std::endl;
}
