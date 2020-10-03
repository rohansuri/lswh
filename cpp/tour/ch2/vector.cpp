#include <iostream>
class Vector {
public:
	Vector(int s) :elem{new double[s]}, sz{s} {}
	int size() {
		return sz;
	}	
	double& operator[](int i) {
		return elem[i];
	}	
	void print() {
		for(int i = 0; i < size(); i++) {
			std::cout << *(elem+i) << " ";
		}
		std::cout << std::endl;
	}


private:
	int sz;
	double *elem;	
};

int main() {
	Vector v(2);
	std::cout << "vector size = " << v.size() << std::endl;
	
	v.print();
	v[0] = 1;
	v[1] = 2;
	v.print();
}
