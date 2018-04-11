package HostPackage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by anton on 2018-02-07.
 */
public class User {

    private Socket clientSocket;
    private String uid;
    private int id;

    public User(Socket clientSocket, String uid, int id) {
        this.id = id;
        this.uid = uid;
        this.clientSocket = clientSocket;
    }

    public OutputStream getOutputStream() throws IOException {
        return clientSocket.getOutputStream();
    }

    public InputStream getInputStream() throws IOException {
        return clientSocket.getInputStream();
    }

    public String getSocketAddress() {
        return clientSocket.getInetAddress().toString().replace("/", "") + ":" + clientSocket.getPort();
    }

    public String getUid() {
        return uid;
    }

    public int getId() {
        return id;
    }

    public boolean socketIsClosed() {
        return clientSocket.isClosed();
    }

    public boolean isOutputShutdown() {
        return clientSocket.isOutputShutdown();
    }

    public void closeSocket() throws IOException {
        clientSocket.close();
    }
}
