// Good: We've wrapped our naked pointer inside a resource handle and tied its lifetime
// to the resource handle. Once the resource handle goes out of scope the pointer allocated
// is also released. Basic RAII.

// Bad: Since we're having a naked pointer inside the class, the default copy ctor will shallow copy
// the object whenever we're passing it around. This means multiple resource handles will be
// handling the same underlying piece of memory.
// This leads to double delete scenario. 
// 
//
/*

Elements:
1
2
3
Sum = 6
Double_Data dtor called
Double_Data dtor finished
Program done
Double_Data dtor called
a.out(20607,0x1167b35c0) malloc: *** error for object 0x7fe724c02aa0: pointer being freed was not allocated
a.out(20607,0x1167b35c0) malloc: *** set a breakpoint in malloc_error_break to debug
/bin/bash: line 1: 20607 Abort trap: 6           ./a.out

*/ 
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
	
        return d;
}

double sum(Double_Data d) {
        return d[0]+d[1]+d[2];
}

int main() {
	auto d = get_data();	
	d.print();
	std::cout << "Sum = " << sum(d) << std::endl;
	std::cout << "Program done" << std::endl;
}
