class A {

};

int main() {
	A a1, a2;
	
	A* const p1 = &a1;
	p1 = &a2; // can't work, since p1 is declared as a constant pointer.	

	/*
	const.cpp:9:5: error: cannot assign to variable 'p1' with const-qualified type 'A *const'
        p1 = &a2; // can't work, since p1 is declared as a constant pointer.    
        ~~ ^
	*/
		
}
