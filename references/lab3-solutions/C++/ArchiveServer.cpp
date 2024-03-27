#include <iostream>
#include <string>
#include <fstream>
#include <cstring>
#include <netinet/in.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <sys/types.h>

#define PORT 54321

void handleInventoryData(int clientSocket) {
    char buffer[1024];
    memset(buffer, 0, sizeof(buffer));
    int bytesReceived = recv(clientSocket, buffer, sizeof(buffer), 0);
    if (bytesReceived <= 0) {
        std::cerr << "Error in receiving inventory data from client" << std::endl;
        close(clientSocket);
        return;
    }

    std::string inventoryData(buffer);
    std::cout << "Received inventory data: " << inventoryData << std::endl;

    // Store inventory data (e.g., write to a file or database)
    std::ofstream outFile("inventory_data.txt", std::ios::app);
    if (!outFile.is_open()) {
        std::cerr << "Error opening file for writing inventory data" << std::endl;
    }
    else {
        outFile << inventoryData << std::endl;
        outFile.close();
    }

    close(clientSocket);
}

int main() {
    int serverSocket = socket(AF_INET, SOCK_STREAM, 0);
    if (serverSocket < 0) {
        std::cerr << "Error creating socket" << std::endl;
        return -1;
    }

    struct sockaddr_in serverAddr;
    memset(&serverAddr, 0, sizeof(serverAddr));
    serverAddr.sin_family = AF_INET;
    serverAddr.sin_port = htons(PORT);
    serverAddr.sin_addr.s_addr = INADDR_ANY;

    if (bind(serverSocket, (struct sockaddr*)&serverAddr, sizeof(serverAddr)) < 0) {
        std::cerr << "Error binding socket" << std::endl;
        close(serverSocket);
        return -1;
    }

    if (listen(serverSocket, 5) < 0) {
        std::cerr << "Error listening on socket" << std::endl;
        close(serverSocket);
        return -1;
    }

    std::cout << "ArchiveServer listening on port " << PORT << std::endl;

    while (true) {
        struct sockaddr_in clientAddr;
        socklen_t clientAddrSize = sizeof(clientAddr);
        int clientSocket = accept(serverSocket, (struct sockaddr*)&clientAddr, &clientAddrSize);
        if (clientSocket < 0) {
            std::cerr << "Error accepting client connection" << std::endl;
            continue;
        }

        handleInventoryData(clientSocket);
    }

    close(serverSocket);
    return 0;
}
