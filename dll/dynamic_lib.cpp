#include <iostream>
#include "dynamic_lib.h"
#include "static_lib.h"
void dynamic_lib(){
	std::cout << "dynamic_lib calling static_lib "<< std::endl;
	static_lib();
}
