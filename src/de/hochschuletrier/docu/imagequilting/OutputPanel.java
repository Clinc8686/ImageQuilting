package de.hochschuletrier.docu.imagequilting;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class OutputPanel extends JPanel {
    /**
     * Constructor defines the border around it.
     */
    public OutputPanel() {
        super();
        this.setBorder(BorderFactory.createTitledBorder("Output"));
    }

    /**
     * Prints the given image on the JPanel.
     *
     * @param endImage The image which should be printed.
     */
    public void printImage(BufferedImage endImage) {
        JLabel output = new JLabel(new ImageIcon(endImage));
        this.add(output, Component.CENTER_ALIGNMENT);
        this.validate();
        this.repaint();
    }
}
