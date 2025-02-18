package dev.totallyspies.spydle.frontend.test;

import dev.totallyspies.spydle.frontend.client.ClientSocketConfig;
import dev.totallyspies.spydle.frontend.client.ClientSocketHandler;
import dev.totallyspies.spydle.shared.proto.messages.SbGuess;
import dev.totallyspies.spydle.shared.proto.messages.SbGuessUpdate;
import dev.totallyspies.spydle.shared.proto.messages.SbMessage;
import dev.totallyspies.spydle.shared.proto.messages.SbStartGame;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;

@Component
@Profile("test")
@ConditionalOnProperty(name = "enable-e2e", havingValue = "true")
public class TestClient {

  private final Logger logger = LoggerFactory.getLogger(TestClient.class);

  private final ClientSocketConfig config;
  private TestPlayer player1;
  private TestPlayer player2;
  private String ip;
  private int port;

  public TestClient(ClientSocketConfig config) {
    this.config = config;
  }

  public void initPlayers() {
    this.player1 =
        new TestPlayer(
            "player1",
            UUID.fromString("11111111-1111-1111-1111-111111111111"),
            config.createClient());
    this.player2 =
        new TestPlayer(
            "player2",
            UUID.fromString("22222222-2222-2222-2222-222222222222"),
            config.createClient());
  }

  @EventListener(ApplicationReadyEvent.class)
  public void onApplicationReady() {
    setIpPort();

    initPlayers();

    testTurnsAndGuesses();

    waitForever();
    // Close the websocket
    // handler.close();
  }

