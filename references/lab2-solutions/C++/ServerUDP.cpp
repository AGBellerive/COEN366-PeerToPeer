#include <iostream>
#include <cstring>
#include <sys/socket.h>
#include <netinet/in.h>
#include <unistd.h>

#define SERVER_PORT 12345
#define BUFFER_SIZE 1024

int main() {
    int serverSocket;
    struct sockaddr_in serverAddr, clientAddr;
    socklen_t addrLen = sizeof(clientAddr);

    // Create UDP socket
    serverSocket = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);
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

    // Receive message from client
    char buffer[BUFFER_SIZE];
    int bytesReceived = recvfrom(serverSocket, buffer, sizeof(buffer), 0, (struct sockaddr *)&clientAddr, &addrLen);
    if (bytesReceived == -1) {
        std::cerr << "Error receiving data from client\n";
        close(serverSocket);
        return 1;
    }

    // Print received message
    buffer[bytesReceived] = '\0';
    std::cout << "Received message from client: " << buffer << std::endl;

    // Send response to client
    std::string response = "Message received";
    if (sendto(serverSocket, response.c_str(), response.length(), 0, (struct sockaddr *)&clientAddr, addrLen) == -1) {
        std::cerr << "Error sending data to client\n";
        close(serverSocket);
        return 1;
    }

    // Close socket
    close(serverSocket);

    return 0;
}
