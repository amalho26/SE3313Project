import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.List;
import javax.swing.*;

class Client extends JFrame {

    private static final long serialVersionUID = 1L;
    private JFrame frame;
    private JTextField clientTypingBoard;
    private JList clientActiveUsersList;
    private JTextArea clientMessageBoard;
    private JButton exit;
    private JRadioButton select;
    private JRadioButton all;
    private JButton send = new JButton("Send");
    private JLabel active = new JLabel("Active Users");

    DataInputStream input;
    DataOutputStream output;
    DefaultListModel<String> dm;
    String id, selectedIds = "";


	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Client window = new Client();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}



    public Client() {
        initialize();
    }

    public Client(String id, Socket s) {
        initialize();
        this.id = id;
        try {
            frame.setTitle("Client: " + id);
            dm = new DefaultListModel<String>();
            clientActiveUsersList.setModel(dm);
            input = new DataInputStream(s.getInputStream()); //set I/O streams
            output = new DataOutputStream(s.getOutputStream());
            new Read().start(); //create a new thread for reading messages
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    class Read extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    String m = input.readUTF();
                    if (m.contains(":;.,/=")) {
                        m = m.substring(6); // comma separated all active user ids
                        dm.clear();
                        StringTokenizer st = new StringTokenizer(m, ","); // split all the clientIds and add to dm below
                        while (st.hasMoreTokens()) {
                            String u = st.nextToken();
                            if (!id.equals(u))
                                dm.addElement(u);
                        }
                    } else {
                        clientMessageBoard.append("" + m + "\n"); //otherwise print on the clients message board
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    private void initialize() { // initialize all the components of UI
        frame = new JFrame();
        frame.setBounds(100, 100, 480, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);
        frame.setTitle("Client");

        clientMessageBoard = new JTextArea();
        clientMessageBoard.setEditable(false);
        clientMessageBoard.setBounds(10, 30, 350, 400);
        frame.getContentPane().add(clientMessageBoard);

        clientTypingBoard = new JTextField();
        clientTypingBoard.setHorizontalAlignment(SwingConstants.LEFT);
        clientTypingBoard.setBounds(10, 450, 350, 60);
        frame.getContentPane().add(clientTypingBoard);
        clientTypingBoard.setColumns(10);


        send.setBounds(370, 450, 100, 25);
        frame.getContentPane().add(send);

        clientActiveUsersList = new JList();
        clientActiveUsersList.setToolTipText("Active Users");
        clientActiveUsersList.setBounds(370, 60, 100, 320);
        frame.getContentPane().add(clientActiveUsersList);

        exit = new JButton("Exit"); //kill process
        exit.setBounds(370, 480, 100, 25);
        frame.getContentPane().add(exit);

        active.setHorizontalAlignment(SwingConstants.LEFT);
        active.setBounds(370, 30, 100, 15);
        frame.getContentPane().add(active);

        select = new JRadioButton("Select Users");
        select.addActionListener(e -> clientActiveUsersList.setEnabled(true));
        select.setSelected(true);
        select.setBounds(370, 390, 100, 25);
        frame.getContentPane().add(select);

        all = new JRadioButton("Send to All");
        all.addActionListener(e -> clientActiveUsersList.setEnabled(false));
        all.setBounds(370, 410, 100, 25);
        frame.getContentPane().add(all);

        ButtonGroup group = new ButtonGroup(); //group radio buttons together
        group.add(select);
        group.add(all);

        exit.addActionListener(e -> {
            try {
                output.writeUTF("exit"); // closes the thread and show the message on server and client's message
                frame.dispose(); // close the frame
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });

        send.addActionListener(e -> {
            String userInput = clientTypingBoard.getText(); // get the message from textbox
            if (userInput != null && !userInput.isEmpty()) {  // only if message is not empty then send it further otherwise do nothing
                try {
                    String msgToServer = "";
                    String cast = "broadcast"; // this will be an identifier to identify type of message
                    int flag = 0; // flag used to check whether used has selected any client or not for multicast

                    if (select.isSelected()) { //if select Users
                        cast = "multicast";
                        List<String> clientList = clientActiveUsersList.getSelectedValuesList(); // get all the users selected on UI
                        if (clientList.size() == 0) //set flag if no one is selected
                            flag = 1;
                        for (String selectedUsr : clientList) { //append all the usernames selected in a variable
                            if (selectedIds.isEmpty())
                                selectedIds += selectedUsr;
                            else
                                selectedIds += ", " + selectedUsr;
                        }
                        msgToServer = cast + ":" + selectedIds + ":" + userInput; //preparing message to be sent to server
                    } else
                        msgToServer = cast + ":" + userInput; //sending to all



                    if (cast.equalsIgnoreCase("multicast")) {
                        if (flag == 1) { //if no one is selected
                            JOptionPane.showMessageDialog(frame, "No user selected");
                        } else { //send message
                            output.writeUTF(msgToServer);
                            clientTypingBoard.setText("");
                            clientMessageBoard.append("<You to " + selectedIds + ">" + userInput + "\n");
                        }
                    } else { //else send to everyone
                        output.writeUTF(msgToServer);
                        clientTypingBoard.setText("");
                        clientMessageBoard.append("<You to All> " + userInput + "\n");
                    }
                    selectedIds = ""; // clear variable
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        frame.setVisible(true);
    }
}