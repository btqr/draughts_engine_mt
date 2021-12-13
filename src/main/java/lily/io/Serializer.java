package lily.io;

import lily.engine.Position;
import lily.movegenerator.MoveGeneratorImpl;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static lily.utils.BoardUtils.*;

public class Serializer {

    private final Path pathToFile;
    static int cnt = 0;
    static int[] patternsCnt = new int[270000];
    static MoveGeneratorImpl moveGenerator = new MoveGeneratorImpl();

    public Serializer(Path pathToFile) {
        this.pathToFile = pathToFile;
    }

    private static String extractFeatures(Position position, int result) {
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
            patternsCnt[index]++;
            patternString.append(index + ";");
        }
        patternString.append(position.getTempoDiff());
        patternString.append(";");
        patternString.append(position.getBalanceDiff());
        patternString.append(";");
        patternString.append((position.getNumberOfWhite() - position.getNumberOfWhiteKings()) - (position.getNumberOfBlack() - position.getNumberOfBlackKings()));
        patternString.append(";");
        patternString.append((position.getNumberOfWhiteKings() > 0 ? 1 : 0) - (position.getNumberOfBlackKings() > 0 ? 1 : 0));
        patternString.append(";");
        patternString.append(Math.max(position.getNumberOfWhiteKings() - 1, 0) - Math.max(position.getNumberOfBlackKings() - 1, 0));
        patternString.append(";");
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
        AtomicInteger i = new AtomicInteger();
        AtomicInteger whiteWins = new AtomicInteger();
        AtomicInteger draws = new AtomicInteger();
        AtomicInteger blackWins = new AtomicInteger();
        try (Stream<String> stream = Files.lines(Paths.get(filePath))) {
            List<String> patternList = new ArrayList<>();
            stream
                    .map(line -> {
                        try {
                            int result;
                            String[] splittedLine = line.split(" ");
                            Position position = notationTranslator.importPositionFromFen(splittedLine[0]);
                            if (splittedLine[splittedLine.length - 1].equals("WHITE_WON")) {
                                whiteWins.getAndIncrement();
                                result = 1;
                            } else if (splittedLine[splittedLine.length - 1].equals("DRAW")) {
                                draws.getAndIncrement();
                                result = 0;
                            } else {
                                blackWins.getAndIncrement();
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
                    .filter(trainingPosition -> moveGenerator.getCaptureMoves(trainingPosition.position).size() == 0)
//                    .filter(trainingPosition -> trainingPosition.result != 0 || draws.get() < 100)
                    .filter(trainingPosition -> {
                        Position position = trainingPosition.getPosition();
                        return position.getNumberOfBlack() == position.getNumberOfWhite() && position.getNumberOfWhiteKings() == position.getNumberOfBlackKings();
                    })
                    .filter(trainingPosition -> {
                        Position position = trainingPosition.getPosition();
                        return position.getNumberOfBlack() + position.getNumberOfWhite() >= 8
                                && position.getNumberOfBlack() + position.getNumberOfWhite() < 37;
                    }).forEach(trainingPosition -> {
                if (patternList.size() > 10000) {
                    String concatenated = StringUtils.join(patternList, "");
                    try {
                        Files.write(Paths.get(pathToFile), concatenated.getBytes(), StandardOpenOption.APPEND);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    patternList.clear();
                }
                String patterns = extractFeatures(trainingPosition.position, trainingPosition.result);
                patternList.add(patterns);
            });
            String concatenated = StringUtils.join(patternList, "");
            Files.write(Paths.get(pathToFile), concatenated.getBytes(), StandardOpenOption.APPEND);
        }
        System.out.println(whiteWins + " " + draws + " " + blackWins);
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
