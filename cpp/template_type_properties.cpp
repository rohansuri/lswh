#include <iostream>
#include <string>
#include <vector>
using namespace std;
template <typename Map>
// Trying Jason Turner's example.
// So here Map is a template type and we're "expecting" its value_type to have
// first and second as attributes? How is value_type defined on the Map?
// Seems there are a lot of implicit properties?
// value_type is usually a member defined on container types that holds
// the type.
// Interesting how we're implicitly expecting certain properties to be defined
// on the type!
void print_map(const Map &map, const std::string key_desc = "key",
               const std::string value_desc = "value")
{
  for_each(begin(map), end(map),
           [&](const typename Map::value_type &data) { /// Here
             std::cout << key_desc << ": '" << data.first << "' "
                       << value_desc << ": '" << data.second << "'\n";
           });
  cout << endl;
}

class Two
{
public:
  Two(int a, int b) : first(a), second(b) {}
  int first, second;
};

int main()
{
  Two t(1, 2);
  vector<Two> v;
  v.push_back(t);
  print_map(v);
  std::cout << "Hello";
}
