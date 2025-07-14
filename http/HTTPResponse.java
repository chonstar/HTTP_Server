package http;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

/**
 * Class representing a single HTTP response message.
 *
 * @version 1.0
 */
public class HTTPResponse {
     private HTTPRequest request;
     private boolean validated;
     private Map<String,String> header_variables; 
     private DataOutputStream stream;

     private String string_path, version, reason, server, date, content_type;
     private int status;
     private long content_length;
     private Path file_path;
     private byte[] file_bytes;

     public HTTPResponse(HTTPRequest request, DataOutputStream stream) {
          this.request = request;
	  this.stream = stream;
	  validated = false;
	  read();
	            
     }     
     
     // Crafts HTTP response and sends it out the socket
     public void craft_response() {
	  if (validated) {
	       try {
	           stream.writeBytes(
	    	        "HTTP/1.1 200 OK\r\n" +
    		        "Server: " + header_variables.get("Server") + "\r\n" +
    			"Content-Length: " + header_variables.get("Content-Length") + "\r\n" +
    			"Date: " + header_variables.get("Date") + "\r\n" +
     			"Content-Type: " + header_variables.get("Content-Type") + "\r\n"
		    ); 
	            stream.writeBytes("\r\n");
	            write_message();
		    stream.flush();
	       }
	       catch (IOException e) {
	            System.out.println("Error sending data out socket");
	       }
	  }
	  else if (!validate_request()) {
	       try {
	           stream.writeBytes(
	    	        "HTTP/1.1 400 Bad Request\r\n" +
    		        "Server: " + header_variables.get("Server") + "\r\n" +
    			"Content-Length: " + header_variables.get("Content-Length") + "\r\n" +
    			"Date: " + header_variables.get("Date") + "\r\n" +
     			"Content-Type: " + header_variables.get("Content-Type") + "\r\n"
		    ); 
	            stream.writeBytes("\r\n");
	            write_message();
		    stream.flush();
	       }
	       catch (IOException e) {
	            System.out.println("Error sending data out socket"); 
	       }
	  }
	  else if (!parse_path()) {
	       try {
	           stream.writeBytes(
	    	        "HTTP/1.1 404 Not Found\r\n" +
    		        "Server: " + header_variables.get("Server") + "\r\n" +
    			"Content-Length: " + header_variables.get("Content-Length") + "\r\n" +
    			"Date: " + header_variables.get("Date") + "\r\n" +
     			"Content-Type: " + header_variables.get("Content-Type") + "\r\n"
		    ); 
	            stream.writeBytes("\r\n");
	            write_message();
		    stream.flush();
	       }
	       catch (IOException e) {
		    System.out.println("Error sending data out socket"); 
	       }
	  }
     } 

     // Getter method for status code
     public String get_status() {
          return Integer.toString(status);     
     }

     // Writes requested file to socket
     private void write_message() {
	  try {
	       file_bytes = Files.readAllBytes(file_path);
	       stream.write(file_bytes);
	  }
	  catch (IOException e) {
	       System.out.println("Error writing data to socket");
	  }
     }	

     // Stores header variables given by HTTP request
     private void parse_header_variables() {
          header_variables = new HashMap<String,String>();
	  header_variables.put("Server","chonco");
	  header_variables.put("Date",current_date_time());
	  header_variables.put("Content-Length",Long.toString(content_length));
	  header_variables.put("Content-Type",content_type); 
     }

     // Provides current time according to RFC2616 standards
     private String current_date_time() {
          ZonedDateTime current = ZonedDateTime.now(ZoneOffset.UTC);
	  DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME.withLocale(Locale.US);
	  date = current.format(formatter);
	  return date;
     }

     // Validates HTTP request
     private boolean validate_request() {
	  if (!request.isValid()) {
	       file_path = Paths.get("errors/400.html");
	       file_details(file_path);
	       status = 400;
	       return false;
	  }
	  return true;
     }
    
     // Measures length and checks type of requested HTML file
     private void file_details(Path file_path) {
          try {
               content_length = Files.size(file_path);
	       content_type = Files.probeContentType(file_path);
	       if (content_type == null) {
		    content_type = "application/octet-stream";
	       }
	  }
	  catch (IOException e) {
	       System.out.println("Error calculating size or content type of file");
	  }
     }

     // Validates and parses requested path
     private boolean parse_path() {
          string_path = request.getPath();
	  if (string_path.equals("/")) {
	       file_path = Paths.get("content/index.html");
	       file_details(file_path);			
	       status = 200;
	       return true;
	  }
	  else {
	       file_path = Paths.get("content" + string_path);
	       if (Files.exists(file_path)) {
		    file_details(file_path);
		    status = 200;
		    return true;
	       }
	       else {
		    file_path = Paths.get("errors/404.html");
		    file_details(file_path);
		    status = 404;
	       }
	       return false;
	  }
     }

     private void read() {
          validated = validate_request() && parse_path();
	  parse_header_variables();
     }
}
