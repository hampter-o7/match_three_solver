package hampter.java.logic;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import hampter.java.util.BoardState;
import hampter.java.util.Swap;

public class PuzzleSolutionFileManager {
    private static final String PUZZLE_SOLUTION_SAVE_FILE = "puzzle-solution.txt";

    public static void createPuzzleSolutionFile() throws IOException {
        File file = new File("match3data.json");
        List<String> inputs = Files.readAllLines(file.toPath());
        long startTime = System.nanoTime();
        StringBuilder puzzleSolutionFileString = new StringBuilder();
        for (int i = 1; i < inputs.size() - 1; i++) {
            String[] input = inputs.get(i).split(":")[1].replaceAll("\"", "").replaceAll(" ", "").split(",");
            byte[][] puzzle = new byte[input.length][input[0].length()];
            ArrayList<Character> colors = new ArrayList<>();
            colors.add('#');
            colors.add('.');
            for (int j = 0; j < input.length; j++) {
                for (int k = 0; k < input[j].length(); k++) {
                    char color = input[j].charAt(k);
                    int number = colors.indexOf(color);
                    if (number == -1) {
                        colors.add(color);
                        number = colors.size() - 1;
                    }
                    puzzle[j][k] = (byte) (number - 1);
                }
            }
            puzzle = cleanupPuzzle(puzzle);
            ArrayList<Swap> swaps = Logic.solveBoard(puzzle, new HashMap<>());
            BoardState puzzleState = new BoardState(puzzle, null, null);
            puzzleSolutionFileString.append(puzzleState.hashCode() + ":");
            for (Swap swap : swaps) {
                puzzleSolutionFileString.append(swap.toSaveFile() + " ");
            }
            puzzleSolutionFileString.append('\n');
        }
        saveSolutions(puzzleSolutionFileString);
        System.out.println("Execution time of solving: " + ((System.nanoTime() - startTime) / 1_000_000) + " ms");
    }

    private static byte[][] cleanupPuzzle(byte[][] grid) {
        int removeFromTop = -1;
        outer: for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if (grid[i][j] > 0) {
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
                if (grid[j][i] == -1) {
                    grid[j][i] = 0;
                }
                if (grid[j][i] > 0) {
                    removeLeft = false;
                    setToBlack = true;
                } else if (setToBlack) {
                    grid[j][i] = -1;
                }
            }
            if (setToBlack) {
                continue;
            } else if (removeLeft) {
                removeFromLeft++;
            } else {
                removeFromRight++;
            }
        }

        byte[][] newGrid = new byte[grid.length - removeFromTop][grid[0].length - removeFromLeft - removeFromRight];
        for (int i = 0; i < newGrid.length; i++) {
            for (int j = 0; j < newGrid[i].length; j++) {
                newGrid[i][j] = grid[i + removeFromTop][j + removeFromLeft];
            }
        }
        return newGrid;
    }

    private static void saveSolutions(StringBuilder content) throws IOException {
        File file = new File(PUZZLE_SOLUTION_SAVE_FILE);
        FileWriter writer = new FileWriter(file);
        writer.write(content.toString());
        writer.close();
    }

    public static HashMap<Integer, ArrayList<Swap>> readSolutions() throws IOException {
        File file = new File(PUZZLE_SOLUTION_SAVE_FILE);
        HashMap<Integer, ArrayList<Swap>> solutions = new HashMap<>();
        for (String line : Files.readAllLines(file.toPath())) {
            String[] splitLine = line.split(":");
            int hash = Integer.parseInt(splitLine[0]);
            ArrayList<Swap> swaps = new ArrayList<>();
            for (String solution : splitLine[1].split(" ")) {
                swaps.add(new Swap(solution));
            }
            solutions.put(hash, swaps);
        }
        return solutions;
    }
}
