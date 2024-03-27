#include <iostream>
#include <string>
#include <ctime>
#include <cstdlib>
#include <cstring>
#include <cstdio>
#include <unistd.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>

int main() {
    try {
        int clientSocket = socket(AF_INET, SOCK_STREAM, 0);
        if (clientSocket == -1) {
            perror("Error creating socket");
            return 1;
        }

        struct sockaddr_in serverAddress;
        serverAddress.sin_family = AF_INET;
        serverAddress.sin_port = htons(12345); // Replace with actual server port
        serverAddress.sin_addr.s_addr = inet_addr("127.0.0.1"); // Replace with actual server IP

        if (connect(clientSocket, (struct sockaddr*)&serverAddress, sizeof(serverAddress)) == -1) {
            perror("Error connecting to server");
            close(clientSocket);
            return 1;
        }

        // Register with the server
        std::string message = "hello";
        send(clientSocket, message.c_str(), message.size(), 0);

        // Wait for acknowledgment from the server
        char buffer[1024];
        memset(buffer, 0, sizeof(buffer));
        recv(clientSocket, buffer, sizeof(buffer), 0);
        std::string response(buffer);
        if (response == "registered") {
            // Send client information (client ID and description)
            std::time_t currentTime = std::time(nullptr);
            std::string clientId = "Client-id = " + std::to_string(currentTime % 60); // Replace with actual client ID
            std::string description = "Sample client description"; // Replace with actual description
            message = clientId + "," + description;
            send(clientSocket, message.c_str(), message.size(), 0);
        }

        // Close socket
        close(clientSocket);
    } catch (const std::exception& e) {
        std::cerr << "Exception: " << e.what() << std::endl;
    }

    return 0;
}
