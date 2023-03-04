import java.io.*;
import java.net.*;

public class Client {


    public static void main(String[] args) {


        if (args.length == 0) {
            System.err.println("Usage: java client <host name> <port number> <FN_ID> <args>");
            System.exit(1);
        }

        // Parse command line arguments
        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        int functionId = Integer.parseInt(args[2]) ;

        try (
                  Socket echoSocket = new Socket(hostName, portNumber)
//                PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
//                BufferedReader in = new BufferedReader( new InputStreamReader(echoSocket.getInputStream() ));
//                BufferedReader stdIn = new BufferedReader( new InputStreamReader(System.in));

        ) {
//          Input and Output streams
            DataInputStream dataIn = new DataInputStream(echoSocket.getInputStream());
            DataOutputStream dataOut = new DataOutputStream(echoSocket.getOutputStream());

//          Declare args
            int authToken,messageId;
            String recipient,messageBody;
            String response;

            switch(functionId) {
                case 1:
                    // Create Account Operation
                    String username = args[3];

                    // Send request to server
                    dataOut.writeInt(functionId);
                    dataOut.writeUTF(username);

                    // Read response from server
                    response = dataIn.readUTF();
                    System.out.println(response);

                    break;
                case 2, 4:
                    // Show Accounts/Show Inbox Operations
                    authToken = Integer.parseInt(args[3]);

                    // Send request to server
                    dataOut.writeInt(functionId);
                    dataOut.writeInt(authToken);

                    // Read response from server
                    response = dataIn.readUTF();
                    System.out.println(response);

                    break;
                case 3:
//                  Send Message Operation

//                  Parse command line arguments

                    authToken = Integer.parseInt(args[3]);
                    recipient = args[4];
                    messageBody = args [5];

//                   Send request to server
                    dataOut.writeInt(functionId);
                    dataOut.writeInt(authToken);
                    dataOut.writeUTF(recipient);
                    dataOut.writeUTF(messageBody);

//                    Read response from server
                    response = dataIn.readUTF();
                    System.out.println(response);

                    break;
                case 5, 6:
//                    Read Message/Delete Message Operations

//                    Parse command line arguments
                    authToken = Integer.parseInt(args[3]);
                    messageId = Integer.parseInt(args[4]);

//                    Send request to server
                    dataOut.writeInt(functionId);
                    dataOut.writeInt(authToken);
                    dataOut.writeInt(messageId);

//                    Read response from server
                    response = dataIn.readUTF();

//                  Display response to the user
                    System.out.println(response);

                    break;
                default:
                    System.out.println("Function Id is not yet supported");
            }

//            Echo Server
//            String userInput;
//            while ((userInput = stdIn.readLine()) != null) {
//                out.println(userInput);
//                System.out.println("echoed : "+in.readLine());
//            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                    hostName);
            System.exit(1);
        }

    }

}