package tests;

import lily.engine.Color;
import lily.engine.Move;
import lily.engine.Position;
import lily.movegenerator.MoveGeneratorImpl;
import lily.utils.BoardUtils;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BoardUtilsTest {

    @Test
    public void shouldHashCorrectly() {
        Position position = new Position(BoardUtils.initialBoard(), Color.WHITE);
        long hashBefore = position.getHashOfPosition();
        MoveGeneratorImpl moveGenerator = new MoveGeneratorImpl();
        Move move = moveGenerator.getLegalMoves(position).get(0);
        var undoFunction = BoardUtils.undoFunction(position);
        BoardUtils.move(move, position);
        Position posAfter = undoFunction.apply(position);
        assertThat(posAfter.getHashOfPosition()).isEqualTo(hashBefore);
    }

    @Test
    public void shouldHashCorrectly2() {
        Position position = new Position(BoardUtils.initialBoard(), Color.WHITE);
        long hashBefore = position.getHashOfPosition();
        MoveGeneratorImpl moveGenerator = new MoveGeneratorImpl();
        Move move = moveGenerator.getLegalMoves(position).get(0);
        BoardUtils.move(move, position);
        BoardUtils.move(new Move(move.getTo(), move.getFrom()), position);
        assertThat(position.getHashOfPosition()).isEqualTo(hashBefore);
    }
}
