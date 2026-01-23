package chess;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        MovesCalculator piece = board.getPiece(myPosition);
        return List.of();
    }
}

private boolean inBounds(int r, int c) {
    return (r >= 1) && (r <= 8) && (c >= 1) && (c <= 8);
}

public interface MovesCalculator {
    Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position);
}

public class BishopMovesCalculator implements MovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        ChessPiece bishop = board.getPieceType(position);
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
}
