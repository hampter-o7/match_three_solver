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

    private static int backgroundColor = 0;
    private static int immovableColor = 0;

    public static int[][] takeScreenShot(boolean isTest, int background, int immovable) {
        backgroundColor = background;
        immovableColor = immovable;
        BufferedImage image = null;
        if (isTest) {
            try {
                image = ImageIO.read(new File("input2.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Robot robot = new Robot();
            WritableImage screenCapture = robot.getScreenCapture(null,
                    new Rectangle2D(538, 119, 1381 - 538, 960 - 119));
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
                redAverage += red;
                greenAverage += green;
                blueAverage += blue;
                if (red > threshold && green > threshold && blue > threshold) {
                    isRelevant++;
                }
            }
        }
        redAverage /= squareSize * squareSize;
        greenAverage /= squareSize * squareSize;
        blueAverage /= squareSize * squareSize;

        return isRelevant > isRelevantThreshold ? redAverage << 16 | greenAverage << 8 | blueAverage : backgroundColor;
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
        printGrid(grid);
        int maxThreshold = 40;
        ArrayList<ArrayList<Integer>> combinedColors = new ArrayList<>();
        for (int i = grid.length - 1; i >= 0; i--) {
            for (int j = 0; j < grid[i].length; j++) {
                int rgb = grid[i][j];
                if (rgb == immovableColor || rgb == backgroundColor) {
                    continue;
                }
                int minDifference = maxThreshold;
                int group = -1;
                for (int k = 0; k < combinedColors.size(); k++) {
                    ArrayList<Integer> combined = combinedColors.get(k);
                    for (int color : combined) {
                        int difference = calculateRGBdifference(rgb, color);
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
                } else {
                    grid[i][j] = combinedColors.get(group).get(0);
                    combinedColors.get(group).add(rgb);
                }
            }
        }
        return grid;
    }

    private static int calculateRGBdifference(int rgb1, int rgb2) {
        int diffR = Math.abs(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF));
        int diffG = Math.abs(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF));
        int diffB = Math.abs((rgb1 & 0xFF) - (rgb2 & 0xFF));
        return (int) Math.sqrt(diffR * diffR + diffG * diffG + diffB * diffB);
    }

    @SuppressWarnings("unused")
    private static void printGrid(int[][] grid) {
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                System.out.print(String.format("%06X ", grid[i][j]));
            }
            System.out.println();
        }
    }
}