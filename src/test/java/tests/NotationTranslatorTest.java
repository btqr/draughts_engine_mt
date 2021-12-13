package tests;

import lily.engine.Color;
import lily.engine.Move;
import lily.io.NotationTranslator;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class NotationTranslatorTest {

    @DataProvider
    public Object[][] translateMoveData() {
        return new Object[][]{
                // white no capturing
                {new Move(55, 66), new Move(32, 28)},
                {new Move(14, 25), new Move(50, 45)},

                // white with capturing
                {new Move(22, 44, List.of(33)), new Move(46, 37, List.of(41))},

                // black no capturing
                {new Move(90, 79), new Move(18, 22)},

                // black with capturing
                {new Move(118, 66, List.of(105, 79)), new Move(6, 28, List.of(11, 22))},
        };
    }

    @Test(dataProvider = "translateMoveData")
    public void shouldTranslateMove(Move moveBefore, Move expectedMove) {
        // when
        NotationTranslator notationTranslator = new NotationTranslator();
        Move moveAfter = notationTranslator.toStandardMove(moveBefore);

        // then
        assertThat(moveAfter).isEqualTo(expectedMove);
        assertThat(notationTranslator.toApplicationMove(moveAfter)).isEqualTo(moveBefore);
    }

    @Test
    public void shouldImportBoardFromFen() {
        // given
        NotationTranslator notationTranslator = new NotationTranslator();
        List<Integer> expectedOccupiedByWhite = List.of(4, 26, 27, 31, 32, 34, 35, 36, 37, 38, 39, 42, 43, 45, 48, 49);
        List<Integer> expectedOccupiedByBlack = List.of(3, 6, 8, 9, 11, 12, 13, 14, 16, 17, 18, 22, 23, 25);
        String fen = "B:WK4,26,27,31,32,34,35,36,37,38,39,42,43,45,48,49:B3,6,8,9,11,12,13,14,16,17,18,22,23,25:H0:F23";

        // when
        int[] board = notationTranslator.importBoardFromFen(fen);

        // then
        List<Integer> fieldsOccupiedByWhite = notationTranslator.occupiedStandarizedFields(board, Color.WHITE);
        List<Integer> fieldsOccupiedByBlack = notationTranslator.occupiedStandarizedFields(board, Color.BLACK);
        assertThat(fieldsOccupiedByWhite).containsExactlyInAnyOrderElementsOf(expectedOccupiedByWhite);
        assertThat(fieldsOccupiedByBlack).containsExactlyInAnyOrderElementsOf(expectedOccupiedByBlack);
    }
}
