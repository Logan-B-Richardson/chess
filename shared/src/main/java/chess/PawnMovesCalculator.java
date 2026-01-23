package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PawnMovesCalculator implements MovesCalculator {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        ChessPiece pawn = board.getPiece(position);
            if (pawn == null) {
            return List.of();
            }
        var moves = new ArrayList<ChessMove>();
        int direction = (pawn.getTeamColor() == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int startRow = (pawn.getTeamColor() == ChessGame.TeamColor.WHITE) ? 2 : 7;
        int promotionRow = (pawn.getTeamColor() == ChessGame.TeamColor.WHITE) ? 8 : 1;
        int r = position.getRow();
        int c = position.getColumn();
        ChessPosition oneForward = new ChessPosition(r + direction, c);
        if (inBounds(r + direction, c) && board.getPiece(oneForward) == null) {
            pawnPromotion(moves, position, oneForward, promotionRow);
            if (r == startRow) {
                ChessPosition twoForward = new ChessPosition(r + 2 * direction, c);
                if (inBounds(r + 2 * direction, c) && board.getPiece(twoForward) == null) {
                    moves.add(new ChessMove(position, twoForward, null));
                }
            }
        }
        int[] captureCols = {c - 1, c + 1};
        for (int cc : captureCols) {
            int rr = r + direction;
            if (!inBounds(rr, cc)) continue;
            ChessPosition diagonal = new ChessPosition(rr, cc);
            ChessPiece target = board.getPiece(diagonal);
            if (target != null && target.getTeamColor() != pawn.getTeamColor()) {
                pawnPromotion(moves, position, diagonal, promotionRow);
            }
        }
        return moves;
    }
    private boolean inBounds(int r, int c) {
        return (r >= 1) && (r <= 8) && (c >= 1) && (c <= 8);
    }
    private void pawnPromotion(List<ChessMove> moves, ChessPosition start, ChessPosition end, int promotionRow) {
        if (end.getRow() == promotionRow) {
            moves.add(new ChessMove(start, end, ChessPiece.PieceType.QUEEN));
            moves.add(new ChessMove(start, end, ChessPiece.PieceType.ROOK));
            moves.add(new ChessMove(start, end, ChessPiece.PieceType.BISHOP));
            moves.add(new ChessMove(start, end, ChessPiece.PieceType.KNIGHT));
        } else {
            moves.add(new ChessMove(start, end, null));
        }
    }
}
