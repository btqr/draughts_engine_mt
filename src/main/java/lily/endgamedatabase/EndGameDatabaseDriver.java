package lily.endgamedatabase;

public interface EndGameDatabaseDriver {

    int lookup(long handle, long black, long white, long king, int color, int cl);
}
