/**
 * Created by Eklavya on 07/12/2016.
 * @author Eklavya Sarkar
 * This is the Server class.
 * Contains the main method, which calls the run() method in ServerThread,
 * a Server Shutdown() method and a ConnectionCounter class.
 */

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

    // Declarations

    // port number for the server
    private static final int PORT_NUM = 8189;

    // the server socket:
    private static ServerSocket ss;

    // set when shutDown() is called to stop the server:
    private static boolean shutDownCalled = false;

    // HashSet for all clients
    private static HashSet<String> clientNamesList = new HashSet<String>();

    // HashSet for message broadcasts
    private static HashSet<PrintWriter> messages = new HashSet<PrintWriter>();

    // Server start time
    static long serverStartTime;

    /**
     * Main method for the server side
     * @param args
     */
    public static void main(String[] args) {
        // For client connections
        Socket incoming;

        // Session-handling thread
        Thread t;

        try {
            // Set up server socket
            ss = new ServerSocket(PORT_NUM);

            // Instance of ConnectionCounter to access its counting methods
            ConnectionCounter stato = new ConnectionCounter();
            System.out.println("LOG -- Server up and running!");

            // Server start time begins here
            serverStartTime = System.currentTimeMillis();

            // While loop which starts the client threads
            while (true) {
                incoming = ss.accept();

                // Start session-handler in new thread
                t = new Thread(new ServerThread(incoming, stato));
                t.start();
            }
        } catch (SocketException se) {
			/* Will be thrown when accept() is called after closing the server
			    socket, in method shutDown(). If shutDownCalled, then simply
			    exit; otherwise, something else has happened: */
            if (!shutDownCalled) {
                System.err.println("Socket problem:");
                System.err.println(se.getMessage());
                // Exit with error code 1
                System.exit(1);
            }
        } catch (IOException ioe) { // Catch exception
            System.err.println("I/O error:");
            System.err.println(ioe.getMessage());
            // Exit with error code 1
            System.exit(1);
        } finally { // Close socket
            if (ss != null) {
                try {
                    ss.close();
                } catch (Exception e) { // Catch exception
                    System.err.println("closing: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Method to shut the server down by closing the server socket
     */
    public static void shutDown() {
        // flag that the server socket has been closed
        shutDownCalled = true;

        if (ss != null) {
            try {
				/* close the server socket; call of accept() in main
				will throw a SocketException */
                ss.close();
            } catch (Exception e) {
                // Something went wrong; give data:
                System.err.println("Problem shutting down:");
                System.err.println(e.getMessage());

                // Exit with error code 1
                System.exit(1);
            }
        }

        // Show in server's logs that server has been shut down
        System.out.println("LOG -- Server has shut down.");
        System.exit(0);
    }

    /**
     * Session-handler class to handle one remote client in a separate thread.
     */
    private static class ServerThread implements Runnable {

        // Variables
        private Socket client;
        private ConnectionCounter counter;
        private String userName;

        /**
         * Constructor
         * @param s
         * @param cc
         */
        ServerThread(Socket s, ConnectionCounter cc) {
            client = s;
            counter = cc;
        }

        /**
         * Run method start by 'start()' in the thread in the main method
         */
        public void run() {
            // Variables

            // For I/O
            BufferedReader in = null;
            PrintWriter out = null;

            // Add a connection in the ConnectionCounter class
            counter.addConnection();
            System.out.println("LOG -- Connection to server established by a client.");

            try {
                // Set up I/O
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));

                // Client input
                String line;
                // Booleans required for each while loop
                boolean done1 = false;
                boolean done2 = false;

                // Prompt client
                out.println("Hello! Welcome to the server! You can type 'Exit' to exit to leave anytime. \n\nPlease choose an untaken username: ");
                out.flush();

                // Username validation - we use a while loop
                while (!done1) {
                    userName = in.readLine(); // Read input
                    System.out.println("LOG -- Username '" + userName + "' has been received!");
                    if ((userName == null) || (userName.equals("Exit"))) {
                        done1 = true;
                    }
                    synchronized (clientNamesList) { // Check if HashSet already input username
                        if (!clientNamesList.contains(userName)) {
                            clientNamesList.add(userName); // Add if it doesn't
                            break;
                        } else {
                            out.println("Username already taken, please choose another:");
                            out.flush();
                            System.out.println("LOG -- Username '" + userName + "' has been rejected!");
                        }
                    }
                }

                // Username acceptance
                System.out.println("LOG -- Username '" + userName + "' has been accepted!");
                out.println("Username accepted. You can now type in to chat. Type 'Help' for more information on commands.");
                out.flush();

                System.out.println("LOG -- " + client.getInetAddress() + " - " + userName + " has connected to the chat.");

                // Client start time begins here
                long clientStartTime = System.currentTimeMillis();

                // Add the output stream to the HashSet containing all the output streams for broadcasting
                messages.add(out);

                // Tell everyone you've joined the chat
                for (PrintWriter writer : messages) {
                    writer.println(userName + " has joined the chat.");
                    writer.flush();
                }

                // Second while loop, post-username confirmation
                while (!done2) {
                    line = in.readLine(); // Read input
                    if ((line == null) || (line.trim().equalsIgnoreCase("Exit"))) {
                        // Quit
                        for (PrintWriter writer : messages) { // Broadcast disconnects
                            writer.println(userName + " has left the chat.");
                            writer.flush();
                        } // Log disconnects
                        System.out.println("LOG -- " + userName + " used 'Exit' command.");
                        System.out.println("LOG -- " + userName + " has disconnected from the chat.");
                        done2 = true;
                    } else if (line.trim().equalsIgnoreCase("stamata")) {
                        // Announce shut down of server
                        // This is a hidden command for an admin user to shut down the server
                        for (PrintWriter writer : messages) {
                            writer.println("The server is shutting down.");
                            writer.flush();
                        }
                        System.out.println("LOG -- " + userName + " used 'stamata' command.");
                        System.out.println("LOG -- Server shutting down.");
                        Server.shutDown(); // Shut down method call
                        done2 = true;
                    } else if (line.trim().equalsIgnoreCase("Help")) {
                        // Prints out the list of commands
                        System.out.println("LOG -- " + userName + " used 'help' command.");
                        out.println("Type the following commands to get their function:\n");
                        out.println("Connections - get total number of connections since this server started");
                        out.println("Currently   - get total number of the current connections");
                        out.println("Server time - get the server's running time");
                        out.println("Client time - get the client's running time");
                        out.println("IP          - get the server's IP address");
                        out.println("Exit        - disconnect from the server\n");
                        out.flush();
                    } else if (line.trim().equalsIgnoreCase("Connections")) {
                        // Prints out total number of connections ever made to this server since its uptime
                        System.out.println("LOG -- " + userName + " used 'connections' command.");
                        out.println("Connections: " + counter.getConnections() + "\n");
                        out.flush();
                    } else if (line.trim().equalsIgnoreCase("Currently")) {
                        // Prints out number of clients connection at the moment
                        System.out.println("LOG -- " + userName + " used 'currently' command.");
                        out.println("Currently : " + counter.getCurrentConnections() + "\n");
                        out.flush();
                    } else if (line.trim().equalsIgnoreCase("IP")) {
                        // Prints out the IP address of the sever
                        System.out.println("LOG -- " + userName + " used 'IP' command.");
                        InetAddress IP = InetAddress.getLocalHost();
                        out.println("IP: " + IP.getHostAddress() + "\n");
                        out.flush();
                    } else if (line.trim().equalsIgnoreCase("Server time")) {
                        // Prints out the uptime of the server
                        System.out.println("LOG -- " + userName + " used 'server time' command.");
                        long serverCurrentTime = System.currentTimeMillis();
                        long serverUpTime = serverCurrentTime - serverStartTime;
                        out.println("The Server has been running for " + serverUpTime + " ms\n");
                        out.flush();
                    } else if (line.trim().equalsIgnoreCase("Client time")) {
                        // Prints out the uptime of the client
                        System.out.println("LOG -- " + userName + " used 'client time' command.");
                        long clientCurrentTime = System.currentTimeMillis();
                        long clientUpTime = clientCurrentTime - clientStartTime;
                        out.println("The client has been connected for " + clientUpTime + " ms\n");
                        out.flush();
                    } else { // Broadcast to everyone the chat input of a client
                        for (PrintWriter writer : messages) {
                            writer.println(userName + ": " + line);
                            writer.flush();
                        }
                        System.out.println("CHAT -- " + userName + ": " + line); // Log it in Server console
                    }
                }
            } catch (IOException e) { // Catch exception
                // Fatal error for this session
            } finally { // Close connections
                try {
                    in.close(); // Close input stream
                } catch (IOException e) {
                }
                if (out != null) {
                    out.close(); // Close output stream
                }
                if (client != null) {
                    try {
                        client.close(); // Close socket
                    } catch (IOException e) {
                    }
                }
                counter.endSession(); // Call ConnectionCounter class
            }
        }
    }

    /**
     * Class for counting the connection stats
     */
    private static class ConnectionCounter {

        // Variables for the stats
        private int connections = 0;
        private int currentConnections = 0;

        // Methods

        /**
         * Returns the number of connections ever made
         * @return connections
         */
        private int getConnections() {
            return connections;
        }

        /**
         * Returns the current connections
         * @return currentConnections
         */
        private int getCurrentConnections() {
            return currentConnections;
        }


        // Adds a count to the current and total number of connections
        private void addConnection() {
            connections++;
            currentConnections++;
        }

        // Removes a count from current connections as the client ends the session
        private void endSession() {
            currentConnections--;
        }
    }
}