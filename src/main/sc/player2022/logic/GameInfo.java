package sc.player2022.logic;

import sc.api.plugins.ITeam;
import sc.plugin2022.Vector;
import sc.plugin2022.*;

import java.util.*;
import java.util.List;

/**
 * Methoden, die wichtige Informationen über das aktuelle Spiel bereitstellen
 */

public class GameInfo {
    private static GameState gameState;

    public static void setGameState(GameState gameState) {
        GameInfo.gameState = gameState;
    }

    /**
     * Gibt zurück, ob sich auf einem Brett an einer Koordinate eine eigene Figur befindet. Darf nicht in anderen
     * Methoden benutzt werden, wenn diese auch Koordinaten annehmen können, an denen keine Figur steht!
     *
     * @param b Brett, für das überprüft werden soll
     * @param piece Koordinate, die überprüft werden soll
     */
    public static boolean isOwn(Board b, Coordinates piece) {
        return b.get(piece).getTeam().equals(gameState.getCurrentTeam());
    }

    /**
     * @param b           Ein beliebiges Spielfeld
     * @param piece Die zu überprüfenden Koordinaten
     * @return Ob an der Position ein Turm ist
     */
    public static boolean isTower(Board b, Coordinates piece) {
        try {
            return b.get(piece).getCount() > 1;
        } catch (NullPointerException e) {
            //System.out.println("isTower: Auf dem Feld " + piece + " steht keine Figur");
            return false;
        }
    }

    /**
     * Gibt zurück, ob sich auf einem Brett an einer Koordinate eine gegnerische Figur befindet.
     *
     * @param b Brett, für das überprüft werden soll
     * @param piece Koordinate, die überprüft werden soll
     */
    public static boolean isOpponent(Board b, Coordinates piece) {
        try {
            return b.get(piece).getTeam().equals(gameState.getOtherTeam());
        } catch (NullPointerException e) {
            return false;
        }
    }

    /**
     * gibt alle schlagenden Moves zurück, die das angegebene Team machen kann, ohne danach den entstehenden Turm in Gefahr zu
     * bringen
     *
     * @param b Ein beliebiges Spielfeld
     * @param own dein Team?
     * @return List of <Move> die nach dem Schlagen nicht bedroht sind
     */
    public static List<Move> canSafelyKill(Board b, boolean own) {
        List<Move> moves = own ? getOwnMoves(b) : getOpponentMoves(b);
        List<Move> out = new ArrayList<>();

        for (Move m : moves) {
            // Ist an der Zielposition ein Gegner und ist diese bedroht (kann also geschlagen werden) und wird die
            // Anzahl der eigenen bedrohten Figuren durch den Zug nicht erhöht?
            if (bedrohteFiguren(b, !own).contains(m.getTo()) && isBedroht(b, m.getTo(), !own) && bedrohtDifferenceAfterMove(b, m, own) <= 0){
                out.add(m);
            }
        }
        return out;
    }

    /**
     * @param b Ein beliebiges Spielfeld
     * @return Alle möglichen eigenen Züge auf dem angegebenen Spielfeld
     */
    public static List<Move> getOwnMoves(Board b) {
        return new GameState(b, gameState.getTurn()).getPossibleMoves();
    }

    /**
     * @param b Ein beliebiges Spielfeld
     * @return Alle möglichen Züge des Gegners auf dem angegebenen Spielfeld
     */
    public static List<Move> getOpponentMoves(Board b) {
        return new GameState(b, gameState.getTurn() - 1).getPossibleMoves();
    }

    /**
     * Gibt, im Gegensatz zur Methode "getCurrentPieces" aus der Klasse GameState, die Koordinaten und Figuren des
     * eigenen Teams auf einem beliebigen Spielfeld zurück.
     *
     * @param b Ein beliebiges Spielfeld
     * @return eine Map mit den Koordinaten und Figuren des eigenen Teams.
     */
    public static Map<Coordinates, Piece> getOwnPieces(Board b) {
        return new GameState(b, gameState.getTurn()).getCurrentPieces();
    }

    /**
     * @param b Ein beliebiges Spielfeld
     * @return eine Map mit den Koordinaten und Figuren des gegnerischen Teams.
     */
    public static Map<Coordinates, Piece> getOpponentPieces(Board b) {
        return new GameState(b, gameState.getTurn() - 1).getCurrentPieces();
    }

