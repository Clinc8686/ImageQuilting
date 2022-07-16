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

public class ImageQuilting {
    /*
    randomImageHeight and randomImageWidth defines the block size of each contiguous image (patches).
    endImageHeight and endImageWidth defines the final image size of the endImage.
    patchSize is the compared pixel width between the patch blocks.
    inputImage is the original image.
    endImage is the synthesized texture.
     */
    public int randomImageHeight = 32;
    public int randomImageWidth = 32;
    public int endImageHeight = 192;
    public int endImageWidth = 192;
    public int overlapSize = 3;
    public BufferedImage inputImage;
    public BufferedImage endImage;

    /*
    main
     */
    ImageQuilting(int randomImageHeight, int randomImageWidth, int endImageHeight, int endImageWidth, int overlapSize) throws IOException {
        long start = System.currentTimeMillis();
        String pathString = System.getProperty("user.dir");
        inputImage = ImageIO.read(new File(pathString+"\\src\\de\\clinc8686\\texture\\imagequilting\\texture_input.jpg"));

        this.randomImageHeight = randomImageHeight;
        this.randomImageWidth = randomImageWidth;
        this.endImageHeight = endImageHeight;
        this.endImageWidth = endImageWidth;
        this.overlapSize = overlapSize;

        endImage = new BufferedImage(endImageHeight, endImageWidth, BufferedImage.TYPE_INT_ARGB);
        ImageQuiltingWithCut();
        //ImageIO.write(endImage, "png", new File(pathString+"\\src\\de\\clinc8686\\texture\\imagequilting\\output_image.png"));

        long end = System.currentTimeMillis();
        NumberFormat formatter = new DecimalFormat("#0.00000");
        System.out.print("Execution time is " + formatter.format((end - start) / 1000d) + " seconds");
    }

    public void ImageQuiltingWithCut() {
        boolean firstImage = true;
        int[][] inputImagePixels = getImagePixels(inputImage);
        BufferedImage startImage = randomisedImage(inputImagePixels);
        inputImagePixels = getImagePixels(startImage);
        BufferedImage bestImage = startImage;

        ArrayList<BufferedImage> allPixelBlocks = getAllPixelBlocks(inputImage);
        ArrayList<BufferedImage> endImageList = new ArrayList<>();
        boolean firstLine = true;
        int toFilledBlocksWidth = (int) Math.ceil(endImageWidth / (double) (randomImageWidth-overlapSize));
        int toFilledBlocksHeight = (int) Math.ceil(endImageHeight / (double) (randomImageHeight-overlapSize));
        for (int yOuterLoop = 0; yOuterLoop < toFilledBlocksHeight; yOuterLoop++) {
            int yPixelBlockPosition = yOuterLoop*randomImageWidth;
            for (int xOuterLoop = 0; xOuterLoop < toFilledBlocksWidth; xOuterLoop++) {
                int xPixelBlockPosition = xOuterLoop*randomImageHeight;
                ArrayList<Coords> bestCutCoordsLR;
                ArrayList<Coords> bestCutCoordsTD = new ArrayList<>();
                if (!firstImage) {
                    int[][] topImagePixels = new int[0][];
                    if (!firstLine) {
                        topImagePixels = getImagePixels(endImageList.get(endImageList.size()-toFilledBlocksWidth));
                        //Change bottom right pixels, to get the newest pixels
                        for (int xTopLeft = xPixelBlockPosition-(overlapSize*xOuterLoop)-overlapSize+randomImageWidth, x = 0; xTopLeft < xPixelBlockPosition-(overlapSize*xOuterLoop)+randomImageWidth; xTopLeft++, x++) {
                            for (int yTopLeft = yPixelBlockPosition-(overlapSize*yOuterLoop)-overlapSize+randomImageHeight, y = randomImageHeight-overlapSize; yTopLeft < yPixelBlockPosition-(overlapSize*yOuterLoop)+randomImageHeight; yTopLeft++, y++) {
                                if (xTopLeft < endImageHeight && yTopLeft < endImageWidth)
                                    topImagePixels[x][y] = endImage.getRGB(xTopLeft, yTopLeft);
                            }
                        }
                    }
                    bestImage = compare(allPixelBlocks, inputImagePixels, topImagePixels, firstLine);
                    inputImagePixels = getImagePixels(bestImage);

                    int[][] leftImagePixels = getImagePixels(endImageList.get(endImageList.size()-1));
                    bestCutCoordsLR = cutOverlapLeft(leftImagePixels, inputImagePixels);

                    if (!firstLine) {
                        bestCutCoordsTD = cutOverlapTop(topImagePixels, inputImagePixels);
                    }

                    //First left pixel block in row
                    BufferedImage concatBlock = bestImage;
                    Graphics combinedImage = endImage.getGraphics();
                    if (!firstLine && xPixelBlockPosition == 0) {
                        int startPositionTD = yPixelBlockPosition-(overlapSize*yOuterLoop);
                        concatTopDownBlock(bestCutCoordsTD, concatBlock);
                        combinedImage.drawImage(concatBlock, 0, startPositionTD, null);
                        combinedImage.dispose();
                    } else {
                        if (firstLine) {
                            concatLeftRightBlock(xOuterLoop, xPixelBlockPosition, bestCutCoordsLR, concatBlock, combinedImage, 0);
                        } else {
                            int startPositionTD = yPixelBlockPosition-(overlapSize*yOuterLoop);
                            concatTopDownBlock(bestCutCoordsTD, concatBlock);
                            concatLeftRightBlock(xOuterLoop, xPixelBlockPosition, bestCutCoordsLR, concatBlock, combinedImage, startPositionTD);
                        }
                    }
                //First image top left or first left pixel block in row
                } else {
                    firstImage = false;
                    concatSimplePatch(bestImage, xPixelBlockPosition, yPixelBlockPosition);
                }
                endImageList.add(bestImage);
            }
            firstLine = false;
        }
    }

