package de.clinc8686.texture.imagequilting;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

public class Main {
    private static final int randomImageHeight = 32;
    private static final int randomImageWidth = 32;
    private static final int endImageHeight = 192;
    private static final int endImageWidth = 192;
    private static final int patchSize = 8;
    private static BufferedImage inputImage;
    private static BufferedImage endImage;

    /*
    main
     */
    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();
        inputImage = ImageIO.read(new File("C:\\Users\\Mario\\OneDrive\\Dokumente\\Programmierung-Privat\\ImageQuilting\\src\\de\\clinc8686\\texture\\imagequilting\\texture_input.jpg"));
        endImage = new BufferedImage(endImageHeight, endImageWidth, BufferedImage.TYPE_INT_RGB);
        //ImageQuiltingWithBest();
        //showImage(endImage);
        ImageQuiltingWithCut();
        showImage(endImage);
        ImageIO.write(endImage, "jpg", new File("C:\\Users\\Mario\\OneDrive\\Dokumente\\Programmierung-Privat\\ImageQuilting\\src\\de\\clinc8686\\texture\\imagequilting\\output_image.jpg"));

        long end = System.currentTimeMillis();
        NumberFormat formatter = new DecimalFormat("#0.00000");
        System.out.print("Execution time is " + formatter.format((end - start) / 1000d) + " seconds");
    }

    /*

     */
    public static void ImageQuiltingWithBest() {
        boolean firstImage = true;
        int[][] inputImagePixels = getImagePixels(inputImage);
        showImage(inputImage);
        BufferedImage startImage = randomisedImage(inputImagePixels);
        inputImagePixels = getImagePixels(startImage);
        showImage(startImage);
        BufferedImage bestImage = startImage;

        ArrayList<BufferedImage> allPixelBlocks = getAllPixelBlocks(inputImage);
        ArrayList<BufferedImage> endImageList = new ArrayList<>();
        boolean firstLine = true;
        int toFilledBlocksWidth = (int) (endImageWidth / randomImageWidth);
        int toFilledBlocksHeight = (int) (endImageHeight / randomImageHeight);
        Graphics2D concatImage = endImage.createGraphics();
        for (int x = 0; x < toFilledBlocksWidth; x++) {
            int xBlock = x*randomImageHeight;
            for (int y = 0; y < toFilledBlocksHeight; y++) {
                int yBlock = y*randomImageWidth;
                if (!firstImage) {
                    int[][] topImagePixels = new int[0][];
                    if (!firstLine) {
                        topImagePixels = getImagePixels(endImageList.get(endImageList.size()-toFilledBlocksWidth));
                    }
                    bestImage = compare(allPixelBlocks, inputImagePixels, topImagePixels, firstLine);
                    int[][] bestPixels = getImagePixels(bestImage);
                    inputImagePixels = bestPixels;
                } else {
                    firstImage = false;
                }
                endImageList.add(bestImage);

                concatImage.drawImage(bestImage, xBlock, yBlock, null);
            }
            firstLine = false;
        }
        concatImage.dispose();
    }

    public static void ImageQuiltingWithCut() {
        boolean firstImage = true;
        int[][] inputImagePixels = getImagePixels(inputImage);
        showImage(inputImage);
        BufferedImage startImage = randomisedImage(inputImagePixels);
        inputImagePixels = getImagePixels(startImage);
        showImage(startImage);
        BufferedImage bestImage = startImage;

        ArrayList<BufferedImage> allPixelBlocks = getAllPixelBlocks(inputImage);
        ArrayList<BufferedImage> endImageList = new ArrayList<>();
        boolean firstLine = true;
        int toFilledBlocksWidth = (int) (endImageWidth / randomImageWidth);
        int toFilledBlocksHeight = (int) (endImageHeight / randomImageHeight);
        for (int xOuterLoop = 0; xOuterLoop < toFilledBlocksWidth; xOuterLoop++) {
            int xPixelBlockPosition = xOuterLoop*randomImageHeight;
            for (int yOuterLoop = 0; yOuterLoop < toFilledBlocksHeight; yOuterLoop++) {
                int yPixelBlockPosition = yOuterLoop*randomImageWidth;
                ArrayList<Coords> bestCutCoords = new ArrayList<>();
                if (!firstImage) {
                    int[][] topImagePixels = new int[0][];
                    if (!firstLine) {
                        topImagePixels = getImagePixels(endImageList.get(endImageList.size()-toFilledBlocksWidth));
                    }
                    bestImage = compare(allPixelBlocks, inputImagePixels, topImagePixels, firstLine);
                    int[][] bestPixels = getImagePixels(bestImage);
                    inputImagePixels = bestPixels;

                    int[][] leftImagePixels = new int[0][];
                    leftImagePixels = getImagePixels(endImageList.get(endImageList.size()-1));
                    bestCutCoords = cutOverlapLeft(leftImagePixels, inputImagePixels);

                    if (xPixelBlockPosition == 0) {
                        //endImageList.add(bestImage);
                        for (int yInnerLoop = 0; yInnerLoop < randomImageHeight; yInnerLoop++) {
                            endImage.setRGB(xPixelBlockPosition, yInnerLoop+yPixelBlockPosition, bestImage.getRGB(0,yInnerLoop));
                            for (int xInnerLoop = 0; xInnerLoop < randomImageWidth; xInnerLoop++) {
                                endImage.setRGB(xInnerLoop+xPixelBlockPosition, yInnerLoop+yPixelBlockPosition, bestImage.getRGB(xInnerLoop,yInnerLoop));
                            }
                        }
                    } else {
                        int xCutRightImage = 0;
                        int xCutLeftImage = 0;
                        for (int yInnerLoop = 0; yInnerLoop < randomImageHeight; yInnerLoop++) {
                            xCutRightImage = bestCutCoords.get(yInnerLoop).x;
                            int startPosition = xPixelBlockPosition-(patchSize*xOuterLoop);
                            for (int xInnerLoop = xCutRightImage; xInnerLoop < randomImageWidth; xInnerLoop++) {
                                endImage.setRGB(xInnerLoop+startPosition, yInnerLoop+yPixelBlockPosition, bestImage.getRGB(xInnerLoop,yInnerLoop));
                            }
                        }
                    }
                } else {
                    firstImage = false;
                    for (int yInnerLoop = 0; yInnerLoop < randomImageHeight; yInnerLoop++) {
                        endImage.setRGB(xPixelBlockPosition, yInnerLoop+yPixelBlockPosition, bestImage.getRGB(0,yInnerLoop));
                        for (int xInnerLoop = 0; xInnerLoop < randomImageWidth; xInnerLoop++) {
                            endImage.setRGB(xInnerLoop+xPixelBlockPosition, yInnerLoop+yPixelBlockPosition, bestImage.getRGB(xInnerLoop,yInnerLoop));
                        }
                    }
                }
                endImageList.add(bestImage);
            }
            firstLine = false;
        }
    }

    private static void cutOverlapTop(int[][] bestImagePixels, int[][] topImagePixels) {
        BufferedImage overlap = new BufferedImage(randomImageWidth, randomImageHeight, BufferedImage.TYPE_INT_RGB);
        for (int firstImageY = 0; firstImageY < patchSize; firstImageY++) {
            int secondImageY = randomImageHeight - firstImageY - 1;

            for (int x = 0; x < patchSize; x++) {
                Color firstImagePixelColor = new Color(topImagePixels[x][firstImageY]);
                Color secondImagePixelColor = new Color(bestImagePixels[x][secondImageY]);
                int difference = calculateAverageDifference(firstImagePixelColor, secondImagePixelColor);
                overlap.setRGB(x, firstImageY, new Color(difference, difference, difference).getRGB());
            }
        }

    }

    private static ArrayList<Coords> cutOverlapLeft(int[][] leftImagePixels, int[][] bestImagePixels) {
        BufferedImage overlap = new BufferedImage(patchSize, randomImageHeight, BufferedImage.TYPE_INT_RGB);
        for (int secondImageX = 0; secondImageX < patchSize; secondImageX++) {
            int firstImageX = randomImageHeight - secondImageX - 1;

            for (int y = 0; y < randomImageHeight; y++) {
                Color firstImagePixelColor = new Color(leftImagePixels[firstImageX][y]);
                Color secondImagePixelColor = new Color(bestImagePixels[secondImageX][y]);
                int difference = calculateAverageDifference(firstImagePixelColor, secondImagePixelColor);
                overlap.setRGB(secondImageX, y, new Color(difference, difference, difference).getRGB());
            }
        }
        return findBestPath(overlap);

    }

    private static ArrayList<Coords> findBestPath(BufferedImage image) {
        ArrayList<Coords> coordsList = new ArrayList<>();
        int height = image.getHeight();
        int width = image.getWidth();
        int startPoint = 0;
        int bestColor = 0;
        for (int i = 0; i < width; i++) {
            int tmpColor = new Color(image.getRGB(i,0)).getGreen();
            if (tmpColor > bestColor) {
                bestColor = tmpColor;
                startPoint = i;
            }
        }

        coordsList.add(new Coords(startPoint, 0));
        int lastXPos = startPoint;
        for (int depth = 1; depth < height; depth++) {
            int color1 = 0, color2, color3 = 0;
            if (lastXPos != 0) {
                color1 = new Color(image.getRGB((lastXPos-1),depth)).getGreen();
            }
            color2 = new Color(image.getRGB(lastXPos,depth)).getGreen();
            if (lastXPos == width) {
                color3 = new Color(image.getRGB(lastXPos+1,depth)).getGreen();
            }

            if (color1 > color2) {
                coordsList.add(new Coords((lastXPos-1),depth));
                lastXPos = lastXPos - 1;
            } else if (color2 > color3) {
                coordsList.add(new Coords(lastXPos,depth));
                lastXPos = lastXPos;
            } else {
                coordsList.add(new Coords((lastXPos+1),depth));
                lastXPos = lastXPos + 1;
            }
        }
        return coordsList;
    }

    public static BufferedImage compare(ArrayList<BufferedImage> allPixelBlocks, int[][] inputImagePixels, int[][] topImagePixels, boolean firstLine) {
        ArrayList<ComparedImage> comparedImages = new ArrayList<>();
        double error = 0;
        for (BufferedImage image : allPixelBlocks) {
            for (int secondImageX = 0; secondImageX < patchSize; secondImageX++) {
                int firstImageX = randomImageWidth - secondImageX - 1;

                for (int y = 0; y < randomImageHeight; y++) {
                    Color firstImagePixelColor = new Color(inputImagePixels[firstImageX][y]);
                    Color secondImagePixelColor = new Color(image.getRGB(secondImageX,y));
                    double difference = calculateDifference(firstImagePixelColor, secondImagePixelColor);
                    error = error + difference;
                }
            }

            for (int firstImageY = 0; firstImageY < patchSize && !firstLine; firstImageY++) {
                int secondImageY = randomImageHeight - firstImageY - 1;

                for (int x = 0; x < patchSize; x++) {
                    Color firstImagePixelColor = new Color(topImagePixels[x][firstImageY]);
                    Color secondImagePixelColor = new Color(image.getRGB(x,secondImageY));
                    double difference = calculateDifference(firstImagePixelColor, secondImagePixelColor);
                    error = error + difference;
                }
            }
            comparedImages.add(new ComparedImage(image, error));
            error = 0;
        }
        return chooseLowestError(comparedImages);
    }

    /*
    Returns the greyscaled color from two Pixels
     */
    private static int calculateAverageDifference(Color firstColor, Color secondColor) {
        double first = (firstColor.getRed() + firstColor.getBlue() + firstColor.getGreen()) / 3.0;
        double second = (secondColor.getRed() + secondColor.getBlue() + secondColor.getGreen()) / 3.0;
        return (int) Math.sqrt(Math.pow((first-second), 2));
    }

    private static double calculateDifference(Color firstColor, Color secondColor) {
        return Math.sqrt(Math.pow((firstColor.getRed()-secondColor.getRed()), 2) + Math.pow((firstColor.getBlue() - secondColor.getBlue()), 2) + Math.pow((firstColor.getGreen()-secondColor.getGreen()), 2));
    }

    /*
    Compares all overlap errors and returns the best.
     */
    private static BufferedImage chooseLowestError(ArrayList<ComparedImage> comparedImages) {
        double min = Double.MAX_VALUE;
        int counter = 0, bestImage = 0;
        for (ComparedImage comIm : comparedImages) {
            if (Double.compare(comIm.difference, min) < 0) {
                min = comIm.difference;
                bestImage = counter;
            }
            counter++;
        }

        return comparedImages.get(bestImage).image;
    }

    /*
    Runs through all pixels and slices each pixel with his neighbour pixels
    into a new block that can later be iterated.
     */
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
        return allPixelBlocks;
    }

    /*
    Prints the image as jframe.
     */
    private static void showImage(BufferedImage image) {
        JFrame frame = new JFrame();
        frame.getContentPane().setLayout(new FlowLayout());
        frame.getContentPane().add(new JLabel(new ImageIcon(image)));
        frame.pack();
        frame.setVisible(true);
    }

    /*
    Choose one random image from the input texture and use it as the start block.
    The size of the start block is defined by randomImageWidth and randomImageHeight.
     */
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

    /*
    Converts the image into his pixel rgb-values and returns it.
     */
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
