package http;

import java.util.HashMap;
import java.util.Scanner;

/**
 * Class representing a single HTTP request message.
 *
 * @version 1.0
 */
public class HTTPRequest {
    private Scanner input;
    private boolean validated;

    private String type;
    private String path;
    private String version;
    private HashMap<String,String> headerVariables;

    public HTTPRequest(Scanner input) {
        this.input = input;
        this.validated = false;
        read();
    }

    public boolean isValid() { return validated; }
    public String getPath() { return path; }
    public String getHostHeader() { return headerVariables.get("Host"); }

    private boolean parseRequestLine() {
        var requestLinePieces = input.nextLine().trim().split("\\s+");
        
        if(requestLinePieces.length != 3) {
            return false;
        }

        type = requestLinePieces[0];
        path = requestLinePieces[1];
        version = requestLinePieces[2];

        if(!type.equals("GET")) {
            return false;
        }

        if(path.charAt(0) != '/') {
            return false;
        }
        
        if(!version.equals("HTTP/1.1")) {
            return false;
        }

        return true;
    }

    private boolean parseHeaderVariables() {
        headerVariables = new HashMap<String,String>();

        String headerLine = input.nextLine().trim();
        while(!headerLine.equals("")) {
            var headerPieces = headerLine.split(":", 2);
            if(headerPieces.length != 2) {
                return false;
            }
            var name = headerPieces[0].trim();
            var value = headerPieces[1].trim();

            if(name.isEmpty() || value.isEmpty()) {
                return false;
            }

            headerVariables.put(name,value);

            headerLine = input.nextLine().trim();
        }

        if(!headerVariables.containsKey("Host")) {
            return false;
        }

        return true;
    }

    private void read() {
        validated = parseRequestLine() && parseHeaderVariables();
    }
}
