/************************************
 * Filename:  SMTPInteraction.java
 ************************************/
import java.net.*;
import java.io.*;
import java.util.Base64;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Open an SMTP connection to mailserver and send one mail.
 *
 */
public class SMTPInteraction {
    /**
     * Socket to the SMTP server
     ***/
    private Socket connection;

    /* Streams for reading from and writing to socket */
    private BufferedReader fromServer;
    private DataOutputStream toServer;

    private static final String CRLF = "\r\n";

    /* Are we connected? Used in close() to determine what to do. */
    private boolean isConnected = false;

    /* Create an SMTPInteraction object. Create the socket and the
       associated streams. Initialise SMTP connection. */
    public SMTPInteraction(EmailMessage mailmessage) throws IOException {
        connection = new Socket(mailmessage.DestHost, mailmessage.DestHostPort);
        attachStreams();

        validateResponse(readResponse(), new int[] {220}, "connect");

        String localhost = InetAddress.getLocalHost().getHostName();
        sendCommand("EHLO " + localhost, new int[] {250});

        if (mailmessage.UseStartTLS) {
            sendCommand("STARTTLS", new int[] {220});
            upgradeToTLS(mailmessage.DestHost, mailmessage.DestHostPort);
            sendCommand("EHLO " + localhost, new int[] {250});
        }

        if (mailmessage.UseAuth) {
            authLogin(mailmessage.Username, mailmessage.Password);
        }

        isConnected = true;
    }

    /* Send message. Write the correct SMTP-commands in the
       correct order. No checking for errors, just throw them to the
       caller. */
    public void send(EmailMessage mailmessage) throws IOException {
        sendCommand("MAIL FROM:<" + mailmessage.Sender + ">", new int[] {250});
        sendCommand("RCPT TO:<" + mailmessage.Recipient + ">", new int[] {250, 251});
        sendCommand("DATA", new int[] {354});
        sendCommand(mailmessage.Headers + CRLF + CRLF + mailmessage.Body + CRLF + ".", new int[] {250});
    }

    /* Close SMTP connection. First, terminate on SMTP level, then
       close the socket. */
    public void close() {
        try {
            if (isConnected) {
                sendCommand("QUIT", new int[] {221});
            }
            connection.close();
            isConnected = false;
        } catch (IOException e) {
            System.out.println("Unable to close connection: " + e);
            isConnected = true;
        }
    }

    private void authLogin(String username, String password) throws IOException {
        sendCommand("AUTH LOGIN", new int[] {334});
        sendCommand(encodeBase64(username), new int[] {334});
        sendCommand(encodeBase64(password), new int[] {235});
    }

    private String encodeBase64(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes());
    }

    private void upgradeToTLS(String host, int port) throws IOException {
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket sslSocket = (SSLSocket) factory.createSocket(connection, host, port, true);
        sslSocket.startHandshake();
        connection = sslSocket;
        attachStreams();
    }

    private void attachStreams() throws IOException {
        fromServer = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        toServer = new DataOutputStream(connection.getOutputStream());
    }

    private String sendCommand(String command, int[] expectedCodes) throws IOException {
        toServer.writeBytes(command + CRLF);
        return validateResponse(readResponse(), expectedCodes, command);
    }

    private String validateResponse(String response, int[] expectedCodes, String context) throws IOException {
        int actualCode = parseReplyCode(response);
        for (int expectedCode : expectedCodes) {
            if (actualCode == expectedCode) {
                return response;
            }
        }
        throw new IOException("SMTP error during '" + context + "': " + response);
    }

    private int parseReplyCode(String response) throws IOException {
        if (response == null || response.length() < 3) {
            throw new IOException("Invalid SMTP response: " + response);
        }
        try {
            return Integer.parseInt(response.substring(0, 3));
        } catch (NumberFormatException e) {
            throw new IOException("Invalid SMTP response code: " + response);
        }
    }

    private String readResponse() throws IOException {
        String firstLine = fromServer.readLine();
        if (firstLine == null) {
            throw new IOException("Connection closed by SMTP server");
        }

        StringBuilder full = new StringBuilder(firstLine);
        if (firstLine.length() < 4 || firstLine.charAt(3) != '-') {
            return full.toString();
        }

        String expectedPrefix = firstLine.substring(0, 3) + " ";
        while (true) {
            String line = fromServer.readLine();
            if (line == null) {
                throw new IOException("Connection closed in multiline SMTP response");
            }
            full.append("\n").append(line);
            if (line.startsWith(expectedPrefix)) {
                break;
            }
        }
        return full.toString();
    }
}