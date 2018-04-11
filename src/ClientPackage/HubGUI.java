package ClientPackage;

import javax.swing.*;
import java.awt.*;

/**
 * Created by anton on 2018-02-14.
 */
public class HubGUI {
    private JPanel mainPanel;
    private JPanel scrollViewContainer;
    private JScrollPane scrollPane;
    private JFrame frame;
    private Client client;
    private int COLUMNS;

    public HubGUI(Client c, String user) {
        client = c;
        frame = new JFrame("HubGUI - " + user);

        scrollViewContainer = new JPanel();
        scrollViewContainer.setPreferredSize(new Dimension(700, 500));

        COLUMNS = (int)Math.floor(scrollViewContainer.getPreferredSize().getWidth() / UserForm.getWIDTH());

        /* Calculate padding between UserCards */
        int padding = (int)Math.floor((scrollViewContainer.getPreferredSize().getWidth() - UserForm.getWIDTH() * COLUMNS) / (COLUMNS + 1));
        scrollViewContainer.setLayout(new FlowLayout(FlowLayout.LEFT, padding, padding));
        scrollPane.getViewport().add(scrollViewContainer);


        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void addUser(String s) {
        UserForm userForm = new UserForm(client, s);
        scrollViewContainer.add(userForm.getMainPanel());
        frame.pack();
    }
}
