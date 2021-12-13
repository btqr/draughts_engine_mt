package lily.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import lily.endgamedatabase.EndGameDatabaseDriverImpl;
import lily.engine.BitBoard;
import lily.engine.Color;
import lily.engine.Move;
import lily.engine.Position;
import lily.io.NotationTranslator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public abstract class BoardUtils {

    public static final int BLACK_KING = -2;
    public static final int BLACK_PAWN = -1;
    public static final int FREE_FIELD = 0;
    public static final int WHITE_PAWN = 1;
    public static final int WHITE_KING = 2;
    public static final int INVALID = 8;

    private static final NotationTranslator notationTranslator = new NotationTranslator();

    // board representation where I = -1
    //  888888888888
    //  881818181818
    //  818181818188
    //  881818181818
    //  818181818188
    //  800000000008
    //  800000000008
    //  88I8I8I8I8I8
    //  8I8I8I8I8I88
    //  88I8I8I8I8I8
    //  8I8I8I8I8I88
    //  888888888888
    private static final List<Integer> initialWhiteFields = List.of(
            14, 16, 18, 20, 22,
            25, 27, 29, 31, 33,
            38, 40, 42, 44, 46,
            49, 51, 53, 55, 57
    );
    private static final List<Integer> initialBlackFields = List.of(
            86, 88, 90, 92, 94,
            97, 99, 101, 103, 105,
            110, 112, 114, 116, 118,
            121, 123, 125, 127, 129
    );
    private static final List<Integer> initialFreeFields = List.of(
            62, 64, 66, 68, 70,
            73, 75, 77, 79, 81
    );

    private static final int[][] leftBalance = {
            {fld(49), fld(39), fld(29), fld(19), fld(9)},
            {fld(44), fld(34), fld(24), fld(14), fld(4)},
            {fld(50), fld(40), fld(30), fld(20), fld(10)},
            {fld(45), fld(35), fld(25), fld(15), fld(5)},
    };

    private static final int[][] rightBalance = {
            {fld(42), fld(32), fld(22), fld(12), fld(2)},
            {fld(47), fld(37), fld(27), fld(17), fld(7)},
            {fld(41), fld(31), fld(21), fld(11), fld(1)},
            {fld(46), fld(36), fld(26), fld(16), fld(6)},
    };

    private static final int[] balance = new int[150];

    private static final List<Integer> legalFields = ImmutableList.copyOf(Iterables.concat(initialWhiteFields, initialFreeFields, initialBlackFields));
    private static final List<Integer> reversedLegalFields;

    private static final long[][] zorbistArr;

    static {
        zorbistArr = new long[200][5];
        for (int field : legalFields()) {
            for (int i = 0; i < 5; i++) {
                zorbistArr[field][i] = (((long) (Math.random() * Long.MAX_VALUE)));
            }
        }
        reversedLegalFields = new ArrayList<>(legalFields);
        Collections.reverse(reversedLegalFields);
        for (int i = 0; i < leftBalance.length; i++) {
            for (int j = 0; j < leftBalance[i].length; j++) {
                balance[leftBalance[i][j]] = -(i + 1);
            }
        }
        for (int i = 0; i < rightBalance.length; i++) {
            for (int j = 0; j < rightBalance[i].length; j++) {
                balance[rightBalance[i][j]] = i + 1;
            }
        }
    }

    public static long positionHash(Position position) {
        long hash = 0;
        for (int field : legalFields()) {
            hash ^= zorbistArr[field][zorbistIdx(position.getBoard()[field])];
        }
        return hash;
    }

    public static List<Integer> legalFields() {
        return legalFields;
    }

    private static List<Integer> initialWhiteFields() {
        return initialWhiteFields;
    }

    private static List<Integer> initialBlackFields() {
        return initialBlackFields;
    }

    private static List<Integer> initialFreeFields() {
        return List.of(
                62, 64, 66, 68, 70,
                73, 75, 77, 79, 81
        );
    }

    public static int[] initialBoard() {
        int[] board = new int[144];
        Arrays.fill(board, INVALID);
        initialWhiteFields().forEach(field -> board[field] = WHITE_PAWN);
        initialFreeFields().forEach(field -> board[field] = FREE_FIELD);
        initialBlackFields().forEach(field -> board[field] = BLACK_PAWN);
        return board;
    }

    public static int[] emptyBoard() {
        int[] board = new int[144];
        Arrays.fill(board, INVALID);
        initialWhiteFields().forEach(field -> board[field] = FREE_FIELD);
        initialFreeFields().forEach(field -> board[field] = FREE_FIELD);
        initialBlackFields().forEach(field -> board[field] = FREE_FIELD);
        return board;
    }

    private static int promoteIfPossible(int figure) {
        if (figure == WHITE_PAWN) {
            return WHITE_KING;
        } else if (figure == BLACK_PAWN) {
            return BLACK_KING;
        } else {
            return figure;
        }
    }

    private static boolean isLastRankForPawn(int field, int figure) {
        if (figure == WHITE_PAWN) {
            return field == 121 || field == 123 || field == 125 || field == 127 || field == 129;
        } else if (figure == BLACK_PAWN) {
            return field == 14 || field == 16 || field == 18 || field == 20 || field == 22;
        } else {
            return false;
        }
    }

    public static int getTempoForField(int field, Color color) {
        int tempo;
        int bonus = field / 24 - 1;
        if (field % 24 > 10) bonus += 1;
        tempo = field / 24 + 2 + bonus;
        if (color == Color.WHITE) return tempo;
        else return 13 - tempo;
    }

    public static int getBalanceForField(int field) {
        return balance[field];
    }

    public static int tempo(Position position, Color color) {
        int cnt = 0;
        int i = 0;
        int j = 1;
        if (color == Color.WHITE) {
            List<Integer> legalFields = legalFields();
            for (int field : legalFields) {
                if (i % 5 == 0) {
                    j++;
                }
                if (position.getBoard()[field] == WHITE_PAWN) cnt += j;
                i++;
            }
        } else {
            for (int field : reversedLegalFields) {
                if (i % 5 == 0) {
                    j++;
                }
                if (position.getBoard()[field] == BLACK_PAWN) cnt += j;
                i++;
            }
        }
        return cnt;
    }

    public static int tempoDiff(Position position) {
        int whiteTempo = 0;
        int blackTempo = 0;
        int i = 0;
        int j = 1;
        List<Integer> legalFields = legalFields();
        for (int field : legalFields) {
            if (i % 5 == 0) {
                j++;
            }
            if (Color.inColor(position.getBoard()[field], Color.WHITE)) whiteTempo += j;
            if (Color.inColor(position.getBoard()[field], Color.BLACK)) blackTempo += (13 - j);
            i++;
        }
        return whiteTempo - blackTempo;
    }

    public static int countBalance(Position position, Color color) {
        int balance = 0;
        int[] board = position.getBoard();
        for (int i = 0; i < leftBalance.length; i++) {
            for (int field : leftBalance[i]) {
                if (Color.inColor(board[field], color)) {
                    balance += (i + 1);
                }
            }
        }
        for (int i = 0; i < rightBalance.length; i++) {
            for (int field : rightBalance[i]) {
                if (Color.inColor(board[field], color)) {
                    balance -= (i + 1);
                }
            }
        }
        return Math.abs(balance);
    }

    private static int zorbistIdx(int figure) {
        switch (figure) {
            case WHITE_PAWN:
                return 1;
            case WHITE_KING:
                return 2;
            case BLACK_PAWN:
                return 3;
            case BLACK_KING:
                return 4;
            case FREE_FIELD:
                return 0;
            default:
                throw new RuntimeException("undefined zorbist index");
        }
    }

    public static BitBoard asBitBoardFast(Position position) {
        long white = position.getWhiteFields();
        long black = position.getBlackFields();
        long kings = position.getKings();
        int color = position.getColorToMove() == Color.WHITE ? EndGameDatabaseDriverImpl.EGDB_WHITE : EndGameDatabaseDriverImpl.EGDB_BLACK;
        return new BitBoard(toOtherNotation(white), toOtherNotation(black), toOtherNotation(kings), color);
    }

    private static long toOtherNotation(long fields) {
        long partOne = (fields >> 1) & (~(-1 << 10));
        long partTwo = ((fields >> 11) & (~(-1 << 10))) << 11;
        long partThree = ((fields >> 21) & (~(-1 << 10))) << 22;
        long partFour = ((fields >> 31) & (~(-1 << 10))) << 33;
        long partFive = ((fields >> 41) & (~(-1 << 10))) << 44;
        return partOne | partTwo | partThree | partFour | partFive;
    }

    private static int fld(int standardField) {
        return notationTranslator.toApplicationField(standardField);
    }

    public static void move(Move move, Position position) {
        int[] board = position.getBoard();
        int fromFigure = board[move.getFrom()];
        long actualHash = position.getHashOfPosition();
        int tempoDiff = position.getTempoDiff();
        int whiteBalance = position.getWhiteBalance();
        int blackBalance = position.getBlackBalance();
        long whiteFields = position.getWhiteFields();
        long blackFields = position.getBlackFields();
        long kings = position.getKings();
        actualHash ^= zorbistArr[move.getFrom()][zorbistIdx(fromFigure)];
        if (board[move.getFrom()] == WHITE_PAWN) {
            tempoDiff -= getTempoForField(move.getFrom(), Color.WHITE);
            whiteBalance -= getBalanceForField(move.getFrom());
            whiteFields = remove(whiteFields, move.getFrom());
        }
        if (board[move.getFrom()] == WHITE_KING) {
            kings = remove(kings, move.getFrom());
            whiteFields = remove(whiteFields, move.getFrom());
        }
        if (board[move.getFrom()] == BLACK_PAWN) {
            tempoDiff += getTempoForField(move.getFrom(), Color.BLACK);
            blackBalance -= getBalanceForField(move.getFrom());
            blackFields = remove(blackFields, move.getFrom());
        }
        if (board[move.getFrom()] == BLACK_KING) {
            kings = remove(kings, move.getFrom());
            blackFields = remove(blackFields, move.getFrom());
        }
        board[move.getFrom()] = FREE_FIELD;
        actualHash ^= zorbistArr[move.getFrom()][zorbistIdx(FREE_FIELD)];
        actualHash ^= zorbistArr[move.getTo()][zorbistIdx(FREE_FIELD)];
        board[move.getTo()] = fromFigure;
        if (board[move.getTo()] == WHITE_PAWN) {
            tempoDiff += getTempoForField(move.getTo(), Color.WHITE);
            whiteBalance += getBalanceForField(move.getTo());
            whiteFields = add(whiteFields, move.getTo());
        }
        if (board[move.getTo()] == WHITE_KING) {
            kings = add(kings, move.getTo());
            whiteFields = add(whiteFields, move.getTo());
        }
        if (board[move.getTo()] == BLACK_PAWN) {
            tempoDiff -= getTempoForField(move.getTo(), Color.BLACK);
            blackBalance += getBalanceForField(move.getTo());
            blackFields = add(blackFields, move.getTo());
        }
        if (board[move.getTo()] == BLACK_KING) {
            kings = add(kings, move.getTo());
            blackFields = add(blackFields, move.getTo());
        }
        if (isLastRankForPawn(move.getTo(), fromFigure)) {
            if (promoteIfPossible(fromFigure) != fromFigure) {
                if (Color.inColor(fromFigure, Color.BLACK)) {
                    position.setNumberOfBlackKings(position.getNumberOfBlackKings() + 1);
                    kings = add(kings, move.getTo());
                }
                if (Color.inColor(fromFigure, Color.WHITE)) {
                    position.setNumberOfWhiteKings(position.getNumberOfWhiteKings() + 1);
                    kings = add(kings, move.getTo());
                }
            }
            board[move.getTo()] = promoteIfPossible(fromFigure);
        }
        actualHash ^= zorbistArr[move.getTo()][zorbistIdx(board[move.getTo()])];
        for (int pawn : move.getCapturedPawns()) {
            if (board[pawn] == WHITE_KING) {
                actualHash ^= zorbistArr[pawn][zorbistIdx(WHITE_KING)];
                position.setNumberOfWhite(position.getNumberOfWhite() - 1);
                position.setNumberOfWhiteKings(position.getNumberOfWhiteKings() - 1);
                whiteBalance -= getBalanceForField(pawn);
                whiteFields = remove(whiteFields, pawn);
                kings = remove(kings, pawn);
            }
            if (board[pawn] == WHITE_PAWN) {
                actualHash ^= zorbistArr[pawn][zorbistIdx(WHITE_PAWN)];
                position.setNumberOfWhite(position.getNumberOfWhite() - 1);
                tempoDiff -= getTempoForField(pawn, Color.WHITE);
                whiteBalance -= getBalanceForField(pawn);
                whiteFields = remove(whiteFields, pawn);
            }
            if (board[pawn] == BLACK_KING) {
                actualHash ^= zorbistArr[pawn][zorbistIdx(BLACK_KING)];
                position.setNumberOfBlack(position.getNumberOfBlack() - 1);
                position.setNumberOfBlackKings(position.getNumberOfBlackKings() - 1);
                blackBalance -= getBalanceForField(pawn);
                blackFields = remove(blackFields, pawn);
                kings = remove(kings, pawn);
            }
            if (board[pawn] == BLACK_PAWN) {
                actualHash ^= zorbistArr[pawn][zorbistIdx(BLACK_PAWN)];
                position.setNumberOfBlack(position.getNumberOfBlack() - 1);
                tempoDiff += getTempoForField(pawn, Color.BLACK);
                blackBalance -= getBalanceForField(pawn);
                blackFields = remove(blackFields, pawn);
            }
            board[pawn] = FREE_FIELD;
            actualHash ^= zorbistArr[pawn][zorbistIdx(FREE_FIELD)];
        }
        position.setHashOfPosition(actualHash);
        position.setTempoDiff(tempoDiff);
        position.setBlackBalance(blackBalance);
        position.setWhiteBalance(whiteBalance);
        position.setWhiteFields(whiteFields);
        position.setBlackFields(blackFields);
        position.setKings(kings);
        if (position.getColorToMove() == Color.WHITE) {
            position.setColorToMove(Color.BLACK);
        } else {
            position.setColorToMove(Color.WHITE);
        }
    }

    public static Function<Position, Position> undoFunction(Position positionBefore) {
        long whiteFieldsBefore = positionBefore.getWhiteFields();
        long blackFieldsBefore = positionBefore.getBlackFields();
        long kingsBefore = positionBefore.getKings();
        long hashBefore = positionBefore.getHashOfPosition();
        int numWhiteBefore = positionBefore.getNumberOfWhite();
        int numBlackBefore = positionBefore.getNumberOfBlack();
        int numWhiteKingsBefore = positionBefore.getNumberOfWhiteKings();
        int numBlackKingsBefore = positionBefore.getNumberOfBlackKings();
        int tempoDiffBefore = positionBefore.getTempoDiff();
        int whiteBalanceBefore = positionBefore.getWhiteBalance();
        int blackBalanceBefore = positionBefore.getBlackBalance();
        Color colorToMove = positionBefore.getColorToMove();
        return posAfter -> {
            long whiteKingsDiff = (kingsBefore & whiteFieldsBefore) ^ (posAfter.getKings() & posAfter.getWhiteFields());
            long blackKingsDiff = (kingsBefore & blackFieldsBefore) ^ (posAfter.getKings() & posAfter.getBlackFields());
            long whitePawnsDiff = ((~kingsBefore) & whiteFieldsBefore) ^ ((~posAfter.getKings()) & posAfter.getWhiteFields());
            long blackPawnsDiff = ((~kingsBefore) & blackFieldsBefore) ^ ((~posAfter.getKings()) & posAfter.getBlackFields());

            flip(posAfter.getBoard(), whitePawnsDiff, WHITE_PAWN);
            flip(posAfter.getBoard(), blackPawnsDiff, BLACK_PAWN);
            flip(posAfter.getBoard(), whiteKingsDiff, WHITE_KING);
            flip(posAfter.getBoard(), blackKingsDiff, BLACK_KING);

            posAfter.setNumberOfWhite(numWhiteBefore);
            posAfter.setNumberOfBlack(numBlackBefore);
            posAfter.setColorToMove(colorToMove);
            posAfter.setHashOfPosition(hashBefore);
            posAfter.setNumberOfWhiteKings(numWhiteKingsBefore);
            posAfter.setNumberOfBlackKings(numBlackKingsBefore);
            posAfter.setTempoDiff(tempoDiffBefore);
            posAfter.setBlackBalance(blackBalanceBefore);
            posAfter.setWhiteBalance(whiteBalanceBefore);
            posAfter.setWhiteFields(whiteFieldsBefore);
            posAfter.setBlackFields(blackFieldsBefore);
            posAfter.setKings(kingsBefore);
            return posAfter;
        };
    }

    public static long add(long fields, int field) {
        fields |= 1L << notationTranslator.toStandardField(field);
        return fields;
    }

    public static long remove(long fields, int field) {
        fields &= ~(1L << notationTranslator.toStandardField(field));
        return fields;
    }

    public static void flip(int[] board, long fields, int pawn) {
        while (Long.numberOfTrailingZeros(fields) != 64L) {
            int field = Long.numberOfTrailingZeros(fields);
            int applicationField = notationTranslator.toApplicationField(field);
            if (board[applicationField] != pawn) board[applicationField] = pawn;
            else board[applicationField] = FREE_FIELD;
            fields &= ~(1L << field);
        }
    }
}
