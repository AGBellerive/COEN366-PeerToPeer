import socket
import time

# Create a TCP socket
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

# Server address and port
server_address = ('localhost', 12345)

try:
    # Connect to the server
    client_socket.connect(server_address)
    print("Connected to server.")

    # Send ping messages for 5 minutes
    start_time = time.time()
    while time.time() - start_time < 300:  # 5 minutes in seconds
        try:
            # Send a ping message to the server
            client_socket.sendall(b'ping')

            # Receive the pong message from the server
            data = client_socket.recv(1024)
            if data:
                print(f"Received '{data.decode()}' from server")
            else:
                print('No data received from server')

            # Wait for 1 second before sending the next ping
            time.sleep(1)

        except socket.error as e:
            print(f"Socket error: {e}")
            break

except socket.error as e:
    print(f"Connection error: {e}")

finally:
    # Close the connection
    client_socket.close()
