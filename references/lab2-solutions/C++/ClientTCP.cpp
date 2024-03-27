#include <iostream>
#include <cstring>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <chrono>
#include <thread> // Include the <thread> header for std::this_thread
#include <ctime>

#define SERVER_IP "127.0.0.1"
#define SERVER_PORT 12345
#define TIMEOUT_SECONDS 300 // 5 minutes

int main() {
    int clientSocket;
    struct sockaddr_in serverAddr;

    // Create TCP socket
    clientSocket = socket(AF_INET, SOCK_STREAM, 0);
    if (clientSocket == -1) {
        std::cerr << "Error creating socket\n";
        return 1;
    }

    // Initialize server address structure
    memset((char *)&serverAddr, 0, sizeof(serverAddr));
    serverAddr.sin_family = AF_INET;
    serverAddr.sin_port = htons(SERVER_PORT);
    serverAddr.sin_addr.s_addr = inet_addr(SERVER_IP);

    // Connect to server
    if (connect(clientSocket, (struct sockaddr *)&serverAddr, sizeof(serverAddr)) == -1) {
        std::cerr << "Error connecting to server\n";
        close(clientSocket);
        return 1;
    }

    // Ping-Pong exchange for 5 minutes
    auto start = std::chrono::steady_clock::now();
    while (true) {
        // Send ping message
        std::string pingMsg = "ping";
        if (send(clientSocket, pingMsg.c_str(), pingMsg.length(), 0) == -1) {
            std::cerr << "Error sending ping message\n";
            close(clientSocket);
            return 1;
        }

        // Receive pong message
        char buffer[1024];
        int bytesReceived = recv(clientSocket, buffer, sizeof(buffer), 0);
        if (bytesReceived == -1) {
            std::cerr << "Error receiving pong message\n";
            close(clientSocket);
            return 1;
        }

        // Print pong message
        buffer[bytesReceived] = '\0';
        std::cout << "Received: " << buffer << std::endl;

        // Check if 5 minutes have passed
        auto end = std::chrono::steady_clock::now();
        auto elapsedSeconds = std::chrono::duration_cast<std::chrono::seconds>(end - start).count();
        if (elapsedSeconds >= TIMEOUT_SECONDS) {
            break;
        }

        // Sleep for 1 second before next iteration
        std::this_thread::sleep_for(std::chrono::seconds(1));
    }

    // Close connection
    close(clientSocket);

    return 0;
}
