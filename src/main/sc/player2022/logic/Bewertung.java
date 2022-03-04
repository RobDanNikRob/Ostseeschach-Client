package sc.player2022.logic;

import sc.plugin2022.Board;

import sc.plugin2022.Coordinates;
import sc.plugin2022.Piece;
import sc.plugin2022.PieceType;

public class Bewertung {

    /**
     * gibt Value von piece zurück, in abängigkeit von Type und Entfernung zur Startlinie, Kompatibel für beide Teams, NullPointer falls falscher Input!
     * @param b
     * @param coordinates
     * @return
     */
    public static int pieceValue(Board b, Coordinates coordinates) {
        int i = 0;
        if (b.get(coordinates).getType() == PieceType.Robbe)
            i = 3;
        else if (b.get(coordinates).getType() == PieceType.Herzmuschel)
            i = 1 + b.get(coordinates).getTeam().getIndex() == 0 ? coordinates.getX() : (7 - coordinates.getX());
        else if (b.get(coordinates).getType() == PieceType.Moewe)
            i = +b.get(coordinates).getTeam().getIndex() == 0 ? coordinates.getX() : (7 - coordinates.getX());

        else if (b.get(coordinates).getType() == PieceType.Seestern)
            i = 3 + b.get(coordinates).getTeam().getIndex() == 0 ? coordinates.getX() : (7 - coordinates.getX());


        return i;
    }


}
