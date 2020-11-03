#include<iterator>
#include<iostream>
class Double_Data {
public:
	Double_Data(const std::size_t size):size_(size), d(new double[size]){}
	
	~Double_Data() {
		std::cout << "Double_Data dtor called" << std::endl;
		delete[] d;
		std::cout << "Double_Data dtor finished" << std::endl;
	}

	
	// TODO: write an iterator.

	void print() {
		std::cout << "Elements:" << std::endl;
		for(int i = 0; i < size_; i++) {
			std::cout << d[i] << std::endl;
		}
	}


	double& operator[](int index) {
		return d[index];
	}
private:
	double* d;
	size_t size_;
};

Double_Data get_data() {
        Double_Data d(3);
        d[0] = 1;
        d[1] = 2;
        d[2] = 3;
// NOTE: This also is doing a copy BUT it seems compiler is moving the object rather than copying
// hence we're not getting the double delete scenario.
// BUT I guess it'd be safer to explicitly move the object?
// Or does the standard guarantee that the object will always be moved?
// I guess that's what they name "RVO" ?
// Return Value Optimisation?
// Yup so from C++17 this is guaranteed.
        return d;
}

// This is the fix for this example.
// We take a reference to the object and hence no copying happens.
// Of course the better fix is to do a deep copy.
double sum(Double_Data& d) {
        return d[0]+d[1]+d[2];
}

int main() {
	auto d = get_data();	
	d.print();
	std::cout << "Sum = " << sum(d) << std::endl;
	std::cout << "Program done" << std::endl;
}
