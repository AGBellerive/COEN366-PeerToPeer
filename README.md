# COEN366-Peer-to-Peer File System (P2FS)

## Overview
A multi-threaded Peer-to-Peer File System (P2FS) built in Java, enabling clients to register, share, and download text files using UDP and TCP protocols. The system includes features like real-time updates, client mobility, and robust error handling.

## Features
- **Multi-threaded server and client** communication using UDP and TCP.
- **File sharing**: Clients can register, publish, retrieve, and remove files.
- **Real-time updates**: Clients receive periodic and real-time updates about available files and registered peers.
- **Concurrency management**: Mutexes and semaphores used to avoid deadlocks.
- **Client mobility**: Clients can update their IP address or UDP socket.
- **Error handling**: Robust error messages for registration, file transfer, and client updates.

## Requirements
- Download and install [jdk17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) when setting up project

## Installation
1. Clone this repository:
    ```bash
    git clone https://github.com/your-username/peer-to-peer-file-system.git](https://github.com/AGBellerive/COEN366-PeerToPeer.git
    ```
2. Open the project in your chosen IDE
3. Run the server
4. Run the client

## Usage
1. **Registration**: Clients must register with the server before they can share files.
2. **File sharing**: Clients can publish and remove files via the server.
3. **File transfer**: Clients can request files from other peers using TCP for file transfer.
4. **Updates**: Clients will receive real-time updates about other registered clients and available files.

## Example
- A client sends a `REGISTER` message to the server with its name, IP address, and UDP socket.
- The client can then `PUBLISH` files, which will be added to the server's list which is then available for all other registered peers to retrieve.
