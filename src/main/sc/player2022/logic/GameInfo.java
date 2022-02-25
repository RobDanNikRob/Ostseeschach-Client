package sc.player2022.logic;

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

    /**
     * @param b Ein beliebiges Spielfeld
     * @param c Die zu überprüfende Koordinate
     * @return ob die Figur an den Koordinaten gedeckt ist.
     */
    public static boolean isGedeckt(Board b, Coordinates c){
        Map<Coordinates, Piece> own = getOwnPieces(b);
        for(Coordinates current : own.keySet()){
            for(Vector v : own.get(current).getPossibleMoves()){
                if(current.plus(v).equals(c)){
                    System.out.println("Die Figur bei " + c + " ist gedeckt von der Figur bei " + current);
                    return true;
                }
            }
        }
        System.out.println("Die Figur bei " + c + " ist nicht gedeckt");
        return false;
    }
}
