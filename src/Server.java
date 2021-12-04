import java.awt.EventQueue;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.*;

class Server {


    private static final long serialVersionUID = 1L;
    private static Map<String, Socket> allClients = new ConcurrentHashMap<>();
    private static Set<String> activeUserSet = new HashSet<>();
    private static int port = 8818;
    private JFrame frame;
    private ServerSocket serverSocket;
    private JTextArea serverMsg;
    private JList activeClientList;
    private DefaultListModel<String> activeDlm = new DefaultListModel<String>();


    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                Server window = new Server();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public Server() {
        initialize();
        try {
            serverSocket = new ServerSocket(port);  // create a socket for server
            serverMsg.append("Server started on port: " + port + "\n"); // print messages to server message board
            serverMsg.append("Waiting for the clients.\n");
            new ClientAccept().start(); // this will create a thread for client
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class ClientAccept extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    Socket cSocket = serverSocket.accept();  // create client socket
                    String id = new DataInputStream(cSocket.getInputStream()).readUTF(); // this will receive the username sent from client register view
                    DataOutputStream cOutput = new DataOutputStream(cSocket.getOutputStream()); // create output stream for client

                    if (activeUserSet != null && activeUserSet.contains(id))
                        cOutput.writeUTF("ID exists. Please enter a new ID");
                     else {
                        allClients.put(id, cSocket); // add new user to allUserList and activeUserSet
                        activeUserSet.add(id);
                        cOutput.writeUTF(""); // clear the existing message
                        activeDlm.addElement(id); // add this user to the active user JList


                        activeClientList.setModel(activeDlm); // show the active and allUser List to the swing app in JList

                        serverMsg.append("ID: " + id + " is connected.\n"); // print message on server that new client has been connected.
                        new MsgRead(cSocket, id).start(); // create a thread to read messages
                        new PrepareCLientList().start(); //create a thread to update all the active clients
                    }
                } catch (IOException ioE) {
                    ioE.printStackTrace();
                }
            }
        }
    }

    class MsgRead extends Thread { // this class reads the messages coming from client and take appropriate actions
        Socket s;
        String cId;

        private MsgRead(Socket s, String id) { // socket and username will be provided by client
            this.s = s;
            this.cId = id;
        }

        @Override
        public void run() {
            while (!allClients.isEmpty()) {  // if allUserList is not empty
                try {
                    String msg = new DataInputStream(s.getInputStream()).readUTF(); // read message from client
                    String[] msgList = msg.split(":");

                    if (msgList[0].equalsIgnoreCase("multicast")) {
                        String[] sendToList = msgList[1].split(", "); //this variable contains list of clients which will receive message
                        for (String usr : sendToList) {

                                if (activeUserSet.contains(usr)) { // check again if user is active then send the message
                                    new DataOutputStream(((Socket) allClients.get(usr)).getOutputStream())
                                            .writeUTF("<" + cId + "> " + msgList[2]); // put message in output stream
                                }
                        }
                    } else if (msgList[0].equalsIgnoreCase("broadcast")) { // if broadcast then send message to all active clients

                        Iterator<String> itr1 = allClients.keySet().iterator(); // iterate over all users
                        while (itr1.hasNext()) {
                            String usrName = (String) itr1.next();
                            if (!usrName.equalsIgnoreCase(cId)) {
                                new DataOutputStream(((Socket) allClients.get(usrName)).getOutputStream()).writeUTF("<" + cId + "> " + msgList[1]);
                            }
                        }
                    } else if (msgList[0].equalsIgnoreCase("exit")) { // if a client's process is killed then notify other clients
                        activeUserSet.remove(cId);
                        serverMsg.append(cId + " disconnected.\n");

                        new PrepareCLientList().start();

                        Iterator<String> itr = activeUserSet.iterator();
                        while (itr.hasNext()) {
                            String usrName2 = (String) itr.next();
                            if (!usrName2.equalsIgnoreCase(cId)) {
                                try {
                                    new DataOutputStream(((Socket) allClients.get(usrName2)).getOutputStream()).writeUTF(cId + " disconnected.");
                                } catch (Exception e) { // throw errors
                                    e.printStackTrace();
                                }
                                new PrepareCLientList().start(); // update the active user list for every client after a user is disconnected
                            }
                        }
                        activeDlm.removeElement(cId); // remove client from Jlist for server
                        activeClientList.setModel(activeDlm); //update the active user list
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class PrepareCLientList extends Thread { //prepares list of active users
        @Override
        public void run() {
            try {
                String ids = "";
                Iterator itr = activeUserSet.iterator();
                while (itr.hasNext()) {
                    String key = (String) itr.next();
                    ids += key + ",";
                }
                if (ids.length() != 0) {
                    ids = ids.substring(0, ids.length() - 1);
                }
                itr = activeUserSet.iterator();
                while (itr.hasNext()) { //iterate over all active users
                    String key = (String) itr.next();
                    try {
                        new DataOutputStream(((Socket) allClients.get(key)).getOutputStream()).writeUTF(":;.,/=" + ids); //set output stream and send the list of active users with identifier prefix :;.,/=
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 600, 480);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);
        frame.setTitle("Server");

        serverMsg = new JTextArea();
        serverMsg.setEditable(false);
        serverMsg.setBounds(10, 30, 350, 400);
        frame.getContentPane().add(serverMsg);
        serverMsg.setText("Starting the Server...\n");

        activeClientList = new JList();
        activeClientList.setBounds(400, 60, 150, 300);
        frame.getContentPane().add(activeClientList);

        JLabel lblNewLabel_1 = new JLabel("Active Users");
        lblNewLabel_1.setBounds(400, 30, 100, 20);
        frame.getContentPane().add(lblNewLabel_1);

    }
}