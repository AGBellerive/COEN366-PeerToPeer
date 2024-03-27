import socket
import time

def main():
    server_ip = "localhost"  # Replace with actual server IP
    server_port = 12345  # Replace with actual server port

    try:
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as client_socket:
            client_socket.connect((server_ip, server_port))

            # Register with the server
            client_socket.sendall(b"hello")

            # Wait for acknowledgment from the server
            response = client_socket.recv(1024).decode()
            if response == "registered":
                # Send client information (client ID and description)
                client_id = f"Client-id = {int(time.time())}"  # Replace with actual client ID
                description = "Sample client description"  # Replace with actual description
                client_socket.sendall(f"{client_id},{description}".encode())

            # Close the connection
            client_socket.close()
    except ConnectionRefusedError:
        print("Error: Server is not running or incorrect server IP/port.")

if __name__ == "__main__":
    main()
