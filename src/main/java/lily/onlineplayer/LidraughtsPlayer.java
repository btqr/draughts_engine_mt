package lily.onlineplayer;

import lily.algorithm.Algorithm;
import lily.engine.Color;
import lily.engine.Move;
import lily.engine.Position;
import lily.io.NotationTranslator;
import lily.settings.AlgorithmSettings;
import lily.utils.BoardUtils;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static lily.utils.BoardUtils.*;


//24 % board size
public class LidraughtsPlayer implements NativeKeyListener {

    private final Algorithm slowAlgorithm;
    private final Algorithm fastAlgorithm;
    private Algorithm currentAlg;
    private int myColorKing = WHITE_KING;
    private int enemyColorKing = BLACK_KING;
    private int myColorPawn = WHITE_PAWN;
    private int enemyColorPawn = BLACK_PAWN;
    private final AtomicInteger moveCnt;

    public LidraughtsPlayer(Algorithm slowAlgorithm, Algorithm fastAlgorithm) {
        this.slowAlgorithm = slowAlgorithm;
        this.fastAlgorithm = fastAlgorithm;
        currentAlg = slowAlgorithm;
        moveCnt = new AtomicInteger(0);
    }

    public void play() throws AWTException {
        Robot robot = new Robot();
        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        Algorithm psvFast = fastAlgorithm;
        boolean canMove = true;
        while (true) {
            BufferedImage screenCapture = new Robot().createScreenCapture(screenRect);
            if (!isMyTurn(screenCapture) && !canMove) {
                canMove = true;
            }
            if (canMove && isMyTurn(screenCapture)) {
                NotationTranslator notationTranslator = new NotationTranslator();
                int[] board = readBoard(notationTranslator, screenCapture);
                screenCapture = new Robot().createScreenCapture(screenRect);
                int[] board2 = readBoard(notationTranslator, screenCapture);
                if (!Arrays.equals(board, board2)) {
                    screenCapture = new Robot().createScreenCapture(screenRect);
                    board = readBoard(notationTranslator, screenCapture);
                }
                Move move = currentAlg.nextMove(new Position(board, Color.WHITE));
                click(robot, notationTranslator.toStandardMove(move).getFrom());
                click(robot, notationTranslator.toStandardMove(move).getTo());
                canMove = false;
                moveCnt.incrementAndGet();
                if (moveCnt.get() == 50) {
                    currentAlg = psvFast;
                }
            }
        }
    }

    private boolean isMyTurn(BufferedImage screenCapture) {
        return screenCapture.getRGB(1630, 550) == -3087937 ||
                screenCapture.getRGB(1630, 550) == -1730407;

    }

    int[] readBoard(NotationTranslator notationTranslator, BufferedImage screenCapture) {
        int[] board = BoardUtils.emptyBoard();
        int currentY = 150;
        for (int i = 0; i < 10; i++) {
            int bonus = i % 2 * 51;
            int currentX = 1145;
            for (int j = 1; j <= 5; j++) {
                int color = screenCapture.getRGB(currentX - bonus, currentY);
                if ((color >> 16) < -250) {
                    board[notationTranslator.toApplicationField(5 * i + j)] = enemyColorPawn;
                }
                if ((color >> 16) > -5 && (color >> 16) < 5) {
                    board[notationTranslator.toApplicationField(5 * i + j)] = myColorPawn;
                }
                if ((color >> 16) > -160 && (color >> 16) < -10) {
                    int colorInternal = screenCapture.getRGB(currentX - bonus + 17, currentY);
                    if ((colorInternal >> 16) > -5 && (colorInternal >> 16) < 5) {
                        board[notationTranslator.toApplicationField(5 * i + j)] = myColorKing;
                    }
                    if ((colorInternal >> 16) < -250) {
                        board[notationTranslator.toApplicationField(5 * i + j)] = enemyColorKing;
                    }
                }

                currentX += 104;
            }
            currentY += 52;
        }
        int numberOfWhite = 0;
        int numberOfBlack = 0;
        int whiteKings = 0;
        int blackKings = 0;
        int whiteKingAdv = 0;
        int blackKingAdv = 0;
        for (int field : legalFields()) {
            if (board[field] == WHITE_PAWN) {
                numberOfWhite++;
            }
            if (board[field] == BLACK_PAWN) {
                numberOfBlack++;
            }
            if (board[field] == WHITE_KING) {
                whiteKings++;
            }
            if (board[field] == BLACK_KING) {
                blackKings++;
            }
        }
        whiteKingAdv = whiteKings > 0 && blackKings == 0 ? 1 : 0;
        blackKingAdv = blackKings > 0 && whiteKings == 0 ? 1 : 0;
        return board;
    }

    void click(Robot robot, int field) {
        int currentY = 150;
        for (int i = 0; i < 10; i++) {
            int bonus = i % 2 * 51;
            int currentX = 1145;
            for (int j = 1; j <= 5; j++) {
                if (i * 5 + j == field) {
                    robot.mouseMove(currentX - bonus, currentY);
                    robot.mousePress(InputEvent.BUTTON1_MASK);
                    robot.mouseRelease(InputEvent.BUTTON1_MASK);
                }
                currentX += 104;
            }
            currentY += 52;
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (NativeKeyEvent.getKeyText(e.getKeyCode()).equals("Slash")) {
            synchronized (this) {
                int tmpMyPawn = myColorPawn;
                int tmpMyKing = myColorKing;
                myColorKing = enemyColorKing;
                myColorPawn = enemyColorPawn;
                enemyColorKing = tmpMyKing;
                enemyColorPawn = tmpMyPawn;
                moveCnt.set(0);
                currentAlg = slowAlgorithm;
                System.out.println("Color switched");
            }
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {

    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {

    }

    public static void main(String[] args) throws AWTException {
        new Robot().mouseMove(1630, 550);
    }

    private static AlgorithmSettings createSettings() {
        return AlgorithmSettings.SettingsBuilder.aSettings()
                .withTimePerMove(Duration.ofMillis(600))
                .withEndgameDatabaseEnabled(true)
                .withHistoryMovesEnabled(true)
                .withQuiescenceSearchEnabled(true)
                .withBonusMovePerCaptureEnabled(true)
                .withLMREnabled(false)
                .build();
    }

    private static AlgorithmSettings createFastSettings() {
        return AlgorithmSettings.SettingsBuilder.aSettings()
                .withTimePerMove(Duration.ofMillis(200))
                .withEndgameDatabaseEnabled(false)
                .withHistoryMovesEnabled(true)
                .withQuiescenceSearchEnabled(true)
                .withBonusMovePerCaptureEnabled(true)
                .withLMREnabled(false)
                .build();
    }
}
