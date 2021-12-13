package lily.engine;

import lily.utils.BoardUtils;

import java.util.Arrays;

import static lily.utils.BoardUtils.*;

public class Position {

    protected int[] board;
    protected Color colorToMove;
    protected int numberOfWhite;
    protected int numberOfBlack;
    protected int numberOfWhiteKings;
    protected int numberOfBlackKings;
    protected int tempoDiff;
    protected int whiteBalance;
    protected int blackBalance;
    protected int mobility;
    protected int legalMobility;
    protected long hashOfPosition;
    private long whiteFields;
    private long blackFields;
    private long kings;

    public Position(int[] board, Color colorToMove) {
        this.board = board;
        this.colorToMove = colorToMove;
        readStats();
        this.hashOfPosition = BoardUtils.positionHash(this);
    }

    public Position(int[] board, Color colorToMove, long hashOfPosition) {
        this.board = board;
        this.colorToMove = colorToMove;
        readStats();
        this.hashOfPosition = hashOfPosition;
    }

    public int[] getBoard() {
        return board;
    }

    public void setBoard(int[] board) {
        this.board = board;
    }

    public Color getColorToMove() {
        return colorToMove;
    }

    public void setColorToMove(Color colorToMove) {
        this.colorToMove = colorToMove;
    }

    public int getNumberOfWhite() {
        return numberOfWhite;
    }

    public void setNumberOfWhite(int numberOfWhite) {
        this.numberOfWhite = numberOfWhite;
    }

    public int getNumberOfBlack() {
        return numberOfBlack;
    }

    public void setNumberOfBlack(int numberOfBlack) {
        this.numberOfBlack = numberOfBlack;
    }

    public long getHashOfPosition() {
        return hashOfPosition;
    }

    public void setHashOfPosition(long hashOfPosition) {
        this.hashOfPosition = hashOfPosition;
    }

    public int getNumberOfWhiteKings() {
        return numberOfWhiteKings;
    }

    public void setNumberOfWhiteKings(int numberOfWhiteKings) {
        this.numberOfWhiteKings = numberOfWhiteKings;
    }

    public int getNumberOfBlackKings() {
        return numberOfBlackKings;
    }

    public void setNumberOfBlackKings(int numberOfBlackKings) {
        this.numberOfBlackKings = numberOfBlackKings;
    }

    public int getTempoDiff() {
        return tempoDiff;
    }

    public void setTempoDiff(int tempoDiff) {
        this.tempoDiff = tempoDiff;
    }

    public int getWhiteBalance() {
        return whiteBalance;
    }

    public void setWhiteBalance(int whiteBalance) {
        this.whiteBalance = whiteBalance;
    }

    public int getBlackBalance() {
        return blackBalance;
    }

    public void setBlackBalance(int blackBalance) {
        this.blackBalance = blackBalance;
    }

    public int getMobility() {
        return mobility;
    }

    public void setMobility(int mobility) {
        this.mobility = mobility;
    }

    public int getLegalMobility() {
        return legalMobility;
    }

    public void setLegalMobility(int legalMobility) {
        this.legalMobility = legalMobility;
    }

    public long getWhiteFields() {
        return whiteFields;
    }

    public void setWhiteFields(long whiteFields) {
        this.whiteFields = whiteFields;
    }

    public long getBlackFields() {
        return blackFields;
    }

    public void setBlackFields(long blackFields) {
        this.blackFields = blackFields;
    }

    public long getKings() {
        return kings;
    }

    public void setKings(long kings) {
        this.kings = kings;
    }

    private void readStats() {
        int numberOfWhite = 0;
        int numberOfBlack = 0;
        int whiteKings = 0;
        int blackKings = 0;
        long whiteFields = 0L;
        long blackFields = 0L;
        long kings = 0L;
        for (int field : legalFields()) {
            if (board[field] == WHITE_PAWN) {
                numberOfWhite++;
                whiteFields = BoardUtils.add(whiteFields, field);
            }
            if (board[field] == BLACK_PAWN) {
                numberOfBlack++;
                blackFields = BoardUtils.add(blackFields, field);
            }
            if (board[field] == WHITE_KING) {
                whiteKings++;
                numberOfWhite++;
                whiteFields = BoardUtils.add(whiteFields, field);
                kings = BoardUtils.add(kings, field);
            }
            if (board[field] == BLACK_KING) {
                blackKings++;
                numberOfBlack++;
                blackFields = BoardUtils.add(blackFields, field);
                kings = BoardUtils.add(kings, field);
            }
        }
        this.setNumberOfWhite(numberOfWhite);
        this.setNumberOfBlack(numberOfBlack);
        this.setNumberOfWhiteKings(whiteKings);
        this.setNumberOfBlackKings(blackKings);
        this.setWhiteBalance(BoardUtils.countBalance(this, Color.WHITE));
        this.setBlackBalance(BoardUtils.countBalance(this, Color.BLACK));
        this.setTempoDiff(BoardUtils.tempoDiff(this));
        this.setWhiteFields(whiteFields);
        this.setBlackFields(blackFields);
        this.setKings(kings);
    }

    @Override
    public Position clone() {
        Position p = new Position(board.clone(), colorToMove);
        p.setNumberOfWhite(numberOfWhite);
        p.setNumberOfBlack(numberOfBlack);
        p.setHashOfPosition(hashOfPosition);
        p.setNumberOfWhiteKings(numberOfWhiteKings);
        p.setNumberOfBlackKings(numberOfBlackKings);
        p.setTempoDiff(tempoDiff);
        p.setWhiteBalance(whiteBalance);
        p.setBlackBalance(blackBalance);
        p.setMobility(mobility);
        p.setWhiteFields(whiteFields);
        p.setBlackFields(blackFields);
        p.setKings(kings);
        return p;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Position position = (Position) o;
        if (!Arrays.equals(board, position.board)) return false;
        return colorToMove == position.colorToMove;
    }

    @Override
    public int hashCode() {
        return (int) hashOfPosition;
    }

    public int getBalanceDiff() {
        return Math.abs(whiteBalance) - Math.abs(blackBalance);
    }

}
