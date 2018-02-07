package HostPackage;

import java.net.Socket;

/**
 * Created by anton on 2018-02-07.
 */
public class User {

    private Socket clientSocket;

    public User(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }
}
