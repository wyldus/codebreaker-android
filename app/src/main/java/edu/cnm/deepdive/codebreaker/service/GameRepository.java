package edu.cnm.deepdive.codebreaker.service;

import android.content.Context;
import edu.cnm.deepdive.codebreaker.model.dao.CompletedGameDao;
import edu.cnm.deepdive.codebreaker.model.entity.Game;
import edu.cnm.deepdive.codebreaker.model.entity.Guess;
import edu.cnm.deepdive.codebreaker.model.entity.CompletedGame;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class GameRepository {

  private final CodebreakerServiceProxy proxy;
  private final Context context;
  private final CompletedGameDao completedGameDao;
  // TODO Add fields for GameDao and GuessDao.

  public GameRepository(Context context) {
    this.context = context;
    proxy = CodebreakerServiceProxy.getInstance();
    completedGameDao = CodebreakerDatabase.getInstance().getCompletedGameDao();
    // TODO Initialize fields for GameDao and GuessDao.
  }

  // TODO Modify to write Game to database after receiving from server.
  public Single<Game> create(String pool, int length) {
    Game gameStub = new Game();
    gameStub.setPool(pool);
    gameStub.setLength(length);
    return proxy.startGame(gameStub)
        .subscribeOn(Schedulers.io());
  }

  // TODO Simplify by using GameWithGuesses POJO.
  public Single<Game> get(String id) {
    return proxy.getGame(id)
        .flatMap((game) -> proxy.getGuesses(game.getServiceKey())
            .map((guesses) -> {
              game.getGuesses().addAll(guesses);
              return game;
            }))
        .subscribeOn(Schedulers.io());
  }

  // TODO Write to database after receiving Guess from server.
  public Single<Game> addGuess(Game game, String text) {
    Guess guess = new Guess();
    guess.setText(text);
    return proxy
        .submitGuess(game.getServiceKey(), guess)
        .flatMap((completedGuess) -> {
          if (completedGuess.isSolution()) {
            CompletedGame completedGame = new CompletedGame();
            completedGame.setServiceKey(game.getServiceKey());
            completedGame.setStarted(game.getCreated());
            completedGame.setCompleted(completedGuess.getCreated());
            completedGame.setAttempts(game.getGuesses().size() + 1);
            completedGame.setCodeLength(game.getLength());
            completedGame.setPoolSize((int) game.getPool().codePoints().count());
            return completedGameDao
                .insert(completedGame)
                .map((id) -> completedGuess);
          } else {
            return Single.just(completedGuess);
          }
        })
        .map((completedGuess) -> {
          game.getGuesses().add(completedGuess);
          game.setGuessCount(game.getGuessCount() + 1);
          game.setSolved(completedGuess.isSolution());
          return game;
        })
        .subscribeOn(Schedulers.io());
  }

}
