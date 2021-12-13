package lily.settings;

import java.time.Duration;

public class AlgorithmSettings {

    private final Duration timePerMove;
    private final boolean endgameDatabaseEnabled;
    private final boolean historyMovesEnabled;
    private final boolean quiescenceSearchEnabled;
    private final boolean bonusMovePerCaptureEnabled;
    private final boolean LMREnabled;
    private final int LMRThreshold;

    public AlgorithmSettings(Duration timePerMove, boolean endgameDatabaseEnabled, boolean historyMovesEnabled, boolean quiescenceSearchEnabled, boolean bonusMovePerCaptureEnabled, boolean LMREnabled, int LMRThreshold) {
        this.timePerMove = timePerMove;
        this.endgameDatabaseEnabled = endgameDatabaseEnabled;
        this.historyMovesEnabled = historyMovesEnabled;
        this.quiescenceSearchEnabled = quiescenceSearchEnabled;
        this.bonusMovePerCaptureEnabled = bonusMovePerCaptureEnabled;
        this.LMREnabled = LMREnabled;
        this.LMRThreshold = LMRThreshold;
    }

    public Duration getTimePerMove() {
        return timePerMove;
    }

    public boolean isEndgameDatabaseEnabled() {
        return endgameDatabaseEnabled;
    }

    public boolean isHistoryMovesEnabled() {
        return historyMovesEnabled;
    }

    public boolean isQuiescenceSearchEnabled() {
        return quiescenceSearchEnabled;
    }

    public boolean isBonusMovePerCaptureEnabled() {
        return bonusMovePerCaptureEnabled;
    }

    public boolean isLMREnabled() {
        return LMREnabled;
    }

    public int getLMRThreshold() {
        return LMRThreshold;
    }


    public static final class SettingsBuilder {
        private Duration timePerMove;
        private boolean endgameDatabaseEnabled;
        private boolean historyMovesEnabled;
        private boolean quiescenceSearchEnabled;
        private boolean bonusMovePerCaptureEnabled;
        private boolean LMREnabled;
        private int LMRThreshold;

        private SettingsBuilder() {
        }

        public static SettingsBuilder aSettings() {
            return new SettingsBuilder();
        }

        public SettingsBuilder withTimePerMove(Duration timePerMove) {
            this.timePerMove = timePerMove;
            return this;
        }

        public SettingsBuilder withEndgameDatabaseEnabled(boolean endgameDatabaseEnabled) {
            this.endgameDatabaseEnabled = endgameDatabaseEnabled;
            return this;
        }

        public SettingsBuilder withHistoryMovesEnabled(boolean historyMovesEnabled) {
            this.historyMovesEnabled = historyMovesEnabled;
            return this;
        }

        public SettingsBuilder withQuiescenceSearchEnabled(boolean quiescenceSearchEnabled) {
            this.quiescenceSearchEnabled = quiescenceSearchEnabled;
            return this;
        }

        public SettingsBuilder withBonusMovePerCaptureEnabled(boolean bonusMovePerCaptureEnabled) {
            this.bonusMovePerCaptureEnabled = bonusMovePerCaptureEnabled;
            return this;
        }

        public SettingsBuilder withLMREnabled(boolean LMREnabled) {
            this.LMREnabled = LMREnabled;
            return this;
        }

        public SettingsBuilder withLMRThreshold(int LMRThreshold) {
            this.LMRThreshold = LMRThreshold;
            return this;
        }

        public AlgorithmSettings build() {
            return new AlgorithmSettings(timePerMove, endgameDatabaseEnabled, historyMovesEnabled, quiescenceSearchEnabled, bonusMovePerCaptureEnabled, LMREnabled, LMRThreshold);
        }
    }
}
