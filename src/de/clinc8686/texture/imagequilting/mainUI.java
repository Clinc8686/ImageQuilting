package de.clinc8686.texture.imagequilting;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class mainUI extends JPanel {
    public static void main(String[] args) throws IOException {
        JFrame mainFrame = new JFrame("Image Quilting");
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        mainFrame.getContentPane().setLayout(new BorderLayout());
        JPanel mainPanel = new JPanel();
        GridLayout gridLayout = new GridLayout(0,3);
        mainPanel.setLayout(gridLayout);
        mainFrame.add(mainPanel);

        inputPanel ip = new inputPanel();
        outputPanel ouP = new outputPanel();
        optionsPanel opP = new optionsPanel(ip, ouP);

        mainPanel.add(ouP);
        mainPanel.add(ip);
        mainPanel.add(opP);

        mainFrame.setVisible(true);
    }
}
