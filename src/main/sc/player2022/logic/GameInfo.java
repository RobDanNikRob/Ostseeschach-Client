package sc.player2022.logic;

import com.thoughtworks.xstream.mapper.Mapper;
import sc.plugin2022.Vector;
import sc.plugin2022.*;

import java.awt.*;
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
     * Gibt zurück, ob sich auf einem Brett an einer Koordinate eine eigene Figur befindet.
     *
     * @param b Brett, für das überprüft werden soll
     * @param c Koordinate, die überprüft werden soll
     */
    public static boolean isOwn(Board b, Coordinates c) {
        try {
            return b.get(c).getTeam().equals(gameState.getCurrentTeam());
        } catch (NullPointerException e) {
            return false;
        }
    }

    /**
     * @param b           Ein beliebiges Spielfeld
     * @param coordinates Die zu überprüfenden Koordinaten
     * @return Ob an der Position ein Turm ist
     */
    public static boolean isTower(Board b, Coordinates coordinates) {
        try {
            return b.get(coordinates).getCount() > 1;
        } catch (NullPointerException e){
            System.out.println("isTower: Auf dem Feld " + coordinates + " steht keine Figur");
            return false;
        }
    }

    /**
     * Gibt zurück, ob sich auf einem Brett an einer Koordinate eine gegnerische Figur befindet.
     *
     * @param b Brett, für das überprüft werden soll
     * @param c Koordinate, die überprüft werden soll
     */
    public static boolean isOpponent(Board b, Coordinates c) {
        try {
            return b.get(c).getTeam().equals(gameState.getOtherTeam());
        } catch (NullPointerException e) {
            return false;
        }
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
     * Gibt als boolean zurück, ob eine eingegebene Figur von einem Gegner bedroht ist, außer wenn sie gedeckt ist
     *
     * @param b     Ein beliebiges Spielfeld
     * @param piece die zu überprüfende Koordinate
     * @return boolean
     */
    public static boolean isBedroht(Board b, Coordinates piece) {
        List<Move> moves = isOwn(b, piece) ? getOpponentMoves(b) : getOwnMoves(b);

        for (Move m : moves) {
            // Schlägt der Move und ist die Figur am Ziel nicht gedeckt ODER schlägt der Move und die gegnerische Figur
            // ist ein Turm?
            if (piece.equals(m.getTo()) && (!isGedeckt(b, piece) || isTower(b, m.getFrom()))) {
                System.out.println("isBedroht: " + piece + " ist bedroht");
                return true;
            }
        }

        System.out.println("isBedroht: " + piece + " ist nicht bedroht");
        return false;
    }

    /**
     * gibt zurück, ob eine figur geschlagen werden kann, unabhängig davon ob sie gedeck ist
     *
     * @param b
     * @param piece
     * @return boolean, kann sie geschlagen werden, unabhänig von Kontermöglichkeit
     */
    public static boolean isAttackable(Board b, Coordinates piece) {
        List<Move> moves = isOwn(b, piece) ? getOpponentMoves(b) : getOwnMoves(b);

        for (Move m : moves) {
            // Schlägt der Move und ist die Figur am Ziel nicht gedeckt ODER schlägt der Move und die gegnerische Figur
            // ist ein Turm?
            if (piece.equals(m.getTo())) {
                return true;
            }
        }

        return false;


    }


    /**
     * gibt eine Liste der Koordinaten zurück, von denen die angegebene Figur bedroht wird
     *
     * @param b     Ein beliebiges Spielfeld
     * @param piece Die zu überprüfende Koordinate
     * @return Coordinates
     */
    public static List<Coordinates> getWirdBedrohtVon(Board b, Coordinates piece) {
        List<Coordinates> out = new ArrayList<>();
        List<Move> moves = isOwn(b, piece) ? getOpponentMoves(b) : getOwnMoves(b);

        if (isBedroht(b, piece)) {
            for (Move m : moves) {
                if (piece.equals(m.getTo())) {
                    out.add(m.getFrom());
                }
            }
        }

        return out;
    }

    /**
     * gibt die Figuren zurück, die von der eingegebenen Figur bedroht werden, außer natürlich gedeckte Figuren die sie theoretisch schlagen könnte
     *
     * @param b     Ein beliebiges Spielfeld
     * @param piece Die zu überprüfende Koordinate
     * @return List<Coordinates>
     */
    public static List<Coordinates> getBedroht(Board b, Coordinates piece) {
        List<Coordinates> out = new ArrayList<>();

        Map<Coordinates, Piece> Map = isOwn(b, piece) ? getOpponentPieces(b) : getOwnPieces(b);
        for (Move m : getMovesFrom(b, piece)) {
            if (Map.containsKey(m.getTo()) && isGedeckt(b, m.getTo())) {
                out.add(m.getTo());
            }
        }
        return out;

    }

    /**
     * @param b Ein beliebiges Spielfeld
     * @param c Die zu überprüfende Koordinate
     * @return Eine Liste von Koordinaten, die von der angegebenen Figur gedeckt werden
     */
    public static List<Coordinates> getDeckt(Board b, Coordinates c) {
        List<Coordinates> out = new ArrayList<>();

        Map<Coordinates, Piece> pieces = isOwn(b, c) ? getOwnPieces(b) : getOpponentPieces(b);

        for (Vector v : b.get(c).getPossibleMoves()) {
            Coordinates to = c.plus(v);
            if (pieces.containsKey(to)) {
                out.add(to);
            }
        }

        return out;
    }

    /**
     * @param b Ein beliebiges Spielfeld
     * @param c Die zu überprüfende Koordinate
     * @return Eine Liste mit Koordinaten, von denen die angegebene Figur gedeckt wird
     */
    public static List<Coordinates> getWirdGedecktVon(Board b, Coordinates c) {
        Map<Coordinates, Piece> pieces = isOwn(b, c) ? getOwnPieces(b) : getOpponentPieces(b);

        List<Coordinates> out = new ArrayList<>();

        if (!isTower(b, c)) {
            for (Coordinates current : pieces.keySet()) {
                for (Vector v : pieces.get(current).getPossibleMoves()) {
                    if (current.plus(v).equals(c)) {
                        System.out.println("Die Figur bei " + c + " ist gedeckt von der Figur bei " + current);
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
     * @param b Ein beliebiges Spielfeld
     * @param c Die zu überprüfende Koordinate
     * @return ob die Figur an den Koordinaten gedeckt ist.
     */
    public static boolean isGedeckt(Board b, Coordinates c) {
        return getWirdGedecktVon(b, c).size() != 0;
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
            if (isGedeckt(b, c)) {
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
            if (isBedroht(b, c)) {
                out.add(c);
            }
        }

        System.out.println("bedrohteFiguren: "+ out);
        return out;
    }

    /**
     *Figuren die jemanden bedrohen
     * @param b
     * @param own
     * @return List
     */
    public static List<Coordinates> bedrohendeFiguren(Board b, boolean own){

        Set<Coordinates> pieces = (own ? getOwnPieces(b) : getOpponentPieces(b)).keySet();
       List<Coordinates> Out = new ArrayList<>();
        for(Coordinates c: pieces){
            if(getBedroht(b, c).size() != 0)
            Out.add(c);
        }
        return Out;

    }

    /**
     * Figuren die einen Turm bedrohen
     * @param b
     * @param own
     * @return List
     */
    public static List<Move> bedrohendeFigurenTurm(Board b, boolean own){

        List<Move> pieces = new ArrayList<>(own ? getOwnMoves(b) : getOpponentMoves(b));
        List<Move> Out = new ArrayList<>();
        for(Move c: pieces){
            int a = gameState.getPointsForTeam(own ? gameState.getCurrentTeam() : gameState.getOtherTeam());
            Board r = b.clone();
            GameState g = new GameState(r, gameState.getTurn());
            r.movePiece(c);
            if(g.getPointsForTeam(own ? g.getCurrentTeam() : g.getOtherTeam()) != a){
                Out.add(c);

            }
        }
        return Out;
    }



    /**
     * gibt als int zurück wie viele Figuren von Towern bedroht sind
     *
     * @param b    Ein beliebiges Spielfeld
     * @param own, true for own false for enemy
     * @return int
     */
    public static List<Coordinates> bedrohteFigurenByTower(Board b, boolean own) {
        List<Coordinates> pieces = bedrohteFiguren(b, own);
        List<Coordinates> out = new ArrayList<>();

        for (Coordinates c : pieces) {
            if (isBedrohtByTower(b, c)) {
                out.add(c);
            }
        }
        return out;
    }

    /**
     * gibt zurück, ob eine Figur von einem Turm bedroht wird
     *
     * @param b     Ein beliebiges Spielfeld
     * @param piece Die zu überprüfende Koordinate
     * @return boolean
     */
    public static boolean isBedrohtByTower(Board b, Coordinates piece) {
        if (!isBedroht(b, piece)) return false;

        Map<Coordinates, Piece> Map = isOwn(b, piece) ? getOpponentPieces(b) : getOwnPieces(b);

        try {
            for (Coordinates c : getWirdBedrohtVon(b, piece)) {
                if (isTower(b, c)) return true;
            }
        } catch (NullPointerException e) {
            return false;
        }
        return false;
    }

    /**
     * gibt als int die differenz der bedrohten Figuren zurück, negativ, wenn weniger, positiv, wenn mehr und null, wenn gleich bleibt
     *
     * @param b    Ein beliebiges Spielfeld
     * @param move Der zu überprüfende Zug
     * @param own  true for own false for enemy
     * @return +-0 int
     */
    public static int bedrohtDifferenceAfterMove(Board b, Move move, boolean own) {
        Board c = b.clone();
        int before = bedrohteFiguren(c, own).size();
        c.movePiece(move);
        int after = bedrohteFiguren(c, own).size();
        return after - before;
    }

    public static int gedecktDifferenceAfterMove(Board b, Move m, boolean own){
        Board c = b.clone();
        int before = gedeckteFiguren(c, own).size();
        c.movePiece(m);
        int after = gedeckteFiguren(c, own).size();
        return after - before;
    }

    /**
     * gibt zurück, ob eine Figur durch andere Blockiert ist (sie deckt eine sonst bedrohte Figur)
     * @param b
     * @param coordinates
     * @return boolean ist blockiert
     */
    public boolean isBlocked(Board b, Coordinates coordinates) {
        List<Coordinates> deckt = getDeckt(b, coordinates);
        for (Coordinates c : deckt) {
            if (isAttackable(b, c))
                return true;
        }
        return false;
    }

    /**
     * gibt als boolean zurück, ob eine Figur nach dem Move bedroht ist
     *
     * @param b    Ein beliebiges Spielfeld
     * @param move Der zu überprüfende Zug
     * @return boolean
     */
    public static boolean isBedrohtAfterMove(Board b, Move move) {
        return isBedroht(b, move.getTo());
    }

    // Erstellt eine Liste mit allen Mooves die Wahrscheinlich zum Durchlaufsieg führt.
    // Falls kein Moove infrage kommt gibt es Null zurück

    public static List<Move>[] durchlaufen(Board b) {
        List<Move> gegnerischeSeite = new ArrayList<Move>();
        for (Move m : getOwnMoves(b)) {
            if (gameState.getCurrentTeam().getIndex() == 0 && !isBedrohtAfterMove(b, m) && m.getFrom().getX() > 3) {
                gegnerischeSeite.add(m);
            }
            if (gameState.getCurrentTeam().getIndex() == 1 && !isBedrohtAfterMove(b, m) && m.getFrom().getX() < 4) {
                gegnerischeSeite.add(m);
            }
        }

        List<Move>[] future = new ArrayList[gegnerischeSeite.size()];
        if (gegnerischeSeite.isEmpty())
            return future;
        else {
            for (Move m: gegnerischeSeite){
                int i = 0;
                future [i] = getOpponentsMovesThatReach(b,m);
                i++;
            }
            return future;

        }
    }

    /*
    Guckt ob eine Figur zu 100% durchlaufen kann
    Der Gegner kann dies nicht verhindern, außer man verliert
     */
    public static List<Move>[] futureDurchlaufen(Board b, List <Move> []  a, int i) {
        List<Move>[] futureMoves = new ArrayList[getOpponentMoves(b).size()];
        Board c = b.clone();
        for (Move n : a[i]) {
            if(Math.abs(n.getTo().getX()-n.getFrom().getX()) == 1) {
            c.movePiece(n);
            }
            if(!getOpponentsMovesThatReach(c,n).isEmpty()){
            futureMoves[i].add(durchlaufen(c)[0].get(i));}
            else if(n.getTo().getX() == 7 || n.getTo().getX() == 0) {
                return futureMoves;
            }

        }
        return futureDurchlaufen(c,futureMoves,i++);
    }
    // Gibt eine Liste zurück mit gegnerischen Mooves die theoretisch das Durchlaufen verhindern können
    public static List<Move> getOpponentsMovesThatReach (Board b, Move m) {
        List<Move> opponentsThatCanReach = new ArrayList <Move>();
        for(Move o : getOpponentMoves(b)){
            if(Math.abs(m.getTo().getY() - o.getTo().getY()) <=3 && m.getTo().getX() - o.getTo().getX() >=-3 && m.getTo().getX() - o.getTo().getX() <=0 && gameState.getCurrentTeam().getIndex() == 0){
                opponentsThatCanReach.add(o);
            }
            if(Math.abs(m.getTo().getY() - o.getTo().getY()) <=3 && m.getTo().getX() - o.getTo().getX() <=3 && m.getTo().getX() - o.getTo().getX() >=0 && gameState.getCurrentTeam().getIndex() == 1){
                opponentsThatCanReach.add(o);
            }
        }
        return opponentsThatCanReach;

    }


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
     * @param c die Koordinaten, an denen der gewünschte Stein steht
     * @return Eine Liste mit Zügen, die die Figur an den angegebenen Koordinaten ausführen kann
     */
    public static List<Move> getMovesFrom(Board b, Coordinates c) {
        List<Move> moves = isOwn(b, c) ? getOwnMoves(b) : getOpponentMoves(b);
        List<Move> out = new ArrayList<>();

        for (Move m : moves) {
            if (m.getFrom().equals(c)) {
                out.add(m);
            }
        }

        return out;
    }

    /**
     * @param b Ein beliebiges Spielfeld
     * @param m Der zu überprüfende Zug
     * @return Ob der angegebene Zug eine Zwickmühle erzeugt: eigene Figur ist nicht bedroht bzw. gedeckt, mindestens
     * zwei gegnerische Figuren sind danach bedroht, (Gegner kann im nächsten Zug nicht beide gleichzeitig decken)
     * 0: keine Zwickmühle
     * 1: Zwickmühle, aber Gegner kann im nächsten Zug alle decken (ist trotzdem blockiert)
     * 2: Zwickmühle und Gegner kann im nächsten Zug nicht alle decken
     */
    public static int zwickmuehle(Board b, Move m) {
        Coordinates to = m.getTo();

        Board sim = b.clone();

        // Können mindestens zwei gegnerische Figuren im nächsten Zug erreicht werden?
        if (!isBedroht(b, to) && bedrohtDifferenceAfterMove(b, m, false) >= 2) {
            sim.movePiece(m);

            // Alle gegnerischen Figuren, die nach dem Zug bedroht werden (müsste >= 2 sein)
            List<Coordinates> bedroht = getBedroht(b, to);
            System.out.println(bedroht.size());

            // Kann der Gegner diese Figuren im folgenden Zug noch gleichzeitig decken?
            for(Move opponentMove : getOpponentMoves(sim)){
                Board sim2 = sim.clone();
                sim2.movePiece(opponentMove);

                if(getDeckt(sim2, opponentMove.getTo()).containsAll(bedroht)){
                    return 1;
                }
            }
            return 2;
        }
        return 0;
    }

    /**
     * Gibt zurück, ob nach einem Zug irgendeine der eigenen Figuren direkt angegriffen werden kann
     *
     * @param m Der zu überprüfende Zug
     */
    public static boolean isAnyoneBedrohtAfterMove(Board b, Move m) {
        Board imag = b.clone();
        imag.movePiece(m);

        Set<Coordinates> ownPieces = getOwnPieces(imag).keySet();
        for (Coordinates c : ownPieces) {
            if (isBedroht(imag, c)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Sucht nach Figuren, die gerade unmittelbar bedroht sind und gibt mögliche rettende Züge zurück
     */
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
