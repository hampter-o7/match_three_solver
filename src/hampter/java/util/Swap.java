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
