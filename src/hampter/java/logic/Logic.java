package hampter.java.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

import hampter.java.util.BoardState;
import hampter.java.util.Swap;

public class Logic {

    private static final int IMMOVABLE_BLOCK = -1;
    private static final int EMPTY_SPACE = 0;

    public static ArrayList<Swap> solveBoard(byte[][] board, HashMap<Integer, ArrayList<Swap>> solutions) {
        long startTime = System.nanoTime();
        ArrayList<Swap> bestSolution = checkSolutions(board, solutions);
        if (bestSolution == null) {
            bestSolution = new ArrayList<>();
            bestSolution = getSolution(board);
        }
        System.out.println("Execution time of solving: " + ((System.nanoTime() - startTime) / 1_000_000) + " ms");
        return bestSolution;
    }

    private static ArrayList<Swap> checkSolutions(byte[][] board, HashMap<Integer, ArrayList<Swap>> solutions) {
        BoardState boardState = new BoardState(board, null, null);
        return solutions.get(boardState.hashCode());
    }

    private static ArrayList<Swap> getSolution(byte[][] board) {
        HashSet<BoardState> memo = new HashSet<>();
        Queue<BoardState> queue = new LinkedList<>();
        queue.offer(new BoardState(board, null, null));

        while (!queue.isEmpty()) {
            BoardState currentBoardState = queue.poll();
            if (memo.contains(currentBoardState))
                continue;
            memo.add(currentBoardState);

            if (checkIfSolutionFound(currentBoardState.getBoard())) {
                ArrayList<Swap> solution = new ArrayList<>();
                currentBoardState.getSolution(solution);
                return solution;
            }

            byte[][] currentBoard = currentBoardState.getBoard();
            for (Swap swap : getPossibleSwaps(currentBoard)) {
                byte[][] newBoard = makeBoardCopy(currentBoard);
                swapNumbers(swap.getX(), swap.getY(), swap.isDown(), newBoard);
                collapseThreeInRowOrMore(newBoard);

                if (checkIfBoardIsUnsolvable(newBoard)) {
                    continue;
                }

                BoardState newBoardState = new BoardState(newBoard, swap, currentBoardState);
                if (memo.contains(newBoardState))
                    continue;

                queue.add(newBoardState);
            }
        }
        return null;
    }

