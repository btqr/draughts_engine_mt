package lily.movegenerator;

import lily.engine.BitBoard;
import lily.engine.Color;
import lily.engine.Move;
import lily.engine.Position;
import lily.io.NotationTranslator;
import lily.utils.BoardUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static lily.endgamedatabase.EndGameDatabaseDriverImpl.EGDB_WHITE;

public class ExtendedMoveGeneratorImpl extends MoveGeneratorImpl {

    private static final int[] BITBOARD_OFFSETS = new int[]{-6, -5, 5, 6};
    private static final List<Integer> OFFSETS = List.of(-13, -11, 11, 13);
    private static final NotationTranslator notationTranslator = new NotationTranslator();

    private final long[] capturedArr = new long[100];
    private final long[] jumpFieldArr = new long[100];
    private final int[] figureArr = new int[100];
    private final int[] fromArr = new int[100];
    private final int[] toArr = new int[100];
    private int last = -1;
    private int fromMostCaptured = -1;
    private int mostCaptured = 0;
    private long myPawns;
    private long enemyPawns;
    private long myMen;
    private long enemyMen;
    private long myKings;
    private long enemyKings;
    private long pawns;
    private long freeFields;
    private long color;
    private final long legalFields = 18005598119394303L;
    private int[] board;
    private List<Move> legalMoves = new ArrayList<>();

    public List<Move> getLegalMoves(Position position) {
        last = -1;
        fromMostCaptured = -1;
        mostCaptured = 0;
        legalMoves = new ArrayList<>(16);
        BitBoard bitBoard = BoardUtils.asBitBoardFast(position);
        myPawns = bitBoard.getColor() == EGDB_WHITE ? bitBoard.getWhite() : bitBoard.getBlack();
        enemyPawns = bitBoard.getColor() == EGDB_WHITE ? bitBoard.getBlack() : bitBoard.getWhite();
        myMen = myPawns & ~bitBoard.getKings();
        enemyMen = enemyPawns & ~bitBoard.getKings();
        pawns = myPawns | enemyPawns;
        freeFields = ~(pawns) & legalFields;
        myKings = myPawns & bitBoard.getKings();
        enemyKings = enemyPawns & bitBoard.getKings();
        color = bitBoard.getColor();
        board = position.getBoard();

        fillMovesWithCapture();
        if (mostCaptured > 0) {
            for (int i = fromMostCaptured; i <= last; i++) {
                legalMoves.add(new Move(toApplication(fromArr[i]), toApplication(toArr[i]), asList(capturedArr[i], mostCaptured), asList(jumpFieldArr[i], mostCaptured)));
            }
        } else {
            fillWithNormalMoves();
        }
        return legalMoves;
    }

    public List<Move> getCaptureMoves(Position position) {
        last = -1;
        fromMostCaptured = -1;
        mostCaptured = 0;
        legalMoves = new ArrayList<>(16);
        BitBoard bitBoard = BoardUtils.asBitBoardFast(position);
        myPawns = bitBoard.getColor() == EGDB_WHITE ? bitBoard.getWhite() : bitBoard.getBlack();
        enemyPawns = bitBoard.getColor() == EGDB_WHITE ? bitBoard.getBlack() : bitBoard.getWhite();
        myMen = myPawns & ~bitBoard.getKings();
        enemyMen = enemyPawns & ~bitBoard.getKings();
        pawns = myPawns | enemyPawns;
        freeFields = ~(pawns) & legalFields;
        myKings = myPawns & bitBoard.getKings();
        enemyKings = enemyPawns & bitBoard.getKings();
        color = bitBoard.getColor();
        board = position.getBoard();

        fillMovesWithCapture();
        if (mostCaptured > 0) {
            for (int i = fromMostCaptured; i <= last; i++) {
                legalMoves.add(new Move(toApplication(fromArr[i]), toApplication(toArr[i]), asList(capturedArr[i], mostCaptured), asList(jumpFieldArr[i], mostCaptured)));
            }
        }
        return legalMoves;
    }

