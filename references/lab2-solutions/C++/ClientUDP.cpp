#include <iostream>
#include <cstring>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <chrono>
#include <ctime>

#define SERVER_IP "127.0.0.1"
#define SERVER_PORT 12345
#define TIMEOUT_SECONDS 5

int main() {
    int clientSocket;
    struct sockaddr_in serverAddr;
    socklen_t addrLen = sizeof(serverAddr);

    // Create UDP socket
    clientSocket = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);
    if (clientSocket == -1) {
        std::cerr << "Error creating socket\n";
        return 1;
    }

    // Set timeout on the socket
    struct timeval timeout;
    timeout.tv_sec = TIMEOUT_SECONDS;
    timeout.tv_usec = 0;
    if (setsockopt(clientSocket, SOL_SOCKET, SO_RCVTIMEO, (char *)&timeout, sizeof(timeout)) < 0) {
        std::cerr << "Error setting socket timeout\n";
        return 1;
    }

    // Initialize server address structure
    memset((char *)&serverAddr, 0, sizeof(serverAddr));
    serverAddr.sin_family = AF_INET;
    serverAddr.sin_port = htons(SERVER_PORT);
    serverAddr.sin_addr.s_addr = inet_addr(SERVER_IP);

    // Get user input
    std::cout << "Enter message: ";
    std::string message;
    std::getline(std::cin, message);

    // Send message to server
    if (sendto(clientSocket, message.c_str(), message.length(), 0, (struct sockaddr *)&serverAddr, addrLen) == -1) {
        std::cerr << "Error sending data to server\n";
        close(clientSocket);
        return 1;
    }

    // Receive response from server
    char buffer[1024];
    int bytesReceived = recvfrom(clientSocket, buffer, sizeof(buffer), 0, (struct sockaddr *)&serverAddr, &addrLen);
    if (bytesReceived == -1) {
        std::cerr << "Timeout: Server did not respond within " << TIMEOUT_SECONDS << " seconds\n";
    } else {
        buffer[bytesReceived] = '\0';
        std::cout << "Server response: " << buffer << std::endl;
    }

    // Close socket
    close(clientSocket);

    return 0;
}
