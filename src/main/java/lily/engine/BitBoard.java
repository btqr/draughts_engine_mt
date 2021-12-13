package lily.engine;

public class BitBoard {

    private final long white;
    private final long black;
    private final long kings;
    private final int color;

    public BitBoard(long white, long black, long kings, int color) {
        this.white = white;
        this.black = black;
        this.kings = kings;
        this.color = color;
    }

    public long getWhite() {
        return white;
    }

    public long getBlack() {
        return black;
    }

    public long getKings() {
        return kings;
    }

    public int getColor() {
        return color;
    }
}
