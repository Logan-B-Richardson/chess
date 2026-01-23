package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KnightMovesCalculator implements MovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        ChessPiece knight = board.getPiece(position);
        if (knight == null) {
            return List.of();
        }
        var moves = new ArrayList<ChessMove>();
        int[][] directions = {{2, 1}, {2, -1}, {-2, 1}, {-2, -1}, {1, 2}, {1, -2}, {-1, 2}, {-1, -2}};
        for (int[] d : directions) {
            int r = position.getRow() + d[0];
            int c = position.getColumn() + d[1];
            if (inBounds(r, c)) {
                ChessPosition end = new ChessPosition(r, c);
                ChessPiece space = board.getPiece(end);
                if (space == null || space.getTeamColor() != knight.getTeamColor()) {
                    moves.add(new ChessMove(position, end, null));
                } else {
                    if (space.getTeamColor() != knight.getTeamColor()) {
                        moves.add(new ChessMove(position, end, null)); // capture
                    }
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