    /*
    Crops the best cut on the horizontal from the best matching image: The not matching pixels will be set to transparent.
     */
    private void concatTopDownBlock(ArrayList<Coords> bestCutCoordsTD, BufferedImage concatBlock) {
        for (int xInnerLoop = 0; xInnerLoop < randomImageWidth; xInnerLoop++) {
            for (int yInnerLoop = 0; yInnerLoop <= bestCutCoordsTD.get(xInnerLoop).y; yInnerLoop++) {
                Color col = new Color(0, 0, 0, 0);
                concatBlock.setRGB(xInnerLoop, yInnerLoop, col.getRGB());
            }
        }
    }

    /*
    Crops the best cut on the vertical from the best matching image: The not matching pixels will be set to transparent.
    It also prints the patch Block on the endImage.
     */
    private void concatLeftRightBlock(int xOuterLoop, int xPixelBlockPosition, ArrayList<Coords> bestCutCoordsLR, BufferedImage concatBlock, Graphics combinedImage, int startPositionTD) {
        int startPositionLR = xPixelBlockPosition-(overlapSize*xOuterLoop);
        for (int yInnerLoop = 0; yInnerLoop < randomImageHeight; yInnerLoop++) {
            for (int xInnerLoop = 0; xInnerLoop <= bestCutCoordsLR.get(yInnerLoop).x; xInnerLoop++) {
                Color col = new Color(0, 0, 0, 0);
                concatBlock.setRGB(xInnerLoop, yInnerLoop, col.getRGB());
            }
        }

        combinedImage.drawImage(concatBlock, startPositionLR, startPositionTD, null);
        combinedImage.dispose();
    }

    /*
    Prints a patch Block on the endImage.
    This has to happen only in the first top left patch block.
    */
    private void concatSimplePatch(BufferedImage bestImage, int xPixelBlockPosition, int yPixelBlockPosition) {
        for (int yInnerLoop = 0; yInnerLoop < randomImageHeight; yInnerLoop++) {
            endImage.setRGB(xPixelBlockPosition, yInnerLoop+yPixelBlockPosition, bestImage.getRGB(0,yInnerLoop));
            for (int xInnerLoop = 0; xInnerLoop < randomImageWidth; xInnerLoop++) {
                endImage.setRGB(xInnerLoop+xPixelBlockPosition, yInnerLoop+yPixelBlockPosition, bestImage.getRGB(xInnerLoop,yInnerLoop));
            }
        }
    }

