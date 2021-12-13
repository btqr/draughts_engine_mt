package lily.optimization;

import lily.engine.Color;
import lily.engine.Position;
import lily.evaluation.PSTBasedEvaluation;
import lily.evaluation.WeightedEvaluation;
import lily.io.NotationTranslator;
import lily.movegenerator.MoveGenerator;
import lily.movegenerator.MoveGeneratorImpl;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.MultivariateOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class PatternPSTTrainer {

    private static final NotationTranslator notationTranslator = new NotationTranslator();
    private static final String POSITIONS_PATH = "";

    public static void main(String[] args) throws IOException {
        MultivariateFunction function = new MultivariateFunction() {
            private final List<TrainingPosition> positionPatterns = readPositions(POSITIONS_PATH);
            private final WeightedEvaluation evaluation = new PSTBasedEvaluation();

            @Override
            public double value(double[] point) {
                evaluation.setWeights(point);
                double error = calculateError(positionPatterns.subList(0, positionPatterns.size()));
                double errorConf = calculateError(positionPatterns.subList(positionPatterns.size() - 2000, positionPatterns.size()));
                System.out.println(error + " " + errorConf + " " + Arrays.toString(point));
                return error;
            }

            private double calculateError(List<TrainingPosition> trainingPositions) {
                double error = 0.0;
                double c = 0.025;
                for (TrainingPosition position : trainingPositions) {
                    double score = evaluation.evaluate(position.position, Color.WHITE);
                    double fScore = 2.0 / (1.0 + Math.pow(Math.E, -c * score)) - 1;
                    error += ((fScore - position.result) * (fScore - position.result));
                }
                return error;
            }
        };

        var low = new double[392];
        var high = new double[392];
        var sigma = new double[392];
        Arrays.fill(low, -1000);
        Arrays.fill(high, 1000);
        Arrays.fill(sigma, 0.2);
        MultivariateOptimizer multivariateOptimizer = new BOBYQAOptimizer(900);
        PointValuePair optimize = multivariateOptimizer.optimize(
                new MaxEval(5000),
                GoalType.MINIMIZE,
                new InitialGuess(new PSTBasedEvaluation().getWeights()),
                new ObjectiveFunction(function),
                new SimpleBounds(low, high));
        System.out.println(Arrays.toString(optimize.getPoint()));
    }

    private static List<TrainingPosition> readPositions(String filePath) throws IOException {
        int i = 0;
        AtomicInteger wins = new AtomicInteger();
        AtomicInteger draws = new AtomicInteger();
        AtomicInteger loses = new AtomicInteger();
        MoveGenerator moveGenerator = new MoveGeneratorImpl();
        List<TrainingPosition> positionList = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)
                .map(line -> {
                    try {
                        double result;
                        String[] splittedLine = line.split(" ");
                        Position position = notationTranslator.importPositionFromFen(splittedLine[0]);
                        if (splittedLine[splittedLine.length - 1].equals("WHITE_WON")) {
                            result = 1.0;
                            wins.getAndIncrement();
                        } else if (splittedLine[splittedLine.length - 1].equals("DRAW")) {
                            result = 0.0;
                            draws.getAndIncrement();
                        } else {
                            result = -1.0;
                            loses.getAndIncrement();
                        }
                        return new TrainingPosition(position, result);
                    } catch (Exception ex) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(p -> moveGenerator.getLegalMoves(p.position).size() > 5)
                .filter(p -> moveGenerator.getLegalMoves(p.position).get(0).getCapturedPawns().size() == 0)
                .filter(p -> !(p.getPosition().getNumberOfWhite() != p.getPosition().getNumberOfBlack()
                        || p.getPosition().getNumberOfBlackKings() != 0
                        || p.getPosition().getNumberOfWhiteKings() != 0
                        || p.getPosition().getNumberOfWhite() + p.getPosition().getNumberOfBlack() < 8))
                .limit(200_000)
                .collect(Collectors.toList());
        System.out.println(i);
        System.out.println(wins.get());
        System.out.println(draws.get());
        System.out.println(loses.get());
        return new ArrayList<>(positionList);
    }

    static class TrainingPosition {
        private final Position position;
        private final double result;

        public TrainingPosition(Position position, double result) {
            this.position = position;
            this.result = result;
        }

        public Position getPosition() {
            return position;
        }

        public double getResult() {
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TrainingPosition position1 = (TrainingPosition) o;

            if (Double.compare(position1.result, result) != 0) return false;
            return position != null ? position.equals(position1.position) : position1.position == null;
        }

        @Override
        public int hashCode() {
            int result1;
            long temp;
            result1 = position != null ? position.hashCode() : 0;
            temp = Double.doubleToLongBits(result);
            result1 = 31 * result1 + (int) (temp ^ (temp >>> 32));
            return result1;
        }
    }
}
