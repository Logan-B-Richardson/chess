package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.Collection;
import java.util.HashSet;

public class BoardUI {
    public static void drawBoard(ChessGame game, ChessGame.TeamColor perspective) {
        drawBoard(game, perspective, new HashSet<>());
    }

    public static void drawBoard(ChessGame game, ChessGame.TeamColor perspective,
                                 Collection<ChessPosition> highlights) {
        if (game == null) {
            System.out.println("No game to display.");
            return;
        }
        ChessBoard board = game.getBoard();
        System.out.print(EscapeSequences.ERASE_SCREEN);
        if (perspective == ChessGame.TeamColor.BLACK) {
            drawBlackView(board, highlights);
        } else {
            drawWhiteView(board, highlights);
        }
        System.out.print(EscapeSequences.RESET_BG_COLOR);
        System.out.print(EscapeSequences.RESET_TEXT_COLOR);
        System.out.println();
    }

    private static void drawBlackView(ChessBoard board, Collection<ChessPosition> highlights) {
        printFilesBlack();
        for (int row = 1; row <= 8; row++) {
            System.out.print(" " + row + " ");
            for (int col = 8; col >= 1; col--) {
                drawSquare(board, row, col, highlights);
            }
            System.out.println(" " + row);
        }
        printFilesBlack();
    }

    private static void drawWhiteView(ChessBoard board, Collection<ChessPosition> highlights) {
        printFilesWhite();
        for (int row = 8; row >= 1; row--) {
            System.out.print(" " + row + " ");
            for (int col = 1; col <= 8; col++) {
                drawSquare(board, row, col, highlights);
            }
            System.out.println(" " + row);
        }
        printFilesWhite();
    }

    private static void printFilesBlack() {
        System.out.println("    h  g  f  e  d  c  b  a");
    }

    private static void printFilesWhite() {
        System.out.println("    a  b  c  d  e  f  g  h");
    }

    private static void drawSquare(ChessBoard board, int row, int col,
                                   Collection<ChessPosition> highlights) {
        ChessPosition position = new ChessPosition(row, col);
        boolean highlighted = highlights != null && highlights.contains(position);

        if (highlighted) {
            System.out.print(EscapeSequences.SET_BG_COLOR_GREEN);
        } else {
            boolean lightSquare = ((row + col) % 2 == 0);
            if (lightSquare) {
                System.out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
            } else {
                System.out.print(EscapeSequences.SET_BG_COLOR_WHITE);
            }
        }

        ChessPiece piece = board.getPiece(position);
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

    private static String blackPiece(ChessPiece piece) {
        return switch (piece.getPieceType()) {
            case KING -> EscapeSequences.BLACK_KING;
            case QUEEN -> EscapeSequences.BLACK_QUEEN;
            case BISHOP -> EscapeSequences.BLACK_BISHOP;
            case KNIGHT -> EscapeSequences.BLACK_KNIGHT;
            case ROOK -> EscapeSequences.BLACK_ROOK;
            case PAWN -> EscapeSequences.BLACK_PAWN;
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
}
