package hampter.logic;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;

import imageLineExtractor.LineCalculator;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.WritableImage;
import javafx.scene.robot.Robot;

public class ScreenShot {

    private static final int LEFT_UPPER_SCREENSHOT_X = 538;
    private static final int LEFT_UPPER_SCREENSHOT_Y = 119;
    private static final int SCREENSHOT_WIDTH = 1381 - LEFT_UPPER_SCREENSHOT_X;
    private static final int SCREENSHOT_HEIGHT = 960 - LEFT_UPPER_SCREENSHOT_Y;

    private static int backgroundColor = 0;
    private static int immovableColor = 0;

    public static int[][] takeScreenShot(boolean isTest, int background, int immovable) {
        backgroundColor = background;
        immovableColor = immovable;
        BufferedImage image = null;
        if (isTest) {
            try {
                image = ImageIO.read(new File("input1.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Robot robot = new Robot();
            WritableImage screenCapture = robot.getScreenCapture(null,
                    new Rectangle2D(LEFT_UPPER_SCREENSHOT_X, LEFT_UPPER_SCREENSHOT_Y, SCREENSHOT_WIDTH,
                            SCREENSHOT_HEIGHT));
            image = SwingFXUtils.fromFXImage(screenCapture, null);
        }
        int[][] grid = fillOutGrid(image);
        grid = detectSameColors(grid);
        return grid;
    }

    private static int[][] fillOutGrid(BufferedImage image) {
        int[][] lines = LineCalculator.calculateLines(image, 1, false);
        int squareSize = LineCalculator.getSquareSize(lines);
        int[][] grid = new int[(int) image.getHeight() / squareSize][(int) image.getWidth() / squareSize];
        for (int i = 0; i < grid.length; i++) {
            Arrays.fill(grid[i], backgroundColor);
        }
        int horizontal = lines[0][lines[0].length - 1];
        int vertical = lines[1][0];
        int startJ = -vertical / squareSize;
        for (int i = 1; horizontal - i * squareSize > 0; i++) {
            int y = horizontal - i * squareSize;
            for (int j = startJ; vertical + (j + 1) * squareSize < image.getWidth(); j++) {
                int x = vertical + j * squareSize;
                grid[grid.length - i][j - startJ] = calculateAverageColor(image, x, y, squareSize);
            }
        }
        grid = cleanupGrid(grid);
        return grid;
    }

    private static int calculateAverageColor(BufferedImage image, int x, int y, int squareSize) {
        int threshold = 220;
        int redAverage = 0;
        int greenAverage = 0;
        int blueAverage = 0;
        int isRelevant = 0;
        int isRelevantThreshold = squareSize * squareSize / 400;
        for (int i = y; i < y + squareSize; i++) {
            for (int j = x; j < x + squareSize; j++) {
                int rgb = image.getRGB(j, i);
                int red = (rgb & 0xFF0000) >> 16;
                int green = (rgb & 0xFF00) >> 8;
                int blue = rgb & 0xFF;
                if (red > threshold && green > threshold && blue > threshold) {
                    isRelevant++;
                    continue;
                }
                redAverage += red;
                greenAverage += green;
                blueAverage += blue;
            }
        }
        redAverage /= squareSize * squareSize;
        greenAverage /= squareSize * squareSize;
        blueAverage /= squareSize * squareSize;

        return isRelevant > isRelevantThreshold ? redAverage << 16 | greenAverage << 8 | blueAverage : backgroundColor;
    }

    public static int increaseContrast(int rgb, double contrastFactor) {
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = rgb & 0xff;
        r = clamp((int) (contrastFactor * (r - 128) + 128));
        g = clamp((int) (contrastFactor * (g - 128) + 128));
        b = clamp((int) (contrastFactor * (b - 128) + 128));
        return (r << 16) | (g << 8) | b;
    }

    public static int clamp(int val) {
        return Math.max(0, Math.min(255, val));
    }

    private static int[][] cleanupGrid(int[][] grid) {
        int removeFromTop = -1;
        outer: for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if (grid[i][j] != backgroundColor) {
                    removeFromTop = i;
                    break outer;
                }
            }
        }
        int removeFromLeft = 0;
        int removeFromRight = 0;
        boolean removeLeft = true;
        for (int i = 0; i < grid[0].length; i++) {
            boolean setToBlack = false;
            for (int j = 0; j < grid.length; j++) {
                if (grid[j][i] != backgroundColor) {
                    removeLeft = false;
                    setToBlack = true;
                } else if (setToBlack) {
                    grid[j][i] = immovableColor;
                }
            }
            if (grid[grid.length - 1][i] != backgroundColor) {
                continue;
            } else if (removeLeft) {
                removeFromLeft++;
            } else {
                removeFromRight++;
            }
        }

        int[][] newGrid = new int[grid.length - removeFromTop][grid[0].length - removeFromLeft - removeFromRight];
        for (int i = 0; i < newGrid.length; i++) {
            for (int j = 0; j < newGrid[i].length; j++) {
                newGrid[i][j] = grid[i + removeFromTop][j + removeFromLeft];
            }
        }
        return newGrid;
    }

    private static int[][] detectSameColors(int[][] grid) {
        int maxThreshold = 10;
        ArrayList<ArrayList<Integer>> combinedColors = new ArrayList<>();
        for (int i = grid.length - 1; i >= 0; i--) {
            for (int j = 0; j < grid[i].length; j++) {
                int rgb = grid[i][j];
                if (rgb == immovableColor || rgb == backgroundColor) {
                    continue;
                }
                double minDifference = maxThreshold;
                int group = -1;
                for (int k = 0; k < combinedColors.size(); k++) {
                    ArrayList<Integer> combined = combinedColors.get(k);
                    for (int color : combined) {
                        double difference = returnCiedeDifference(rgb, color);
                        if (difference < minDifference) {
                            minDifference = difference;
                            group = k;
                        }
                    }
                }
                if (group == -1) {
                    ArrayList<Integer> newCombinedColor = new ArrayList<>();
                    newCombinedColor.add(rgb);
                    combinedColors.add(newCombinedColor);
                    group = combinedColors.size() - 1;
                } else {
                    combinedColors.get(group).add(rgb);
                }
                grid[i][j] = increaseContrast(combinedColors.get(group).get(0), 1.3f);
            }
        }
        return grid;
    }

    @Deprecated
    @SuppressWarnings(value = { "unused" })
    private static int calculateRgbDifference(int rgb1, int rgb2) {
        int diffR = Math.abs(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF));
        int diffG = Math.abs(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF));
        int diffB = Math.abs((rgb1 & 0xFF) - (rgb2 & 0xFF));
        return (int) Math.sqrt(diffR * diffR + diffG * diffG + diffB * diffB);
    }

    private static double returnCiedeDifference(int rgb1, int rgb2) {
        double[] lab1 = rgbToLab(rgb1);
        double[] lab2 = rgbToLab(rgb2);
        return calculateColorDifferenceCiede(lab1[0], lab1[1], lab1[2], lab2[0], lab2[1], lab2[2]);
    }

    private static double[] rgbToLab(int rgb) {
        double r = pivotRgb((rgb >> 16 & 0xFF) / 255.0);
        double g = pivotRgb((rgb >> 8 & 0xFF) / 255.0);
        double b = pivotRgb((rgb & 0xFF) / 255.0);

        double x = pivotXyz((r * 0.4124 + g * 0.3576 + b * 0.1805) / 95.047);
        double y = pivotXyz((r * 0.2126 + g * 0.7152 + b * 0.0722) / 100.000);
        double z = pivotXyz((r * 0.0193 + g * 0.1192 + b * 0.9505) / 108.883);

        return new double[] { 116 * y - 16, 500 * (x - y), 200 * (y - z) };
    }

    private static double pivotRgb(double n) {
        return (n > 0.04045 ? Math.pow((n + 0.055) / 1.055, 2.4) : n / 12.92) * 100;
    }

    private static double pivotXyz(double n) {
        double i = Math.cbrt(n);
        return n > 0.008856 ? i : 7.787 * n + 16 / 116;
    }

    private static double calculateColorDifferenceCiede(double L1, double a1, double b1, double L2, double a2,
            double b2) {
        double LMean = (L1 + L2) / 2.0;
        double C1 = Math.sqrt(a1 * a1 + b1 * b1);
        double C2 = Math.sqrt(a2 * a2 + b2 * b2);
        double CMean = (C1 + C2) / 2.0;

        double G = (1 - Math.sqrt(Math.pow(CMean, 7) / (Math.pow(CMean, 7) + Math.pow(25, 7)))) / 2;
        double a1Prime = a1 * (1 + G);
        double a2Prime = a2 * (1 + G);

        double C1Prime = Math.sqrt(a1Prime * a1Prime + b1 * b1);
        double C2Prime = Math.sqrt(a2Prime * a2Prime + b2 * b2);
        double CMeanPrime = (C1Prime + C2Prime) / 2;

        double h1Prime = Math.atan2(b1, a1Prime) + 2 * Math.PI * (Math.atan2(b1, a1Prime) < 0 ? 1 : 0);
        double h2Prime = Math.atan2(b2, a2Prime) + 2 * Math.PI * (Math.atan2(b2, a2Prime) < 0 ? 1 : 0);
        double HMeanPrime = ((Math.abs(h1Prime - h2Prime) > Math.PI) ? (h1Prime + h2Prime + 2 * Math.PI) / 2
                : (h1Prime + h2Prime) / 2);

        double T = 1.0 - 0.17 * Math.cos(HMeanPrime - Math.PI / 6.0) + 0.24 * Math.cos(2 * HMeanPrime)
                + 0.32 * Math.cos(3 * HMeanPrime + Math.PI / 30) - 0.2 * Math.cos(4 * HMeanPrime - 21 * Math.PI / 60);

        double deltahPrime = ((Math.abs(h1Prime - h2Prime) <= Math.PI) ? h2Prime - h1Prime
                : (h2Prime <= h1Prime) ? h2Prime - h1Prime + 2 * Math.PI : h2Prime - h1Prime - 2 * Math.PI);

        double deltaLPrime = L2 - L1;
        double deltaCPrime = C2Prime - C1Prime;
        double deltaHPrime = 2.0 * Math.sqrt(C1Prime * C2Prime) * Math.sin(deltahPrime / 2.0);
        double SL = 1.0 + ((0.015 * (LMean - 50) * (LMean - 50)) / (Math.sqrt(20 + (LMean - 50) * (LMean - 50))));
        double SC = 1.0 + 0.045 * CMeanPrime;
        double SH = 1.0 + 0.015 * CMeanPrime * T;

        double deltaTheta = (30 * Math.PI / 180)
                * Math.exp(-((180 / Math.PI * HMeanPrime - 275) / 25) * ((180 / Math.PI * HMeanPrime - 275) / 25));
        double RC = (2 * Math.sqrt(Math.pow(CMeanPrime, 7) / (Math.pow(CMeanPrime, 7) + Math.pow(25, 7))));
        double RT = (-RC * Math.sin(2 * deltaTheta));

        double KL = 1;
        double KC = 1;
        double KH = 1;

        double deltaE = Math.sqrt(
                ((deltaLPrime / (KL * SL)) * (deltaLPrime / (KL * SL))) +
                        ((deltaCPrime / (KC * SC)) * (deltaCPrime / (KC * SC))) +
                        ((deltaHPrime / (KH * SH)) * (deltaHPrime / (KH * SH))) +
                        (RT * (deltaCPrime / (KC * SC)) * (deltaHPrime / (KH * SH))));

        return deltaE;
    }

    @SuppressWarnings(value = { "unused" })
    private static void printGrid(int[][] grid) {
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                System.out.print(String.format("%06X ", grid[i][j]));
            }
            System.out.println();
        }
    }
}