  public void testTurnsAndGuesses() {
    /* Test scenario:
     * player1 joins the game, gets an UPDATE_PLAYER_LIST message
     * player2 joins the game, both players get an UPDATE_PLAYER_LIST message
     * player1 starts the game, duration is 12 seconds, turn time is 3 seconds
     * both players get a GAME_START message with all players
     * both players get NEW_TURN message, assigned string: "gis", player2 makes a turn
     * player2 is the first player to make a turn
     * player2 makes a right guess, gets 4 points; both players get GUESS_RESULT message, guess is correct
     * both players get NEW_TURN message, player1 makes a new turn, assigned string: "igh"
     * player1 makes a wrong guess "QWERTY"; both players get a GUESS_RESULT message, guess is wrong
     * By this time, player1 has 0 points, player2 has 4 points, player2 wins
     * when the game ends, both players get the list of players sorted by score, descending
     */
    this.player1.open(ip, port);
    waitMs(500);
    this.player2.open(ip, port);
    waitMs(500);
    testStartNewGame(player1, 12, 3);
    waitMs(1500);
    testGuessUpdate(player1, "shouldbeignoredbyserver");
    waitMs(100);
    testGuessUpdate(player2, "gis"); // need to wait more here
    waitMs(100);
    testGuess(player2, "gist"); // right guess
    waitMs(500);
    testGuess(player1, "QWERTY"); // wrong guess
    /*
    Expected logs (shortened):
    */
    /*
    Established connection to websocket
    Opened socket connection with gameserver at localhost:7654, with client ID 1....1 and name player1
    Firing message with PayloadCase UPDATE_PLAYER_LIST with client 1....1, message: update_player_list {  players {    player_name: "player1"  }}
    Established connection to websocket
    Opened socket connection with gameserver at localhost:7654, with client ID 2....2 and name player2
    Firing message with PayloadCase UPDATE_PLAYER_LIST with client 1....1, message: update_player_list {  players {    player_name: "player1"  }  players {    player_name: "player2"  }}
    Firing message with PayloadCase UPDATE_PLAYER_LIST with client 2....2, message: update_player_list {  players {    player_name: "player1"  }  players {    player_name: "player2"  }}
    Sending server message start_game{total_game_time_seconds:12turn_time_seconds:3}
    Firing message with PayloadCase GAME_START with client 2....2, message: game_start {  players {    player_name: "player1"  }  players {    player_name: "player2"  }  total_game_time_seconds: 12  turn_time_seconds: 3}
    Firing message with PayloadCase GAME_START with client 1....1, message: game_start {  players {    player_name: "player1"  }  players {    player_name: "player2"  }  total_game_time_seconds: 12  turn_time_seconds: 3}
    Firing message with PayloadCase NEW_TURN with client 1....1, message: new_turn {  assigned_string: "gis"  current_player {    player_name: "player2"  }}
    Firing message with PayloadCase NEW_TURN with client 2....2, message: new_turn {  assigned_string: "gis"  current_player {    player_name: "player2"  }}
    Firing message with PayloadCase TIMER_TICK with client 1....1, message: timer_tick {  game_time_left_seconds: 12  turn_time_left_seconds: 3}
    Firing message with PayloadCase TIMER_TICK with client 2....2, message: timer_tick {  game_time_left_seconds: 12  turn_time_left_seconds: 3}
    Firing message with PayloadCase TIMER_TICK with client 2....2, message: timer_tick {  game_time_left_seconds: 11  turn_time_left_seconds: 2}
    Firing message with PayloadCase TIMER_TICK with client 1....1, message: timer_tick {  game_time_left_seconds: 11  turn_time_left_seconds: 2}
    Sending server message guess{guessed_word:"gist"}
    Firing message with PayloadCase GUESS_RESULT with client 2....2, message: guess_result {  player_name: "player2"  guess: "gist"  correct: true}
    Firing message with PayloadCase GUESS_RESULT with client 1....1, message: guess_result {  player_name: "player2"  guess: "gist"  correct: true}
    Firing message with PayloadCase NEW_TURN with client 1....1, message: new_turn {  assigned_string: "igh"  current_player {    player_name: "player1"  }}
    Firing message with PayloadCase NEW_TURN with client 2....2, message: new_turn {  assigned_string: "igh"  current_player {    player_name: "player1"  }}
    Firing message with PayloadCase UPDATE_PLAYER_LIST with client 2....2, message: update_player_list {  players {    player_name: "player1"  }  players {    player_name: "player2"    score: 4  }}
    Firing message with PayloadCase UPDATE_PLAYER_LIST with client 1....1, message: update_player_list {  players {    player_name: "player1"  }  players {    player_name: "player2"    score: 4  }}
    Sending server message guess{guessed_word:"QWERTY"}
    Firing message with PayloadCase GUESS_RESULT with client 2....2, message: guess_result {  player_name: "player1"  guess: "QWERTY"}
    Firing message with PayloadCase GUESS_RESULT with client 1....1, message: guess_result {  player_name: "player1"  guess: "QWERTY"}
    Firing message with PayloadCase TIMER_TICK with client 1....1, message: timer_tick {  game_time_left_seconds: 10  turn_time_left_seconds: 2}
    Firing message with PayloadCase TIMER_TICK with client 2....2, message: timer_tick {  game_time_left_seconds: 10  turn_time_left_seconds: 2}
    Firing message with PayloadCase TIMER_TICK with client 2....2, message: timer_tick {  game_time_left_seconds: 9  turn_time_left_seconds: 1}
    Firing message with PayloadCase TIMER_TICK with client 1....1, message: timer_tick {  game_time_left_seconds: 9  turn_time_left_seconds: 1}
    Firing message with PayloadCase NEW_TURN with client 1....1, message: new_turn {  assigned_string: "ni"  current_player {    player_name: "player2"    score: 4  }}
    Firing message with PayloadCase NEW_TURN with client 2....2, message: new_turn {  assigned_string: "ni"  current_player {    player_name: "player2"    score: 4  }}
    Firing message with PayloadCase TIMER_TICK with client 1....1, message: timer_tick {  game_time_left_seconds: 8  turn_time_left_seconds: 3}
    Firing message with PayloadCase TIMER_TICK with client 2....2, message: timer_tick {  game_time_left_seconds: 8  turn_time_left_seconds: 3}
    Firing message with PayloadCase TIMER_TICK with client 1....1, message: timer_tick {  game_time_left_seconds: 7  turn_time_left_seconds: 2}
    Firing message with PayloadCase TIMER_TICK with client 2....2, message: timer_tick {  game_time_left_seconds: 7  turn_time_left_seconds: 2}
    Firing message with PayloadCase TIMER_TICK with client 1....1, message: timer_tick {  game_time_left_seconds: 6  turn_time_left_seconds: 1}
    Firing message with PayloadCase TIMER_TICK with client 2....2, message: timer_tick {  game_time_left_seconds: 6  turn_time_left_seconds: 1}
    Firing message with PayloadCase TIMER_TICK with client 1....1, message: timer_tick {  game_time_left_seconds: 5}
    Firing message with PayloadCase TIMER_TICK with client 2....2, message: timer_tick {  game_time_left_seconds: 5}
    Firing message with PayloadCase NEW_TURN with client 1....1, message: new_turn {  assigned_string: "bb"  current_player {    player_name: "player1"  }}
    Firing message with PayloadCase NEW_TURN with client 2....2, message: new_turn {  assigned_string: "bb"  current_player {    player_name: "player1"  }}
    Firing message with PayloadCase TIMER_TICK with client 1....1, message: timer_tick {  game_time_left_seconds: 4  turn_time_left_seconds: 3}
    Firing message with PayloadCase TIMER_TICK with client 2....2, message: timer_tick {  game_time_left_seconds: 4  turn_time_left_seconds: 3}
    Firing message with PayloadCase TIMER_TICK with client 2....2, message: timer_tick {  game_time_left_seconds: 3  turn_time_left_seconds: 2}
    Firing message with PayloadCase TIMER_TICK with client 1....1, message: timer_tick {  game_time_left_seconds: 3  turn_time_left_seconds: 2}
    Firing message with PayloadCase TIMER_TICK with client 1....1, message: timer_tick {  game_time_left_seconds: 2  turn_time_left_seconds: 1}
    Firing message with PayloadCase TIMER_TICK with client 2....2, message: timer_tick {  game_time_left_seconds: 2  turn_time_left_seconds: 1}
    Firing message with PayloadCase NEW_TURN with client 1....1, message: new_turn {  assigned_string: "rb"  current_player {    player_name: "player2"    score: 4  }}
    Firing message with PayloadCase NEW_TURN with client 2....2, message: new_turn {  assigned_string: "rb"  current_player {    player_name: "player2"    score: 4  }}
    Firing message with PayloadCase TIMER_TICK with client 1....1, message: timer_tick {  game_time_left_seconds: 1  turn_time_left_seconds: 1}
    Firing message with PayloadCase TIMER_TICK with client 2....2, message: timer_tick {  game_time_left_seconds: 1  turn_time_left_seconds: 1}
    Firing message with PayloadCase GAME_END with client 2....2, message: game_end {  players {    player_name: "player2"    score: 4  }  players {    player_name: "player1"  }}
    Firing message with PayloadCase GAME_END with client 1....1, message: game_end {  players {    player_name: "player2"    score: 4  }  players {    player_name: "player1"  }}
    Closed connection to websocket, status: CloseStatus[code=1006, reason=null]
    Closed connection to websocket, status: CloseStatus[code=1006, reason=null]
    */
  }

