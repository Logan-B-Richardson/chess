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

}
