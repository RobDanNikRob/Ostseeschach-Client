package sc.player2022.logic;

import sc.plugin2022.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bewertung {
    private static GameState gameState;

    public static void setGameState(GameState gameState) {
        Bewertung.gameState = gameState;
    }

    /**
     * gibt Value von piece zurück, in abängigkeit von Type und Entfernung zur Startlinie, Kompatibel für beide Teams, NullPointer falls falscher Input!
     * @param b Ein beliebiges Spielfeld
     * @param piece Die zu überprüfende Figur
     * @return Den Wert der Figur
     */
    public static int pieceValue(Board b, Coordinates piece) {
        int value = 0;
        Piece p = b.get(piece);

        // Typ; max. 3
        switch (p.getType()){
            case Robbe:
            case Seestern:
                value = 3;
                break;
            case Moewe:
                value = 2;
                break;
            case Herzmuschel:
                value = 1;
                break;
        }

        // Entfernung von der Startlinie, außer bei Robbe; max. 6
        if(p.getType() != PieceType.Robbe){
            value += p.getTeam().getIndex() == 0 ? piece.getX() : (7 - piece.getX());
        }

        return value;
    }

    /**
     * Gibt den besten Zug aus einer Liste von Zügen zurück. Sollte nur für Finetuning benutzt werden, um zwischen
     * etwa gleichwertigen Zügen zu entscheiden
     * Kriterien: Piecevalue, Erhöhung der Anzahl der eigenen gedeckten Figuren, Erhöhung der Anzahl der gegnerischen
     * bedrohten Figuren, Ziehen nach vorne
     * @param moves Eine Liste von zu überprüfenden Zügen
     * @return Den besten Zug aus der Liste
     */
    public static Move besterZug(Board b, List<Move> moves){
        // Züge mit Bewertung
        Map<Move, Integer> moveRating = new HashMap<>();
        int total = 0;

        for(Move m : moves){
            int value = 0;
            Piece p = b.get(m.getFrom());

            // Piece Value
            value += pieceValue(b, m.getFrom());

            //Erhöhung der bedrohten Figuren des Gegners
            value += GameInfo.bedrohtDifferenceAfterMove(b, m, false);

            //Verringerung der bedrohten eigenen Figuren
            value -= GameInfo.bedrohtDifferenceAfterMove(b, m, true);

            // Ziehen nach vorne: Unterschied der x-Koordinate vor und nach dem Zug, außer bei Robben
            if(p.getType() != PieceType.Robbe){
                value += m.getDelta().getDx() * (p.getTeam().getIndex() == 0 ? 1 : -1);
            }

            // Kann der Gegner nach dem Move das Spiel gewinnen? Dann auf keinen Fall diesen Move nehmen
            Board sim = b.clone();
            sim.movePiece(m);
            if(!GameInfo.canWin(b, false).isEmpty()){
                value = 0;
            }

            moveRating.put(m, value);
            total += value;
        }

        // Je größer die Bewertung, desto größer die Wahrscheinlichkeit, dass der Zug gewählt wird
        int rand = (int) (Math.random() * total);
        Move out = moves.get(0);

        int sum = 0;
        for(Move m : moveRating.keySet()){
            sum += moveRating.get(m);
            if(sum >= rand){
                out = m;
                break;
            }
        }

        System.out.println("Bewertung: " + moveRating);
        return out;
    }
}
