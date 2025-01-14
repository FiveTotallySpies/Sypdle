package dev.totallyspies.spydle.frontend.views.game_room_panels;

import dev.totallyspies.spydle.frontend.interface_adapters.game_room.GameRoomViewModel;
import dev.totallyspies.spydle.shared.proto.messages.Player;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.*;

/**
 * Represents the main panel for the game room during gameplay.
 *
 * <p>The {@code GamePanel} manages the display and interactions for player panels, the substring
 * being guessed, the game timer, and the turn timer. It uses a circular layout to display player
 * information dynamically, updating as the game progresses.
 */
public class GamePanel extends JPanel {

  /** The ViewModel managing the state of the game room. */
  private final GameRoomViewModel model;

  /** Map of player names to their corresponding {@link PlayerPanel} instances. */
  private final Map<String, PlayerPanel> playerPanels = new LinkedHashMap<>();

  /** Label displaying the current substring to be guessed. */
  private final JLabel substringLabel;

  /** Label displaying the remaining time for the overall game. */
  private final JLabel timerLabel;

  /** Label displaying the remaining time for the current player's turn. */
  private final JLabel timerPlayer;

  /**
   * Constructs a {@code GamePanel} with the given game room ViewModel.
   *
   * @param model the {@link GameRoomViewModel} that provides game data and state.
   */
  public GamePanel(GameRoomViewModel model) {
    this.model = model;
    setLayout(null);
    setBackground(new Color(195, 217, 255));

    // Center substring label
    substringLabel = new JLabel("SUBSTRING", SwingConstants.CENTER);
    substringLabel.setFont(new Font("Arial", Font.BOLD, 18));
    substringLabel.setBounds(300, 200, 200, 50);
    add(substringLabel);

    timerPlayer = new JLabel("", SwingConstants.CENTER);
    timerPlayer.setFont(new Font("Arial", Font.PLAIN, 16));
    timerPlayer.setBounds(300, 250, 200, 50);
    add(timerPlayer);

    // Timer label
    timerLabel = new JLabel("Timer: 0:00", SwingConstants.CENTER);
    timerLabel.setFont(new Font("Arial", Font.PLAIN, 16));
    timerLabel.setBounds(650, 10, 100, 30);
    add(timerLabel);

    // Create player panels in a circle layout
    createPlayerPanels();
  }

  /** Creates and positions player panels in a circular layout. */
  private void createPlayerPanels() {
    for (PlayerPanel panel : playerPanels.values()) {
      remove(panel); // Remove panel from the GamePanel
    }
    playerPanels.values().forEach(PlayerPanel::cleanupParent);
    playerPanels.clear();
    for (Player player : model.getPlayerList()) {
      PlayerPanel playerPanel = new PlayerPanel(player.getPlayerName(), player.getScore(), this);
      playerPanels.put(player.getPlayerName(), playerPanel);
      add(playerPanel);
    }

    int centerX = 400;
    int centerY = 250;
    int radius = 150;

    int width = 100;
    int height = 100;
    int j = 0;
    for (PlayerPanel panel : playerPanels.values()) {
      double angle = 2 * Math.PI * (j++) / playerPanels.size();
      int x = (int) (centerX + radius * Math.cos(angle) - ((double) width / 2));
      int y = (int) (centerY + radius * Math.sin(angle) - ((double) height / 2));
      panel.setLocation(x, y, width, height);
    }
  }

  /**
   * Updates the game panel to reflect the current game state.
   *
   * <p>Updates player panels, highlights the current turn player, updates the timers, and sets the
   * substring to be guessed.
   */
  public void updateGame() {
    // Update list of players
    Set<String> currentPlayerNames = playerPanels.keySet();
    Set<String> modelPlayerNames =
        model.getPlayerList().stream().map(Player::getPlayerName).collect(Collectors.toSet());
    boolean playersChanged =
        currentPlayerNames.size() != modelPlayerNames.size()
            || modelPlayerNames.stream().anyMatch(name -> !currentPlayerNames.contains(name));
    if (playersChanged) {
      createPlayerPanels();
    }

    // Update points
    for (Player player : model.getPlayerList()) {
      playerPanels.get(player.getPlayerName()).updateScore(player.getScore());
    }

    // Highlight the current turn player's panel with a red border
    Player currentTurnPlayer = model.getCurrentTurnPlayer();
    if (currentTurnPlayer != null) {
      highlightPlayerPanel(currentTurnPlayer); // Highlight current player
    }

    substringLabel.setText(model.getCurrentSubstring());
    if (model.getTurnTimerSeconds() > 0) {
      timerPlayer.setText("Guess in " + model.getTurnTimerSeconds());
    } else {
      timerPlayer.setText("");
    }
    timerLabel.setText(
        "Timer: "
            + model.getGameTimerSeconds() / 60
            + ":"
            + String.format("%02d", model.getGameTimerSeconds() % 60));

    revalidate();
    repaint();
  }

  // displays the string that the current player is typing
  public void updateStringDisplayed(String playerName, GameRoomViewModel.Guess guess) {
    playerPanels.get(playerName).setPlayerGuess(guess.getCurrentWord(), guess.getVerdict());
  }

  private void highlightPlayerPanel(Player currentTurnPlayer) {
    // Loop through all player panels and set the border accordingly
    for (Map.Entry<String, PlayerPanel> entry : playerPanels.entrySet()) {
      Player player =
          model.getPlayerList().stream()
              .filter(p -> p.getPlayerName().equals(entry.getKey()))
              .findFirst()
              .orElse(null);
      if (player != null && player.equals(currentTurnPlayer)) {
        entry.getValue().setPlayerBorder(Color.RED, 5); // Set red border for current player
      } else {
        entry.getValue().setPlayerBorder(Color.GRAY, 2); // Set default gray border for others
      }
    }
  }
}
