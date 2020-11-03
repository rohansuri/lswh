// Double_Data, Int_Data, Float_Data?
// Templates!
#include<cstddef>
#include<iostream>
template<typename Value_Type>
class Data {
public:
	Data(const std::size_t size): size_(size), d(new Value_Type[size]){}

	~Data() {
		delete[] d;
	}

	Value_Type& operator[](int i){
		return d[i];
	}

	void print() {
		for(int i = 0; i < size_; i++){
			std::cout << d[i] << std::endl;
		}
	}
private:
	Value_Type *d;
	std::size_t size_;
};


template<typename Value_Type>
Data<Value_Type> get_data(Value_Type v1, Value_Type v2, Value_Type v3) {
	Data<Value_Type> d(3);
	d[0] = v1;
	d[1] = v2;
	d[2] = v3;
	return d;
}

int main() {
	auto d = get_data<int>(1, 2, 3);
	d.print();
	auto d2 = get_data<float>(1.1, 2.2, 3.3);
	d2.print();	
	auto d3 = get_data<std::string>("hi", "bye", "yo");
	d3.print();	
}
