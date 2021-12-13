package lily.onlineplayer.lidraughtsAPI;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lily.algorithm.Algorithm;
import lily.algorithm.AlgorithmImpl;
import lily.engine.Move;
import lily.evaluation.PSTBasedEvaluation;
import lily.io.NotationTranslator;
import lily.movegenerator.ExtendedMoveGeneratorImpl;
import lily.settings.AlgorithmSettings;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class API {

    private final static String AUTH_TOKEN = "";

    static Algorithm algorithm = new AlgorithmImpl(new PSTBasedEvaluation(), new ExtendedMoveGeneratorImpl(), AlgorithmSettings.SettingsBuilder.aSettings()
            .withTimePerMove(Duration.ofMillis(1000))
            .withEndgameDatabaseEnabled(true)
            .withHistoryMovesEnabled(true)
            .withQuiescenceSearchEnabled(true)
            .withBonusMovePerCaptureEnabled(true)
            .withLMREnabled(true)
            .withLMRThreshold(2)
            .build());

    public static void main(String[] args) throws IOException, InterruptedException {
        while (true) {
            Thread.sleep(1000);
            play();
        }
    }

    private static void play() throws IOException, InterruptedException {
        JsonNode node = getGames();
        for (int i = 0; i < node.get("nowPlaying").size(); i++) {
            JsonNode game = node.get("nowPlaying").get(i);
            String gameId = game.get("gameId").asText();
            String fen = game.get("fen").asText();
            boolean isMyTurn = game.get("isMyTurn").asBoolean();
            if (isMyTurn) {
                Move move = algorithm.nextMove(new NotationTranslator().importPositionFromFen("W:" + fen));
                sendMoveAsync(gameId, move);
            }
        }
    }

    private static JsonNode getGames() throws IOException, InterruptedException {
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder(
                URI.create("https://lidraughts.org/api/account/playing"))
                .GET()
                .header("accept", "application/json")
                .header("Authorization", AUTH_TOKEN)
                .build();
        var responseString = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(responseString);
    }

    private static void sendMoveAsync(String gameId, Move move) {
        NotationTranslator notationTranslator = new NotationTranslator();
        List<Integer> fields = new ArrayList<>();
        fields.add(move.getFrom());
        var reversed = move.getJumpFieldPawns();
        Collections.reverse(reversed);
        fields.addAll(reversed);
        fields.add(move.getTo());


        int from = notationTranslator.toStandardField(fields.get(0));
        int to = notationTranslator.toStandardField(fields.get(1));
        String fromAsString = String.valueOf(from);
        String toAsString = String.valueOf(to);
        if (from < 10) {
            fromAsString = "0" + fromAsString;
        }
        if (to < 10) {
            toAsString = "0" + toAsString;
        }
        String moveAsString = fromAsString + "" + toAsString;

        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder(
                URI.create("https://lidraughts.org/api/bot/game/" + gameId + "/move/" + moveAsString))
                .POST(HttpRequest.BodyPublishers.noBody())
                .header("accept", "application/json")
                .header("Authorization", AUTH_TOKEN)
                .build();
        System.out.println("https://lidraughts.org/api/bot/game/" + gameId + "/move/" + moveAsString);
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

    }
}
