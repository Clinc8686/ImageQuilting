package de.clinc8686.texture.imagequilting;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class outputPanel extends JPanel {
    JLabel output;

    public outputPanel() {
        super();
        this.setBorder(BorderFactory.createTitledBorder("Output"));
    }

    public void printImage(BufferedImage endImage) {
        output = new JLabel(new ImageIcon(endImage));
        this.add(output, Component.CENTER_ALIGNMENT);
        this.validate();
        this.repaint();
    }
}