    /**
     * Gibt als boolean zurück, ob eine eingegebene Koordinate von einem Gegner bedroht ist, außer wenn sie gedeckt ist
     *
     * @param b     Ein beliebiges Spielfeld
     * @param c die zu überprüfende Koordinate
     * @param own Ob für das eigene oder das gegnerische Team überprüft werden soll. Das ist notwendig, damit auch
     *            Koordinaten übergeben werden können, an denen keine Figur steht, weshalb dort nicht das Team ermittelt
     *            werden kann
     * @return boolean
     */
    public static boolean isBedroht(Board b, Coordinates c, boolean own) {
        List<Move> moves = own ? getOpponentMoves(b) : getOwnMoves(b);

        for (Move m : moves) {
            // Schlägt der Move und ist die Figur am Ziel nicht gedeckt ODER schlägt der Move und die gegnerische Figur
            // ist ein Turm?
            if (c.equals(m.getTo()) && (!isGedeckt(b, c, own) || isTower(b, m.getFrom()))) {
                //System.out.println("isBedroht: " + c + " ist bedroht");
                return true;
            }
        }

        //System.out.println("isBedroht: " + c + " ist nicht bedroht");
        return false;
    }

    /**
     * Gibt zurück, ob eine Koordinate geschlagen werden kann, unabhängig davon, ob sie gedeckt ist
     *
     * @param b Ein beliebiges Spielfeld
     * @param c Die zu überprüfende Koordinate
     * @return boolean, kann sie geschlagen werden, unabhängig von Kontermöglichkeit
     */
    public static boolean isAttackable(Board b, Coordinates c, boolean own) {
        List<Move> moves = own ? getOpponentMoves(b) : getOwnMoves(b);

        for (Move m : moves) {
            if (c.equals(m.getTo())) {
                return true;
            }
        }

        return false;
    }


    /**
     * Gibt eine Liste der Figuren zurück, von denen die angegebene Koordinate bedroht wird
     *
     * @param b     Ein beliebiges Spielfeld
     * @param c Die zu überprüfende Koordinate
     * @return Coordinates
     */
    public static List<Coordinates> getWirdBedrohtVon(Board b, Coordinates c, boolean own) {
        List<Coordinates> out = new ArrayList<>();
        List<Move> moves = own ? getOpponentMoves(b) : getOwnMoves(b);

        if (isBedroht(b, c, own)) {
            for (Move m : moves) {
                if (c.equals(m.getTo())) {
                    out.add(m.getFrom());
                }
            }
        }

        return out;
    }

    /**
     * Gibt die Figuren zurück, die von der eingegebenen Figur bedroht werden, außer natürlich gedeckte Figuren die sie
     * theoretisch schlagen könnte
     *
     * @param b     Ein beliebiges Spielfeld
     * @param piece Die zu überprüfende Figur
     * @return List<Coordinates>
     */
    public static List<Coordinates> getBedroht(Board b, Coordinates piece) {
        List<Coordinates> out = new ArrayList<>();

        for (Move m : getMovesFrom(b, piece)) {
            if (isBedroht(b, m.getTo(), !isOwn(b, piece))) {
                out.add(m.getTo());
            }
        }
        return out;

    }

    /**
     * Gibt eine Liste von Figuren zurück, die von der angegebenen Figur gedeckt werden.
     * @param b Ein beliebiges Spielfeld
     * @param piece Die zu überprüfende Figur
     * @return Eine Liste von Koordinaten, die von der angegebenen Figur gedeckt werden
     */
    public static List<Coordinates> getDeckt(Board b, Coordinates piece) {
        List<Coordinates> out = new ArrayList<>();

        Map<Coordinates, Piece> pieces = isOwn(b, piece) ? getOwnPieces(b) : getOpponentPieces(b);

        for (Vector v : b.get(piece).getPossibleMoves()) {
            Coordinates to = piece.plus(v);
            if (pieces.containsKey(to)) {
                out.add(to);
            }
        }

        return out;
    }

