package lily.io;

import lily.engine.Position;
import lily.engine.Result;

public class PositionWithResult {

    private final Position position;
    private final Result result;

    public PositionWithResult(Position position, Result result) {
        this.position = position;
        this.result = result;
    }

    public Position getPosition() {
        return position;
    }

    public Result getResult() {
        return result;
    }
}
