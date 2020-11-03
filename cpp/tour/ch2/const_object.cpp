// Making an object const, doesn't allow modifications to any of its members.
// This isn't the case with Java's final.
// Where what you're making final is actually just the reference, not the underlying object.
#include <iostream>
class A {
public:
	int x;
};

int main() {
	A a;
	const A* p = &a;
	p->x = 1; // Fails.
	/*
		error: cannot assign to variable 'p' with const-qualified type 'const A *'
	
	*/
}
