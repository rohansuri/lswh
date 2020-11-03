int main() {
	int x, y;
	
	const int* p1 = &x;
	
	*p1 = y; // cannot change the underlying object.
	
	/*
		ptr_to_const_object.cpp:6:6: error: read-only variable is not assignable
        	*p1 = y; // cannot change the underlying object.
        	~~~ ^
		1 error generated.

	*/	
}
