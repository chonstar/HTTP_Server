package http;

import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.concurrent.Executors;

/**
 * Class representing an HTTP Server.
 *
 * @version 1.0
 */
public class HTTPServer {
    public static void main(String[] args) {
        try (
            /* Accept connections on port 80 */        
            var listener = new ServerSocket(80);
        ) {
            /* create a thread pool with 100 threads */
            var threadPool = Executors.newFixedThreadPool(100);

            /* each new connection is handled by one thread */
            while(true) {
                threadPool.execute(new HTTPConnection(listener.accept()));
            }
        } catch(IOException e) {
            /* Have to catch IOexceptions for most socket calls */
            System.out.println("main thread: error: " + e);
        }
    }
}
