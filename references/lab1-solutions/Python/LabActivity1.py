import threading
import random
import time

# Global variable
number = 0
lock = threading.Lock()

# Function for generating random numbers
def generate_numbers():
    global number
    while True:
        with lock:
            number = random.randint(1, 20)
            print(f"Generated Number: {number}")
        time.sleep(1)

# Function for comparing numbers with 10
def compare_with_10():
    global number
    while True:
        time.sleep(1)
        with lock:
            if number > 10:
                print(f"{number} is greater than 10")
            else:
                print(f"{number} is not greater than 10")

# Create two threads
generate_thread = threading.Thread(target=generate_numbers)
compare_thread = threading.Thread(target=compare_with_10)

# Start the threads
generate_thread.start()
compare_thread.start()

# Wait for both threads to finish (this won't happen in this case, as they run indefinitely)
generate_thread.join()
compare_thread.join()
