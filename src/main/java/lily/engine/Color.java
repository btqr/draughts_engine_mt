package lily.engine;

import lily.utils.BoardUtils;

public enum Color {
    WHITE,
    BLACK;

    public static boolean inColor(int figure, Color color) {
        if (color == WHITE) {
            return figure == BoardUtils.WHITE_PAWN || figure == BoardUtils.WHITE_KING;
        } else {
            return figure == BoardUtils.BLACK_PAWN || figure == BoardUtils.BLACK_KING;
        }
    }

    public static int kingCodeForColor(Color color) {
        if (color == WHITE) {
            return BoardUtils.WHITE_KING;
        } else {
            return BoardUtils.BLACK_KING;
        }
    }

    public static int pawnCodeForColor(Color color) {
        if (color == WHITE) {
            return BoardUtils.WHITE_PAWN;
        } else {
            return BoardUtils.BLACK_PAWN;
        }
    }

    public static Color opposite(Color color) {
        if (color == WHITE) {
            return BLACK;
        } else {
            return WHITE;
        }
    }
}
