package lily.io;

import lily.engine.GameResult;
import lily.engine.Position;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class SerializerPST {

    private final Path pathToFile;
    private final NotationTranslator notationTranslator = new NotationTranslator();

    public SerializerPST(Path pathToFile) {
        this.pathToFile = pathToFile;
    }

    public void serialize(Position position, GameResult gameResult) {
        try {
            Files.write(pathToFile, extractPatternString(position, gameResult).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void serializeGame(List<Position> positions, GameResult gameResult) {
        positions.stream()
                .map(pos -> extractPatternString(pos, gameResult))
                .forEach(pString -> {
                    try {
                        Files.write(pathToFile, pString.getBytes(), StandardOpenOption.APPEND);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    private String extractPatternString(Position position, GameResult gameResult) {
        String serialized = notationTranslator.exportToFen(position);
        serialized += " ";
        serialized += gameResult.getResult().name();
        serialized += "\n";
        return serialized;
    }
}
