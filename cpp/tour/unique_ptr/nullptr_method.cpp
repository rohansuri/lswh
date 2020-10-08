class Foo {

public:
	void call() {}
};

int main() {
	Foo *f;
	f->call();
	// Works. CPP allows method invocations on nullptr just like Golang and unlike Java.
	// This is to avoid null checks that will slow the program down.
	// https://stackoverflow.com/questions/38027402/unique-pointer-still-holds-the-object-after-moving
}
