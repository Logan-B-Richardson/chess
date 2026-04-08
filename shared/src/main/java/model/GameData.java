package model;

import chess.ChessGame;

public record GameData (
        int gameid,
        String whiteusername,
        String blackusername,
        String gamename,
        ChessGame game
) {}
