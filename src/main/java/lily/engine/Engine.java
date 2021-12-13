package lily.engine;

import lily.algorithm.AlgorithmImpl;
import lily.evaluation.PSTBasedEvaluation;
import lily.movegenerator.MoveGeneratorImpl;
import lily.settings.AlgorithmSettings;
import lily.utils.BoardUtils;

import java.time.Duration;

public class Engine {
    public static void main(String[] args) {
        MoveGeneratorImpl moveGenerator = new MoveGeneratorImpl();
        var algo = new AlgorithmImpl(new PSTBasedEvaluation(), moveGenerator, createSettings());
        System.out.println(algo.nextMove(new Position(BoardUtils.initialBoard(), Color.WHITE)));
    }

    private static AlgorithmSettings createSettings() {
        return AlgorithmSettings.SettingsBuilder.aSettings()
                .withTimePerMove(Duration.ofMillis(10000))
                .withEndgameDatabaseEnabled(true)
                .withHistoryMovesEnabled(true)
                .withQuiescenceSearchEnabled(true)
                .withBonusMovePerCaptureEnabled(true)
                .withLMREnabled(true)
                .withLMRThreshold(3)
                .build();
    }
}
