#include <iostream>
#include <string>
#include <vector>
#include <thread>
#include <mutex>
#include <condition_variable>
#include <chrono>
#include <atomic>
#include <cstring>
#include <netinet/in.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <sys/types.h>

#define PORT 12345
#define MAX_CLIENTS 4

std::mutex mtx;
std::condition_variable cv;
std::vector<std::string> clients;
std::atomic<int> registeredClients(0);

void handleClient(int clientSocket) {
    char buffer[1024];
    memset(buffer, 0, sizeof(buffer));
    int bytesReceived = recv(clientSocket, buffer, sizeof(buffer), 0);
    if (bytesReceived <= 0) {
        std::cerr << "Error in receiving data from client" << std::endl;
        close(clientSocket);
        return;
    }

    std::string message(buffer);

    if (message == "hello") {
        std::unique_lock<std::mutex> lock(mtx);
        int clientNumber = registeredClients++;
        lock.unlock();

        send(clientSocket, "registered\n", strlen("registered\n"), 0);

        bytesReceived = recv(clientSocket, buffer, sizeof(buffer), 0);
        if (bytesReceived <= 0) {
            std::cerr << "Error in receiving data from client" << std::endl;
            close(clientSocket);
            return;
        }
        std::string client(buffer);
        clients.push_back(client);

        if (clientNumber == MAX_CLIENTS - 1) {
            std::string clientsData;
            for (const auto& c : clients) {
                clientsData += c + " ";
            }
            sendToArchiveServer(clientsData);
        }
    }

    close(clientSocket);
}

void sendToArchiveServer(const std::string& clientData) {
    int archiveSocket = socket(AF_INET, SOCK_STREAM, 0);
    if (archiveSocket < 0) {
        std::cerr << "Error creating socket for archive server" << std::endl;
        return;
    }

    struct sockaddr_in archiveAddr;
    memset(&archiveAddr, 0, sizeof(archiveAddr));
    archiveAddr.sin_family = AF_INET;
    archiveAddr.sin_port = htons(54321);
    inet_pton(AF_INET, "127.0.0.1", &archiveAddr.sin_addr);

    if (connect(archiveSocket, (struct sockaddr*)&archiveAddr, sizeof(archiveAddr)) < 0) {
        std::cerr << "Error connecting to archive server" << std::endl;
        close(archiveSocket);
        return;
    }

    send(archiveSocket, clientData.c_str(), clientData.size(), 0);

    close(archiveSocket);
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

    std::cout << "Server listening on port " << PORT << std::endl;

    while (true) {
        struct sockaddr_in clientAddr;
        socklen_t clientAddrSize = sizeof(clientAddr);
        int clientSocket = accept(serverSocket, (struct sockaddr*)&clientAddr, &clientAddrSize);
        if (clientSocket < 0) {
            std::cerr << "Error accepting client connection" << std::endl;
            continue;
        }

        std::thread(handleClient, clientSocket).detach();
    }

    close(serverSocket);
    return 0;
}
