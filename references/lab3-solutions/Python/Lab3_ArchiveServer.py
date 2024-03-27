import socket

def main():
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as server_socket:
        server_socket.bind(('localhost', 54321))  # Replace with actual port
        server_socket.listen()

        print("ArchiveServer listening on port 54321")

        while True:
            client_socket, _ = server_socket.accept()
            handle_inventory_data(client_socket)

def handle_inventory_data(client_socket):
    with client_socket:
        inventory_data = client_socket.recv(1024).decode()
        print("Received inventory data:", inventory_data)
        # Store inventory data (e.g., write to a file or database)

if __name__ == "__main__":
    main()
