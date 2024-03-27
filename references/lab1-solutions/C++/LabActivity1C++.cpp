#include <iostream>
#include <thread>
#include <mutex>
#include <condition_variable>
#include <random>
#include <chrono>

std::mutex mtx;
std::condition_variable cv;
int randomNumber;
bool generated = false;

class NumberGenerator {
public:
    void operator()() {
        std::random_device rd;
        std::mt19937 gen(rd());
        std::uniform_int_distribution<> dis(0, 19);

        while (true) {
            std::this_thread::sleep_for(std::chrono::seconds(1));
            {
                std::unique_lock<std::mutex> lck(mtx);
                randomNumber = dis(gen);
                std::cout << "Generated Num: " << randomNumber << std::endl;

                generated = true;
                cv.notify_one();
            }
        }
    }
};

class NumberComparator {
public:
    void operator()() {
        while (true) {
            {
                std::unique_lock<std::mutex> lck(mtx);
                cv.wait(lck, []{ return generated; });

                if (randomNumber > 10) {
                    std::cout << randomNumber << " is greater than 10" << std::endl;
                } else if (randomNumber < 10) {
                    std::cout << randomNumber << " is not greater than 10" << std::endl;
                } else {
                    std::cout << randomNumber << " is equal to 10" << std::endl;
                }
                generated = false;
            }
        }
    }
};

int main() {
    std::thread numberGeneratorThread((NumberGenerator()));
    std::thread numberComparatorThread((NumberComparator()));

    numberGeneratorThread.join();
    numberComparatorThread.join();

    return 0;
}