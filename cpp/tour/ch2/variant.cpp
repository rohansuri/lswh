#include <variant>
// Either an internal node or holds a value.
class Node {
public:
	Node(int i) {
		v = i;
	}

	Node(Node* n) {
		v = n;
	}
	bool internal() {
		return std::holds_alternative<Node*>(v);
	}

	int value() {
		return std::get<int>(v);
	}

private:	
	std::variant<Node*, int> v;
	
};

int main() {
	Node n(1);
		

}

