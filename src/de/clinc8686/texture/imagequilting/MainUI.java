package de.clinc8686.texture.imagequilting;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class MainUI extends JPanel {
    public static void main(String[] args) throws IOException {
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
