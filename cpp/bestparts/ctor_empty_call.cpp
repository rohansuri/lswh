#include<iostream>
// https://stackoverflow.com/questions/180172/default-constructor-with-empty-brackets
// most vexing parse
// i.e. anything that can be interpreted as a function will be interpreted as a function by C++.

class Foo {};
int main() {
	// warning: empty parentheses interpreted as a function declaration
	Foo f();
	
}
