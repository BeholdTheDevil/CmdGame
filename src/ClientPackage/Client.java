package ClientPackage;

import javax.swing.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by anton on 2018-02-07.
 */
public class Client {

    public Client() {
        try {
            Socket server = getHubConnection();
            System.out.printf("Connected to server at %s%n", server.getInetAddress().getHostAddress());
            server.close();
        } catch(IOException ioe) {
            System.out.println("Unable to establish hub connection.");
        }
    }

    private Socket getHubConnection() throws IOException {
        InetAddress address = null;
        int port = -1;
        String[] result;
        do {
            result = JOptionPane.showInputDialog(null, "ServerHub:", "Connect to hub", JOptionPane.QUESTION_MESSAGE).split(":");
            if(result.length == 2) {
                try {
                    address = InetAddress.getByName(result[0]);
                    port = Integer.parseInt(result[1]);
                } catch(NumberFormatException nfe) {
                    port = -1;
                    address = null;
                } catch(UnknownHostException uho) {
                    port = -1;
                    address = null;
                }
            } else {
                System.out.println("Invalid address format (xxx.xxx.xxx.xxx:xxxxx)");
            }
        } while(address == null || port == -1);

        return new Socket(address, port);
    }
}