    /**
     * Gibt zurück, ob eine Koordinate geschützt ist (also gedeckt und angreifbar, aber nicht bedroht)
     * @param b Ein beliebiges Spielfeld
     * @param c Die zu überprüfende Koordinate
     * @return ob die Koordinate geschützt ist
     */
    public static boolean isGeschuetzt(Board b, Coordinates c, boolean own){
        return isGedeckt(b, c, own) && isAttackable(b, c, own) && !isBedroht(b, c, own);
    }

    /**
     * gibt alle Figuren zurück, die von einer angegebenen Figur beschützt werden
     *
     * @param b Ein beliebiges Spielfeld
     * @param piece Die zu überprüfende Figur
     * @return List<Coordinates>
     */
    public static List<Coordinates> getSchuetzt(Board b, Coordinates piece) {
        List<Coordinates> deckt = getDeckt(b, piece);
        List<Coordinates> schuetzt = new ArrayList<>();

        for (Coordinates c : deckt) {
            if (isGeschuetzt(b, c, isOwn(b, piece))){
                schuetzt.add(c);
            }
        }
        return schuetzt;

    }



    /**
     * gibt zurück, ob eine Figur blockiert ist (sie deckt eine sonst bedrohte Figur)
     *
     * @param b Ein beliebiges Spielfeld
     * @param piece Die zu überprüfende Figur
     * @return boolean ist blockiert
     */
    public static boolean isBlocked(Board b, Coordinates piece) {
        return !getSchuetzt(b, piece).isEmpty();
    }

    /**
     * gibt anzahl blockierter Figuren für ein team zurück
     * @param b
     * @param own
     * @return List mit Coordinates der blockierten Figuren
     */
    public static List<Coordinates> blockierteFiguren(Board b, boolean own){
        List<Coordinates> blocked = new ArrayList<>();

        for(Coordinates c : own ? getOwnPieces(b).keySet() : getOpponentPieces(b).keySet()) {
            if(isBlocked(b, c) && !blocked.contains(c)){
                blocked.add(c);
            }
        }
        return blocked;

    }

    /**
     * gibt anzahl der blockierten Figuren nach einem Move an in int
     * @param b
     * @param move
     * @return wenn weniger dann negativ
     */
    public static int blockierteFigurenDifferenceAfterMove(Board b, Move move){
        Board c = b.clone();
        c.movePiece(move);


        return blockierteFiguren(c, isOwn(b, move.getFrom())).size() - blockierteFiguren(b, isOwn(b, move.getFrom())).size();


    }



    /**
     * Gibt alle geschützten Figuren eines Teams zurück
     * @param b Ein beliebiges Spielfeld
     * @param own Eigenes Team?
     * @return Eine Liste mit allen geschützten Figuren des angegebenen Teams
     */
    public static List<Coordinates> geschuetzteFiguren(Board b, boolean own){
        Set<Coordinates> pieces = (own ? getOwnPieces(b) : getOpponentPieces(b)).keySet();
        List<Coordinates> out = new ArrayList<>();

        for(Coordinates c : pieces){
            if(isGeschuetzt(b, c, own)){
                out.add(c);
            }
        }

        return out;
    }

    /**
     * Gibt den Unterschied von geschützten Figuren des angegebenen Teams vor und nach dem Zug zurück
     * @param b Ein beliebiges Spielfeld
     * @param m Der zu überprüfende Zug
     * @param own Eigenes Team?
     * @return Negativ: weniger geschützte Figuren nach dem Zug, positiv: mehr
     */
    public static int geschuetztDifferenceAfterMove(Board b, Move m, boolean own){
        Board sim = b.clone();
        sim.movePiece(m);
        return geschuetzteFiguren(sim, own).size() - geschuetzteFiguren(b, own).size();
    }

    /**
     * Gibt die Figuren zurück, von denen die angegebene Koordinate gedeckt wird
     * @param b Ein beliebiges Spielfeld
     * @param c Die zu überprüfende Koordinate
     * @return Eine Liste mit Figuren, von denen die angegebene Koordinate gedeckt wird
     */
    public static List<Coordinates> getWirdGedecktVon(Board b, Coordinates c, boolean own) {
        Map<Coordinates, Piece> pieces = own ? getOwnPieces(b) : getOpponentPieces(b);

        List<Coordinates> out = new ArrayList<>();

        if (!isTower(b, c)) {
            for (Coordinates current : pieces.keySet()) {
                for (Vector v : pieces.get(current).getPossibleMoves()) {
                    if (current.plus(v).equals(c)) {
                        //System.out.println(c + " ist gedeckt von der Figur bei " + current);
                        out.add(current);
                    }
                }
            }
        }

        if (out.size() == 0) {
            //System.out.println("Die Figur bei " + c + " ist nicht gedeckt");
        }

        return out;
    }

