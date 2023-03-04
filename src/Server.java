import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.regex.Pattern;


public class Server {


//  The static ArrayList of accounts
    private static ArrayList<Account> accounts = new ArrayList<>();

//  Getter
    public static ArrayList<Account> getAccounts() {
        return accounts;
    }

//  A method that allows the client to add a new account to the ArrayList
    public static void addAccount(Account account){
        accounts.add(account);
    }

//  A method that allows the client to see the existing accounts
    public static void showAccounts() {
        int i=0;
        System.out.println("showAccounts method called --START--");
        for (Account account : accounts) {
            System.out.println((++i)+" Username: "+ account.getUsername()+" Auth token: "+account.getAuthToken());
        }
        System.out.println("showAccounts method called --END--");

    }

//  A method that creates an Account and returns an authToken
    private static int createAccount(String username) {
        Account newAcc = new Account(username);
        addAccount(newAcc);
        int t=createToken();
        newAcc.setAuthToken(t);
//        showAccounts();
        return t;
    }

//  A method that creates and returns a randomly generated Token
    public static int createToken(){
        return (int) (Math.random()*999+1);
    }

    public static void main(String[] args) throws IOException {

        if (args.length != 1) {
            System.err.println("Usage: java server <port number>");
            System.exit(1);
        }


        int portNumber = Integer.parseInt(args[0]);

        // Create `ServerSocket` object to listen for incoming client connections.
        ServerSocket serverSocket = new ServerSocket(portNumber);

        try {
            while(true){
                // Accept a connection from a client socket
                Socket clientSocket = serverSocket.accept();

                // Create a new thread to handle the client
                Thread thread = new Thread(new ClientHandler(clientSocket));

                // Start the thread
                thread.start();
            }

        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                    + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }


    static class ClientHandler implements Runnable {
        private Socket socket;

        DataInputStream dataIn;
        DataOutputStream dataOut;


        // Constructor
        public ClientHandler(Socket socket){
            this.socket = socket;
            try {
                this.dataIn = new DataInputStream(this.socket.getInputStream());
                this.dataOut = new DataOutputStream(this.socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
//                Print message each time a new client connects to the server
//                System.out.println("New client connected!");
                int authToken;
                int messageId;
                int functionId = dataIn.readInt();
//                System.out.println("FUNCTION ID RECEIVED FROM CLIENT: " + functionId);
                switch(functionId){
                    case 1:
//                      Create Account Operation

//                      Get args from client
                        String username = dataIn.readUTF();

//                      Check if username contains special characters
                        boolean usernameIsValid  = !Pattern.compile("[^a-zA-Z0-9_ ]").matcher(username).find();


//                      Check if username exists
                        boolean usernameExists = false;
                        for(Account account :accounts){
                            if (username.equals(account.username)) {
                                usernameExists = true;
                                break;
                            }
                        }

//                      Check if username is valid or if it already exists
                        if(!usernameIsValid){
                            dataOut.writeUTF("Invalid Username");
                        }else if (usernameExists){
                            dataOut.writeUTF("Sorry, the user already exists");
                        }else {
                            authToken = createAccount(username);

//                          Send auth token to the client
                            dataOut.writeUTF(authToken+"");
                        }
                        break;

                    case 2:
//                      Show Accounts operation

//                      Get args from client
                        authToken = dataIn.readInt();

//                      Check token validity
                        if(!tokenIsValid(authToken)){
                            dataOut.writeUTF("Invalid Auth Token");
                        }else{
//                          Send a formatted string to the client as a response
                            dataOut.writeUTF(showAccounts(accounts));
                        }
                        break;

                    case 3:
//                      Send Message Operation

//                      Get args from client
                        authToken = dataIn.readInt();
                        String recipient = dataIn.readUTF();
                        String messageBody = dataIn.readUTF();
//                        System.out.println("Received from client: Auth Token: " + authToken + " Recipient: " + recipient +" MessageBody: " + messageBody );

//                      Check conditions (token not valid, recipient doesn't exist)
                        if(!tokenIsValid(authToken)){
                            dataOut.writeUTF("Invalid Auth Token");
                        }else if (!recipientExists(recipient)){
                            dataOut.writeUTF("User does not exist");
                        }else{
                            dataOut.writeUTF("OK");
                            String sender = getUsername(authToken);
//                            System.out.println("Sender: " + sender);

                            //Send message
                            Message message = new Message(false,sender,recipient,messageBody);
                            for(Account acc: accounts){
                                if (recipient.equals(acc.username)){
                                    acc.addMessage(message);
//                                    System.out.println("Message sent successfully from: " + sender + " to: " +  recipient);
                                }
                            }
                        }
                        break;

                    case 4:
//                      Show Inbox Operation

//                      Get args from client
                        authToken = dataIn.readInt();

//                      Check token validity
                        if(!tokenIsValid(authToken)){
                            dataOut.writeUTF("Invalid Auth Token");
                        }else{
//                            Get account related to the received auth token
                              Account account = getAccount(authToken);

//                            Get messageBox related to the account object
                              ArrayList<Message> messageBox = account.getMessageBox();

//                            Send response to client
                              dataOut.writeUTF(showInbox(messageBox));
                        }
                        break;

                    case 5:
//                      Read Message Operation

//                      Get args from client
                        authToken = dataIn.readInt();
                        messageId = dataIn.readInt();

//                      Check token and message_id validity
                        if(!tokenIsValid(authToken)){
                            dataOut.writeUTF("Invalid Auth Token");
                        }
                        else if(!messageIdIsValid(authToken,messageId)){
                            dataOut.writeUTF("Message ID does not exist");
                        }
                        else {
                            String formattedMessage=getFormattedMessage(authToken,messageId);

                            // Send response to client
                            dataOut.writeUTF(formattedMessage);
                        }
                        break;

                    case 6:
//                      Delete Message Operation

//                      Get args from client
                        authToken = dataIn.readInt();
                        messageId = dataIn.readInt();

//                      Check token and message_id validity
                        if(!tokenIsValid(authToken)){
                            dataOut.writeUTF("Invalid Auth Token");
                        } else if (!messageIdIsValid(authToken,messageId)) {
                            dataOut.writeUTF("Message does not exist");
                        } else{
                            dataOut.writeUTF("OK");
//                          Delete message
                            deleteMessage(authToken,messageId);
                        }
                        break;

                    default:
                        System.out.println("Function_ID invalid. Select between 1-6");
                }


//                Echo server functionality. Comment out the related section in Client as well to enable functionality
//                // Get the input and output streams for the socket
//                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
//                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//
////              Echo server
//                String inputLine;
//                while ((inputLine = in.readLine()) != null) {
//                    out.println(inputLine);
//                    System.out.println("echoing: "+inputLine);
//                }

            }catch (IOException e){
                e.printStackTrace();
            }

        }

//        A method that receives an Arraylist of message objects and returns a formatted string showing the inbox
        private String showInbox(ArrayList<Message> messageBox) {
            String s = "";
            if(messageBox.size()==0){
            s="Your inbox is empty";
            }else{
                int i=0;
                for(Server.Message message: messageBox){
                    s+=message.getId() + "." + " from: " + message.getSender() + (message.getIsRead()?"":"*")+(++i<messageBox.size() ? "\n" : "");
                }
            }
            return s;
        }

        //        A method that receives an Arraylist of account objects and returns a formatted string showing all the accounts
        private String showAccounts(ArrayList<Account> accounts) {
            String s = "";
            int i = 0;
            for (Account account : accounts) {
//                s += (++i) + "." + " Username: " + account.getUsername() + " Auth Token: " + account.getAuthToken() + (i < accounts.size() ? "\n" : "");
                s+=(++i) + ". " + account.getUsername() + (i < accounts.size() ? "\n" : "");
            }
            return s;
        }

        //      A method that receives an authToken and a messageId and deletes the message related to the messageId
        private void deleteMessage(int authToken, int messageId) {
            Account acc = getAccount(authToken);
            ArrayList<Message> messageBox = acc.getMessageBox();
            for(Message message: messageBox){
                if(message.getId()==messageId){
                    messageBox.remove(message);
                    break;
                }
            }
        }

//      A method that receives an authToken and a messageId and returns true if the messageId is valid, otherwise returns false
        private boolean messageIdIsValid(int authToken,int messageId) {
            Account acc = getAccount(authToken);
            ArrayList<Message> messageBox = acc.getMessageBox();
            boolean exists = false;
            for(Message message: messageBox){
                if (message.getId() == messageId) {
                    exists = true;
                    break;
                }
            }
            return exists;
        }

//      A method that receives an authToken and a messageId and returns a string of the following format: (<sender>) <message>
        private String getFormattedMessage(int authToken,int messageId){
            Account acc = getAccount(authToken);
            ArrayList<Message> messageBox = acc.getMessageBox();
            String formattedMessage = null;
            for(Message message: messageBox){
                if (message.getId() == messageId) {
//                  Mark message as read
                    message.isRead=true;
//                  Format message as (<sender>)<message>
                    formattedMessage = "("+message.sender+")" + " " + message.body;
                    break;
                }
            }
            return formattedMessage;
        }

//      A method that receives an Auth Token and returns the username related to that token. Returns -1 if there is no related username
        private String getUsername(int authToken) {
            for (Account account:accounts){
                if(authToken == account.getAuthToken()) {
                    return account.getUsername();
                }
            }
            return "-1";
        }

//      A method that receives an Auth Token and returns the corresponding Account. Returns null if no corresponding Account is found
        private Account getAccount(int authToken){
            for(Account acc:accounts){
                if(authToken == acc.getAuthToken()){
                    return acc;
                }
            }
            System.out.println("getAccount method failed.Returning null");
            return null;
        }

//      A method that receives a recipient and return true if the recipient exists, otherwise returns false
        private boolean recipientExists(String recipient) {
            for (Account account:accounts){
                if(recipient.equals(account.getUsername())) {
                    return true;
                }
            }
            return false;
        }

//      A method that receives an Auth Token and returns true if it is related to an existing account, otherwise returns false
        public static boolean tokenIsValid(int authToken){
            for (Account account:accounts){
                if(account.getAuthToken()==authToken) {
                    return true;
                }
            }
            return false;
        }

    }


    public static class Account implements Serializable {

        // Constructor
        public Account(String username) {
            this.username=username;
        }

        // Properties
        private String username;
        private int authToken;
        private ArrayList<Message> messageBox = new ArrayList<>();

        // A method to add a message to the messageBox
        public void addMessage(Message message){
            messageBox.add(message);
        }

        // Setters
        public void setAuthToken(int authToken) {
            this.authToken = authToken;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        //Getters
        public int getAuthToken() {
            return authToken;
        }

        public String getUsername() {
            return username;
        }

        public ArrayList<Message> getMessageBox() {
            return messageBox;
        }
    }


    public static class Message implements Serializable {

        // Constructor
        public Message(boolean isRead,String sender,String receiver,String body){
            this.isRead=isRead;
            this.sender=sender;
            this.receiver=receiver;
            this.body=body;
            this.id = nextId++; // assign the next available id
        }

        // Properties
        private boolean isRead;
        private String sender;
        private String receiver;
        private String body;

        private static int nextId = 1; // static filed to track the next available id
        private int id;

        // Setters
        public void setId(int id) {
            this.id = id;
        }

        public void setIsRead(boolean isRead){
            this.isRead = isRead;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public void setReceiver(String receiver) {
            this.receiver = receiver;
        }

        public void setSender(String sender) {
            this.sender = sender;
        }

        // Getters
        public int getId() {
            return id;
        }

        public boolean getIsRead(){
            return isRead;
        }

        public String getBody() {
            return body;
        }

        public String getReceiver() {
            return receiver;
        }

        public String getSender() {
            return sender;
        }
    }

}
