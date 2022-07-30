package de.hochschuletrier.docu.imagequilting;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class ComparedImage {
    public BufferedImage image;
    public double difference;

    ComparedImage(BufferedImage image, double difference) {
        this.difference = difference;
        this.image = image;
    }
}
