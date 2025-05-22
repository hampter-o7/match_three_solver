package hampter.java.util;

import java.util.ArrayList;
import java.util.Arrays;

public class BoardState {
    private byte[][] board;
    private Swap swap;
    private BoardState prevBoardState;
    private int hash;

    public BoardState(byte[][] board, Swap swap, BoardState prevBoardState) {
        this.board = board;
        this.swap = swap;
        this.prevBoardState = prevBoardState;
        this.hash = computeHash(board);
    }

    private int computeHash(byte[][] array) {
        int result = 1;
        for (byte[] row : array) {
            result = 31 * result + Arrays.hashCode(row);
        }
        return result;
    }

    public void getSolution(ArrayList<Swap> solution) {
        if (this.swap == null) {
            return;
        }
        solution.addFirst(this.swap);
        prevBoardState.getSolution(solution);
    }

    public byte[][] getBoard() {
        return board;
    }

    public Swap getSwap() {
        return swap;
    }

    public BoardState getPrevBoardState() {
        return prevBoardState;
    }

    @Override
    public boolean equals(Object o) {
        return Arrays.deepEquals(this.board, ((BoardState) o).board);
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