    private void fillWithNormalMoves() {
        fillMovesOfKings();
        fillMovesOfPawns();
    }

    private void fillMovesOfPawns() {
        int leftOffset = 5;
        int rightOffset = 6;
        if (color == EGDB_WHITE) {
            long toFields = (myMen >> leftOffset) & freeFields;
            while (toFields != 0) {
                long to = toFields & -toFields;
                toFields -= to;
                int fieldTo = Long.numberOfTrailingZeros(to);
                legalMoves.add(new Move(toApplication(fieldTo + leftOffset), toApplication(fieldTo)));
            }
            toFields = (myMen >> rightOffset) & freeFields;
            while (toFields != 0) {
                long to = toFields & -toFields;
                toFields -= to;
                int fieldTo = Long.numberOfTrailingZeros(to);
                legalMoves.add(new Move(toApplication(fieldTo + rightOffset), toApplication(fieldTo)));
            }
        } else {
            long toFields = (myMen << leftOffset) & freeFields;
            while (toFields != 0) {
                long to = toFields & -toFields;
                toFields -= to;
                int fieldTo = Long.numberOfTrailingZeros(to);
                legalMoves.add(new Move(toApplication(fieldTo - leftOffset), toApplication(fieldTo)));
            }
            toFields = (myMen << rightOffset) & freeFields;
            while (toFields != 0) {
                long to = toFields & -toFields;
                toFields -= to;
                int fieldTo = Long.numberOfTrailingZeros(to);
                legalMoves.add(new Move(toApplication(fieldTo - rightOffset), toApplication(fieldTo)));
            }
        }
    }

    private void fillMovesOfKings() {
        long kings = myKings;
        while (kings != 0) {
            int originalField = Long.numberOfTrailingZeros(kings);
            int field = toApplication(originalField);
            for (int offset : OFFSETS) {
                int i = 1;
                while (board[field + offset * i] == BoardUtils.FREE_FIELD) {
                    legalMoves.add(new Move(field, field + offset * i));
                    i++;
                }
            }
            kings &= ~(1L << originalField);
        }
    }

    private void fillMovesWithCapture() {
        fillMovesWithPawnCapture();
        fillMovesWithKingCapture();
    }

    private void fillMovesWithPawnCapture() {
        int leftOffset = 5;
        int rightOffset = 6;
        long toBits = (myMen >> leftOffset * 2) & (enemyPawns >> leftOffset) & freeFields;
        while (toBits != 0L) {
            long to = toBits & -toBits;
            toBits -= to;
            long start = to << 2 * leftOffset;
            fillMovesWithPawnCapture(start, to, to << leftOffset, 0L, freeFields | start);
        }
        toBits = (myMen >> rightOffset * 2) & (enemyPawns >> rightOffset) & freeFields;
        while (toBits != 0L) {
            long to = toBits & -toBits;
            toBits -= to;
            long start = to << 2 * rightOffset;
            fillMovesWithPawnCapture(start, to, to << rightOffset, 0L, freeFields | start);
        }
        toBits = (myMen << leftOffset * 2) & (enemyPawns << leftOffset) & freeFields;
        while (toBits != 0L) {
            long to = toBits & -toBits;
            toBits -= to;
            long start = to >> 2 * leftOffset;
            fillMovesWithPawnCapture(start, to, to >> leftOffset, 0L, freeFields | start);
        }
        toBits = (myMen << rightOffset * 2) & (enemyPawns << rightOffset) & freeFields;
        while (toBits != 0L) {
            long to = toBits & -toBits;
            toBits -= to;
            long start = to >> 2 * rightOffset;
            fillMovesWithPawnCapture(start, to, to >> rightOffset, 0L, freeFields | start);
        }
    }

