import socket
from threading import Thread

PORT = 12345
registered_clients = 0
clients = []

def main():
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.bind(('localhost', PORT))
    server_socket.listen(4)  # Handle up to 4 clients
    print("Server listening on port", PORT)

    while True:
        client_socket, _ = server_socket.accept()
        thread = Thread(target=client_handler, args=(client_socket,))
        thread.start()

def client_handler(client_socket):
    global registered_clients, clients

    with client_socket:
        in_data = client_socket.recv(1024).decode()

        if in_data == "hello":
            # Register the client
            client_socket.sendall(b"registered")
            registered_clients += 1
            client = client_socket.recv(1024).decode()
            print(client)
            clients.append(client)
            if registered_clients == 4:
                send_to_archive_server(str(clients))

def send_to_archive_server(client_data):
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as archive_socket:
        archive_socket.connect(('localhost', 54321))  # Replace with actual ArchiveServer IP and port
        archive_socket.sendall(client_data.encode())

if __name__ == "__main__":
    main()
