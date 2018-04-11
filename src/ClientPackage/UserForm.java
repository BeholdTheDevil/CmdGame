package ClientPackage;

import ClientPackage.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by anton on 2018-02-14.
 */
public class UserForm {
    private JButton button1;
    private JTextField textField1;
    private JPanel mainPanel;
    private Client client;
    private static final int HEIGHT = 100;
    private static final int WIDTH = 200;


    public UserForm(Client c, String s) {
        client = c;
        textField1.setText(s);
        textField1.setHorizontalAlignment(JTextField.CENTER);
        textField1.setEditable(false);
        mainPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        mainPanel.setBorder(BorderFactory.createLineBorder(new Color(51, 51, 51), 2, true));
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                client.challenger(textField1.getText());
            }
        });
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public static int getWIDTH() {
        return WIDTH;
    }

    public static int getHEIGHT() {
        return HEIGHT;
    }
}
