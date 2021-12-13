package lily.io;

import lily.engine.Color;
import lily.engine.Move;
import lily.engine.Position;
import lily.engine.Result;
import lily.utils.BoardUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

public class NotationTranslator {

    private static final Map<Integer, Integer> applicationToStandard = Map.ofEntries(
            fld(14, 50), fld(16, 49), fld(18, 48), fld(20, 47), fld(22, 46),
            fld(25, 45), fld(27, 44), fld(29, 43), fld(31, 42), fld(33, 41),
            fld(38, 40), fld(40, 39), fld(42, 38), fld(44, 37), fld(46, 36),
            fld(49, 35), fld(51, 34), fld(53, 33), fld(55, 32), fld(57, 31),

            fld(62, 30), fld(64, 29), fld(66, 28), fld(68, 27), fld(70, 26),
            fld(73, 25), fld(75, 24), fld(77, 23), fld(79, 22), fld(81, 21),

            fld(86, 20), fld(88, 19), fld(90, 18), fld(92, 17), fld(94, 16),
            fld(97, 15), fld(99, 14), fld(101, 13), fld(103, 12), fld(105, 11),
            fld(110, 10), fld(112, 9), fld(114, 8), fld(116, 7), fld(118, 6),
            fld(121, 5), fld(123, 4), fld(125, 3), fld(127, 2), fld(129, 1)
    );
    private static final Map<Integer, Integer> standardToApplication = applicationToStandard.entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

    private final static int[] applicationToStandardArray = new int[130];
    private final static int[] standardToApplicationArray = new int[130];

    static {
        applicationToStandard.keySet().forEach(key -> applicationToStandardArray[key] = applicationToStandard.get(key));
        standardToApplication.keySet().forEach(key -> standardToApplicationArray[key] = standardToApplication.get(key));
    }

    public int toApplicationField(int standardField) {
        int row = (standardField - 1) / 5;
        int odd = (row + 1) / 2;
        int even = row / 2;
        return 129 - (standardField - 1) * 2 - odd - 3 * even;
    }

    public int toStandardField(int applicationField) {
        return applicationToStandardArray[applicationField];
    }

    public Move toStandardMove(Move move) {
        return translateMove(move, applicationToStandard);
    }

    public Move toApplicationMove(Move move) {
        return translateMove(move, standardToApplication);
    }

    private Move translateMove(Move move, Map<Integer, Integer> translationMap) {
        int translatedFrom = translationMap.get(move.getFrom());
        int translatedTo = translationMap.get(move.getTo());
        List<Integer> translatedCapturedPawns = move.getCapturedPawns().stream()
                .map(translationMap::get)
                .collect(Collectors.toList());
        return new Move(translatedFrom, translatedTo, translatedCapturedPawns);
    }

    public int[] importBoardFromFen(String fen) {
        String[] splitted = fen.split(":");
        String firstColor = splitted[1];
        String secondColor = splitted[2];
        int[] board = BoardUtils.emptyBoard();
        if (firstColor.charAt(0) == 'W') {
            add(board, firstColor.substring(1), Color.WHITE);
            add(board, secondColor.substring(1), Color.BLACK);
        } else {
            add(board, firstColor.substring(1), Color.BLACK);
            add(board, secondColor.substring(1), Color.WHITE);
        }
        return board;
    }

    public Position importPositionFromFen(String fen) {
        int[] board = importBoardFromFen(fen);
        Color colorToMove = fen.split(":")[0].equals("W") ? Color.WHITE : Color.BLACK;
        return new Position(board, colorToMove);
    }

    public String exportToFen(Position position) {
        String fen = "";
        if (position.getColorToMove() == Color.WHITE) {
            fen += "W:W";
        } else {
            fen += "B:W";
        }
        int[] board = position.getBoard();
        for (int field : BoardUtils.legalFields()) {
            if (board[field] == BoardUtils.WHITE_PAWN) {
                fen += toStandardField(field) + ",";
            } else if (board[field] == BoardUtils.WHITE_KING) {
                fen += "K" + toStandardField(field) + ",";
            }
        }
        fen = fen.substring(0, fen.length() - 1);
        fen += ":B";
        for (int field : BoardUtils.legalFields()) {
            if (board[field] == BoardUtils.BLACK_PAWN) {
                fen += toStandardField(field) + ",";
            } else if (board[field] == BoardUtils.BLACK_KING) {
                fen += "K" + toStandardField(field) + ",";
            }
        }
        return fen.substring(0, fen.length() - 1);
    }

