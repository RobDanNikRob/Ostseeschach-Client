package sc.player2022.logic;

import com.thoughtworks.xstream.mapper.Mapper;
import sc.plugin2022.Vector;
import sc.plugin2022.*;

import java.util.*;

/**
 * Methoden, die wichtige Informationen über das aktuelle Spiel bereitstellen
 */

public class GameInfo {
    private static GameState gameState;

    public static void setGameState(GameState gameState) {
        GameInfo.gameState = gameState;
    }

    /** Gibt zurück, ob sich auf einem Brett an einer Koordinate eine eigene Figur befindet.
     *
     * @param b Brett, für das überprüft werden soll
     * @param c Koordinate, die überprüft werden soll
     */
    public static boolean isOwn(Board b, Coordinates c){
        try{
            return b.get(c).getTeam().equals(gameState.getCurrentTeam());
        } catch (NullPointerException e){
            return false;
        }
    }
    public static boolean isTower(Board b, Coordinates coordinates){
       if(b.get(coordinates).getCount() > 1)
           return true;
        return false;
    }

    /** Gibt zurück, ob sich auf einem Brett an einer Koordinate eine gegnerische Figur befindet.
     *
     * @param b Brett, für das überprüft werden soll
     * @param c Koordinate, die überprüft werden soll
     */
    public static boolean isOpponent(Board b, Coordinates c){
        try{
            return b.get(c).getTeam().equals(gameState.getOtherTeam());
        } catch (NullPointerException e){
            return false;
        }
    }

    /**
     * @param b Ein beliebiges Spielfeld
     * @return Alle möglichen eigenen Züge auf dem angegebenen Spielfeld
     */
    public static List<Move> getOwnMoves(Board b){
        return new GameState(b, gameState.getTurn()).getPossibleMoves();
    }

    /**
     * @param b Ein beliebiges Spielfeld
     * @return Alle möglichen Züge des Gegners auf dem angegebenen Spielfeld
     */
    public static List<Move> getOpponentMoves(Board b){
        return new GameState(b, gameState.getTurn() - 1).getPossibleMoves();
    }

    /**
     * Gibt, im Gegensatz zur Methode "getCurrentPieces" aus der Klasse GameState, die Koordinaten und Figuren des
     * eigenen Teams auf einem beliebigen Spielfeld zurück.
     * @param b Ein beliebiges Spielfeld
     * @return eine Map mit den Koordinaten und Figuren des eigenen Teams.
     */
    public static Map<Coordinates, Piece> getOwnPieces(Board b){
        return new GameState(b, gameState.getTurn()).getCurrentPieces();
    }

    /**
     * @param b Ein beliebiges Spielfeld
     * @return eine Map mit den Koordinaten und Figuren des gegnerischen Teams.
     */
    public static Map<Coordinates, Piece> getOpponentPieces(Board b){
        return new GameState(b, gameState.getTurn() - 1).getCurrentPieces();
    }

    /**
     * Gibt als boolean zurück, ob eine eingegeben Figur von einem Gegner bedroht ist
     * @param b
     * @param piece
     * @return boolean
     */
    public static boolean isBedroht(Board b, Coordinates piece) {


            List<Move> moves;
            if (isOwn(b, piece))
                moves = getOpponentMoves(b);
            else
                moves = getOwnMoves(b);

            for (Move m : moves) {
                if (piece.equals(m.getTo()) && !isGedeckt(b, piece)) {
                    return true;
                }
            }

        return false;
    }




    /**
     * gibt, wenn die Figur bedroht ist, die Koordinaten des Pieces zurück, dass die Figur bedroht, sonst ausgabe null
     * @param b
     * @param piece
     * @return Coordinates
     */
    public static Coordinates isBedrohtBy(Board b, Coordinates piece){

        if(isOwn(b, piece)){
        if(isBedroht(b, piece)) {
    for (Move m : getOpponentMoves(b)) {
        Move w = m;
        if (piece.equals(m.getTo())) {
            return m.getFrom();
        }
    }
}
        return null;
        }else {
            if (isBedroht(b, piece)) {
                for (Move m : getOwnMoves(b)) {
                    Move w = m;
                    if (piece.equals(m.getTo())) {
                        return m.getFrom();
                    }
                }
            }
            return null;

        }
    }

