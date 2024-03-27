#include <iostream>
#include <cstring>
#include <sys/socket.h>
#include <netinet/in.h>
#include <unistd.h>

#define SERVER_PORT 12345

int main() {
    int serverSocket, clientSocket;
    struct sockaddr_in serverAddr, clientAddr;
    socklen_t addrLen = sizeof(clientAddr);

    // Create TCP socket
    serverSocket = socket(AF_INET, SOCK_STREAM, 0);
    if (serverSocket == -1) {
        std::cerr << "Error creating socket\n";
        return 1;
    }

    // Initialize server address structure
    memset((char *)&serverAddr, 0, sizeof(serverAddr));
    serverAddr.sin_family = AF_INET;
    serverAddr.sin_port = htons(SERVER_PORT);
    serverAddr.sin_addr.s_addr = htonl(INADDR_ANY);

    // Bind socket to port
    if (bind(serverSocket, (struct sockaddr *)&serverAddr, sizeof(serverAddr)) == -1) {
        std::cerr << "Error binding socket\n";
        close(serverSocket);
        return 1;
    }

    // Listen for incoming connections
    if (listen(serverSocket, 5) == -1) {
        std::cerr << "Error listening for connections\n";
        close(serverSocket);
        return 1;
    }

    // Accept incoming connection
    clientSocket = accept(serverSocket, (struct sockaddr *)&clientAddr, &addrLen);
    if (clientSocket == -1) {
        std::cerr << "Error accepting connection\n";
        close(serverSocket);
        return 1;
    }

    // Ping-Pong exchange for 5 minutes
    while (true) {
        // Receive ping message
        char buffer[1024];
        int bytesReceived = recv(clientSocket, buffer, sizeof(buffer), 0);
        if (bytesReceived == -1) {
            std::cerr << "Error receiving ping message\n";
            close(clientSocket);
            close(serverSocket);
            return 1;
        }

        // Print ping message
        buffer[bytesReceived] = '\0';
        std::cout << "Received: " << buffer << std::endl;

        // Send pong message
        std::string pongMsg = "pong";
        if (send(clientSocket, pongMsg.c_str(), pongMsg.length(), 0) == -1) {
            std::cerr << "Error sending pong message\n";
            close(clientSocket);
            close(serverSocket);
            return 1;
        }
    }

    // Close connection
    close(clientSocket);
    close(serverSocket);

    return 0;
}
