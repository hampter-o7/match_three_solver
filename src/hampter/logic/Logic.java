package hampter.logic;

import java.util.ArrayList;
import java.util.Scanner;

import hampter.util.Swap;

public class Logic {

    private static final int IMMOVABLE_BLOCK = -1;
    private static final int EMPTY_SPACE = 0;

    public static void solveBoard(int[][] board) {
        ArrayList<Swap> bestSolution = new ArrayList<>();
        getSolution(bestSolution, new ArrayList<>(), board);
        System.out.println(bestSolution);
    }

    private static void getSolution(ArrayList<Swap> bestSolution, ArrayList<Swap> solutionSwaps,
            int[][] board) {
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
            int[][] oldBoard = makeBoardCopy(board);
            swapNumbers(swap.getX(), swap.getY(), swap.isDown(), board);
            collapseThreeInRowOrMore(board);
            solutionSwaps.add(swap);
            getSolution(bestSolution, solutionSwaps, board);
            solutionSwaps.removeLast();
            board = oldBoard;
        }
    }

    private static void collapseThreeInRowOrMore(int[][] board) {
        int[][] oldBoard = makeBoardCopy(board);
        do {
            oldBoard = makeBoardCopy(board);
            findAllInRowAndRemove(board);
            dropAllNumbers(board);
        } while (!isBoardSame(oldBoard, board));
    }

    private static boolean isBoardSame(int[][] otherBoard, int[][] board) {
        for (int i = 0; i < otherBoard.length; i++) {
            for (int j = 0; j < otherBoard[0].length; j++) {
                if (board[i][j] != otherBoard[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void dropAllNumbers(int[][] board) {
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
                board[i][j] = i - counter >= 0 ? board[i - counter][j] : 0;
            }
        }
    }

    private static void findAllInRowAndRemove(int[][] board) {
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
    }

    private static ArrayList<Swap> getPossibleSwaps(int[][] board) {
        ArrayList<Swap> possibleSwaps = new ArrayList<>();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == EMPTY_SPACE || board[i][j] == IMMOVABLE_BLOCK) {
                    continue;
                }
                if (i + 1 != board.length && board[i + 1][j] != IMMOVABLE_BLOCK) {
                    swapNumbers(i, j, true, board);
                    if (checkIfAtLeastThreeInRow(board)) {
                        possibleSwaps.add(new Swap(i, j, true));
                    }
                    swapNumbers(i, j, true, board);
                }
                if (j + 1 != board[i].length && board[i][j + 1] != EMPTY_SPACE && board[i][j + 1] != IMMOVABLE_BLOCK) {
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

    private static boolean checkIfAtLeastThreeInRow(int[][] board) {
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

    private static void swapNumbers(int x, int y, boolean isDown, int[][] board) {
        int temp = board[x][y];
        board[x][y] = board[x + (isDown ? 1 : 0)][y + (isDown ? 0 : 1)];
        board[x + (isDown ? 1 : 0)][y + (isDown ? 0 : 1)] = temp;
    }

    private static int[][] makeBoardCopy(int[][] board) {
        int[][] newBoard = new int[board.length][board[0].length];
        for (int i = 0; i < newBoard.length; i++) {
            for (int j = 0; j < newBoard[i].length; j++) {
                newBoard[i][j] = board[i][j];
            }
        }
        return newBoard;
    }

    @Deprecated
    @SuppressWarnings(value = { "unused" })
    private static void printBoard(int[][] board) {
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