    private void fillMovesWithPawnCapture(long startField, long currentField, long captured, long jumpFields, long freeAndStart) {
        long enemyAndCaptured = enemyPawns - captured;
        jumpFields |= currentField;
        if (((currentField << 12) & (enemyAndCaptured << 6) & (freeAndStart)) != 0) {
            fillMovesWithPawnCapture(startField, currentField << 12, captured | (currentField << 6), jumpFields, freeAndStart);
        }
        if (((currentField << 10) & (enemyAndCaptured << 5) & (freeAndStart)) != 0) {
            fillMovesWithPawnCapture(startField, currentField << 10, captured | (currentField << 5), jumpFields, freeAndStart);
        }
        if (((currentField >> 10) & (enemyAndCaptured >> 5) & (freeAndStart)) != 0) {
            fillMovesWithPawnCapture(startField, currentField >> 10, captured | (currentField >> 5), jumpFields, freeAndStart);
        }
        if (((currentField >> 12) & (enemyAndCaptured >> 6) & (freeAndStart)) != 0) {
            fillMovesWithPawnCapture(startField, currentField >> 12, captured | (currentField >> 6), jumpFields, freeAndStart);
        }
        jumpFields &= ~(currentField);
        updateLongestMoves(startField, currentField, captured, jumpFields);
    }

    private void updateLongestMoves(long startField, long currentField, long captured, long jumpFields) {
        int capturedSize = Long.bitCount(captured);
        if (capturedSize > 0 && capturedSize >= mostCaptured) {
            int start = Long.numberOfTrailingZeros(startField);
            int current = Long.numberOfTrailingZeros(currentField);
            last++;
            capturedArr[last] = captured;
            jumpFieldArr[last] = jumpFields;
            figureArr[last] = board[start];
            fromArr[last] = start;
            toArr[last] = current;
            if (capturedSize > mostCaptured) {
                fromMostCaptured = last;
                mostCaptured = capturedSize;
            }
        }
    }

    private void fillMovesWithKingCapture() {
        long kings = myKings;
        while (kings != 0) {
            int field = Long.numberOfTrailingZeros(kings);
            fillMovesWithKingCapture(1L << field, 1L << field, 0L, freeFields | (1L << field), 0L, 0L);
            kings &= ~(1L << field);
        }
    }

    private void fillMovesWithKingCapture(long startField, long currentField, long captured, long freeAndStart, long jumpFields, long lastDir) {
        long enemyAndCaptured = enemyPawns - captured;
        jumpFields |= currentField;
        int k = 6;
        if (lastDir != 6) {
            while (((currentField << k) & myPawns) == 0 && (currentField << k & legalFields) != 0 && (((currentField << k) & enemyPawns) == 0)) {
                k += 6;
            }
            if (((currentField << k) & myPawns) == 0 && (currentField << k & enemyAndCaptured) != 0) {
                for (long i = k + 6; Long.numberOfTrailingZeros(currentField << i) < 55; i += 6) {
                    if (((currentField << i) & (freeAndStart)) != 0) {
                        fillMovesWithKingCapture(startField, currentField << i, captured | (currentField << k), freeAndStart, jumpFields, -6);
                    } else {
                        break;
                    }
                }
            }
        }
        k = 5;
        if (lastDir != 5) {
            while (((currentField << k) & myPawns) == 0 && (currentField << k & legalFields) != 0 && (((currentField << k) & enemyPawns) == 0)) {
                k += 5;
            }
            if (((currentField << k) & myPawns) == 0 && (currentField << k & enemyAndCaptured) != 0) {
                for (long i = k + 5; Long.numberOfTrailingZeros(currentField << i) < 55; i += 5) {
                    if (((currentField << i) & (freeAndStart)) != 0) {
                        fillMovesWithKingCapture(startField, currentField << i, captured | (currentField << k), freeAndStart, jumpFields, -5);
                    } else {
                        break;
                    }
                }
            }
        }
        k = 5;
        if (lastDir != -5) {
            while (((currentField >> k) & myPawns) == 0 && (currentField >> k & legalFields) != 0 && (((currentField >> k) & enemyPawns) == 0)) {
                k += 5;
            }
            if (((currentField >> k) & myPawns) == 0 && (currentField >> k & enemyAndCaptured) != 0) {
                for (long i = k + 5; currentField >> i != 0; i += 5) {
                    if (((currentField >> i) & (freeAndStart)) != 0) {
                        fillMovesWithKingCapture(startField, currentField >> i, captured | (currentField >> k), freeAndStart, jumpFields, 5);
                    } else {
                        break;
                    }
                }
            }
        }
        k = 6;
        if (lastDir != -6) {
            while (((currentField >> k) & myPawns) == 0 && (currentField >> k & legalFields) != 0 && (((currentField >> k) & enemyPawns) == 0)) {
                k += 6;
            }
            if (((currentField >> k) & myPawns) == 0 && (currentField >> k & enemyAndCaptured) != 0) {
                for (long i = k + 6; currentField >> i != 0; i += 6) {
                    if (((currentField >> i) & (freeAndStart)) != 0) {
                        fillMovesWithKingCapture(startField, currentField >> i, captured | (currentField >> k), freeAndStart, jumpFields, 6);
                    } else {
                        break;
                    }
                }
            }
        }
        jumpFields &= ~(currentField);
        jumpFields &= ~(startField);
        updateLongestMoves(startField, currentField, captured, jumpFields);
    }

