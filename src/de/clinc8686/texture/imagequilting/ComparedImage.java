package de.clinc8686.texture.imagequilting;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class ComparedImage {
    BufferedImage image;
    double difference;

    ComparedImage(BufferedImage image, double difference) {
        this.difference = difference;
        this.image = image;
    }
}