    /**
     * Gibt zurück, ob eine Koordinate gedeckt ist.
     * @param b Ein beliebiges Spielfeld
     * @param c Die zu überprüfende Koordinate
     * @return ob die Figur an der Koordinate gedeckt ist.
     */
    public static boolean isGedeckt(Board b, Coordinates c, boolean own) {
        return !getWirdGedecktVon(b, c, own).isEmpty();
    }

    /**
     * @param b   Ein beliebiges Spielfeld
     * @param own true für das eigene Team, false für das gegnerische
     * @return Eine Liste von Koordinaten von allen gedeckten Figuren des angegebenen Teams
     */
    public static List<Coordinates> gedeckteFiguren(Board b, boolean own) {
        Set<Coordinates> pieces = (own ? getOwnPieces(b) : getOpponentPieces(b)).keySet();
        List<Coordinates> out = new ArrayList<>();

        for (Coordinates c : pieces) {
            if (isGedeckt(b, c, own)) {
                out.add(c);
            }
        }

        return out;
    }

    /**
     * gibt bedrohte figuren eines angegebenen Teams zurück
     *
     * @param b   Ein beliebiges Spielfeld
     * @param own true for own false for enemy
     * @return Eine Liste mit den Koordinaten von allen bedrohten Figuren
     */
    public static List<Coordinates> bedrohteFiguren(Board b, boolean own) {
        Set<Coordinates> pieces = (own ? getOwnPieces(b) : getOpponentPieces(b)).keySet();
        List<Coordinates> out = new ArrayList<>();

        for (Coordinates c : pieces) {
            if (isBedroht(b, c, own)) {
                out.add(c);
            }
        }

        //System.out.println("bedrohteFiguren: " + out);
        return out;
    }

    /**
     * Alle Figuren eines Teams, die jemanden bedrohen
     *
     * @param b Ein beliebiges Spielfeld
     * @param own true für eigenes Team, false für gegnerisches Team
     * @return List
     */
    public static List<Coordinates> bedrohendeFiguren(Board b, boolean own) {
        Set<Coordinates> pieces = (own ? getOwnPieces(b) : getOpponentPieces(b)).keySet();
        List<Coordinates> out = new ArrayList<>();

        for (Coordinates c : pieces) {
            if (getBedroht(b, c).size() != 0)
                out.add(c);
        }

        return out;
    }

    /**
     *
     * @param b
     * @param own
     * @return
     */
    public static List<Coordinates> bedrohteTower(Board b, boolean own){
        List<Coordinates> out = new ArrayList<>();
        for(Coordinates piece : bedrohteFiguren(b, own)){
            if(isTower(b, piece)){
                out.add(piece);
            }
        }

        return out;
    }

    /**
     * Gibt alle Moves zurück, mit denen man einen Punkt machen kann
     *
     * @param b Ein beliebiges Spielfeld
     * @param own true für eigenes Team, false für gegnerisches Team
     * @return Liste mit Moves, mit denen man einen Punkt machen kann
     */
    public static List<Move> getPointMoves(Board b, boolean own) {
        List<Move> pieces = own ? getOwnMoves(b) : getOpponentMoves(b);
        List<Move> out = new ArrayList<>();
        ITeam team = own ? gameState.getCurrentTeam() : gameState.getOtherTeam();

        for (Move c : pieces) {
            int currentPoints = gameState.getPointsForTeam(team);

            GameState g = gameState.clone();
            g.setTurn(gameState.getTurn() + (own ? 0 : 1));
            g.performMove(c);
            // Hat das Team nach dem Zug mehr Punkte als vorher?
            if (g.getPointsForTeam(team) > currentPoints) {
                out.add(c);
            }
        }
        return out;
    }

