package sc.player2022.logic;

import sc.plugin2022.*;
import sc.plugin2022.Vector;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

    /** Gibt alle möglichen Züge des Gegners zurück
     *
     * @param board Ein beliebiges Spielfeld
     * @return Alle möglichen Züge des Gegners auf dem angegebenen Spielfeld
     */
    public static List<Move> getOpponentMoves(Board board){
        List<Move> opponentMoves = new ArrayList<>();

        for(Coordinates c : board.keySet()){
            if(isOpponent(board, c)){
                for (Vector v : board.get(c).getPossibleMoves()){
                    Move m = Move.create(c, v);
                    if(m != null && !isOpponent(board, m.getTo())){
                        opponentMoves.add(m);
                    }
                }
            }
        }

        return opponentMoves;
    }

    /**
     * @param b
     * @return eine Map mit den Koordinaten und Figuren des eigenen Teams.
     */
    public static Map<Coordinates, Piece> getOwnPieces(Board b){
        Map<Coordinates, Piece> ownPieces = new HashMap<>();
        for(Coordinates c : b.getKeys()){
            if(isOwn(b, c)){
                ownPieces.put(c, b.get(c));
            }
        }
        return ownPieces;
    }

    public static Map<Coordinates, Piece> getOpponentPieces(Board b){
        Map<Coordinates, Piece> opponentPieces = new HashMap<>();
        for(Coordinates c : b.getKeys()){
            if(isOpponent(b, c)){
                opponentPieces.put(c, b.get(c));
            }
        }
        return opponentPieces;
    }

    /**
     * Gibt die Koordinaten aller eigenen Figuren zurück
     * @param b
     * @return
     */
    public static List<Coordinates> getOwnPieceLocations(Board b){
        return b.getKeys().stream().filter(c -> isOwn(b, c)).collect(Collectors.toList());
    }

    /**
     * Gibt die Koordinaten aller gegnerischen Figuren zurück
     */
    public static List<Coordinates> getOpponentPieceLocations(Board b){
        return b.getKeys().stream().filter(c -> isOpponent(b, c)).collect(Collectors.toList());
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
        List<Coordinates> ownPieces = getOwnPieceLocations(imag);
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
        List<Coordinates> pieces = getOwnPieceLocations(b);
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
     * @param b
     * @param c
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
