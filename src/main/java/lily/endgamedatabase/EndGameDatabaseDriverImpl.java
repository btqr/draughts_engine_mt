package lily.endgamedatabase;

/**
 * @author Jan-Jaap van Horssen
 */
public class EndGameDatabaseDriverImpl implements EndGameDatabaseDriver {

    private static final String DLL_NAME = "kregdb";

    static {
        System.loadLibrary(DLL_NAME);
    }

    public static final int EGDB_BLACK = 0;
    public static final int EGDB_WHITE = 1;

    public static final int EGDB_WIN = 1;
    public static final int EGDB_LOSS = 2;
    public static final int EGDB_DRAW = 3;
    public static final int EGDB_DRAW_OR_LOSS = 4;
    public static final int EGDB_WIN_OR_DRAW = 5;


    private native long egdbOpen(String options, int cache_mb, String dir);

    private native int egdbLookup(long handle, long black, long white, long king, int color, int cl);

    private static EndGameDatabaseDriverImpl instance;

    public static EndGameDatabaseDriverImpl getInstance() {
        if (instance == null) {
            instance = new EndGameDatabaseDriverImpl();
        }
        return instance;
    }

    private EndGameDatabaseDriverImpl() {
    }

    /**
     * Open egdb driver for the database referred to by 'dir'.
     * A log file should be opened first by calling openLog().
     *
     * @param options  Kingsrow egdb driver options, e.g. "maxpieces=6"
     * @param cache_mb cache size to be allocated in MB
     * @param dir      path to the Kingsrow egdb files, such as "C:/Program Files/Kingsrow International/wld_database" or "C:/Program Files/Kingsrow International/mtc_database"
     * @return egdb driver handle
     */
    public long open(String options, int cache_mb, String dir) {
        return egdbOpen(options, cache_mb, dir);
    }

    /**
     * Lookup a value in the database for the position (black, white, king, color).
     *
     * @param handle egdb driver handle
     * @param black  bitboard with the black pieces
     * @param white  bitboard with the white pieces
     * @param king   bitboard with the king positions
     * @param color  color to move, EGDB_BLACK or EGDB_WHITE
     * @param cl     conditional lookup
     * @return lookup value, one of EGDB_SUBDB_UNAVAILABLE, EGDB_NOT_IN_CACHE, EGDB_UNKNOWN, EGDB_WIN, EGDB_LOSS, EGDB_DRAW, EGDB_DRAW_OR_LOSS, EGDB_WIN_OR_DRAW
     */
    public final int lookup(long handle, long black, long white, long king, int color, int cl) {
        return egdbLookup(handle, black, white, king, color, cl);
    }
}
