// Pointer to stack allocated array, undefined behaviour?
#include<iostream>
const int SIZE = 5;

int* foo() {
	int a[SIZE]; // stack allocated;
	for(int i = 0; i < SIZE; i++) {
		std::cout << a[i] << ",";
	}
	std::cout << std::endl;
	int* b = a;
	return b;
} // stack allocated array would be released, accessing b is undefined?

int main() {

	int* arr = foo();
	for(int i = 0; i < SIZE; i++) {
		std::cout << arr[i] << ",";
	}
	std::cout << std::endl;
	// program does print some garbage
	// but doesn't crash on accessing arr.
	// let's try writing?

	arr[0] = 1;
	// writing works too.
}

