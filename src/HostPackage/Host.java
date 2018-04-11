package HostPackage;

import com.sun.org.apache.bcel.internal.generic.INSTANCEOF;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by anton on 2018-02-06.
 */
public class Host {

    private static ArrayList<User> users = new ArrayList<>();
    private static int port = 11111;

    private static int newUserId = 1;

    private static DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    private static final int THREAD_POOL_SIZE = 5;
    private static final int QUEUE_CAPACITY = 10;
    private static ArrayBlockingQueue<Socket> connectionQueue;
    private static ConnectionHandler connectionHandlers[];

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            logg("Server running on port " + port);

            connectionQueue = new ArrayBlockingQueue<Socket>(QUEUE_CAPACITY);

            //Create connection handler threads and store them in array for accessability.
            connectionHandlers = new ConnectionHandler[THREAD_POOL_SIZE];
            for(int i = 0; i < THREAD_POOL_SIZE; i++) {
                connectionHandlers[i] = new ConnectionHandler();
            }


            Socket clientSocket;

            CommandThread cmd = new CommandThread();
            DisconnectionThread dsc = new DisconnectionThread();

            while(true) {
                clientSocket = serverSocket.accept();
                //If users in connectionQueue is over maximum as defined by QUEUE_CAPACITY new user will be blocked.
                connectionQueue.put(clientSocket);
            }
        } catch(IOException ioe) {
            logg("Error creating ServerSocket: " + ioe.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private synchronized static void disconnectUser(User u) {
        try {
            if(!u.socketIsClosed())
                u.closeSocket();
            logg("User '" + u.getUid() + "' has been disconnected");
        } catch (IOException e) {
            logg("Error when disconnecting: " + e.getMessage());
        }
    }

    private static void updateUserList() throws IOException {
        PrintStream outgoing;
        String sendString;
        synchronized(users) {
            if(users.size() > 1) {
                for(User currentUser : users) {
                    outgoing = new PrintStream(currentUser.getOutputStream());
                    sendString = "USR";
                    for(User u : users) {
                        if(!u.getUid().equals(currentUser.getUid())) {
                            sendString += "," + u.getUid();
                        }
                    }
                    outgoing.println(sendString);
                }
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

    private static void registerUser(Socket client, String s) throws IOException {
        synchronized (users) {
            users.add(new User(client, s, newUserId++));
        }
        updateUserList();
    }

    private static void connectUsers(String challenger, String target) throws IOException {
        logg("User '" + challenger + "' challenged user '" + target + "'");

        PrintStream outgoingTarget;
        PrintStream outgoingChallenger;
        DataInputStream is;
        String response;

        User challengeUser = getUserById(challenger);
        User targetUser = getUserById(target);

        try {
            //Send challenge request to specified user
            outgoingTarget = new PrintStream(targetUser.getOutputStream());
            outgoingChallenger = new PrintStream(challengeUser.getOutputStream());
            outgoingTarget.println("CHA," + challenger);
            /*outgoingTarget.flush();
            outgoingTarget.close();*/

            //Retrieve response from user
            is = new DataInputStream(targetUser.getInputStream());
            while((response = is.readLine()) != null) {
                if(response.equals("ACC")) {
                    outgoingChallenger.println(targetUser.getSocketAddress());
                    outgoingTarget.println(challengeUser.getSocketAddress());
                } else if(response.equals("DEN")){
                    outgoingChallenger.println("-1");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        synchronized (users) {
            users.remove(targetUser);
            users.remove(challengeUser);
            updateUserList();
            disconnectUser(targetUser);
            disconnectUser(challengeUser);
        }
    }

    private static User getUserById(String id) {
        synchronized (users) {
            for(User u : users) {
                if(u.getUid().equals(id))
                    return u;
            }
        }
        return null;
    }

    private static class DisconnectionThread extends Thread {

        double t1, t2;

        DisconnectionThread() {
            logg(this + " handling disconnections");
            setDaemon(true);
            start();
        }

        public void run() {
            t2 = System.currentTimeMillis();
            PrintStream os;
            ArrayList<User> toRemove;

            while(true) {
                t1 = System.currentTimeMillis();

                if(t1 - t2 >= 3000) {
                    toRemove = new ArrayList<>();
                    for(User u : users) {
                        try {
                            os = new PrintStream(u.getOutputStream());
                            os.println(0);
                            if(u.socketIsClosed() || u.isOutputShutdown() || os.checkError()) {
                                logg("User '" + u.getUid() + "' connection is closed");
                                disconnectUser(u);
                                toRemove.add(u);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    users.removeAll(toRemove);
                    if(toRemove.size() > 0) {
                        try {
                            updateUserList();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    t2 = t1;
                }
            }
        }
    }

    private static class CommandThread extends Thread {

        CommandThread() {
            logg(this + " handling user input");
            setDaemon(true);
            start();
        }

        public void run() {
            Scanner scan = new Scanner(System.in);
            PrintStream outgoing;
            String adminInput;

            while(true) {
                adminInput = scan.nextLine().replaceAll("[^a-zA-Z]", "").toLowerCase();

                switch(adminInput) {
                    case "bye":
                        for(ConnectionHandler ch : connectionHandlers) {
                            ch.interrupt();
                        }

                        synchronized (users) {
                            for(User u : users) {
                                try {
                                    outgoing = new PrintStream(u.getOutputStream());
                                    outgoing.println("BYE");
                                    logg("Disconnected user " + u.getUid());
                                } catch (IOException e) {
                                    logg("Error disconnecting user " + u.getUid() + " : " + e.getMessage());
                                }
                            }
                        }

                        logg("Shutting down...");
                        System.exit(0);
                        break;

                    case "users":
                        logg("Users currently connected ([id] \"username\"):");
                        synchronized (users) {
                            if(users.size() < 1) {
                                System.out.println("\t\t\tNone");
                                break;
                            }

                            for(User u : users) {
                                System.out.printf("\t\t\t[%d] \"%s\"%n", u.getId(), u.getUid());
                            }
                        }

                        break;

                    case "help":
                        logg("Available commands:");
                        System.out.println("\t\t\tbye - Safe shutdown");
                        System.out.println("\t\t\thelp - Show this command help information");
                        System.out.println("\t\t\tusers - Show all users currently connected");

                        break;

                    default:
                        logg("Unknown command: '" + adminInput + "'");
                        break;
                }
            }
        }
    }

    private static class ConnectionHandler extends Thread {

        public boolean running;

        ConnectionHandler() {
            logg("Connection handler " + this + " initialized");
            setDaemon(true);
            start();
        }

        public void run() {
            running = true;
            while (running) {
                Socket client;
                PrintStream outgoing;   // Stream for sending data.
                String response;
                String[] split;
                DataInputStream is;

                try {
                    client = connectionQueue.take();

                    String clientAddress = client.getInetAddress().toString();
                    try {
                        logg("Connection from " + clientAddress + " is being handled by " + this);

                        is = new DataInputStream(client.getInputStream());

                        while ((response = is.readLine()) != null) {
                            split = response.split(",");
                            switch (split[0]) {
                                case "REG":
                                    registerUser(client, split[1]);
                                    logg("User '" + split[1] + "' registered");
                                    break;
                                case "CON":
                                    connectUsers(split[1], split[2]);
                                    logg("User '" + split[1] + "' connected to user '" + split[2] + "'");
                                    break;
                            }
                        }

                    } catch (IOException ioe) {
                        System.out.println("Error on connection with: "
                                + clientAddress + ": " + ioe);
                    }

                } catch (InterruptedException e) {
                    running = false;
                    break;
                }
            }
        }
    }
}