    public List<Integer> occupiedStandarizedFields(int[] board, Color color) {
        List<Integer> legalFields = BoardUtils.legalFields();
        List<Integer> occupiedFields = new ArrayList<>();
        for (int field : legalFields) {
            if (color == Color.WHITE && (board[field] == BoardUtils.WHITE_PAWN || board[field] == BoardUtils.WHITE_KING)) {
                occupiedFields.add(toStandardField(field));
            } else if (color == Color.BLACK && (board[field] == BoardUtils.BLACK_PAWN || board[field] == BoardUtils.BLACK_KING)) {
                occupiedFields.add(toStandardField(field));
            }
        }
        return occupiedFields;
    }

    public String toPDN(List<Move> moves) {
        StringBuilder pgn = new StringBuilder();
        for (int i = 1; i <= moves.size(); i++) {
            if ((i + 1) % 2 == 0) {
                pgn.append((i + 1) / 2 + ". ");
            }
            pgn.append(toStandardMove(moves.get(i - 1)).toString());
            pgn.append(" ");
        }
        return pgn.toString();
    }

    private void add(int[] board, String fenForColor, Color color) {
        String[] fields = fenForColor.split(",");
        for (String field : fields) {
            if (field.charAt(0) == 'G') {
                continue;
            } else if (field.charAt(0) == 'K') {
                int applicationField = toApplicationField(Integer.parseInt(field.substring(1)));
                board[applicationField] = Color.kingCodeForColor(color);
            } else {
                int applicationField = toApplicationField(Integer.parseInt(field));
                board[applicationField] = Color.pawnCodeForColor(color);
            }
        }
    }

    private static Map.Entry<Integer, Integer> fld(int key, int value) {
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    public void positionsFromPDNToLabeledFile(String inputPdnFilePath, String outputFilePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(inputPdnFilePath));
        File outputFile = new File(outputFilePath);
        List<PositionWithResult> positionFromGame = new ArrayList<>();
        for (int i = 0; i < lines.size(); i += 7) {
            if (i % 1000 == 0) {
                Files.writeString(Paths.get(outputFilePath), positionFromGame.stream()
                        .map(positionWithResult -> exportToFen(positionWithResult.getPosition()) + " " + positionWithResult.getResult().name() + "\n")
                        .collect(Collectors.joining()), StandardOpenOption.APPEND);
                System.out.println(i);
                positionFromGame.clear();
            }
            positionFromGame.addAll(extractPositionFromGame(lines.subList(i, i + 7)));
        }
    }

    private List<PositionWithResult> extractPositionFromGame(List<String> lines) {
        char score = lines.get(3).split(" ")[1].charAt(1);
        String movesLine = lines.get(5);
        Result result;
        if (score == '2') {
            result = Result.WHITE_WON;
        } else if (score == '1') {
            result = Result.DRAW;
        } else {
            result = Result.BLACK_WON;
        }
        List<Move> moves = Arrays.stream(movesLine.split(" "))
                .filter(word -> word.contains("-") || word.contains("x"))
                .filter(word -> !word.equals("1-1"))
                .filter(word -> !word.equals("2-0"))
                .filter(word -> !word.equals("0-2"))
                .map(word -> {
                    String[] splitted = word.split("x|-");
                    return new Move(Integer.parseInt(splitted[0]), Integer.parseInt(splitted[splitted.length - 1]));
                })
                .collect(Collectors.toList());
        List<PositionWithResult> positionWithResults = new ArrayList<>();
        positionWithResults.add(new PositionWithResult(new Position(BoardUtils.initialBoard(), Color.WHITE), result));
        for (Move move : moves) {
            PositionWithResult last = positionWithResults.get(positionWithResults.size() - 1);
            Position clonedPosition = last.getPosition().clone();
            BoardUtils.move(toApplicationMove(move), clonedPosition);
            PositionWithResult newOne = new PositionWithResult(clonedPosition, result);
            positionWithResults.add(newOne);
        }
        return positionWithResults;
    }
}
