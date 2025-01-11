package hampter.logic;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import imageLineExtractor.LineCalculator;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.WritableImage;
import javafx.scene.robot.Robot;

public class ScreenShot {

    public static int[][] takeScreenShot() {
        // Robot robot = new Robot();
        // WritableImage screenCapture = robot.getScreenCapture(null, new
        // Rectangle2D(538, 119, 1381 - 538, 960 - 119));
        // BufferedImage image = SwingFXUtils.fromFXImage(screenCapture, null);
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File("input.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        int[][] grid = fillOutGrid(image);
        grid = detectSameColors(grid);
        return grid;
    }

    private static int[][] fillOutGrid(BufferedImage image) {
        int[][] lines = LineCalculator.calculateLines(image, 1, false);
        int squareSize = LineCalculator.getSquareSize(lines);
        int[][] grid = new int[(int) image.getHeight() / squareSize][(int) image.getWidth() / squareSize];
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
        int isRelevantColor = 0;
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
                    isRelevantColor++;
                }
            }
        }
        redAverage /= squareSize * squareSize;
        greenAverage /= squareSize * squareSize;
        blueAverage /= squareSize * squareSize;

        return isRelevantColor > 50 ? redAverage << 16 | greenAverage << 8 | blueAverage : -2;
    }

    private static int[][] cleanupGrid(int[][] grid) {
        int removeFromTop = 0;
        outer: for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if (grid[i][j] != -2) {
                    break outer;
                }
            }
            removeFromTop++;
        }
        int removeFromLeft = 0;
        int removeFromRight = 0;
        boolean removeLeft = true;
        outer: for (int i = 0; i < grid[0].length; i++) {
            for (int j = 0; j < grid.length; j++) {
                if (grid[j][i] != -2) {
                    removeLeft = false;
                    continue outer;
                }
            }
            if (removeLeft) {
                removeFromLeft++;
            } else {
                removeFromRight++;
            }
        }
        for (int i = 0; i < grid[0].length; i++) {
            boolean setToBlack = false;
            for (int j = 0; j < grid.length; j++) {
                if (grid[j][i] != -2) {
                    setToBlack = true;
                } else {
                    if (setToBlack) {
                        grid[j][i] = -1;
                    }
                }
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
        int threshold = 20;
        ArrayList<ArrayList<int[]>> colors = new ArrayList<>();
        for (int i = grid.length - 1; i >= 0; i--) {
            outer: for (int j = 0; j < grid[i].length; j++) {
                if (grid[i][j] == -1) {
                    continue;
                }
                int rgb = grid[i][j];
                int red = (rgb & 0xFF0000) >> 16;
                int green = (rgb & 0xFF00) >> 8;
                int blue = rgb & 0xFF;
                for (ArrayList<int[]> colorShades : colors) {
                    for (int[] colorShade : colorShades) {
                        int shadeRGB = colorShade[0];
                        int red1 = (shadeRGB & 0xFF0000) >> 16;
                        int green1 = (shadeRGB & 0xFF00) >> 8;
                        int blue1 = shadeRGB & 0xFF;
                        if (Math.abs(red - red1) + Math.abs(green - green1) + Math.abs(blue - blue1) < threshold) {
                            colorShades.add(new int[] { rgb, i, j });
                            continue outer;
                        }
                    }
                }
                ArrayList<int[]> newColorShade = new ArrayList<>();
                newColorShade.add(new int[] { rgb, i, j });
                colors.add(newColorShade);
            }
        }
        ArrayList<int[]> tooSmallArrayList = isOneArrayListWithLessThanThreeElements(colors);
        while (tooSmallArrayList != null) {
            outer: for (int i = tooSmallArrayList.size() - 1; i >= 0; i--) {
                int rgb = tooSmallArrayList.get(i)[0];
                int red = (rgb & 0xFF0000) >> 16;
                int green = (rgb & 0xFF00) >> 8;
                int blue = rgb & 0xFF;
                for (ArrayList<int[]> colorShades : colors) {
                    for (int[] colorShade : colorShades) {
                        int shadeRGB = colorShade[0];
                        int red1 = (shadeRGB & 0xFF0000) >> 16;
                        int green1 = (shadeRGB & 0xFF00) >> 8;
                        int blue1 = shadeRGB & 0xFF;
                        if (Math.abs(red - red1) + Math.abs(green - green1) + Math.abs(blue - blue1) < threshold) {
                            colorShades.add(tooSmallArrayList.get(i));
                            tooSmallArrayList.remove(i);
                            continue outer;
                        }
                    }
                }
            }
            if (tooSmallArrayList.isEmpty()) {
                tooSmallArrayList = isOneArrayListWithLessThanThreeElements(colors);
                threshold = 10;
            } else {
                threshold += 1;
            }
        }
        int[][] newGrid = new int[grid.length][grid[0].length];
        for (ArrayList<int[]> color : colors) {
            int rgb = color.get(0)[0];
            for (int[] cords : color) {
                newGrid[cords[1]][cords[2]] = rgb;
            }
        }
        return newGrid;
    }

    private static ArrayList<int[]> isOneArrayListWithLessThanThreeElements(ArrayList<ArrayList<int[]>> colors) {
        for (ArrayList<int[]> color : colors) {
            if (color.size() < 3) {
                colors.remove(color);
                return color;
            }
        }
        return null;
    }
}