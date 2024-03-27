import socket

# Create a UDP socket
client_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

# Server address and port
server_address = ('localhost', 12345)

# Set timeout for the client socket (in seconds)
client_socket.settimeout(5)  # 5 seconds timeout

while True:
    try:
        # Read input from user
        message = input("Enter message to send to server: ")

        # Send data to server
        client_socket.sendto(message.encode(), server_address)

        # Receive response from server
        data, server = client_socket.recvfrom(1024)
        print(f"Received response from {server}: {data.decode()}")

    except socket.timeout:
        print("Server not responding. Timeout occurred.")
        break

# Close the socket
client_socket.close()
