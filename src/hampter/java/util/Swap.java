package hampter.java.util;

public class Swap {

    private int x;
    private int y;
    private boolean down;

    public Swap(int x, int y, boolean down) {
        this.x = x;
        this.y = y;
        this.down = down;
    }

    public Swap(String fromSaveFile) {
        String[] stringSwap = fromSaveFile.split(",");
        this.x = Integer.parseInt(stringSwap[0]);
        this.y = Integer.parseInt(stringSwap[1]);
        this.down = stringSwap[2].equals("1");
    }

    public String toSaveFile() {
        return String.join(",", String.valueOf(x), String.valueOf(y), down ? "1" : "0");
    }

    @Override
    public String toString() {
        return String.format("{%d, %d, %s}", x + 1, y + 1, down ? "down" : "right");
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isDown() {
        return down;
    }
}
