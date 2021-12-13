package lily.algorithm;

import lily.endgamedatabase.EndGameDatabaseDriverImpl;
import lily.engine.BitBoard;
import lily.engine.Color;
import lily.engine.Move;
import lily.engine.Position;
import lily.evaluation.Evaluation;
import lily.io.NotationTranslator;
import lily.movegenerator.MoveGenerator;
import lily.settings.AlgorithmSettings;
import lily.transpositiontable.FastTranspositionTable;
import lily.transpositiontable.HashResult;
import lily.transpositiontable.PositionInfo;
import lily.utils.BoardUtils;

import java.util.List;
import java.util.Optional;

import static lily.endgamedatabase.EndGameDatabaseDriverImpl.*;

public class AlgorithmImpl implements Algorithm {

    private static final String EGDB_PATH_WLD = "";
    private static final int MAX_DEPTH = 10000;
    private static final EndGameDatabaseDriverImpl egdb = EndGameDatabaseDriverImpl.getInstance();
    private static final long handle = egdb.open("maxpieces=7", 1000, EGDB_PATH_WLD);

    private final Evaluation evaluation;
    private final MoveGenerator moveGenerator;
    private final AlgorithmSettings algorithmSettings;
    private final FastTranspositionTable fastTranspositionTable = new FastTranspositionTable();
    private Move[][] killerMoves = new Move[20][2];
    private int[][] historyMoves = new int[130][130];

    private Double score = 0.0;
    private long startTime;
    private int nodeCounter;
    private boolean timeStopped;
    private boolean pvNode = false;

    private static int nodes = 0;

    public AlgorithmImpl(Evaluation evaluation, MoveGenerator moveGenerator, AlgorithmSettings algorithmSettings) {
        this.evaluation = evaluation;
        this.moveGenerator = moveGenerator;
        this.algorithmSettings = algorithmSettings;
    }

    private void initBeforeSearch() {
        this.nodeCounter = 0;
        this.fastTranspositionTable.reset();
        killerMoves = new Move[20][2];
        historyMoves = new int[130][130];
        this.timeStopped = false;
        this.startTime = System.nanoTime();
    }

    @Override
    public Move nextMove(Position position) {
        initBeforeSearch();
        Move bestMove = null;
        int depth = 0;
        for (int i = 1; i <= MAX_DEPTH; i++) {
            pvs(position.clone(), i, i, -100000.0, 100000.0, position.getColorToMove(), 0);

            if (isTimeFinished()) {
                depth = i - 1;
                break;
            }
            score = fastTranspositionTable.getCachedScore(position.getHashOfPosition(), position.getColorToMove(), depth, -400000, 500000);
            bestMove = fastTranspositionTable.getBestMove(position.getHashOfPosition(), position.getColorToMove());
        }
        if (bestMove == null) {
            System.out.println("No best move, depth = " + depth);
            return moveGenerator.getLegalMoves(position).get(0);
        }
        return bestMove;
    }

    public double getScore(Position position, int depth) {
        initBeforeSearch();
        Move bestMove = null;
        for (int i = 1; i <= depth; i++) {
            pvs(position.clone(), i, i, -100000.0, 100000.0, position.getColorToMove(), 0);
            score = fastTranspositionTable.getCachedScore(position.getHashOfPosition(), position.getColorToMove(), depth, -400000, 500000);
            bestMove = fastTranspositionTable.getBestMove(position.getHashOfPosition(), position.getColorToMove());
        }
        if (bestMove == null) {
            System.out.println("No best move, depth = " + depth);
            return -10;
        }
        System.out.println("Depth: " + depth);
        System.out.println("    Number of nodes: " + nodes);
        System.out.println("    Time: " + 1.0 * (System.nanoTime() - startTime) / 1_000_000_000);
        System.out.println("    Move: " + new NotationTranslator().toStandardMove(bestMove));
        System.out.println("    Score: " + score);
        System.out.println();
        if (score == null) return -1234.0;
        return score;
    }

    private boolean isTimeFinished() {
        return System.nanoTime() - startTime > algorithmSettings.getTimePerMove().toNanos();
    }

