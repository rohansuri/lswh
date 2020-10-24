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

	Naive_Vector& operator=(const Naive_Vector& v){
		// why can't we just call the copy ctor here?
		// we can, that's the copy-and-swap idiom.
		std::cout << "copy assignment invoked" << std::endl;
		Naive_Vector v2(v); // copy ctor.
		this->swap(v2); // memberwise swap, not std::swap, since that itself would invoke copy assignment.
		return *this;
	} // dtor gets called on v2. releasing the memory held by old *this.


	void swap(Naive_Vector& v) {
		// Member-wise swap.
		std::swap(m, v.m);
		std::swap(capacity_, v.capacity_);
		std::swap(size_, v.size_);
	}


	~Naive_Vector(){
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
}
