package de.clinc8686.texture.imagequilting;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Random;

public class Main {
    private static final int randomImageHeight = 16;
    private static final int randomImageWidth = 16;
    private static final int endImageHeight = 192;
    private static final int endImageWidth = 192;
    private static final int patchSize = 4;
    private static BufferedImage inputImage;
    private static BufferedImage endImage;

    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();
        inputImage = ImageIO.read(new File("C:\\Users\\Mario\\OneDrive\\Dokumente\\Programmierung-Privat\\ImageQuilting\\src\\de\\clinc8686\\texture\\imagequilting\\texture_input.jpg"));
        endImage = new BufferedImage(endImageHeight, endImageWidth, BufferedImage.TYPE_INT_RGB);
        ImageQuilting();
        showImage(endImage);

        long end = System.currentTimeMillis();
        NumberFormat formatter = new DecimalFormat("#0.00000");
        System.out.print("Execution time is " + formatter.format((end - start) / 1000d) + " seconds");
    }

    public static void ImageQuilting() {
        int[][] inputImagePixels = getImagePixels(inputImage);
        showImage(inputImage);
        BufferedImage startImage = randomisedImage(inputImagePixels);
        showImage(startImage);

        ArrayList<BufferedImage> allPixelBlocks = getAllPixelBlocks(inputImage);
        ArrayList<BufferedImage> endImageList = new ArrayList<>();
        boolean firstLine = true;
        int toFilledBlocksWidth = (int) (endImageWidth / randomImageWidth);
        int toFilledBlocksHeight = (int) (endImageHeight / randomImageHeight);
        for (int x = 0; x < toFilledBlocksWidth; x++) {
            int yBlock = x*randomImageHeight;
            for (int y = 0; y < toFilledBlocksHeight; y++) {
                int xBlock = x*randomImageWidth;
                int[][] topImagePixels = new int[0][];
                if (!firstLine) {
                    topImagePixels = getImagePixels(endImageList.get(endImageList.size()-toFilledBlocksWidth));
                }
                BufferedImage bestImage = compare(allPixelBlocks, inputImagePixels, topImagePixels, firstLine);
                int[][] bestPixels = getImagePixels(bestImage);
                inputImagePixels = bestPixels;
                endImageList.add(bestImage);

                for (int yPixelBlock = 0; yPixelBlock < randomImageWidth; yPixelBlock++) {
                    endImage.setRGB(xBlock, yPixelBlock+yBlock, bestImage.getRGB(0,yPixelBlock));
                    for (int xPixelBlock = 0; xPixelBlock < randomImageHeight; xPixelBlock++) {
                        endImage.setRGB(xPixelBlock+xBlock, yPixelBlock+yBlock, bestImage.getRGB(xPixelBlock,yPixelBlock));
                    }
                }
            }
            firstLine = false;
        }
    }

    public static BufferedImage compare(ArrayList<BufferedImage> allPixelBlocks, int[][] inputImagePixels, int[][] topImagePixels, boolean firstLine) {
        ArrayList<ComparedImage> comparedImages = new ArrayList<>();
        int minimalError = 0;
        double error = 0;
        for (BufferedImage image : allPixelBlocks) {
            for (int secondImageX = 0; secondImageX < patchSize; secondImageX++) {
                int firstImageX = randomImageWidth - secondImageX - 1;

                for (int y = 0; y < randomImageHeight; y++) {
                    Color firstImagePixelColor = new Color(inputImagePixels[firstImageX][y]);
                    int firImageRed = firstImagePixelColor.getRed();
                    int firImageGreen = firstImagePixelColor.getGreen();
                    int firImageBlue = firstImagePixelColor.getBlue();

                    Color secondImagePixelColor = new Color(image.getRGB(secondImageX,y));
                    int secImageRed = secondImagePixelColor.getRed();
                    int secImageGreen = secondImagePixelColor.getGreen();
                    int secImageBlue = secondImagePixelColor.getBlue();

                    int redPow = (firImageRed - secImageRed) * (firImageRed - secImageRed);
                    int greenPow = (firImageGreen - secImageGreen) * (firImageGreen - secImageGreen);
                    int bluePow = (firImageBlue - secImageBlue) * (firImageBlue - secImageBlue);
                    double difference = Math.sqrt(redPow + greenPow + bluePow);
                    error = error + difference;
                }
            }

            for (int firstImageY = 0; firstImageY < patchSize && !firstLine; firstImageY++) {
                int secondImageY = randomImageHeight-firstImageY - 1;

                for (int x = 0; x < patchSize; x++) {
                    Color firstImagePixelColor = new Color(topImagePixels[x][firstImageY]);
                    int firImageRed = firstImagePixelColor.getRed();
                    int firImageGreen = firstImagePixelColor.getGreen();
                    int firImageBlue = firstImagePixelColor.getBlue();

                    Color secondImagePixelColor = new Color(image.getRGB(x,secondImageY));
                    int secImageRed = secondImagePixelColor.getRed();
                    int secImageGreen = secondImagePixelColor.getGreen();
                    int secImageBlue = secondImagePixelColor.getBlue();

                    int redPow = (firImageRed - secImageRed) * (firImageRed - secImageRed);
                    int greenPow = (firImageGreen - secImageGreen) * (firImageGreen - secImageGreen);
                    int bluePow = (firImageBlue - secImageBlue) * (firImageBlue - secImageBlue);
                    double difference = Math.sqrt(redPow + greenPow + bluePow);
                    error = error + difference;
                }
            }
            comparedImages.add(new ComparedImage(image, error));
        }

        return chooseLowestError(comparedImages);
    }

    private static BufferedImage chooseLowestError(ArrayList<ComparedImage> comparedImages) {
        double min = Double.MAX_VALUE;
        int counter = 0, bestImage = 0;
        for (ComparedImage comIm : comparedImages) {
            if (comIm.difference < min) {
                min = comIm.difference;
                bestImage = counter;
            }
            counter++;
        }

        return comparedImages.get(bestImage).image;
    }

    private static ArrayList<BufferedImage> getAllPixelBlocks(BufferedImage inputImage) {
        int[][] inputImagePixels = getImagePixels(inputImage);
        int distanceToBorderXAxis = inputImagePixels.length - randomImageWidth;
        int distanceToBorderYAxis = inputImagePixels[0].length - randomImageHeight;

        ArrayList<BufferedImage> allPixelBlocks = new ArrayList<>();
        for (int ix = 0; ix < distanceToBorderXAxis; ix++) {
            for (int iy = 0; iy < distanceToBorderYAxis; iy++) {
                BufferedImage startImage = new BufferedImage(randomImageWidth, randomImageHeight, BufferedImage.TYPE_INT_RGB);
                for (int x = 0; x < randomImageWidth; x++) {
                    for (int y = 0; y < randomImageHeight; y++) {
                        Color color = new Color(inputImagePixels[x+ix][y+iy]);
                        startImage.setRGB(x, y, color.getRGB());
                    }
                }
                allPixelBlocks.add(startImage);
            }
        }
        return allPixelBlocks;  //size = 2304
    }

    private static void showImage(BufferedImage image) {
        JFrame frame = new JFrame();
        frame.getContentPane().setLayout(new FlowLayout());
        frame.getContentPane().add(new JLabel(new ImageIcon(image)));
        frame.pack();
        frame.setVisible(true);
    }

    private static BufferedImage randomisedImage(int[][] inputPixels) {
        BufferedImage startImage = new BufferedImage(randomImageWidth, randomImageHeight, BufferedImage.TYPE_INT_RGB);
        Random rand = new Random();
        int startXValue = rand.nextInt(inputPixels.length-randomImageWidth);
        int startYValue = rand.nextInt(inputPixels[0].length-randomImageHeight);

        int wStartImage = 0, hStartImage = 0;
        for (int w = startXValue; w < startXValue+randomImageWidth; w++) {
            for (int h = startYValue; h < startYValue+randomImageHeight; h++) {
                Color color = new Color(inputPixels[w][h]);
                startImage.setRGB(wStartImage, hStartImage, color.getRGB());
                hStartImage++;
            }
            hStartImage = 0;
            wStartImage++;
        }
        return startImage;
    }

    private static int[][] getImagePixels(BufferedImage inputImage) {
        int imageHeight = inputImage.getHeight();
        int imageWidth = inputImage.getWidth();

        int[][] pixels = new int[imageWidth][imageHeight];

        for (int w = 0; w < imageWidth; w++) {
            for (int h = 0; h < imageWidth; h++) {
                pixels[w][h] = inputImage.getRGB(w, h);
            }
        }
        return pixels;
    }
}
