package model;

import chess.ChessGame;

public record GameData (
        int gameid,
        ChessGame game,
        String whiteusername,
        String blackusername,
        String gamename
) {}