    /*
    Calculates the best cut path between the top and bottom image
     */
    private ArrayList<Coords> cutOverlapTop(int[][] topImagePixels, int[][] bestImagePixels) {
        BufferedImage overlap = new BufferedImage(randomImageWidth, overlapSize, BufferedImage.TYPE_INT_ARGB);
        for (int firstImageY = 0; firstImageY < overlapSize; firstImageY++) {
            int secondImageY = overlapSize - firstImageY - 1;
            for (int x = 0; x < randomImageHeight; x++) {
                Color firstImagePixelColor = new Color(topImagePixels[x][firstImageY]);
                Color secondImagePixelColor = new Color(bestImagePixels[x][secondImageY]);
                int difference = calculateAverageDifference(firstImagePixelColor, secondImagePixelColor);
                overlap.setRGB(x, firstImageY, new Color(difference, difference, difference).getRGB());
            }
        }

        overlap = rotateImage(overlap);
        ArrayList<Coords> rotatedBestCoords = findBestPath(overlap);
        return rotateCoords(rotatedBestCoords);
    }

    /*
    Rotates the Coords back
     */
    private ArrayList<Coords> rotateCoords(ArrayList<Coords> coords) {
        ArrayList<Coords> newCoords = new ArrayList<>();
        for (Coords co : coords) {
            newCoords.add(new Coords(co.y, co.x));
        }
        return newCoords;
    }

