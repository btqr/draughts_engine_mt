package lily.onlineplayer;

import lily.algorithm.Algorithm;
import lily.algorithm.AlgorithmImpl;
import lily.evaluation.PatternBasedEvaluation;
import lily.movegenerator.MoveGeneratorImpl;
import lily.settings.AlgorithmSettings;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

import java.awt.*;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.LogManager;

public class OnlinePlayerManager {

    public static void main(String[] args) throws AWTException, NativeHookException {
        GlobalScreen.registerNativeHook();
        LogManager.getLogManager().reset();
        Algorithm psv = new AlgorithmImpl(new PatternBasedEvaluation(), new MoveGeneratorImpl(), createSettings());
        Algorithm psvFast = new AlgorithmImpl(new PatternBasedEvaluation(), new MoveGeneratorImpl(), createFastSettings());
        LogManager.getLogManager().getLogger("global").setLevel(Level.OFF);

        Player player = new Player(psv, psvFast);
        GlobalScreen.addNativeKeyListener(player);
        player.play();
    }

    private static AlgorithmSettings createSettings() {
        return AlgorithmSettings.SettingsBuilder.aSettings()
                .withTimePerMove(Duration.ofMillis(2550))
                .withEndgameDatabaseEnabled(true)
                .withHistoryMovesEnabled(true)
                .withQuiescenceSearchEnabled(true)
                .withBonusMovePerCaptureEnabled(true)
                .withLMREnabled(true)
                .withLMRThreshold(3)
                .build();
    }

    private static AlgorithmSettings createFastSettings() {
        return AlgorithmSettings.SettingsBuilder.aSettings()
                .withTimePerMove(Duration.ofMillis(2550))
                .withEndgameDatabaseEnabled(true)
                .withHistoryMovesEnabled(true)
                .withQuiescenceSearchEnabled(true)
                .withBonusMovePerCaptureEnabled(true)
                .withLMREnabled(true)
                .withLMRThreshold(3)
                .build();
    }
}