    public static List<Move> canWin(Board b, boolean own){
        List<Move> out = new ArrayList<>();

        for (Move m : own ? getOwnMoves(b) : getOpponentMoves(b)) {
            GameState sim = gameState.clone();
            // Rundenänderung, damit auch gegnerische Züge ausgeführt werden können
            sim.setTurn(gameState.getTurn() + (own ? 0 : 1));
            sim.performMove(m);
            if (sim.getPointsForTeam(own ? gameState.getCurrentTeam() : gameState.getOtherTeam()) >= 2) {
                out.add(m);
            }
        }

        return out;
    }


    /**
     * gibt zurück, welche Figuren von Towern bedroht sind
     *
     * @param b    Ein beliebiges Spielfeld
     * @param own, true for own false for enemy
     * @return Liste mit Figuren, die von Türmen bedroht sind
     */
    public static List<Coordinates> bedrohteFigurenByTower(Board b, boolean own) {
        List<Coordinates> pieces = bedrohteFiguren(b, own);
        List<Coordinates> out = new ArrayList<>();

        for (Coordinates c : pieces) {
            if (isBedrohtByTower(b, c, own)) {
                out.add(c);
            }
        }

        return out;
    }

    /**
     * gibt zurück, ob eine Koordinate von einem Turm bedroht wird
     *
     * @param b     Ein beliebiges Spielfeld
     * @param c Die zu überprüfende Koordinate
     * @param own Soll für eigenes Team überprüft werden?
     * @return boolean
     */
    public static boolean isBedrohtByTower(Board b, Coordinates c, boolean own) {
        if (!isBedroht(b, c, own)) return false;

        for (Coordinates bedrohtVon : getWirdBedrohtVon(b, c, own)) {
            if (isTower(b, bedrohtVon)) return true;
        }
        return false;
    }

    /**
     * gibt als int die differenz der bedrohten Figuren nach einem Zug zurück, negativ, wenn weniger, positiv, wenn mehr
     * und null, wenn gleich bleibt
     *
     * @param b    Ein beliebiges Spielfeld
     * @param move Der zu überprüfende Zug
     * @param own  true for own false for enemy
     * @return +-0 int
     */
    public static int bedrohtDifferenceAfterMove(Board b, Move move, boolean own) {
        Board c = b.clone();
        c.movePiece(move);
        return bedrohteFiguren(c, own).size() - bedrohteFiguren(b, own).size();
    }

    /**
     * Gibt die Differenz der gedeckten Figuren nach einem Zug zurück
     * @param b Ein beliebiges Spielfeld
     * @param m Der zu überprüfende Zug
     * @param own Eigenes Team?
     * @return Negativ: weniger gedeckte Figuren als vorher, positiv: mehr
     */
    public static int gedecktDifferenceAfterMove(Board b, Move m, boolean own) {
        Board c = b.clone();
        c.movePiece(m);
        return gedeckteFiguren(c, own).size() - gedeckteFiguren(b, own).size();
    }

    /**
     * gibt als boolean zurück, ob eine Figur nach dem Move bedroht ist
     *
     * @param b    Ein beliebiges Spielfeld
     * @param move Der zu überprüfende Zug
     * @return boolean
     */
    public static boolean isBedrohtAfterMove(Board b, Move move) {
        return isBedroht(b, move.getTo(), isOwn(b, move.getFrom()));
    }

    /**
     * Wird eine Koordinate durch den Zug einer anderen Figur des gleichen Teams gedeckt?
     * @param b Ein beliebiges Spielfeld
     * @param c Die zu überprüfenden Koordinaten
     * @param m Der zu überprüfende Zug
     * @return Ob die Koordinate durch den Zug gedeckt wird
     */
    public static boolean isGedecktAfterMove(Board b, Coordinates c, Move m){
        Board sim = b.clone();
        sim.movePiece(m);
        return isGedeckt(sim, c, isOwn(b, m.getFrom()));
    }

    // Erstellt eine Liste mit allen Mooves die Wahrscheinlich zum Durchlaufsieg führt.
    // Falls kein Moove infrage kommt gibt es Null zurück

