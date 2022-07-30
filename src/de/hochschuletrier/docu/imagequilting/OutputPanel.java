package de.hochschuletrier.docu.imagequilting;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class OutputPanel extends JPanel {
    private JLabel output;

    public OutputPanel() {
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
