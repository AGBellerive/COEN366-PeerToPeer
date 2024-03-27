import socket

# Create a TCP socket
server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

# Bind the socket to the server address and port
server_address = ('localhost', 12345)
server_socket.bind(server_address)

# Listen for incoming connections
server_socket.listen(5)  # Allow up to 5 connections

print('Server is listening...')

while True:
    # Wait for a connection
    connection, client_address = server_socket.accept()
    
    try:
        print(f"Connection from {client_address}")

        while True:
            # Receive data from client
            data = connection.recv(1024)
            if data:
                print(f"Received '{data.decode()}' from client")

                # Send a pong message back to the client
                connection.sendall(b'pong')
            else:
                print('No data received from client')
                break  # Exit the loop if no data is received

    finally:
        # Close the connection
        connection.close()
