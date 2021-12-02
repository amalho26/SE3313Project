import java.awt.*;
import java.io.*;
import java.net.Socket;
import javax.swing.*;


class Login extends JFrame{

    private JFrame f = new JFrame();
    private JTextField id = new JTextField();
    private JButton login = new JButton();
    private JLabel label = new JLabel();
    private Socket s;
    private int port = 8818;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {

            Login frame = new Login();
            frame.f.setVisible(true);

        });
    }

    public Login() { //constructor
        initialize();
    }

    private void initialize() {

        f.setBounds(100, 100, 400, 250);//setting Frame components
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().setLayout(null);
        f.setTitle("Login Portal");

        id.setBounds(125, 75, 200, 25);//setting Textfield components
        f.getContentPane().add(id);
        id.setColumns(10);

        login.setText("Login");
        login.setFont(new Font("Tahoma", Font.PLAIN, 17));//setting Button components
        login.setBounds(125, 120, 100, 50);
        f.getContentPane().add(login);

        label.setText("Enter ID:");
        label.setFont(new Font("Tahoma", Font.PLAIN, 17));//setting Label components
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBounds(10, 62, 132, 47);
        f.getContentPane().add(label);

        login.addActionListener(e -> {

            try{

                s = new Socket("localhost", port); // create socket
                DataInputStream input = new DataInputStream(s.getInputStream()); //create I/O stream
                DataOutputStream output = new DataOutputStream(s.getOutputStream());
                output.writeUTF(id.getText());
                String serverMessage = new DataInputStream(s.getInputStream()).readUTF(); //receive message on socket

                if(serverMessage.equals("ID exists. Please enter a new ID."))
                    JOptionPane.showMessageDialog(f,  "ID exists. Please enter a new ID.");
                else{
                    new Client(id.getText(), s); //if id is unique, create a new Client thread + close login frame
                    f.dispose();
                }
            }catch(Exception er) {
                er.printStackTrace();
            }

        });

    }
}