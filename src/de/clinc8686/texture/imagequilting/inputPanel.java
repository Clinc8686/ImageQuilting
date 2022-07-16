package de.clinc8686.texture.imagequilting;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class inputPanel extends JPanel {
    JLabel input;
    inputPanel() {
        super();
        this.setBorder(BorderFactory.createTitledBorder("Input"));
        input = new JLabel();
        this.add(input, Component.CENTER_ALIGNMENT);
    }

    public void printImage(BufferedImage inputImage) {
        ImageIcon ii = new ImageIcon(inputImage);
        input.setIcon(ii);
        this.validate();
        this.repaint();
    }
}