    private double pvs(Position position, int startDepth, int depth, double alpha, double beta, Color maximazingColor, int bonusDepth) {
        pvNode = alpha + 1 < beta;
        nodes++;
        if (nodeCounter % 5000 == 0 && isTimeFinished()) {
            timeStopped = true;
            return 0;
        }

        if (bonusDepth + depth <= 0) {
            nodeCounter++;
            if (algorithmSettings.isQuiescenceSearchEnabled()) {
                return quiescence(alpha, beta, position, startDepth - depth);
            } else {
                return evaluation.evaluate(position, position.getColorToMove());
            }
        }

        Double cachedScore = fastTranspositionTable.getCachedScore(position.getHashOfPosition(), position.getColorToMove(), depth, alpha, beta);
        if (cachedScore != null && !pvNode) {
            return cachedScore;
        }

        Move bestMove = fastTranspositionTable.getBestMove(position.getHashOfPosition(), position.getColorToMove());

        if (bestMove == null) {
            if (depth + bonusDepth > 1 && position.getNumberOfWhite() + position.getNumberOfBlack() > 8) {
                for (int i = 1; i <= Math.min(depth + bonusDepth - 1, 2); i++) {
                    pvs(position, i, i, -100000, 100000, maximazingColor, 0);
                }
                bestMove = fastTranspositionTable.getBestMove(position.getHashOfPosition(), position.getColorToMove());
            }
        }

        List<Move> moves = moveGenerator.getLegalMoves(position);
        sortMoves(moves, bestMove, position.getColorToMove(), startDepth - depth, position);
        if (moves.isEmpty()) {
            return -10000000.0;
        }

        if (algorithmSettings.isBonusMovePerCaptureEnabled() && !moves.get(0).getCapturedPawns().isEmpty()) {
            bonusDepth++;
        }

        if (algorithmSettings.isEndgameDatabaseEnabled()) {
            Optional<Double> score = getScoreFromEndgameDatabase(moves, startDepth, depth, position);
            if (score.isPresent()) {
                return score.get();
            }
        }

        PositionInfo calculatedPositionInfo = calculatePositionInfo(moves, position, startDepth, depth, alpha, beta,
                maximazingColor, bonusDepth);
        if (!timeStopped && calculatedPositionInfo.getHashResult() != HashResult.ALPHA) {
            fastTranspositionTable.writeEntry(position.getHashOfPosition(), calculatedPositionInfo);
        }

        return calculatedPositionInfo.getValue();
    }

    private void sortMoves(List<Move> moves, Move bestMove, Color colorToMove, int totalDepth, Position position) {
        moves.sort((m1, m2) -> {
            if (m1.equals(bestMove)) {
                return -1;
            }
            if (m2.equals(bestMove)) {
                return 1;
            }
            if (totalDepth < 20) {
                if (m1.equals(killerMoves[totalDepth][0])) {
                    return -1;
                }
                if (m2.equals(killerMoves[totalDepth][0])) {
                    return 1;
                }
                if (m1.equals(killerMoves[totalDepth][1])) {
                    return -1;
                }
                if (m2.equals(killerMoves[totalDepth][1])) {
                    return 1;
                }
            }
            int res = historyMoves[m1.getFrom()][m1.getTo()] - historyMoves[m2.getFrom()][m2.getTo()];
            if (res != 0) return -res;
            if (colorToMove == Color.WHITE) {
                return m2.getFrom() - m1.getFrom();
            } else {
                return m1.getFrom() - m2.getFrom();
            }
        });
    }

    private PositionInfo calculatePositionInfo(List<Move> moves, Position position, int startDepth, int depth,
                                               double alpha, double beta, Color maximazingColor, int bonusDepth) {
        int i;
        Move bestMove = null;
        HashResult hashResult = HashResult.ALPHA;
        for (i = 0; i < moves.size(); i++) {
            Move move = moves.get(i);
            var undoFunction = BoardUtils.undoFunction(position);
            BoardUtils.move(move, position);
            double newCandidateValue;
            if (i == 0) {
                newCandidateValue = -pvs(position, startDepth, depth - 1, -beta, -alpha, maximazingColor, bonusDepth);
            } else if (!algorithmSettings.isLMREnabled()) {
                newCandidateValue = -pvs(position, startDepth, depth - 1, -alpha - 0.1, -alpha, maximazingColor, bonusDepth);
                if (alpha < newCandidateValue && newCandidateValue < beta) {
                    newCandidateValue = -pvs(position, startDepth, depth - 1, -beta, -alpha, maximazingColor, bonusDepth);
                }
            } else {
                if (startDepth > 2 && i > algorithmSettings.getLMRThreshold() && !pvNode) {
                    newCandidateValue = -pvs(position, startDepth, depth - 2, -alpha - 0.1, -alpha, maximazingColor, bonusDepth);
                } else {
                    newCandidateValue = -pvs(position, startDepth, depth - 1, -alpha - 0.1, -alpha, maximazingColor, bonusDepth);
                }
                if (alpha < newCandidateValue && newCandidateValue < beta) {
                    newCandidateValue = -pvs(position, startDepth, depth - 1, -beta, -alpha, maximazingColor, bonusDepth);
                }
            }
            if (newCandidateValue > alpha) {
                if (newCandidateValue >= beta) {
                    if (algorithmSettings.isHistoryMovesEnabled() && move.getCapturedPawns().size() == 0 && startDepth - depth < 20) {
                        historyMoves[move.getFrom()][move.getTo()] += (startDepth - depth) * (startDepth - depth);
                        killerMoves[startDepth - depth][1] = killerMoves[startDepth - depth][0];
                        killerMoves[startDepth - depth][0] = move;
                    }
                    hashResult = HashResult.BETA;
                } else {
                    hashResult = HashResult.EXACT;
                }
            }
            if (bestMove == null || alpha < newCandidateValue) {
                bestMove = move;
            }
            alpha = Math.max(alpha, newCandidateValue);
            position = undoFunction.apply(position);
            if (alpha >= beta) {
                break;
            }
        }

        return new PositionInfo(bestMove, position.getColorToMove(), alpha, depth, hashResult, position.getHashOfPosition());

    }

