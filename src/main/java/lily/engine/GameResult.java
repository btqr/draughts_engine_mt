package lily.engine;

import java.util.List;

public class GameResult {
    private final Result result;
    private final List<Move> moves;

    public GameResult(Result result, List<Move> moves) {
        this.result = result;
        this.moves = moves;
    }

    public Result getResult() {
        return result;
    }

    public List<Move> getMoves() {
        return moves;
    }
}

