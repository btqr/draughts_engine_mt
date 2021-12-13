package lily.optimization;

import lily.algorithm.Algorithm;
import lily.algorithm.AlgorithmImpl;
import lily.engine.GameResult;
import lily.engine.Judge;
import lily.engine.Result;
import lily.evaluation.PSTBasedEvaluation;
import lily.evaluation.WeightedEvaluation;
import lily.movegenerator.MoveGeneratorImpl;
import lily.settings.AlgorithmSettings;

import java.time.Duration;
import java.util.Arrays;

public class StepTrainer {

    public static void main(String[] args) {
        MoveGeneratorImpl moveGenerator = new MoveGeneratorImpl();
        Judge judge = new Judge(moveGenerator);
        double[] weights = new PSTBasedEvaluation().getWeights();
        for (int j = 0; j < 10000; j++) {
            WeightedEvaluation oldEval = new PSTBasedEvaluation();
            oldEval.setWeights(weights);
            WeightedEvaluation newEval = new PSTBasedEvaluation();
            double[] newWeights = newEval.getWeights();
            double delta = j % 2 == 0 ? 0.1 : -0.1;
            newWeights[j % 392] += delta;
            newEval.setWeights(newWeights);
            int oldComposition = 0;
            int newComposition = 0;
            for (int k = 0; k < 5; k++) {
                for (int i = 0; i < 10; i++) {
                    Algorithm first = new AlgorithmImpl(oldEval, moveGenerator, createSettings());
                    Algorithm second = new AlgorithmImpl(newEval, moveGenerator, createSettings());
                    GameResult gameResult = judge.playGame(first, second);
                    if (gameResult.getResult() == Result.WHITE_WON) oldComposition++;
                    if (gameResult.getResult() == Result.BLACK_WON) newComposition++;
                }
                for (int i = 0; i < 10; i++) {
                    Algorithm first = new AlgorithmImpl(newEval, moveGenerator, createSettings());
                    Algorithm second = new AlgorithmImpl(oldEval, moveGenerator, createSettings());
                    GameResult gameResult = judge.playGame(first, second);
                    if (gameResult.getResult() == Result.WHITE_WON) newComposition++;
                    if (gameResult.getResult() == Result.BLACK_WON) oldComposition++;
                }
                System.out.println("Old wins: " + oldComposition);
                System.out.println("New wins: " + newComposition);
            }
            if (newComposition > (oldComposition + 7)) {
                weights = newWeights;
                System.out.println(Arrays.toString(newWeights));
            }
        }
    }

    private static AlgorithmSettings createSettings() {
        return AlgorithmSettings.SettingsBuilder.aSettings()
                .withTimePerMove(Duration.ofMillis(100))
                .withEndgameDatabaseEnabled(false)
                .withHistoryMovesEnabled(true)
                .withQuiescenceSearchEnabled(true)
                .withBonusMovePerCaptureEnabled(true)
                .withLMREnabled(true)
                .withLMRThreshold(3)
                .build();
    }
}
