#include <iostream>
#include <mutex>
#include <vector>
#include <thread>
#include <atomic>

std::mutex m;
std::atomic<bool> initDone;
int x = 0;
constexpr auto EXPECTED_VALUE = 123;

void init(){
	std::lock_guard<std::mutex> lock(m);
	x = EXPECTED_VALUE;
	initDone.store(true, std::memory_order_relaxed);
}

int main(){
	auto read= [](){
		std::cout << "reader started " << std::endl;
		while(true){
			if(initDone.load(std::memory_order_relaxed)){
				if(x!=EXPECTED_VALUE){
					// Can this happen?
					std::cout << " x is =  " << x << std::endl;
				}
			}
		}
	};
	std::thread thread(read);
	std::this_thread::sleep_for(std::chrono::seconds(1));
	std::cout << "writer doing init" << std::endl;
	init();
	thread.join();
}
