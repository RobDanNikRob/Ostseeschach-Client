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

/**
 * Das Herz des Clients:
 * Eine sehr simple Logik, die ihre Züge zufällig wählt,
 * aber gültige Züge macht.
 * <p>
 * Außerdem werden zum Spielverlauf Konsolenausgaben gemacht.
 */
public class Logic implements IGameHandler {
  private static final Logger log = LoggerFactory.getLogger(Logic.class);

  /** Aktueller Spielstatus. */
  private GameState gameState;

  public void onGameOver(@NotNull GameResult data) {
    log.info("Das Spiel ist beendet, Ergebnis: {}", data);
  }

  @Override @NotNull
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
    for(Move m : possibleMoves){
      GameState sim = gameState.clone();
      sim.performMove(m);
      if(sim.getPointsForTeam(gameState.getCurrentTeam()) >= 2){
        return m;
      }
    }

    // Bewertung der Figuren


    // Wählen von guten Zügen

    List<Move> savingMoves = GameInfo.getSavingMoves(board);
    if(savingMoves.size() > 0){
      for(Move m : savingMoves){
        if(GameInfo.isOpponent(board, m.getTo())){
          System.out.println(m + " kann sich retten und dabei schlagen");
          return m;
        }
      }
      System.out.println(savingMoves + " können zum Retten benutzt werden");
      return savingMoves.get((int) (Math.random() * savingMoves.size()));
    }

    List<Move> badMoves = new ArrayList<>();
    for(Move m : possibleMoves){
      if (GameInfo.isOpponent(board, m.getTo()) && !GameInfo.isAttackableAfterMove(board, m)){
        System.out.println(m + " kann schlagen und ist danach sicher");
        return m;
      }
      if(GameInfo.isAttackableAfterMove(board, m)){
        System.out.println(m + " macht mich angreifbar");
        badMoves.add(m);
      }
    }

    if(possibleMoves.size() != badMoves.size()){
      possibleMoves.removeAll(badMoves);
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
    this.gameState = (GameState) gameState;
    GameInfo.setGameState((GameState) gameState);
    log.info("Zug: {} Dran: {}", gameState.getTurn(), gameState.getCurrentTeam());
  }

  @Override
  public void onError(@NotNull String error) {
    log.warn("Fehler: {}", error);
  }
}