    private List<Integer> asList(long captured, int capturedSize) {
        List<Integer> lst = new ArrayList<>(capturedSize);
        while (Long.numberOfTrailingZeros(captured) != 64L) {
            int field = Long.numberOfTrailingZeros(captured);
            int applicationField = toApplication(field);
            lst.add(applicationField);
            captured &= ~(1L << field);
        }
        return lst;
    }

    private int toApplication(int bitBoardField) {
        int cnt = 1;
        if (bitBoardField > 10) cnt--;
        if (bitBoardField > 21) cnt--;
        if (bitBoardField > 32) cnt--;
        if (bitBoardField > 43) cnt--;
        return notationTranslator.toApplicationField(bitBoardField + cnt);
    }

    private static int toStd(int bitBoardField) {
        int cnt = 1;
        if (bitBoardField > 10) cnt--;
        if (bitBoardField > 21) cnt--;
        if (bitBoardField > 32) cnt--;
        if (bitBoardField > 43) cnt--;
        return bitBoardField + cnt;
    }

    public static void main(String[] args) {
        Position position = new Position(BoardUtils.initialBoard(), Color.WHITE);
        ExtendedMoveGeneratorImpl moveGenerator = new ExtendedMoveGeneratorImpl();
        long start = System.nanoTime();
        int depth = 10;
        perft(moveGenerator, position, depth);
        System.out.println(Arrays.toString(arr));
        System.out.println("n = " + (n - 1)
                + ", time = " + Duration.ofNanos(System.nanoTime() - start).toMillis() + "ms"
                + ", KN/s = " + 1.0 * (n - 1) / (1.0 * (Math.max(Duration.ofNanos(System.nanoTime() - start).toSeconds(), 1))) / 1000.0);
    }

    static int n = 0;
    static long[] arr = new long[20];

    public static void perft(ExtendedMoveGeneratorImpl moveGenerator, Position position, int depth) {
        n++;
        arr[depth]++;
        if (depth == 1) {
            long moves = moveGenerator.getLegalMoves(position).size();
            n += moves;
            arr[depth - 1] += moves;
            return;
        }
        List<Move> legalMoves = moveGenerator.getLegalMoves(position);
        for (Move move : legalMoves) {
            Function<Position, Position> undoFunction = BoardUtils.undoFunction(position);
            BoardUtils.move(move, position);
            perft(moveGenerator, position, depth - 1);
            undoFunction.apply(position);
        }
    }
}