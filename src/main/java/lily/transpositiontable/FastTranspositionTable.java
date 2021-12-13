package lily.transpositiontable;

import lily.engine.Color;
import lily.engine.Move;

import java.util.HashMap;
import java.util.Map;

public class FastTranspositionTable {

    private final Map<Integer, PositionInfo> cacheMap = new HashMap<>();
    private int cntAll = 0;
    private int cntNeg = 0;


    public Double getCachedScore(long hashOfPosition, Color colorToMove, int depth, double alpha, double beta) {
        cntAll++;
        PositionInfo positionInfo = cacheMap.get(calculateKey(hashOfPosition, colorToMove));
        if (positionInfo != null && positionInfo.getColorToMove() == colorToMove
                && depth <= positionInfo.getDepth() && positionInfo.getHashOfPosition() == hashOfPosition) {
            double eval = positionInfo.getValue();
            if (positionInfo.getHashResult() == HashResult.EXACT) {
                return eval;
            }
            if (positionInfo.getHashResult() == HashResult.ALPHA && eval <= alpha) {
                return alpha;
            }
            if (positionInfo.getHashResult() == HashResult.BETA && eval >= beta) {
                return beta;
            }
        }
        cntNeg++;
        return null;
    }

    public Move getBestMove(long hashOfPosition, Color colorToMove) {
        PositionInfo positionInfo = cacheMap.get(calculateKey(hashOfPosition, colorToMove));
        if (positionInfo != null && positionInfo.getColorToMove() == colorToMove && positionInfo.getHashOfPosition() == hashOfPosition) {
            return positionInfo.getBestMove();
        }
        return null;
    }

    public void writeEntry(long hashOfPosition, PositionInfo positionInfo) {
        int key = calculateKey(hashOfPosition, positionInfo.getColorToMove());
        PositionInfo oldPositionInfo = cacheMap.get(key);
        if (oldPositionInfo != null && oldPositionInfo.getHashOfPosition() == hashOfPosition &&
                oldPositionInfo.getColorToMove() == positionInfo.getColorToMove()
                && positionInfo.getDepth() <= oldPositionInfo.getDepth()) {
        } else {
            cacheMap.put(key, positionInfo);
        }
    }

    public void reset() {
        cacheMap.clear();
    }


    private int calculateKey(long hashOfPosition, Color color) {
        if (color == Color.WHITE) hashOfPosition += 1;
        if (color == Color.BLACK) hashOfPosition += 0;
        return (int) (hashOfPosition % Integer.MAX_VALUE);
    }

}