  public void testGuessUpdate(TestPlayer player, String update) {
    var message =
        SbMessage.newBuilder()
            .setGuessUpdate(SbGuessUpdate.newBuilder().setGuessedWord(update))
            .build();

    player.send(message);
  }

  public void testStartNewGame(TestPlayer player, int gameTimeSeconds, int turnTimeSeconds) {
    var message =
        SbMessage.newBuilder()
            .setStartGame(
                SbStartGame.newBuilder()
                    .setTotalGameTimeSeconds(gameTimeSeconds)
                    .setTurnTimeSeconds(turnTimeSeconds))
            .build();

    player.send(message);
  }

  public void testGuess(TestPlayer player, String guess) {
    var message =
        SbMessage.newBuilder().setGuess(SbGuess.newBuilder().setGuessedWord(guess)).build();
    player.send(message);
  }

  // Custom event fired after socket close
  @EventListener(ClientSocketHandler.CloseEvent.class)
  public void onSocketClose(ClientSocketHandler.CloseEvent event) {
    UUID clientId = event.getClientId();
    CloseStatus reason = event.getStatus();
    // Close window, do other logic?
  }

  private void setIpPort() {
    Scanner in = new Scanner(System.in);
    System.out.println("Enter IP:");
    this.ip = in.next();
    System.out.println("Enter PORT:");
    this.port = in.nextInt();
  }

  private void waitMs(long ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private void waitForever() {
    try {
      new Semaphore(0).acquire();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
