package chess;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;
import chess.MovesCalculator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BishopMovesCalculator implements MovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        ChessPiece bishop = board.getPiece(position);
        if (bishop == null) {
            return List.of();
        }
        var moves = new ArrayList<ChessMove>();
        int[][] directions = {{1,1},{1,-1},{-1,1},{-1,-1}};
        for (int[] d : directions) {
            int r = position.getRow() + d[0];
            int c = position.getColumn() + d[1];
            while (inBounds(r, c)) {
                ChessPosition end = new ChessPosition(r,c);
                ChessPiece space = board.getPiece(end);
                if (space == null) {
                    moves.add(new ChessMove(position, end, null));
                }
                r += d[0];
                c += d[1];
            }
        }
        return moves;
    }

    private boolean inBounds(int r, int c) {
        return (r >= 1) && (r <= 8) && (c >= 1) && (c <= 8);
    }
}