    public static List<Move> durchlaufen(Board b, boolean own) {
        List<Move> gegnerischeSeite = new ArrayList<Move>();
        for (Move m : own ? getOwnMoves(b) : getOpponentMoves(b)) {
            if (gameState.getCurrentTeam().getIndex() == 0 && !isBedrohtAfterMove(b, m) && m.getFrom().getX() > 3 && b.get(m.getFrom()).getType() != PieceType.Robbe &&  m.getTo().getX() - m.getFrom().getX() == -1) {
                gegnerischeSeite.add(m);
                System.out.println("Erfolgreich move: "+m+" in gegnerischeSeite geaddet");
            }
            if (gameState.getCurrentTeam().getIndex() == 1 && !isBedrohtAfterMove(b, m) && m.getFrom().getX() < 4 && b.get(m.getFrom()).getType() != PieceType.Robbe &&  m.getTo().getX() - m.getFrom().getX() == 1) {
                gegnerischeSeite.add(m);
                System.out.println("Erfolgreich move: "+m+" in gegnerischeSeite geaddet");

            }
        }

        List<Move> future = new ArrayList<Move> ();
        if (gegnerischeSeite.isEmpty())
            return future;
        else {
            for (Move m: gegnerischeSeite){
                future = futureDurchlaufen(b,getOpponentsMovesThatReach(b,m));
            }
            return future;

        }
    }

    /*
    Guckt ob eine Figur zu 100% durchlaufen kann
    Der Gegner kann dies nicht verhindern, außer man selbst verliert
     */
    public static List<Move> futureDurchlaufen(Board b, List <Move> a) {
        List<Move>futureMoves = new ArrayList<>();
        Board c = b.clone();
        for (Move n : getNachVorne(c,a)) {
            while (!isBedroht(c,n.getTo(),true)){
                c.movePiece(n);
                c = gameState.getBoard();
                for (Move m : getOpponentsMovesThatReach(c, n)) {
                    c.movePiece(m);
                    c = gameState.getBoard();
                    System.out.println("Move m: " + m);
                }
                futureMoves.add(n);
                c = gameState.getBoard();
                break;
            }
            if (n.getTo().getX() == 7 || n.getTo().getX() == 0) {
                    System.out.println("Theoretisches Durchlaufen geschafft");
                    futureMoves.add(n);
                    break;
                }
        }
        System.out.println("futureDurchlaufen ist einmal durchgelaufen. \n c =\n" + c + "\n futureMoves = " + futureMoves );
        return futureMoves;
    }

    // Filtert die eigenen Moves für durchlaufen, damit es nicht zu lange dauert
    public static List<Move> getNachVorne (Board b, List<Move> a){
        List<Move> agressiveMoves = new ArrayList();
        for (Move m : a) {
            if (gameState.getCurrentTeam().getIndex() == 1 && m.getTo().getX() - m.getFrom().getX() == -1 && !isBedrohtAfterMove(b, m)) {
                agressiveMoves.add(m);
            }
            if (gameState.getCurrentTeam().getIndex() == 0 && m.getTo().getX() - m.getFrom().getX() == 1 && !isBedrohtAfterMove(b, m)) {
                agressiveMoves.add(m);
            }
        } return agressiveMoves;
    }

    // Filtert die gegnerischen Moves für durchlaufen, damit es nicht zu lange dauert
    // Gibt eine Liste zurück mit gegnerischen Mooves die theoretisch das Durchlaufen verhindern können
    // Y Koordinaten Abweichung +/- 3

    public static List<Move> getOpponentsMovesThatReach (Board b, Move m) {
        List<Move> opponentsThatCanReach = new ArrayList <Move>();
        for(Move o : getOpponentMoves(b)){
            if(Math.abs(m.getTo().getY() - o.getTo().getY()) <=-1*(m.getFrom().getX()-7) && m.getTo().getX() - o.getTo().getX() >=-3 && m.getTo().getX() - o.getTo().getX() <=0 && gameState.getCurrentTeam().getIndex() == 0){
                opponentsThatCanReach.add(o);
            }
            if(Math.abs(m.getTo().getY() - o.getTo().getY()) <=(m.getFrom().getX()) && m.getTo().getX() - o.getTo().getX() <=3 && m.getTo().getX() - o.getTo().getX() >=0 && gameState.getCurrentTeam().getIndex() == 1){
                opponentsThatCanReach.add(o);
            }
        }
        System.out.println("Alle gegnerische Figuren die die Durchlaufende Figure erreichen können: "+opponentsThatCanReach);
        return opponentsThatCanReach;

    }

