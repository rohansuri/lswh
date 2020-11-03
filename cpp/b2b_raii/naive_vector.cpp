#include<iostream>
#include<algorithm>
#include<stdexcept>

class Naive_Vector {

public:
	Naive_Vector(int capacity):capacity_(capacity), size_(0){
		if(capacity < 0) {
			throw std::invalid_argument("capacity must be > 0");
		}
		m = new int[capacity_];
	}

	// it is interesting to think about why the signature here needs a reference.
	// otherwise we'd be needing a copy ctor again?
	// infact compiler gives an error:
	// error: copy constructor must pass its first argument by reference
	Naive_Vector(const Naive_Vector& v) {
		std::cout << "copy ctor invoked " << std::endl;
		size_ = v.size_;
		capacity_ = v.capacity_;
		m = new int[v.capacity_];
		std::copy(v.m, v.m+v.capacity_, m);
	}

	Naive_Vector(Naive_Vector&& v){
		std::cout << "move ctor invoked " << std::endl;
		swap(*this, v);
	}

	// Note how this handles both copy assignment as well as move assignment.
	// We leave it to the caller to decide how will they construct v.
	// They can either copy construct or move construct it.
	// For example a = b will copy construct.
	// a = b + c will move construct.
	// https://stackoverflow.com/questions/3106110/what-is-move-semantics
	Naive_Vector& operator=(Naive_Vector v){ 
		std::cout << "assignment invoked" << std::endl;
		swap(*this, v);
		return *this;
	} // dtor gets called on v2. releasing the memory held by old *this.


	// Friend to enable ADL.
	friend void swap(Naive_Vector& v1, Naive_Vector& v2) {
		// Member-wise swap.
		std::swap(v1.m, v2.m);
		std::swap(v1.capacity_, v2.capacity_);
		std::swap(v1.size_, v2.size_);
	}


	~Naive_Vector(){
		std::cout << "dtor called" << std::endl;
		delete [] m;
	}

	void push_back(int element){
		if(size_ == capacity_) {
			int* n = new int[2 * capacity_];
			std::copy(m, m+capacity_, n);
			std::swap(m, n);
			capacity_ = 2 * (capacity_ ? capacity_ : 1);
			delete [] n;
		}
		m[size_] = element;
		size_++;
	}

	int& operator[](int index) const {
		if(index >= capacity_) {
			throw std::invalid_argument("index "  + std::to_string(index) +  " greater than capacity " + std::to_string(capacity_));
		}
		return m[index];
	}

	int capacity() const {
		return capacity_;
	}

	int size()  const {
		return size_;
	}

private:
	int size_;
	int capacity_;
	int* m;	

};


void copy(Naive_Vector v) {
	// Passed in arg would be copied into v.
	// And this v would be released after function returns.
}

void fill(Naive_Vector& v) {

	for(int i = 0; i < 100; i++) {
		v.push_back(i);
	}
	std::cout << "size=" << v.size() << std::endl;
	std::cout << "capacity=" << v.capacity() << std::endl;
}

void check(const Naive_Vector& v) {
	
	for(int i = 0; i < 100; i++) {
		assert(v[i] == i);
	}
	std::cout << "Test passes " << std::endl;
}

Naive_Vector fill() {
	Naive_Vector v(0);
	fill(v);
	std::cout << "rvo move ctor should be called" << std::endl;
	return v;
}

int main() {

	Naive_Vector v(0);

	// Naive vector has double free problem, since default copy ctor will copy pointer directly.
	copy(v);
	fill(v);
	check(v);	
	
	std::cout << "invoking copy ctor " << std::endl;
	Naive_Vector v2 = v; // copy ctor
	std::cout << "invoking copy assignment" << std::endl;
	v = v2; // copy assignment
	check(v);
	
	v = fill(); // returned object moved into v. v's dtor should be called.
}
