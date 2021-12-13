package tests;

import lily.engine.Color;
import lily.io.NotationTranslator;
import lily.utils.BoardUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;

public class MoveGeneratorTest {

    private NotationTranslator notationTranslator;

    @BeforeClass
    public void setUp() {
        notationTranslator = new NotationTranslator();
    }

    @DataProvider
    public Object[][] moveGeneratorTestData() {
        return new Object[][]{
                // no captures, no kings
                {BoardUtils.initialBoard(), Color.WHITE,
                        "31-26", "31-27", "32-27", "32-28", "33-28", "33-29", "34-29", "34-30", "35-30"},
                {BoardUtils.initialBoard(), Color.BLACK,
                        "16-21", "17-21", "17-22", "18-22", "18-23", "19-23", "19-24", "20-24", "20-25"},
                {notationTranslator
                        .importBoardFromFen("W:W24,27,31,32,34,36,37,38,39,40,41,42,43,44,45,47,48,49:B2,3,4,6,7,8,9,10,11,12,13,14,15,16,17,18,23,28:H0:F12"),
                        Color.WHITE,
                        "31-26", "27-21", "27-22", "24-19", "24-20", "38-33", "39-33", "34-29", "34-30", "40-35"},

                // no capture, kings
                {notationTranslator.importBoardFromFen("W:WK1,12,48:B28:H0:F1"), Color.WHITE,
                        "48-42", "48-43", "12-7", "12-8", "1-6", "1-7"},
                {notationTranslator.importBoardFromFen("W:W16,24,29,34,35,40,K45:BK47,K49"), Color.BLACK,
                        "49-44", "49-43", "49-38", "49-32", "49-27", "49-21", "47-42", "47-38", "47-33", "47-41", "47-36"},

                // capture, no kings
                {notationTranslator.importBoardFromFen("W:W44,46,50:B39"), Color.WHITE, "44x33"},
                {notationTranslator.importBoardFromFen("W:W43,44,46,48,50:B39"), Color.WHITE, "44x33", "43x34"},
                {notationTranslator.importBoardFromFen("W:W44,46,48,49,50:B9,10,18,28,29,38,39"), Color.WHITE, "44x15"},
                {notationTranslator.importBoardFromFen("W:W43,44,46,48,49,50:B8,9,10,19,29,39"), Color.WHITE,
                        "44x15", "43x12"},
                {notationTranslator.importBoardFromFen("W:W50:B31,32,33,34,41,42,43,44"), Color.WHITE,
                        "50x30", "50x30"},
                {notationTranslator.importBoardFromFen("W:W12,14,23,24:B3,9"), Color.BLACK, "9x7"},

                // capture with same field traversing, no kings
                {notationTranslator.importBoardFromFen("W:W7,8,12,14,17,18,23,24,33:B3,9,13"), Color.BLACK, "13x13", "13x13"},

                // capture with kings
                {notationTranslator.importBoardFromFen("W:WK48:B26,30"), Color.WHITE, "48x25"},
                {notationTranslator.importBoardFromFen("W:WK45,K48:B1,14,27,34"), Color.WHITE, "48x36", "48x31"},
                {notationTranslator.importBoardFromFen("W:W17,21,K48:B12,18,25,29,30,39"), Color.WHITE, "48x23"},
                {notationTranslator.importBoardFromFen("W:WK1,7,10,32,41:BK23"), Color.BLACK, "23x46"},
                {notationTranslator.importBoardFromFen("W:WK10:BK41"), Color.BLACK, "41x5"},

                // capture with kings, turkish
                {notationTranslator.importBoardFromFen("W:W13,23,24,28,34:B4,K9,10"), Color.BLACK, "9x19"},

                // other
                {notationTranslator
                        .importBoardFromFen("B:W27,28,31,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50:B1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,18,19,20,22:H0:F2"),
                        Color.BLACK,
                        "22x33"},
                {notationTranslator
                        .importBoardFromFen("B:W29,32,45:B3,5,6,7,11,13,14,15,25,30,36,K47:H0:F36"), Color.BLACK, "47x24", "47x20"},
                {notationTranslator
                        .importBoardFromFen("W:WK4,6,31,32,35,38,42,43,45,50:B17,18:H0:F37"), Color.WHITE, "4x11"},
        };
    }

//    @Test(dataProvider = "moveGeneratorTestData")
//    public void shouldReturnCorrectMoves(int[] board, Color color, String... expectedMoves) {
//        // given
//        MoveGenerator moveGenerator = new MoveGenerator();
//
//        // when
//        List<Move> legalMoves = moveGenerator.getLegalMoves(board, color);
//
//        // then
//        List<String> legalMovesAsStandarizedStrings = legalMoves.stream()
//                .map(notationTranslator::toStandardMove)
//                .map(Move::toString)
//                .collect(Collectors.toList());
//        assertThat(legalMovesAsStandarizedStrings)
//                .containsExactlyInAnyOrder(expectedMoves);
}
