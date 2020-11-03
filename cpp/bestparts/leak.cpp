#include<iostream>
// Leak example.
// No clear ownership of this returned double defined.
// Who should release the double array?
//
// Never use naked return.
// Always wrap it in a resource handle aka RAII.
// So that the memory allocated can automatically be released as part of destructor.
// Or use smart pointers.
double* get_data() {
	double* d = new double[3];
	d[0] = 1;
	d[1] = 2;
	d[2] = 3;
	return d;
}

double sum(double* d) {
	return d[0]+d[1]+d[2];
}


int main() {
	std::cout<<sum(get_data()); // returned double array is leaking, no one releases memory for it.
}