    /*
    Rotates the given Image 90 degrees and returns it
     */
    private BufferedImage rotateImage(BufferedImage image) {
        int height = image.getHeight();
        int width = image.getWidth();
        BufferedImage rotatedImage = new BufferedImage(height, width, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                rotatedImage.setRGB(height - j - 1, i, image.getRGB(i, j));

        return rotatedImage;
    }

    /*
    Calculates the best cut path between the left and right image.
     */
    private ArrayList<Coords> cutOverlapLeft(int[][] leftImagePixels, int[][] bestImagePixels) {
        /*
        Calculate the difference between the left and right pixels and convert it into grayscale pixels.
        From the grayscaled Pixels it creates a new smaller image, which has the size from the overlapSize * randomImageHeight.
         */
        BufferedImage overlap = new BufferedImage(overlapSize, randomImageHeight, BufferedImage.TYPE_INT_ARGB);
        for (int secondImageX = 0; secondImageX < overlapSize; secondImageX++) {
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

    /*
    Defines the best path through the grayscales image.
     */
    private ArrayList<Coords> findBestPath(BufferedImage image) {
        ArrayList<Coords> bestCoords;
        ArrayList<ArrayList<Coords>> coordsList = new ArrayList<>();
        int height = image.getHeight();
        int width = image.getWidth();
        int startPoint = 0;
        int[] costs = new int[width];

        //Checks from all first column rows the path length
        for (int row = 0; row < width; row++) {
            coordsList.add(new ArrayList<>());
            int tmpColor = new Color(image.getRGB(row,0)).getGreen();
            costs[row] = costs[row] + tmpColor;
            coordsList.get(row).add(new Coords(startPoint, 0));
            int lastXPos = row;

            for (int depth = 1; depth < height; depth++) {
                int color1 = 0, color2 = 0, color3 = 0;
                if (lastXPos != 0) {
                    color1 = new Color(image.getRGB((lastXPos-1),depth)).getGreen();
                }
                color2 = new Color(image.getRGB(lastXPos,depth)).getGreen();
                if (lastXPos < (width-1)) {
                    color3 = new Color(image.getRGB(lastXPos+1,depth)).getGreen();
                }

                if (color1 > color2) {
                    coordsList.get(row).add(new Coords((lastXPos-1),depth));
                    lastXPos = lastXPos - 1;
                    tmpColor = color1;
                } else if (color2 > color3 || lastXPos >= (width-1)) {
                    coordsList.get(row).add(new Coords(lastXPos,depth));
                    lastXPos = lastXPos;
                    tmpColor = color2;
                } else {
                    coordsList.get(row).add(new Coords((lastXPos+1),depth));
                    lastXPos = lastXPos + 1;
                    tmpColor = color3;
                }
                costs[row] = costs[row] + tmpColor;
            }
        }

        //Choose the shortest path from the List
        int best = 0;
        int highestError = 0;
        for (int i = 0; i < width; i++) {
            if (costs[i] > highestError) {
                highestError = costs[i];
                best = i;
            }
        }
        bestCoords = coordsList.get(best);
        return bestCoords;
    }

    /*
    Search for best matching image and returns it.
     */
    public BufferedImage compare(ArrayList<BufferedImage> allPixelBlocks, int[][] inputImagePixels, int[][] topImagePixels, boolean firstLine) {
        ArrayList<ComparedImage> comparedImages = new ArrayList<>();
        double error = 0;
        for (BufferedImage image : allPixelBlocks) {
            for (int secondImageX = 0; secondImageX < overlapSize; secondImageX++) {
                int firstImageX = randomImageWidth - secondImageX - 1;
                for (int y = 0; y < randomImageHeight; y++) {
                    Color firstImagePixelColor = new Color(inputImagePixels[firstImageX][y]);
                    Color secondImagePixelColor = new Color(image.getRGB(secondImageX,y));
                    double difference = calculateDifference(firstImagePixelColor, secondImagePixelColor);
                    error = error + difference;
                }
            }

            for (int firstImageY = 0; firstImageY < overlapSize && !firstLine; firstImageY++) {
                int secondImageY = randomImageHeight - firstImageY - 1;
                for (int x = 0; x < overlapSize; x++) {
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
    Returns the grayscaled color from two rgb Pixels based on the difference.
     */
    private int calculateAverageDifference(Color firstColor, Color secondColor) {
        double first = (firstColor.getRed() + firstColor.getBlue() + firstColor.getGreen()) / 3.0;
        double second = (secondColor.getRed() + secondColor.getBlue() + secondColor.getGreen()) / 3.0;
        return (int) Math.sqrt(Math.pow((first-second), 2));
    }

    /*
    Returns the difference between two rgb Pixels.
     */
    private double calculateDifference(Color firstColor, Color secondColor) {
        return Math.sqrt(Math.pow((firstColor.getRed()-secondColor.getRed()), 2) + Math.pow((firstColor.getBlue() - secondColor.getBlue()), 2) + Math.pow((firstColor.getGreen()-secondColor.getGreen()), 2));
    }

    /*
    Compares all overlap errors and returns the best.
     */
    private BufferedImage chooseLowestError(ArrayList<ComparedImage> comparedImages) {
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
    private ArrayList<BufferedImage> getAllPixelBlocks(BufferedImage inputImage) {
        int[][] inputImagePixels = getImagePixels(inputImage);
        int distanceToBorderXAxis = inputImagePixels.length - randomImageWidth;
        int distanceToBorderYAxis = inputImagePixels[0].length - randomImageHeight;

        ArrayList<BufferedImage> allPixelBlocks = new ArrayList<>();
        for (int ix = 0; ix < distanceToBorderXAxis; ix++) {
            for (int iy = 0; iy < distanceToBorderYAxis; iy++) {
                BufferedImage startImage = new BufferedImage(randomImageWidth, randomImageHeight, BufferedImage.TYPE_INT_ARGB);
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
    private void showImage(BufferedImage image) {
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
    private BufferedImage randomisedImage(int[][] inputPixels) {
        BufferedImage startImage = new BufferedImage(randomImageWidth, randomImageHeight, BufferedImage.TYPE_INT_ARGB);
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
    private int[][] getImagePixels(BufferedImage inputImage) {
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
