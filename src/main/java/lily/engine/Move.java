package lily.engine;

import java.util.ArrayList;
import java.util.List;

public class Move {

    private int figure;
    private int from;
    private int to;
    private List<Integer> capturedPawns;
    private int[] capturedPawnsArr;
    private int capturedPawnsArrSize;
    private List<Integer> jumpFieldPawns;

    public Move() {
    }

    public Move(int from, int to) {
        this.from = from;
        this.to = to;
        this.capturedPawns = new ArrayList<>();
        this.jumpFieldPawns = new ArrayList<>();
    }

    public Move(int from, int to, List<Integer> capturedPawns) {
        this.from = from;
        this.to = to;
        this.capturedPawns = capturedPawns;
        this.jumpFieldPawns = new ArrayList<>();
    }

    public Move(int from, int to, List<Integer> capturedPawns, List<Integer> jumpFieldPawns) {
        this.from = from;
        this.to = to;
        this.capturedPawns = capturedPawns;
        this.jumpFieldPawns = jumpFieldPawns;
    }

    public Move(int figure, int from, int to) {
        this.figure = figure;
        this.from = from;
        this.to = to;
        this.capturedPawns = new ArrayList<>();
        this.jumpFieldPawns = new ArrayList<>();
    }

    public Move(int figure, int from, int to, List<Integer> capturedPawns) {
        this.figure = figure;
        this.from = from;
        this.to = to;
        this.capturedPawns = capturedPawns;
        this.jumpFieldPawns = new ArrayList<>();
    }


    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public int getFigure() {
        return figure;
    }

    public List<Integer> getCapturedPawns() {
        return capturedPawns;
    }

    public void setCapturedPawns(List<Integer> capturedPawns) {
        this.capturedPawns = capturedPawns;
    }

    public void setFigure(int figure) {
        this.figure = figure;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public int[] getCapturedPawnsArr() {
        return capturedPawnsArr;
    }

    public void setCapturedPawnsArr(int[] capturedPawnsArr) {
        this.capturedPawnsArr = capturedPawnsArr;
    }

    public int getCapturedPawnsArrSize() {
        return capturedPawnsArrSize;
    }

    public void setCapturedPawnsArrSize(int capturedPawnsArrSize) {
        this.capturedPawnsArrSize = capturedPawnsArrSize;
    }

    public List<Integer> getJumpFieldPawns() {
        return jumpFieldPawns;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Move move = (Move) o;

        if (figure != move.figure) return false;
        if (from != move.from) return false;
        if (to != move.to) return false;
//        return capturedPawns != null ? capturedPawns.equals(move.capturedPawns) : move.capturedPawns == null;
        return capturedPawns.size() == move.getCapturedPawns().size();
    }

    @Override
    public int hashCode() {
        int result = figure;
        result = 31 * result + from;
        result = 31 * result + to;
        result = 31 * result + (capturedPawns != null ? capturedPawns.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
//        NotationTranslator notationTranslator = new NotationTranslator();
//        if (capturedPawns.size() == 0) {
//            return notationTranslator.toStandardField(from) + "-" + notationTranslator.toStandardField(to);
//        } else {
//            return notationTranslator.toStandardField(from) + "x" + notationTranslator.toStandardField(to);
//        }
        if (capturedPawns.size() == 0) {
            return from + "-" + to;
        } else {
            return from + "x" + to;
        }
    }
}