    /**
     * Gibt alle Züge eines Teams nach Figur geordnet zurück
     * @param b Ein beliebiges Spielfeld
     * @param own Eigenes Team?
     * @return Eine Map, in der jeder Figur alle ihre möglichen Züge zugeordnet werden
     */
    public static Map<Coordinates, List<Move>> getMovesForEveryPiece(Board b, boolean own) {
        Map<Coordinates, Piece> pieces = own ? getOwnPieces(b) : getOpponentPieces(b);
        Map<Coordinates, List<Move>> out = new HashMap<>();

        for (Coordinates c : pieces.keySet()) {
            out.put(c, getMovesFrom(b, c));
        }

        return out;
    }

    /**
     * @param b Ein beliebiges Spielfeld
     * @param piece die Koordinaten, an denen die gewünschte Figur steht
     * @return Eine Liste mit Zügen, die die Figur an den angegebenen Koordinaten ausführen kann
     */
    public static List<Move> getMovesFrom(Board b, Coordinates piece) {
        List<Move> moves = isOwn(b, piece) ? getOwnMoves(b) : getOpponentMoves(b);
        List<Move> out = new ArrayList<>();

        for (Move m : moves) {
            if (m.getFrom().equals(piece)) {
                out.add(m);
            }
        }

        return out;
    }

    /**
     * Befindet sich das angegebene Team in einer Zwickmühle (mindestens 2 Figuren bedroht, es können nicht alle im
     * nächsten Zug gedeckt werden)
     * @param b Ein beliebiges Spielfeld
     * @param own Eigenes Team?
     * @return Ob das Team in einer Zwickmühle ist
     */
    public static boolean zwickmuehle(Board b, boolean own){
        int bedroht = bedrohteFiguren(b, own).size();

        // Mindestens 2 bedrohte Figuren
        if(bedroht >= 2) {
            for (Move m : own ? getOwnMoves(b) : getOpponentMoves(b)) {
                Board sim = b.clone();
                sim.movePiece(m);

                // Können nicht alle durch einen Zug gedeckt werden?
                if (!bedrohteFiguren(b, own).isEmpty()) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Erzeugt der Zug eine Zwickmühle für den Gegner: eigene Figur ist nicht bedroht bzw. gedeckt, mindestens zwei gegnerische Figuren
     * sind danach bedroht, (Gegner kann im nächsten Zug nicht beide gleichzeitig decken)
     * @param b Ein beliebiges Spielfeld
     * @param m Der zu überprüfende Zug
     * @return Eine Liste mit Figuren, die danach bedroht sind
     */
    public static List<Coordinates> zwickmuehleAfterMove(Board b, Move m) {
        boolean own = isOwn(b, m.getFrom());
        Board sim = b.clone();
        sim.movePiece(m);

        if(zwickmuehle(sim, !own)){
            return bedrohteFiguren(b, !own);
        }

        return new ArrayList<>();
    }

    /**
     * Gibt zurück, ob nach einem Zug irgendeine der eigenen Figuren bedroht ist
     *
     * @param b Ein beliebiges Spielfeld
     * @param m Der zu überprüfende Zug
     */
    @Deprecated
    public static boolean isAnyoneBedrohtAfterMove(Board b, Move m) {
        Board imag = b.clone();
        imag.movePiece(m);

        Set<Coordinates> ownPieces = getOwnPieces(imag).keySet();
        for (Coordinates c : ownPieces) {
            if (isBedroht(imag, c, isOwn(b, m.getFrom()))) {
                return true;
            }
        }

        return false;
    }

    /**
     * Sucht nach Figuren, die gerade unmittelbar bedroht sind und gibt mögliche rettende Züge zurück
     */
    @Deprecated
    public static List<Move> getSavingMoves(Board b) {
        //todo: ausschließen wenn Figuren gedeckt sind, einschließen wenn die gegnerische Figur ein Turm ist
        Set<Coordinates> pieces = getOwnPieces(b).keySet();
        List<Coordinates> threatened = new ArrayList<>();
        List<Move> savingMoves = new ArrayList<>();

        for (Move m : getOpponentMoves(b)) {
            if (pieces.contains(m.getTo())) {
                threatened.add(m.getTo());
            }
        }

        for (Move m : gameState.getPossibleMoves()) {
            if (threatened.contains(m.getFrom()) && !isAnyoneBedrohtAfterMove(b, m)) {
                savingMoves.add(m);
            }
        }

        return savingMoves;
    }
}
