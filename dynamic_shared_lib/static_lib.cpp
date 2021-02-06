#include <iostream>
#include "static_lib.h"

class A {
	public:
		A(){
			std::cout << "A ctor " << std::endl;
		}
		~A(){
			std::cout << "A dtor " << std::endl;
		}
};

void static_lib(){
	static A a;
}
