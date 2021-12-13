package lily.movegenerator;

import lily.engine.Move;
import lily.engine.Position;

import java.util.List;

public interface MoveGenerator {

    List<Move> getLegalMoves(Position position);

    List<Move> getCaptureMoves(Position position);
}
