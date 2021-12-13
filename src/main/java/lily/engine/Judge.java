package lily.engine;

import lily.algorithm.Algorithm;
import lily.io.SerializerPST;
import lily.movegenerator.MoveGeneratorImpl;
import lily.utils.BoardUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Judge {

    private final static String PST_FILE_PATH = "";

    private final MoveGeneratorImpl moveGenerator;
    private final SerializerPST serializer;

    public Judge(MoveGeneratorImpl moveGenerator) {
        this.moveGenerator = moveGenerator;
        this.serializer = new SerializerPST(new File(PST_FILE_PATH).toPath());
    }

    public GameResult playGame(Algorithm whitePlayer, Algorithm blackPlayer) {
        List<Move> moves = new ArrayList<>();
        List<Position> positions = new ArrayList<>();
        Position position = new Position(BoardUtils.initialBoard(), Color.WHITE);
        while (moveGenerator.getLegalMoves(position).size() > 0) {
            List<Move> legalMoves = moveGenerator.getLegalMoves(position);
            positions.add(position.clone());
            Move move;
            if (position.getColorToMove() == Color.WHITE) {
                move = whitePlayer.nextMove(position.clone());
            } else {
                move = blackPlayer.nextMove(position.clone());
            }
            if (!legalMoves.contains(move)) {
                throw new RuntimeException("Invalid move!");
            }
            moves.add(move);
            BoardUtils.move(move, position);
            if (moves.size() > 200) {
                serializer.serializeGame(positions, new GameResult(Result.DRAW, moves));
                return new GameResult(Result.DRAW, moves);
            }
        }
        if (position.getColorToMove() == Color.WHITE) {
            serializer.serializeGame(positions, new GameResult(Result.BLACK_WON, moves));
            return new GameResult(Result.BLACK_WON, moves);
        } else {
            serializer.serializeGame(positions, new GameResult(Result.WHITE_WON, moves));
            return new GameResult(Result.WHITE_WON, moves);
        }

    }
}
