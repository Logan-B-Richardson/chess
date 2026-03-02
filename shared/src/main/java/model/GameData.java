package model;

import chess.ChessGame;

public record GameData (
       ChessGame gameid,
       String whiteusername,
       String blackusername,
       String gamename
) {}
