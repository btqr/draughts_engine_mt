package lily.io;

import lily.engine.Color;
import lily.engine.Position;
import lily.utils.BoardUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static lily.utils.BoardUtils.*;

public class UpgradedSerializer {

    private final Path pathToFile;
    static int cnt = 0;
    static Map<String, Integer> whiteWins = new HashMap<>();
    static Map<String, Integer> draws = new HashMap<>();
    static Map<String, Integer> blackWins = new HashMap<>();
    static int whiteWinsTotal = 0;
    static int blackWinsTotal = 0;

    public UpgradedSerializer(Path pathToFile) {
        this.pathToFile = pathToFile;
    }

    private static void extractPatternsCollectStats(Position position, int result) {
        cnt++;
        if (cnt % 10000 == 0) System.out.println(cnt);
        StringBuilder patternString = new StringBuilder();
        int[][] patterns = getPatterns();
        int[] board = position.getBoard();
        for (int i = 0; i < patterns.length; i++) {
            int[] pattern = patterns[i];
            String patternAsString = "";
            for (int j = 0; j < pattern.length; j++) {
                if (board[pattern[j]] == WHITE_PAWN || board[pattern[j]] == WHITE_KING) patternAsString += "1";
                if (board[pattern[j]] == BLACK_PAWN || board[pattern[j]] == BLACK_KING) patternAsString += "2";
                if (board[pattern[j]] == FREE_FIELD) patternAsString += "0";
            }
            int index = i * 6561 + Integer.parseInt(patternAsString, 3);
            patternString.append(index + ";");
        }
        if (result == 1) {
            whiteWinsTotal++;
            if (whiteWins.containsKey(patternString.toString())) {
                whiteWins.put(patternString.toString(), whiteWins.get(patternString.toString()) + 1);
            } else {
                whiteWins.put(patternString.toString(), 1);
            }
        }
        if (result == 0) {
            if (draws.containsKey(patternString.toString())) {
                draws.put(patternString.toString(), draws.get(patternString.toString()) + 1);
            } else {
                draws.put(patternString.toString(), 1);
            }
        }
        if (result == -1) {
            blackWinsTotal++;
            if (blackWins.containsKey(patternString.toString())) {
                blackWins.put(patternString.toString(), blackWins.get(patternString.toString()) + 1);
            } else {
                blackWins.put(patternString.toString(), 1);
            }
        }
    }

    private static String extractPatternsWithTempoAndBalance(Position position, int result) {
        cnt++;
        if (cnt % 10000 == 0) System.out.println(cnt);
        StringBuilder patternString = new StringBuilder();
        int[][] patterns = getPatterns();
        int[] board = position.getBoard();
        for (int i = 0; i < patterns.length; i++) {
            int[] pattern = patterns[i];
            String patternAsString = "";
            for (int j = 0; j < pattern.length; j++) {
                if (board[pattern[j]] == WHITE_PAWN || board[pattern[j]] == WHITE_KING) patternAsString += "1";
                if (board[pattern[j]] == BLACK_PAWN || board[pattern[j]] == BLACK_KING) patternAsString += "2";
                if (board[pattern[j]] == FREE_FIELD) patternAsString += "0";
            }
            int index = i * 6561 + Integer.parseInt(patternAsString, 3);
            patternString.append(index + ";");
        }
        if (whiteWins.computeIfAbsent(patternString.toString(), (__) -> 0) > blackWins.computeIfAbsent(patternString.toString(), (__) -> 0) && whiteWins.computeIfAbsent(patternString.toString(), (__) -> 0) > draws.computeIfAbsent(patternString.toString(), (__) -> 0)) {
            result = 1;
        }
        if (blackWins.computeIfAbsent(patternString.toString(), (__) -> 0) > whiteWins.computeIfAbsent(patternString.toString(), (__) -> 0) && blackWins.computeIfAbsent(patternString.toString(), (__) -> 0) > draws.computeIfAbsent(patternString.toString(), (__) -> 0)) {
            result = -1;
        }
        if (draws.computeIfAbsent(patternString.toString(), (__) -> 0) > whiteWins.computeIfAbsent(patternString.toString(), (__) -> 0) && draws.computeIfAbsent(patternString.toString(), (__) -> 0) > blackWins.computeIfAbsent(patternString.toString(), (__) -> 0)) {
            result = 0;
        }
        patternString.append(BoardUtils.tempo(position, Color.WHITE) - BoardUtils.tempo(position, Color.BLACK));
        patternString.append(";");
        patternString.append(BoardUtils.countBalance(position, Color.WHITE) - BoardUtils.countBalance(position, Color.BLACK));
        patternString.append(";");
//        if (whiteWins.get())
        patternString.append(result);
        patternString.append("\n");
        return patternString.toString();
    }

