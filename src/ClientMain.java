/**
 * Created by Eklavya on 07/12/2016.
 * * @author Eklavya Sarkar
 * The main method class calls all the methods from other classes.
 */

import java.net.*;

public class ClientMain {

    /**
     * The main method for the client class
     * @param args
     */
    public static void main(String args[]) {

        // Variables

        // port number for the server
        final int PORT_NUM = 8189;

        // Socket
        Socket s = null;

        // Create instance
        ClientInstance client1 = new ClientInstance(s, PORT_NUM);

        // Call setUp method, which in turn starts the thread
        client1.setUp();

    }
}
