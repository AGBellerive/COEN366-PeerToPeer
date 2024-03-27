import socket

# Create a UDP socket
server_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

# Bind the socket to the server address and port
server_address = ('localhost', 12345)
server_socket.bind(server_address)

print('Server is listening...')

while True:
    # Receive message from client and client address
    data, client_address = server_socket.recvfrom(1024)
    
    # Print received message and client address
    print(f"Received message from {client_address}: {data.decode()}")

    # Sending a response back to the client
    response = "Message received!"
    server_socket.sendto(response.encode(), client_address)
