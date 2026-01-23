package chess;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;

public interface MovesCalculator {
    Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position);
}
