#include <iostream>
#include "static_lib.h"
#include "dynamic_lib.h"
int main(){
	std::cout << "calling static_lib in main " << std::endl;
	static_lib();
	std::cout << "calling dynamic_lib in main " << std::endl;
	dynamic_lib();	
}
