package HostPackage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by anton on 2018-02-06.
 */
public class Host {

    public Host(int port) {
        ArrayList<User> users = new ArrayList<>();
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.printf("Server running on port %d%n", port);
            Socket clientSocket;
            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            Date date;
            while(true) {
                clientSocket = serverSocket.accept();
                date = new Date();
                users.add(new User(clientSocket));
                System.out.printf("[%s] User with address %s:%s connected%n", dateFormat.format(date), clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());
            }
        } catch(IOException ioe) {
            System.out.println("Error creating ServerSocket.");
        }
    }
}
