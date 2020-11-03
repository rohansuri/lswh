class Foo {
public:
	virtual void foo() = 0;
};

class Bar: public Foo {
};

class Baz: public Bar {
	void foo() {}
};

int main(){
	// Both are abstract class
	// Foo f; // error: variable type 'Foo' is an abstract class
	// Bar b; // error: variable type 'Bar' is an abstract class 

	// Baz is not abstract
	Baz bz;
}