    /**
     * gibt anzahl bedrohte figuren eines angegebenen Teams zurück
     * @param b
     * @param own, true for own false for enemy
     * @return
     */
    public static int countBedrohteFiguren(Board b, boolean own){
        int count = 0;
     if(own) {

         for (Coordinates c : getOwnPieces(b).keySet()) {
             if (isBedroht(b, c))
                 count++;
         }
     }else{

         for (Coordinates c : getOpponentPieces(b).keySet()) {
             if (isBedroht(b, c))
                 count++;
         }
     }
        return count;
    }

    /**
     * gibt als int zurück wie viele Figuren von Towern bedroht sind
     * @param b
     * @param own, true for own false for enemy
     * @return int
     */
    public static int countBedrohteFigurenByTower(Board b, boolean own){
        int count = 0;
        if (own ) {
            for(Coordinates c : getOwnPieces(b).keySet()){
                if(isBedrohtbyTower(b, c))
                    count++;
            }
        }else{
            for(Coordinates c : getOpponentPieces(b).keySet()){
                if(isBedrohtbyTower(b, c))
                    count++;
            }
        }
        return count;
    }
    /**
     * gibt als int zurück wie viele Figuren von Towern bedroht sind
     * @param b
     * @return int
     */
    public static int countBedrohteFigurenEnemyByTower(Board b){
        int count = 0;
        for(Coordinates c : getOpponentPieces(b).keySet()){
            if(isBedrohtbyTower(b, c))
                count++;
        }

        return count;
    }

    /**
     * gibt zurück ob eine Figur von einen Turm bedroht wird
     * @param b
     * @param piece
     * @return boolean
     */
    public static boolean isBedrohtbyTower(Board b, Coordinates piece){
    if(!isBedroht(b, piece))
        return false;
        if(isOwn(b, piece)){
            try {
                if (getOpponentPieces(b).get(isBedrohtBy(b, piece)).getCount() > 1)
                    return true;
            } catch (NullPointerException e) {
                return false;
            }
        }else{
            try {
            if (getOwnPieces(b).get(isBedrohtBy(b, piece)).getCount() > 1)
                return true;
        } catch (NullPointerException e) {
            return false;
            }
        }
    return false;
    }

    /**
     * gibt als int die differenz der bedrohten Figuren zurück, negtiv wenn weniger, positiv wenn mehr und null wenn gleich bleibt
     * @param b
     * @param move
     * @param own true for own false for enemy
     * @return +-0 int
     */
    public static int bedrohtDifferenceAfterMove(Board b, Move move, boolean own){
            Board c = b.clone();
            int before = countBedrohteFiguren(c, own);
            c.movePiece(move);
            int after = countBedrohteFiguren(c, own);
            return after - before;
    }

    /**
     * gibt als boolean zurück ob eine Figur nach dem Move Bedroht ist
     * @param b
     * @param move
     * @return boolean
     */
    public static boolean isBedrohtAfterMove(Board b, Move move){

        if(isBedroht(b, move.getTo()))
            return true;
        return false;

    }

    /**
     * Gibt zurück, ob man nach einem Zug direkt angegriffen werden kann
     * @param m Der zu überprüfende Zug
     */
    public static boolean isAttackableAfterMove(Board b, Move m){
        Board imag = b.clone();
        imag.movePiece(m);

        // Werden durch den Zug Figuren angreifbar gemacht?
        // todo: ausschließen wenn Figuren gedeckt sind, einschließen wenn gegnerische Figur ein Turm ist
        Set<Coordinates> ownPieces = getOwnPieces(imag).keySet();
        for(Move opMove : getOpponentMoves(imag)){
            if(ownPieces.contains(opMove.getTo())){
                return true;
            }
        }

        return false;
    }

