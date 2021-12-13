package tests;

import lily.endgamedatabase.EndGameDatabaseDriverImpl;
import lily.engine.BitBoard;
import lily.engine.Position;
import lily.io.NotationTranslator;
import lily.utils.BoardUtils;

public class EndgameDBTest {

    private static final String EGDB_PATH_WLD = "";
    private static final String LOG_FILE = "";

    public static void main(String[] args) {
        EndGameDatabaseDriverImpl egdb = EndGameDatabaseDriverImpl.getInstance();

        System.out.println("Initializing kingsrow egdb...");


        long handle = egdb.open("maxpieces=7", 1000, EGDB_PATH_WLD);
        if (handle == 0L) {
            System.out.println("kregdb: error: could not open egdb driver " + EGDB_PATH_WLD);
            return;
        }
        System.out.println("Kingsrow WLD egdb initialized (" + EGDB_PATH_WLD + ")");

        Position position = new NotationTranslator().importPositionFromFen("B:WK24,26:BK32,K33,38:H3:F70");
        BitBoard bitBoard = BoardUtils.asBitBoardFast(position);

        long res = egdb.lookup(handle, bitBoard.getBlack(), bitBoard.getWhite(), bitBoard.getKings(), bitBoard.getColor(), 1);
        System.out.println(res);

    }
}
