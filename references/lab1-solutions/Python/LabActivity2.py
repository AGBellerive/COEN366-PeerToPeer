import threading
import random
import time

# Global variable to track whether a number is generated or not
number_generated = False
random_number = None

# Mutex for synchronizing access to the shared variable
mutex = threading.Lock()

def generate_number():
    global number_generated, random_number
    while True:
        with mutex:
            # Generate a random number every 1 second if no number is already generated
            if not number_generated:
                random_number = random.randint(1, 20)  # Generate a random number between 1 and 20
                print(f"Generated number: {random_number}")
                number_generated = True
        time.sleep(1)  # Sleep for 1 second

def compare_number():
    global number_generated, random_number
    while True:
        with mutex:
            # Compare the generated number if it exists
            if number_generated:
                if random_number > 10:
                    print("Comparison result:", random_number, "is greater than 10")
                elif random_number < 10:
                    print("Comparison result:", random_number, "is not greater than 10")
                else:
                    print("Comparison result:", random_number, "is equal to 10")
                number_generated = False
        time.sleep(1)  # Sleep for 1 second

# Create multiple threads for generation and comparison
generation_threads = [threading.Thread(target=generate_number) for _ in range(3)]  # Create 3 generation threads
comparison_threads = [threading.Thread(target=compare_number) for _ in range(2)]  # Create 2 comparison threads

# Start all the threads
for thread in generation_threads + comparison_threads:
    thread.start()

# Wait for the threads to finish (this will never happen in this example since the threads run indefinitely)
for thread in generation_threads + comparison_threads:
    thread.join()
