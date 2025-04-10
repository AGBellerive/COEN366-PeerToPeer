package org.coen366;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.List;

/**
 * This class will be the core of the communication
 * between the server and the client. This will primarily be used
 * for Registration tasks(2.1), Update (2.5),
 */
/*
To see how to de/serialize an  object, view lab3
Server:
 ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
 ...
 ClientInfo incomingClient = (ClientInfo) in.readObject();

 Client:
this.socket = new Socket("localhost",PORT);
this.out = new ObjectOutputStream(socket.getOutputStream());
...
this.out.writeObject(client);
 */
public class Message implements Serializable {
    private Status action;
    private int rqNumber;
    //Should the request number be associated to the client or the message they are sending
    //private Client clientInfo;
    private String reason;

    private ClientInfo clientInfo;

    private List<ClientInfo> listOfClientsInfosForUpdate;

    private String file;

    private int chunkNum;
    private String text;
    private InetAddress newIPAddress;
    private int newClientPort;

    private int tcpSocketNum;

    private InetAddress clientIPAddress;



    /**
     * For a new user to register, they must send a message
     * through udp with the fields
     * REGISTER RQ# Name IP Address UDP socket#
     * @param action will be the enum signifying the action that will be taken
     * @param rqNumber is the message id
    //     * @param client is the users data like name, socket, ipaddress
     */
    public Message(Status action, int rqNumber,ClientInfo clientInfo) {
        this.action = action;
        this.rqNumber = rqNumber;
        this.clientInfo = clientInfo;
    }

    public Message(Status action,int rqNumber, String reason){
        this.action = action;
        this.rqNumber = rqNumber;
        this.reason = reason;
    }

    public Message(Status action,int rqNumber){
        this.action = action;
        this.rqNumber = rqNumber;
    }

    public Message(Status action,int rqNumber, String file, ClientInfo clientInfo){
        this.action = action;
        this.rqNumber = rqNumber;
        this.file = file;
        this.clientInfo=clientInfo;
    }




    public Message(Status status, int rqNum, ClientInfo storedClient, String currentFilePathToDelete) {
    }

    public Message(Status action, int rqNumber, int tcpSocketNum, InetAddress clientIPAddress) {
        this.action=action;
        this.rqNumber=rqNumber;
        this.tcpSocketNum=tcpSocketNum;
        this.clientIPAddress=clientIPAddress;
    }


    public Status getAction() {
        return action;
    }

    public void setAction(Status action) {
        this.action = action;
    }

    public int getRqNumber() {
        return rqNumber;
    }

    public void setRqNumber(int rqNumber) {
        this.rqNumber = rqNumber;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public ClientInfo getClientInfo() {
        return clientInfo;
    }

    public List<ClientInfo> getListOfClientsInfosForUpdate() {
        return listOfClientsInfosForUpdate;
    }

    public void setListOfClientsInfosForUpdate(List<ClientInfo> listOfClientsInfosForUpdate) {
        this.listOfClientsInfosForUpdate = listOfClientsInfosForUpdate;
    }

    public String getFile() {
        return file;
    }

    public InetAddress getNewIPAddress() {
        return newIPAddress;
    }

    public void setNewIPAddress(InetAddress newIPAddress) {
        this.newIPAddress = newIPAddress;
    }

    public int getNewClientPort() {
        return newClientPort;
    }

    public void setNewClientPort(int newClientPort) {
        this.newClientPort = newClientPort;
    }

    @Override
    public String toString() {
        return "Message{" +
                "action=" + action +
                ", rqNumber=" + rqNumber +
                ", clientInfo=" + clientInfo +
                ", reason='" + reason + '\'' +
                ", file='"+file+'\''+
                ", newIPAddress='" + newIPAddress +'\''+
                ", newClientPort='" + newClientPort +
                '}';
    }

    public Message(Status action, int rqNumber,String fileName, int chunkNumber, String text){
        this.action = action;
        this.rqNumber = rqNumber;
        this.file= fileName;
        this.chunkNum = chunkNumber;
        this.text = text;

    }


    public int getChunkNum() {
        return chunkNum;
    }

    public void setChunkNum(int chunkNum) {
        this.chunkNum = chunkNum;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getTcpSocketNum() {
        return tcpSocketNum;
    }

    public void setTcpSocketNum(int tcpSocketNum) {
        this.tcpSocketNum = tcpSocketNum;
    }

    public InetAddress getClientIPAddress() {
        return clientIPAddress;
    }

    public void setClientIPAddress(InetAddress clientIPAddress) {
        this.clientIPAddress = clientIPAddress;
    }
}