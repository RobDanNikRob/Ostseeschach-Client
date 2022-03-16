package sc.player2022.logic;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sc.api.plugins.IGameState;
import sc.player.IGameHandler;
import sc.plugin2022.*;
import sc.shared.GameResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static sc.player2022.logic.GameInfo.*;

/**
 * Das Herz des Clients:
 * Eine sehr simple Logik, die ihre Züge zufällig wählt,
 * aber gültige Züge macht.
 * <p>
 * Außerdem werden zum Spielverlauf Konsolenausgaben gemacht.
 */
public class Logic implements IGameHandler {
    private static final Logger log = LoggerFactory.getLogger(Logic.class);

    /**
     * Aktueller Spielstatus.
     */
    private GameState gameState;

    public void onGameOver(@NotNull GameResult data) {
        log.info("Das Spiel ist beendet, Ergebnis: {}", data);
    }

    @Override
    @NotNull
    public Move calculateMove() {
        long startTime = System.currentTimeMillis();
        log.info("Es wurde ein Zug von {} angefordert.", gameState.getCurrentTeam());

        // Wichtige Spielinformationen
        Board board = gameState.getBoard();
        Map<Coordinates, Piece> ownPieces = GameInfo.getOwnPieces(board);
        Map<Coordinates, Piece> opponentPieces = GameInfo.getOpponentPieces(board);
        List<Move> possibleMoves = GameInfo.getOwnMoves(board);
        List<Move> opponentMoves = GameInfo.getOpponentMoves(board);

        // Kann mit einem Zug das Spiel gewonnen werden?
        if(!canWin(board, true).isEmpty()){
            return canWin(board, true).get(0);
        }

        //Listen der Züge
        ArrayList<Move> goodMoves = new ArrayList<>();
        ArrayList<Move> badMoves = new ArrayList<>();
        ArrayList<Move> mediumMoves = new ArrayList<>();

        //Verteidigung
        // Für Spieler 2 im letzten Zug spielt Verteidigen keine Rolle
        if (gameState.getTurn() != 59 && !bedrohteFiguren(board, true).isEmpty()) {
            // Gegnerische bedrohende Figuren
            List<Coordinates> bedrohend = bedrohendeFiguren(board, false);

            // Kann die bedrohende Figur gefahrlos geschlagen werden?
            List<Move> angriffMoves = new ArrayList<>();
            for(Move m : canSafelyKill(board, true)){
                if(bedrohend.contains(m.getTo())){
                    // Kann durch den Zug ein Punkt gemacht werden (Turm schlägt bedrohende Figur)
                    if(getPointMoves(board, true).contains(m)){
                        System.out.println("Verteidigung: Punkt machen: " + m);
                        return m;
                    }
                    angriffMoves.add(m);
                }
            }

            if(!angriffMoves.isEmpty()){
                System.out.println("Verteidigung: Angriff: " + angriffMoves);
                return Bewertung.besterZug(board, angriffMoves);
            }

            // Kann die bedrohte Figur sich selbst in Sicherheit bewegen bzw. von einer anderen Figur gedeckt werden?
            int highest = 1;
            List<Move> verteidigungsMoves = new ArrayList<>();
            for(Move m : possibleMoves){
                // Wenn es bedrohte Tower gibt, werden nur Moves von Towern betrachtet, damit diese sich in Sicherheit
                // bringen können
                if(bedrohteTower(board, true).isEmpty() || isTower(board, m.getFrom())){
                    int diff = (-1) * bedrohtDifferenceAfterMove(board, m, true);
                    System.out.println("Verteidigung: bedroht difference after move " + m + ": " + diff);

                    // Sind durch den Zug weniger Figuren bedroht als durch alle anderen? Dann leere die Liste und
                    // speichere zukünftig nur noch gleich gute Züge
                    if(diff >= highest){
                        if (diff > highest) {
                            verteidigungsMoves.clear();
                            highest = diff;
                        }

                        verteidigungsMoves.add(m);
                    }
                }
            }

            // Wenn es bedrohte Tower gibt, dann diese auf jeden Fall in Sicherheit bringen, ansonsten nur Figuren saven
            // wenn es keine Zwickmühle gibt
            if ((!bedrohteTower(board, true).isEmpty() || !zwickmuehle(board, true)) && !verteidigungsMoves.isEmpty()) {
                System.out.println("Verteidigung: In Sicherheit bewegen bzw. bedrohte Figur decken: " + verteidigungsMoves);
                return Bewertung.besterZug(board, verteidigungsMoves);
            }
        }

        // Prüft ob ein Punkt gemacht werden kann
        List<Move> pointMoves = getPointMoves(board, true);
        if (pointMoves.size() != 0){
            System.out.println("Punkt machen: " + pointMoves);
            return Bewertung.besterZug(board, pointMoves);
        }


        //prüft ob das Durchlaufen möglich ist
        if (!durchlaufen(gameState.getBoard(), true).isEmpty()) {
           return durchlaufen(gameState.getBoard(), true).get(0);
      }



        //canSafelyKill
        if (!canSafelyKill(board, true).isEmpty()) {
            System.out.println("Sicher schlagen: " + canSafelyKill(board, true));
            return Bewertung.besterZug(board, canSafelyKill(board, true));
        }

        // Verhindern einer Zwickmühle des Gegners im nächsten Zug
        List<Move> zwickmuehleVerhindern = new ArrayList<>();
        for(Move m : opponentMoves){
            List<Coordinates> bedrohtZwickmuehle = zwickmuehleAfterMove(board, m);
            if(!bedrohtZwickmuehle.isEmpty()){
                for(Move ownMove : possibleMoves){
                    Board sim = board.clone();
                    sim.movePiece(ownMove);

                    // Bedrohen des Feldes, von dem aus der Gegner die Zwickmühle erzeugt
                    if(isBedroht(sim, m.getTo(), false)){
                        zwickmuehleVerhindern.add(ownMove);
                    }

                    // Können die bedrohten Figuren innerhalb von zwei Zügen gedeckt werden?
                    for(Move ownMove2 : getOwnMoves(sim)){
                        Board sim2 = sim.clone();
                        sim2.movePiece(ownMove2);
                        if(gedeckteFiguren(sim2, true).containsAll(bedrohtZwickmuehle)){
                            zwickmuehleVerhindern.add(ownMove);
                        }
                    }
                }
            }
        }

        if(!zwickmuehleVerhindern.isEmpty()){
            System.out.println("Zwickmühle verhindern: " + zwickmuehleVerhindern);
            return Bewertung.besterZug(board, zwickmuehleVerhindern);
        }

        //blockierte differenz
        List<Move> blockedMoves = new ArrayList<>();
        for (Move possibleMove : possibleMoves) {
            if(blockierteFigurenDifferenceAfterMove(board, possibleMove) > 0){
                blockedMoves.add(possibleMove);
            }

        }
        if(blockedMoves.size() != 0)
            return Bewertung.besterZug(board, blockedMoves);

        //Schlechte Züge
        for(int i = 0; i < possibleMoves.size(); i++) {

            if(bedrohtDifferenceAfterMove(board, possibleMoves.get(i), true) > 0){
            possibleMoves.remove(i);
            i--;
            continue;
            }
            Board c = board.clone();
            c.movePiece(possibleMoves.get(i));
           /* if(durchlaufen(board, true).isEmpty() && !durchlaufen(c, false).isEmpty()){
                possibleMoves.remove(i);
                i--;
            }*/
        }


        // Wählen des besten Zugs
        System.out.println("Übrige Züge: " + possibleMoves);
        Move move = Bewertung.besterZug(board, possibleMoves);


        //Debug-Ausgaben
        System.out.println("Eigene Figuren: " + ownPieces);
        System.out.println("Gegnerische Figuren: " + opponentPieces);
        System.out.println("Eigene Züge: " + possibleMoves);
        System.out.println("Gegnerische Züge: " + opponentMoves);

        log.info("Sende {} nach {}ms.", move, System.currentTimeMillis() - startTime);
        return move;
    }

    @Override
    public void onUpdate(IGameState gameState) {
        GameState g = (GameState) gameState;
        this.gameState = g;
        GameInfo.setGameState(g);
        Bewertung.setGameState(g);
        log.info("Zug: {} Dran: {}", gameState.getTurn(), gameState.getCurrentTeam());
    }

    @Override
    public void onError(@NotNull String error) {
        log.warn("Fehler: {}", error);
    }
}
