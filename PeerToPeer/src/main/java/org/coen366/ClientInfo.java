package org.coen366;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class ClientInfo implements Serializable {
    private String name;
    private InetAddress  ipAddress;
    private List<String> files;
    private int rqNum;


    public ClientInfo(String name, InetAddress ipAddress) {
        this.name = name;
        this.ipAddress = ipAddress;
        this.files = new ArrayList<>();
        this.rqNum = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(InetAddress ipAddress) {
        this.ipAddress = ipAddress;
    }

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }

    public int getRqNum() {
        return rqNum;
    }

    public void setRqNum(int rqNum) {
        this.rqNum = rqNum;
    }

    public void addToFiles(String file){
        this.files.add(file);
    }

    @Override
    public String toString() {
        return "ClientInfo{" +
                "name='" + name + '\'' +
                ", ipAddress=" + ipAddress +
                ", files=" + files +
                ", rqNum=" + rqNum +
                '}';
    }
}
