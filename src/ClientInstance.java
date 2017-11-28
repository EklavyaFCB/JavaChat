import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by Eklavya on 12/12/2016.
 * This is a ClientInstance class created by ClientMain
 */
public class ClientInstance implements Runnable {

    // Variables
    private Socket sock;
    private final int PORT_NUMBER;
    private String IP_Address;
    private Scanner IP_Input = new Scanner(System.in);

    /**
     * Constructor
     * @param s
     * @param PORT_NUM
     */
    ClientInstance(Socket s, int PORT_NUM) {
        sock = s;
        PORT_NUMBER = PORT_NUM;
    }

    /**
     * The SetUp method declares and starts the thread for the client
     */
    public void setUp() {

        // I/O variables
        BufferedReader stdIn = null;
        PrintWriter out = null;
        boolean IP_test = false;

        try {
            // IP Connection to server
            // We (purposefully) only make this java program to work on localhost for this project's scale
            while (!IP_test) {
                System.out.println("Please enter the IP address of your desired connection:");
                if ((IP_Input.nextLine()).equals("localhost")) {
                    IP_Address = "localhost";
                    IP_test = true;
                } else {
                    System.out.println("Hint: the connection has to be localhost.");
                }
            }

            sock = new Socket(IP_Address, PORT_NUMBER);

            // I/O Streams
            stdIn = new BufferedReader(new InputStreamReader(System.in));
            out = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));

            // Thread
            Thread t;
            t = new Thread(new ClientInstance(sock, PORT_NUMBER));
            t.start();

            // Variable
            String messageOut;

            // Handle outgoing messages
            boolean done1 = false; // boolean for first while loop
            while (!done1) {
                // Type and flush outgoing message (username)
                messageOut = stdIn.readLine();
                out.println(messageOut);
                out.flush();
            }
        } catch (IOException e) { // Catch exception
            e.printStackTrace();
        } finally { // Close all connections

            // Close output stream
            if (out != null) {
                out.close();
            }

            // Close stdIn input stream
            if (stdIn != null) {
                try {
                    stdIn.close();
                } catch (IOException e) { // Catch exception
                }
            }

            // Close socket
            if (sock != null) {
                try {
                    sock.close();
                } catch (IOException e) { // Catch exception
                }
            }
        } System.exit(0); // Exit with error code 0
    }

    // Handle incoming messages
    /**
     * The run method started in the setUp method.
     * This handles the incoming messages.
     */
    public void run() {
        // Variable
        BufferedReader in = null;

        try {
            // Input stream variables
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            String messageIn;
            boolean done2 = false;

            // Second while loop
            while (!done2) {
                messageIn = in.readLine(); // read incoming message
                if ((messageIn == null) || (messageIn.trim().equals("Exit"))) {
                    // Quit if necessary
                    done2 = true;
                } else {
                    System.out.println(messageIn);
                }
            }
        } catch (IOException e) { // Catch exception
            e.printStackTrace();
        } finally { // Close input connection
            if (in != null) {
                try {
                    in.close(); // Close input stream
                } catch (IOException e) { // Catch exception
                }
            }
        } System.exit(0); // Exit with error code 0
    }
}
