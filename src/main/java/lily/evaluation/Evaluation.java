package lily.evaluation;

import lily.engine.Color;
import lily.engine.Position;

public interface Evaluation {

    double evaluate(Position position, Color color);
}