    /**
     * Sucht nach Figuren, die gerade unmittelbar bedroht sind und gibt mögliche rettende Züge zurück
     */
    public static List<Move> getSavingMoves(Board b){
        //todo: ausschließen wenn Figuren gedeckt sind, einschließen wenn die gegnerische Figur ein Turm ist
        Set<Coordinates> pieces = getOwnPieces(b).keySet();
        List<Coordinates> threatened = new ArrayList<>();
        List<Move> savingMoves = new ArrayList<>();

        for(Move m : getOpponentMoves(b)){
            if(pieces.contains(m.getTo())){
                threatened.add(m.getTo());
            }
        }

        for(Move m : gameState.getPossibleMoves()){
            if(threatened.contains(m.getFrom()) && !isAttackableAfterMove(b, m)){
                savingMoves.add(m);
            }
        }

        return savingMoves;
    }

    // Erstellt eine Liste mit allen Mooves die Wahrscheinlich zum Durchlaufsieg führt.
    // Falls kein Moove infrage kommt gibt es Null zurück
    public static List<Move> durchlaufen (Board b){
        List<Move> gegnerischeSeite = new ArrayList<Move>();
        for(Move m:getOwnMoves(b)){
            if(gameState.getCurrentTeam().getIndex() == 0 && !isBedrohtAfterMove(b, m) && m.getFrom().getX() >3){
                gegnerischeSeite.add(m);
            }
            if(gameState.getCurrentTeam().getIndex() == 1 && !isBedrohtAfterMove(b, m) && m.getFrom().getX() <4){
                gegnerischeSeite.add(m);
            }
        }
        if (!gegnerischeSeite.isEmpty())
        return gegnerischeSeite;
        return null;
    }

    public static List<Move>[] futureDurchlaufen (Board b){
        int i = 0;
        List<Move>[] futureMoves = new ArrayList[getOpponentMoves(b).size()];
        for(Move n:getOpponentMoves(b)){
            Board c = b.clone();
            c.movePiece(n);

            futureMoves[i] = durchlaufen(c);
            i++;
    }
        return futureMoves;
    }

    /**
     * @param b Ein beliebiges Spielfeld
     * @param c Die zu überprüfende Koordinate
     * @return ob die Figur an den Koordinaten gedeckt ist.
     */
    public static boolean isGedeckt(Board b, Coordinates c){
        Map<Coordinates, Piece> pieces = isOwn(b, c) ? getOwnPieces(b) : getOpponentPieces(b);

        for(Coordinates current : pieces.keySet()){
            for(Vector v : pieces.get(current).getPossibleMoves()){
                if(current.plus(v).equals(c)){
                    System.out.println("Die Figur bei " + c + " ist gedeckt von der Figur bei " + current);
                    return true;
                }
            }
        }
        System.out.println("Die Figur bei " + c + " ist nicht gedeckt");
        return false;
    }

    /**
     * @param b Ein beliebiges Spielfeld
     * @param c die Koordinaten, an denen der gewünschte Stein steht
     * @return Eine Liste mit Zügen, die die Figur an den angegebenen Koordinaten ausführen kann
     */
    public static List<Move> getMovesFrom(Board b, Coordinates c){
        List<Move> moves;
        List<Move> out = new ArrayList<>();

        if(isOwn(b, c)){
            moves = getOwnMoves(b);
        } else {
            moves = getOpponentMoves(b);
        }

        for(Move m : moves){
            if(m.getFrom().equals(c)){
                out.add(m);
            }
        }

        return out;
    }

    /**
     * @param b Ein beliebiges Spielfeld
     * @param m Der zu überprüfende Zug
     * @return Ob der angegebene Zug eine Zwickmühle erzeugt (eigene Figur ist nicht bedroht bzw. gedeckt, mindestens
     * zwei gegnerische Figuren sind bedroht, Gegner kann im nächsten Zug nicht beide gleichzeitig decken)
     */
    public static boolean zwickmuehle(Board b, Move m){
        Coordinates to = m.getTo();

        Board sim = b.clone();
        if(!isBedroht(b, to) || isGedeckt(b, to)){
            sim.movePiece(m);
            // können mindestens zwei gegnerische Figuren im nächsten Zug erreicht werden?

            if(bedrohtDifferenceAfterMove(b, m, false) >= 2){

            }
        }
        return false;
    }
}
