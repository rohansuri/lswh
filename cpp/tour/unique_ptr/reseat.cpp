// Mentioned in Herb's talk.
// Reseat means changing what object unique_ptr points to.
#include<memory>

int main() {

	std::unique_ptr<int> x = std::make_unique<int>(1);
	int y = 2;
	// reseat x and make it point to y.
	x.reset(&y);
	
}
