package lily.transpositiontable;

import lily.engine.Color;
import lily.engine.Move;

public class PositionInfo {
    private final Move bestMove;
    private final Color colorToMove;
    private final double value;
    private final int depth;
    private final HashResult hashResult;
    private final long hashOfPosition;

    public PositionInfo(Move bestMove, Color colorToMove, double value, int depth, HashResult hashResult, long hashOfPosition) {
        this.bestMove = bestMove;
        this.colorToMove = colorToMove;
        this.value = value;
        this.depth = depth;
        this.hashResult = hashResult;
        this.hashOfPosition = hashOfPosition;
    }

    public Move getBestMove() {
        return bestMove;
    }

    public double getValue() {
        return value;
    }

    public int getDepth() {
        return depth;
    }

    public Color getColorToMove() {
        return colorToMove;
    }

    public HashResult getHashResult() {
        return hashResult;
    }

    public long getHashOfPosition() {
        return hashOfPosition;
    }
}
