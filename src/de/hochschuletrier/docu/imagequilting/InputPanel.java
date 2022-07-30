package de.hochschuletrier.docu.imagequilting;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class InputPanel extends JPanel {
    private JLabel input;

    InputPanel() {
        super();
        this.setBorder(BorderFactory.createTitledBorder("Input"));
        this.setLayout(new FlowLayout());
        input = new JLabel();
        this.add(input, Component.CENTER_ALIGNMENT);
    }

    public void printImage(BufferedImage inputImage) {
        input = new JLabel(new ImageIcon(inputImage));
        this.add(input, Component.CENTER_ALIGNMENT);
        this.validate();
        this.repaint();
    }

    public void printFirstBlock(BufferedImage firstBlock) {
        JLabel label = new JLabel("<html></br>Random choosed first top, left Block: </br></html>", SwingConstants.CENTER);
        this.add(label, Component.CENTER_ALIGNMENT);
        input = new JLabel(new ImageIcon(firstBlock));
        this.add(input, Component.CENTER_ALIGNMENT);
        this.validate();
        this.repaint();
    }
}
