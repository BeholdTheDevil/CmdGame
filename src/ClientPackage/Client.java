package ClientPackage;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by anton on 2018-02-07.
 */
public class Client {

    Socket server;

    private static ArrayList<String> connectedUsers = new ArrayList<>();
    private static HubGUI hubGUI;

    private static DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    public Client() {
        try {
            server = getHubConnection();

            DataInputStream is = new DataInputStream(server.getInputStream());
            PrintStream os = new PrintStream(server.getOutputStream());

            String username;
            do {
                username = JOptionPane.showInputDialog(null, "User creation", "Pick a username", JOptionPane.QUESTION_MESSAGE);
                if(username == null) {
                    server.close();
                    System.exit(0);
                }
            } while(username.equals(""));
            os.println("REG," + username);
            /*os.flush();
            os.close();*/

            hubGUI = new HubGUI(this, username);
            String serverResponse;
            String[] split;
            while((serverResponse = is.readLine()) != null) {
                if(serverResponse.equals("BYE")) {
                    System.out.println("Server shutdown, disconnecting");
                    System.exit(0);
                }

                if(serverResponse.contains("USR")) {
                    logg(serverResponse);
                    split = serverResponse.replace("USR,", "").split(",");
                    updateUserCards(split);
                } /*else if(serverResponse.contains("CHA")) {
                    //Challenged by:
                    challenged(serverResponse, server);
                }*/
            }
            //server.close();
        } catch(IOException ioe) {
            System.out.println("Unable to establish hub connection.");
        }
    }

    private void updateUserCards(String[] updateList) {
        for(int i = 0; i < updateList.length; i++) {
            logg(updateList[i]);
            if(!connectedUsers.contains(updateList[i])) {
                logg("Add to list");
                connectedUsers.add(updateList[i]);
                hubGUI.addUser(updateList[i]);
            }
        }
    }

    private static void logg(String s) {
        Date now = new Date();
        System.out.printf(" [%s] %s%n", dateFormat.format(now), s);
    }

    private static void logg(int d) {
        Date now = new Date();
        System.out.printf(" [%s] %d%n", dateFormat.format(now), d);
    }

    private void challenged(String serverResponse, Socket server) throws IOException {
        PrintStream os = new PrintStream(server.getOutputStream());
        //Create prompt for challenge
        int acc = JOptionPane.showConfirmDialog(null, "Challenged!", "You have been challenged by " +
                serverResponse.substring(serverResponse.lastIndexOf(","), serverResponse.length()), JOptionPane.YES_NO_OPTION);

        if(acc == JOptionPane.YES_OPTION) {
            os.println("ACC");
            connectPeerToPeer();
        } else if(acc == JOptionPane.NO_OPTION) {
            os.println("DEN");
        }
    }

    void challenger(String opponent) {
        System.out.println("Waiting for opponent to respond!");
        connectPeerToPeer();
    }

    void connectPeerToPeer() {
        try {
            String challengerIp;
            DataInputStream is = new DataInputStream(server.getInputStream());

            while((challengerIp = is.readLine()) != null) {
                if(challengerIp.equals("-1")) {
                    System.out.println("Challenge denied");
                    break;
                } else {
                    server.bind(new InetSocketAddress(challengerIp, 9999));
                }
            }
        } catch(IOException e) {

        }
    }

    /*
    * Connect to HUB
    * */
    private Socket getHubConnection() throws IOException {
        InetAddress address = null;
        int port = -1;
        String input;
        String[] result;
        do {
            input = (String)JOptionPane.showInputDialog(null, "ServerHub:", "Connect to hub", JOptionPane.QUESTION_MESSAGE, null, null, "127.0.0.1:11111");
            if(input != null) {
                result = input.split(":");
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
            } else {
                server.close();
                System.exit(0);
            }
        } while(address == null || port == -1);

        return new Socket(address, port);
    }
}