    private static boolean checkIfSolutionFound(byte[][] board) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] > 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean checkIfBoardIsUnsolvable(byte[][] board) {
        boolean[][] seen = new boolean[board.length][board[0].length];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (board[i][j] > 0 && !seen[i][j]) {
                    ArrayList<Byte> group = new ArrayList<>();
                    findGroup(group, board, seen, i, j);
                    if (checkIfGroupIsUnsolvable(group)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static void findGroup(ArrayList<Byte> group, byte[][] board, boolean[][] seen, int x, int y) {
        if (seen[x][y] || board[x][y] < 1) {
            return;
        }
        seen[x][y] = true;
        group.add(board[x][y]);
        if (x > 0)
            findGroup(group, board, seen, x - 1, y);
        if (x < board.length - 1)
            findGroup(group, board, seen, x + 1, y);
        if (y > 0)
            findGroup(group, board, seen, x, y - 1);
        if (y < board[0].length - 1)
            findGroup(group, board, seen, x, y + 1);
    }

    private static boolean checkIfGroupIsUnsolvable(ArrayList<Byte> group) {
        Collections.sort(group, (a, b) -> a - b);
        byte currentNumber = group.get(0);
        int counter = 0;
        for (int i = 0; i < group.size(); i++) {
            if (currentNumber == group.get(i)) {
                counter++;
            } else {
                if (counter < 3) {
                    return true;
                }
                currentNumber = group.get(i);
                counter = 1;
            }
        }
        return false;
    }

    private static void collapseThreeInRowOrMore(byte[][] board) {
        boolean hasBoardChanged = true;
        while (hasBoardChanged) {
            hasBoardChanged = findAllInRowAndRemove(board) && dropAllNumbers(board);
        }
    }

    private static boolean isBoardSame(byte[][] otherBoard, byte[][] board) {
        for (int i = 0; i < otherBoard.length; i++) {
            for (int j = 0; j < otherBoard[0].length; j++) {
                if (board[i][j] != otherBoard[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean dropAllNumbers(byte[][] board) {
        boolean hasChanged = false;
        for (int j = 0; j < board[0].length; j++) {
            int counter = 0;
            for (int i = board.length - 1; i >= 0; i--) {
                while (i - counter >= 0 && board[i - counter][j] == EMPTY_SPACE) {
                    counter++;
                }
                if (i - counter >= 0 && board[i - counter][j] == IMMOVABLE_BLOCK) {
                    while (board[i][j] != IMMOVABLE_BLOCK) {
                        board[i][j] = EMPTY_SPACE;
                        i--;
                    }
                    counter = 0;
                    continue;
                }
                hasChanged = true;
                board[i][j] = i - counter >= 0 ? board[i - counter][j] : 0;
            }
        }
        return hasChanged;
    }

    private static boolean findAllInRowAndRemove(byte[][] board) {
        ArrayList<int[]> allRemovals = new ArrayList<>();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == EMPTY_SPACE || board[i][j] == IMMOVABLE_BLOCK) {
                    continue;
                }
                int currentNum = board[i][j];
                int x = i;
                int y = j;
                while (x > 0 && board[x - 1][y] == currentNum) {
                    x--;
                }
                int counterX = 0;
                while (x < board.length && board[x][y] == currentNum) {
                    counterX++;
                    x++;
                }
                if (counterX > 2) {
                    allRemovals.add(new int[] { i, j });
                    continue;
                }
                x = i;
                while (y > 0 && board[x][y - 1] == currentNum) {
                    y--;
                }
                int counterY = 0;
                while (y < board[0].length && board[x][y] == currentNum) {
                    counterY++;
                    y++;
                }
                if (counterY > 2) {
                    allRemovals.add(new int[] { i, j });
                }
            }
        }
        for (int[] removal : allRemovals) {
            board[removal[0]][removal[1]] = 0;
        }
        return !allRemovals.isEmpty();
    }

    private static ArrayList<Swap> getPossibleSwaps(byte[][] board) {
        ArrayList<Swap> possibleSwaps = new ArrayList<>();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == EMPTY_SPACE || board[i][j] == IMMOVABLE_BLOCK) {
                    continue;
                }
                if (i + 1 != board.length && board[i + 1][j] != IMMOVABLE_BLOCK && board[i][j] != board[i + 1][j]) {
                    swapNumbers(i, j, true, board);
                    if (checkIfAtLeastThreeInRow(board)) {
                        possibleSwaps.add(new Swap(i, j, true));
                    }
                    swapNumbers(i, j, true, board);
                }
                if (j + 1 != board[i].length && board[i][j + 1] != EMPTY_SPACE && board[i][j + 1] != IMMOVABLE_BLOCK
                        && board[i][j] != board[i][j + 1]) {
                    swapNumbers(i, j, false, board);
                    if (checkIfAtLeastThreeInRow(board)) {
                        possibleSwaps.add(new Swap(i, j, false));
                    }
                    swapNumbers(i, j, false, board);
                }
            }
        }
        return possibleSwaps;
    }

    private static boolean checkIfAtLeastThreeInRow(byte[][] board) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == EMPTY_SPACE || board[i][j] == IMMOVABLE_BLOCK) {
                    continue;
                }
                if (i > 0 && i < board.length - 1 && board[i - 1][j] == board[i][j]
                        && board[i + 1][j] == board[i][j]) {
                    return true;
                }
                if (j > 0 && j < board[i].length - 1 && board[i][j - 1] == board[i][j]
                        && board[i][j + 1] == board[i][j]) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void swapNumbers(int x, int y, boolean isDown, byte[][] board) {
        byte temp = board[x][y];
        board[x][y] = board[x + (isDown ? 1 : 0)][y + (isDown ? 0 : 1)];
        board[x + (isDown ? 1 : 0)][y + (isDown ? 0 : 1)] = temp;
    }

    private static byte[][] makeBoardCopy(byte[][] board) {
        byte[][] newBoard = new byte[board.length][board[0].length];
        for (int i = 0; i < newBoard.length; i++) {
            for (int j = 0; j < newBoard[i].length; j++) {
                newBoard[i][j] = board[i][j];
            }
        }
        return newBoard;
    }

    @Deprecated
    @SuppressWarnings(value = { "unused" })
    private static void getSolution(ArrayList<Swap> bestSolution, ArrayList<Swap> solutionSwaps,
            byte[][] board, ArrayList<byte[][]> memoTable) {
        if (!bestSolution.isEmpty() && solutionSwaps.size() >= bestSolution.size()) {
            return;
        }
        for (byte[][] seenBoard : memoTable) {
            if (isBoardSame(board, seenBoard)) {
                return;
            }
        }
        memoTable.add(makeBoardCopy(board));
        outer: for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] > 0) {
                    break outer;
                }
            }
            if (i == board.length - 1 && (bestSolution.isEmpty() || solutionSwaps.size() < bestSolution.size())) {
                bestSolution.clear();
                bestSolution.addAll(solutionSwaps);
                return;
            }
        }
        for (Swap swap : getPossibleSwaps(board)) {
            byte[][] oldBoard = makeBoardCopy(board);
            swapNumbers(swap.getX(), swap.getY(), swap.isDown(), board);
            collapseThreeInRowOrMore(board);
            solutionSwaps.add(swap);
            getSolution(bestSolution, solutionSwaps, board, memoTable);
            solutionSwaps.removeLast();
            board = oldBoard;
        }
    }

    @Deprecated
    @SuppressWarnings(value = { "unused" })
    private static void printBoard(byte[][] board) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                System.out.print(board[i][j]);
            }
            System.out.println();
        }
        System.out.println();
    }

    @Deprecated
    @SuppressWarnings(value = { "unused" })
    private static void getBoard(int[][] board) {
        Scanner scanner = new Scanner(System.in);
        board = new int[scanner.nextInt()][scanner.nextInt()];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                board[i][j] = scanner.nextInt();
            }
        }
        scanner.close();
    }
}