    private static int asFigure(char figureAsString) {
        if (figureAsString == '0') return FREE_FIELD;
        if (figureAsString == '1') return WHITE_PAWN;
        if (figureAsString == '2') return BLACK_PAWN;
        throw new IllegalStateException("aaa");
    }

    private static int[][] getPatterns() {
        List<Integer> legalFields = legalFields();
        int[][] patterns = new int[28][8];
        int cntExt = 0;
        for (int i = 0; i < legalFields.size(); i++) {
            if (i > 0 && (i + 1) % 5 == 0 || legalFields.get(i) > 94) continue;
            int cnt = 0;
            for (int k = 0; k < 4; k++) {
                for (int j = 0; j < 2; j++) {
                    patterns[cntExt][cnt] = legalFields.get(i + j + k * 5);
                    cnt++;
                }
            }
            cntExt++;
        }
        return patterns;
    }


    private static void exportPositionsAsPatterns(String filePath, String pathToFile) throws IOException {
        NotationTranslator notationTranslator = new NotationTranslator();
        try (Stream<String> stream = Files.lines(Paths.get(filePath))) {
            stream.map(line -> {
                try {
                    int result;
                    String[] splittedLine = line.split(" ");
                    Position position = notationTranslator.importPositionFromFen(splittedLine[0]);
                    if (splittedLine[splittedLine.length - 1].equals("WHITE_WON")) {
                        result = 1;
                    } else if (splittedLine[splittedLine.length - 1].equals("DRAW")) {
                        result = 0;
                    } else {
                        result = -1;
                    }
                    return new TrainingPosition(position, result);
                } catch (Exception ex) {
                    return null;
                }
            }).filter(Objects::nonNull)
                    .filter(trainingPosition -> {
                        Position position = trainingPosition.getPosition();
                        return position.getNumberOfWhite() == position.getNumberOfBlack()
                                && position.getNumberOfWhiteKings() == 0
                                && position.getNumberOfBlackKings() == 0
                                && position.getNumberOfBlack() + position.getNumberOfWhite() > 7;
                    }).forEach(trainingPosition -> extractPatternsCollectStats(trainingPosition.position, trainingPosition.result));
        }
        AtomicInteger i = new AtomicInteger();
        cnt = 0;
        Set<Position> positions = new HashSet<>();
        try (Stream<String> stream = Files.lines(Paths.get(filePath))) {
            AtomicInteger whiteW = new AtomicInteger();
            AtomicInteger blackW = new AtomicInteger();
            List<String> patternList = new ArrayList<>();
            stream
                    .map(line -> {
                        try {
                            int result;
                            String[] splittedLine = line.split(" ");
                            Position position = notationTranslator.importPositionFromFen(splittedLine[0]);
                            if (splittedLine[splittedLine.length - 1].equals("WHITE_WON")) {
                                result = 1;
                            } else if (splittedLine[splittedLine.length - 1].equals("DRAW")) {
                                result = 0;
                            } else {
                                result = -1;
                            }
                            return new TrainingPosition(position, result);
                        } catch (Exception ex) {
                            i.getAndIncrement();
                            System.out.println(line);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .filter(trainingPosition -> !positions.contains(trainingPosition.position))
                    .filter(trainingPosition -> {
                        if (trainingPosition.result == -1) blackW.getAndIncrement();
                        if (trainingPosition.result == 1) whiteW.getAndIncrement();
                        Position position = trainingPosition.getPosition();
                        return position.getNumberOfWhite() == position.getNumberOfBlack()
                                && position.getNumberOfWhiteKings() == 0
                                && position.getNumberOfBlackKings() == 0
                                && position.getNumberOfBlack() + position.getNumberOfWhite() > 7;
                    })
                    .filter(trainingPosition -> trainingPosition.result != -1 || blackW.get() < Math.min(whiteWinsTotal, blackWinsTotal))
                    .filter(trainingPosition -> trainingPosition.result != 1 || whiteW.get() < Math.min(whiteWinsTotal, blackWinsTotal))
                    .forEach(trainingPosition -> {
                        positions.add(trainingPosition.position);
                        if (patternList.size() > 10000) {
                            String concatenated = StringUtils.join(patternList, "");
                            try {
                                Files.write(Paths.get(pathToFile), concatenated.getBytes(), StandardOpenOption.APPEND);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            patternList.clear();
                        }
                        String patterns = extractPatternsWithTempoAndBalance(trainingPosition.position, trainingPosition.result);
                        patternList.add(patterns);
                    });
            String concatenated = StringUtils.join(patternList, "");
            Files.write(Paths.get(pathToFile), concatenated.getBytes(), StandardOpenOption.APPEND);
        }
    }


    public static void main(String[] args) throws IOException {
        String fromPath = "";
        String toPath = "";
        exportPositionsAsPatterns(fromPath, toPath);
    }

    static class TrainingPosition {
        private final Position position;
        private final int result;

        public TrainingPosition(Position position, int result) {
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

