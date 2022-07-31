package de.hochschuletrier.docu.imagequilting;

import javax.swing.*;
import java.awt.*;

public class MainUI extends JPanel {
    /**
     * Start of the Program. Defines the frame with the panels on it.
     */
    public static void main(String[] args) {
        JFrame mainFrame = new JFrame("Image Quilting");
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        mainFrame.getContentPane().setLayout(new BorderLayout());
        JPanel mainPanel = new JPanel();
        GridLayout gridLayout = new GridLayout(0,3);
        mainPanel.setLayout(gridLayout);
        mainFrame.add(mainPanel);

        InputPanel ip = new InputPanel();
        OutputPanel ouP = new OutputPanel();
        OptionsPanel opP = new OptionsPanel(ip, ouP);

        mainPanel.add(ouP);
        mainPanel.add(ip);
        mainPanel.add(opP);

        mainFrame.setVisible(true);
    }
}
