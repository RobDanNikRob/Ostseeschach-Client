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
        for (Move m : possibleMoves) {
            GameState sim = gameState.clone();
            sim.performMove(m);
            if (sim.getPointsForTeam(gameState.getCurrentTeam()) >= 2) {
                return m;
            }
        }

        //Listen der Züge
        ArrayList<Move> goodMoves = new ArrayList<>();
        ArrayList<Move> badMoves = new ArrayList<>();
        ArrayList<Move> mediumMoves = new ArrayList<>();

        //Verteidigung
        // Für Spieler 2 im letzten Zug spielt Verteidigen keine Rolle
        System.out.println("test test");
        if (gameState.getTurn() != 59 && !bedrohteFiguren(board, true).isEmpty()) {
            System.out.println("test erfolgreich");
            // Gegnerische bedrohende Figuren
            List<Coordinates> bedrohend = bedrohendeFiguren(board, false);

            // Kann die bedrohende Figur gefahrlos geschlagen werden?
            List<Move> angriffMoves = new ArrayList<>();
            for(Move m : canSafelyKill(board, true)){
                if(bedrohend.contains(m.getTo())){
                    angriffMoves.add(m);
                }
            }

            if(!angriffMoves.isEmpty()){
                System.out.println("Verteidigung: Angriff" + angriffMoves);
                return Bewertung.besterZug(board, angriffMoves);
            }

            // Kann die bedrohte Figur sich selbst in Sicherheit bewegen bzw. von einer anderen Figur gedeckt werden?
            int highest = 1;
            List<Move> verteidigungsMoves = new ArrayList<>();
            for(Move m : possibleMoves){
                int diff = (-1) * bedrohtDifferenceAfterMove(board, m, true);
                System.out.println("Verteidigung: difference after move " + m + ": " + diff);

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

            if (!verteidigungsMoves.isEmpty()) {
                System.out.println("Verteidigung: In Sicherheit bewegen bzw. bedrohte Figur decken: " + verteidigungsMoves);
                return Bewertung.besterZug(board, verteidigungsMoves);
            }

        }


        //Schlagen

        //prüft ob Turm des Gegners geschlagen werden kann
        List<Move> turmSchlaeger = bedrohendeFigurenTurm(board, true);
        if (turmSchlaeger.size() != 0)
            return Bewertung.besterZug(board, turmSchlaeger);


        // Bewertung der Figuren

        //Wählen von guten Zügen 2.0


        // Wählen von guten Zügen

        List<Move> savingMoves = GameInfo.getSavingMoves(board);
        if (savingMoves.size() > 0) {
            for (Move m : savingMoves) {
                if (GameInfo.isOpponent(board, m.getTo())) {
                    System.out.println(m + " kann sich retten und dabei schlagen");
                    return m;
                }
            }
            System.out.println(savingMoves + " können zum Retten benutzt werden");
            return savingMoves.get((int) (Math.random() * savingMoves.size()));
        }

        List<Move> badMovesOld = new ArrayList<>();
        for (Move m : possibleMoves) {
            if (GameInfo.isOpponent(board, m.getTo()) && !GameInfo.isAnyoneBedrohtAfterMove(board, m)) {
                System.out.println(m + " kann schlagen und ist danach sicher");
                return m;
            }
            if (GameInfo.isAnyoneBedrohtAfterMove(board, m)) {
                System.out.println(m + " macht mich angreifbar");
                badMovesOld.add(m);
            }
        }

        if (possibleMoves.size() != badMovesOld.size()) {
            possibleMoves.removeAll(badMovesOld);
        }
        Move move = possibleMoves.get((int) (Math.random() * possibleMoves.size()));

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