    private Optional<Double> getScoreFromEndgameDatabase(List<Move> moves, int startDepth, int depth, Position position) {
        if (moves.get(0).getCapturedPawns().isEmpty()
                && (startDepth - depth == 1)
                && position.getNumberOfWhite() + position.getNumberOfBlack() <= 7) {
            Color originalColorToMove = position.getColorToMove();
            position.setColorToMove(Color.opposite(originalColorToMove));
            List<Move> psuedoMoves = moveGenerator.getLegalMoves(position);
            position.setColorToMove(originalColorToMove);
            if (psuedoMoves.size() > 0 && psuedoMoves.get(0).getCapturedPawns().isEmpty()) {
                BitBoard bitBoard = BoardUtils.asBitBoardFast(position);
                long eval = egdb.lookup(handle, bitBoard.getBlack(), bitBoard.getWhite(), bitBoard.getKings(), bitBoard.getColor(), 1);
                double evalBonus = evaluation.evaluate(position, position.getColorToMove());
                if (eval == EGDB_WIN) {
                    return Optional.of(10000000.0 + evalBonus);
                } else if (eval == EGDB_WIN_OR_DRAW) {
                    return Optional.of(10000.0 + evalBonus);
                } else if (eval == EGDB_LOSS) {
                    return Optional.of(-10000000.0 + evalBonus);
                } else if (eval == EGDB_DRAW_OR_LOSS) {
                    return Optional.of(-10000.0 + evalBonus);
                } else if (eval == EGDB_DRAW) {
                    return Optional.of(0.0 + evalBonus);
                }
            }
        }
        return Optional.empty();
    }

    private double quiescence(double alpha, double beta, Position position, int totalDepth) {
        double eval = evaluation.evaluate(position, position.getColorToMove());

        if (eval >= beta) return beta;
        if (eval > alpha) alpha = eval;

        List<Move> moves = moveGenerator.getCaptureMoves(position);
        if (moves.size() == 0 || moves.get(0).getCapturedPawns().size() == 0) {
            return alpha;
        }
        sortMoves(moves, null, position.getColorToMove(), totalDepth, position);
        Move newBestMove = null;
        HashResult hashResult = HashResult.ALPHA;

        for (Move move : moves) {
            var undoFunc = BoardUtils.undoFunction(position);
            BoardUtils.move(move, position);
            double newCandidateValue = -quiescence(-beta, -alpha, position, totalDepth + 1);
            undoFunc.apply(position);
            if (newCandidateValue > alpha) {
                hashResult = HashResult.EXACT;
                newBestMove = move;
                alpha = newCandidateValue;
                if (newCandidateValue >= beta) {
                    if (!timeStopped) {
                        PositionInfo positionInfo = new PositionInfo(move, position.getColorToMove(), beta, 0, HashResult.BETA, position.getHashOfPosition());
                        fastTranspositionTable.writeEntry(position.getHashOfPosition(), positionInfo);
                    }
                    return beta;
                }
            }
        }

        PositionInfo positionInfo = new PositionInfo(newBestMove, position.getColorToMove(), alpha, 0, hashResult, position.getHashOfPosition());
        if (!timeStopped && positionInfo.getHashResult() != HashResult.ALPHA) {
            fastTranspositionTable.writeEntry(position.getHashOfPosition(), positionInfo);
        }
        return alpha;
    }
}