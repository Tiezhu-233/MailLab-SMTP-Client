/*************************************
 *  Filename:  HTTPInteraction.java
 **+***+******************************/

import java.net.*;
import java.io.*;
import java.util.*;

/**
 * Class for downloading one object from a Http server.
 *
 */
public class HTTPInteraction {
	private String host;
	private String path;
	private String requestMessage;
	
		
	private static final int HTTP_PORT = 80;
	private static final String CRLF = "\r\n";
	private static final int BUF_SIZE = 4096; 
	private static final int MAX_OBJECT_SIZE = 102400;

 	/** Create a HTTPInteraction object. ***/

	public HTTPInteraction(String url) {
		// Split "URL" into "host name" and "path name"
		// If the URL does not have a path, use "/" as default path
		if (url.contains("/")) {
			String[] parts = url.split("/", 2); // Split into host and path
			host = parts[0];
			path = "/" + parts[1]; // Add "/" before the path
		} else {
			host = url; // If no path is provided, set default
			path = "/";
		}

		// Construct requestMessage
		requestMessage = "GET " + path + " HTTP/1.1" + CRLF;
		requestMessage += "Host: " + host + CRLF;
		requestMessage += "Connection: close" + CRLF + CRLF;

		return; // Explicit return to end constructor, though not necessary
	}


	/* Send Http request, parse response and return requested object 
	 * as a String (if no errors), 
	 * otherwise return meaningful error message. 
	 * Don't catch Exceptions. EmailClient will handle them. */		
	public String send() throws IOException {
		
		/* buffer to read object in 4kB chunks */
		char[] buf = new char[BUF_SIZE];

		/* Maximum size of object is 100kB, which should be enough for most objects. 
		 * Change constant if you need more. */		
		char[] body = new char[MAX_OBJECT_SIZE];
		
		String statusLine="";	// status line
		int status;		// status code
		String headers="";	// headers
		int bodyLength=-1;	// lenghth of body
				
		String[] tmp;
		
		/* The socket to the server */
		Socket connection;
		
		/* Streams for reading from and writing to socket */
		BufferedReader fromServer;
		DataOutputStream toServer;
		

		
		/* Connect to http server on port 80.
		 * Assign input and output streams to connection. */		

		connection = new Socket(host, HTTP_PORT);
		fromServer = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		toServer = new DataOutputStream(connection.getOutputStream());




		/* Send requestMessage to http server */
		/* Fill in */
		toServer.writeBytes(requestMessage);



		/* Read the status line from response message */
		statusLine = fromServer.readLine();
		//System.out.println("Status Line:\n"+statusLine+CRLF);
		
		/* Extract status code from status line. If status code is not 200,
		 * close connection and return an error message. 
		 * Do NOT throw an exception */		
		/* Fill in */
		if (statusLine == null || !statusLine.startsWith("HTTP/1.")) {
			return "Invalid response from server";
		}

		tmp = statusLine.split(" ");
		status = Integer.parseInt(tmp[1]);
		if (status != 200) {
			return "Error: " + statusLine;
		}


		/* Read header lines from response message, convert to a string, 
 		 * and assign to "headers" variable. 
		 * Recall that an empty line indicates end of headers.
		 * Extract length  from "Content-Length:" (or "Content-length:") 
		 * header line, if present, and assign to "bodyLength" variable. 
		*/
		/* Fill in */ 		// requires about 10 lines of code
		String headerLine;
		while ((headerLine = fromServer.readLine()) != null && !headerLine.isEmpty()) {
			headers += headerLine + CRLF;
			if (headerLine.toLowerCase().startsWith("content-length:")) {
				bodyLength = Integer.parseInt(headerLine.split(":")[1].trim());

			}
		}




		/* If object is larger than MAX_OBJECT_SIZE, close the connection and 
		 * return meaningful message. */
		if (bodyLength > MAX_OBJECT_SIZE) {
			return ("Object too large: " + bodyLength);
		}
		/* Read the body in chunks of BUF_SIZE using buf[] and copy the chunk
		 * into body[]. Stop when either we have
		 * read Content-Length bytes or when the connection is
		 * closed (when there is no Content-Length in the response). 
		 * Use one of the read() methods of BufferedReader here, NOT readLine().
		 * Make sure not to read more than MAX_OBJECT_SIZE characters.
		 */

		/* Fill in */   // Requires 10-20 lines of code

		int currentPos = 0;



		if (bodyLength == -1) {
			// No Content-Length, read until connection closes
			int count;
			while ((count = fromServer.read(buf, 0, BUF_SIZE)) != -1) {
				if (currentPos + count > MAX_OBJECT_SIZE) {
					break;
				}
				System.arraycopy(buf, 0, body, currentPos, count);
				currentPos += count;
			}
		} else {
			// Use Content-Length to read specific amount
			while (currentPos < bodyLength) {
				int remainingSize = Math.min(BUF_SIZE, bodyLength - currentPos);
				int count = fromServer.read(buf, 0, remainingSize);

				if (count == -1) {
					break;
				}

				if (currentPos + count > MAX_OBJECT_SIZE) {
					break;
				}

				System.arraycopy(buf, 0, body, currentPos, count);
				currentPos += count;
			}
		}

		/* At this points body[] should hold to body of the downloaded object and
		 * bytesRead should hold the number of bytes read from the BufferedReader
		 */

		/* Close connection and return object as String. */

		connection.close();
		return(new String(body, 0, currentPos));
	}
}


