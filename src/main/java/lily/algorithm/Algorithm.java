package lily.algorithm;

import lily.engine.Move;
import lily.engine.Position;

public interface Algorithm {

    Move nextMove(Position position);
}
