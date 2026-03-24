package ui;

import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessBoard;

public class BoardUI {
    public static void drawBoard(ChessGame game, ChessGame.TeamColor perspective) {
        if (game == null) {
            System.out.println("No game to display.");
            return;
        }
        ChessBoard board = game.getBoard();
        System.out.print(EscapeSequences.ERASE_SCREEN);
        if (perspective == ChessGame.TeamColor.BLACK) {
            drawBlackView(board);
        } else {
            drawWhiteView(board);
        }
        System.out.print(EscapeSequences.RESET_BG_COLOR);
        System.out.print(EscapeSequences.RESET_TEXT_COLOR);
        System.out.println();
    }

    private static void drawWhiteView(ChessBoard board) {
        printFilesWhite();
        for (int row = 8; row >= 1; row --) {
            System.out.print(" " + row + " ");
            for (int col = 1; col <= 8; col++) {
                drawSquare(board, row, col);
            }
            System.out.println(" " + row);
        }
        printFilesWhite();
    }




    private static void printFilesWhite() {
        System.out.println("    a  b  c  d  e  f  g  h");
    }




    private static void drawSquare(ChessBoard board, int row, int col) {
        boolean lightSquare = ((row + col) % 2 == 0);

        if (lightSquare) {
            System.out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
        } else {
            System.out.print(EscapeSequences.SET_BG_COLOR_DARK_GREEN);
        }

        ChessPiece piece = board.getPiece(new chess.ChessPosition(row, col));
        System.out.print(pieceString(piece));

        System.out.print(EscapeSequences.RESET_BG_COLOR);
    }

    private static String pieceString(ChessPiece piece) {
        if (piece == null) {
            return EscapeSequences.EMPTY;
        }

        return switch (piece.getTeamColor()) {
            case WHITE -> whitePiece(piece);
            case BLACK -> blackPiece(piece);
        };
    }

    private static String whitePiece(ChessPiece piece) {
        return switch (piece.getPieceType()) {
            case KING -> EscapeSequences.WHITE_KING;
            case QUEEN -> EscapeSequences.WHITE_QUEEN;
            case BISHOP -> EscapeSequences.WHITE_BISHOP;
            case KNIGHT -> EscapeSequences.WHITE_KNIGHT;
            case ROOK -> EscapeSequences.WHITE_ROOK;
            case PAWN -> EscapeSequences.WHITE_PAWN;
        };

}
