package http;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * HTTPConnection represents a single client connection to this server.
 *
 * @version 1.0
 */
public class HTTPConnection implements Runnable {
    /**
     * Already connected socket for one client.
     */
    private Socket client;

    /**
     * Input channel.
     */
    private Scanner socketInput;

    /*
     * Output channel.
     */
    private DataOutputStream socketOutput;

    /**
     * Current state of this connection.
     *
     * If EOF or any invalid messages are detected, set this to false.
     */
    private boolean connected;

    /**
     * Required constructor.
     *
     * @param client client's socket created in the main thread
     */
    HTTPConnection(Socket client) {
        this.client = client;
    }

    /**
     * Handle a single request message.
     */
    private void processRequest() {
        try {
            var request = new HTTPRequest(socketInput);
	    try {
	         socketOutput = new DataOutputStream(client.getOutputStream());
	    }
	    catch (IOException e) {
	         System.out.println("Error initializing DataOutputStream");
	    }
	    var response = new HTTPResponse(request, socketOutput);
	    String status = response.get_status();
            if (request.isValid()) {
                 String url = request.getHostHeader() + request.getPath();
                 System.out.println(client + ": Received valid request for " + url);
		 System.out.println(client + ": Sending " + status + " response");
		 response.craft_response();
            }
            else {
                 System.out.println(client + ": Received invalid request");
		 System.out.println(client + ": Sending " + status + " response");
		 response.craft_response();
                 connected = false;
            }
        } catch (NoSuchElementException e) {
            connected = false;
        }
    }

    /**
     * Handle a new connection.
     */
    @Override
    public void run() {
        System.out.println(client + ": connected");
        connected = true;

        try {
            /* setup input and output streams for the client */
            socketInput = new Scanner(client.getInputStream());
            socketOutput = new DataOutputStream(client.getOutputStream());

            /* keep proccessing requests from the client until either
             * they disconnect or we do */
            while(connected) {
                processRequest();
            }
        } catch(Exception e) {
            System.out.println(client + ": socket error: " + e);
        } finally {
            /* close the client connection */
            try {
                client.close();
            } catch (IOException e) {}
            System.out.println(client + ": closed");
        }
    }
}
