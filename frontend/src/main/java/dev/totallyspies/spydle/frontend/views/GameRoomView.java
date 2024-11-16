package dev.totallyspies.spydle.frontend.views;

import dev.totallyspies.spydle.frontend.views.game_room_panels.GamePanel;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

@Component
public class GameRoomView extends JPanel {
    public GamePanel gamePanel;

    public GameRoomView() {
        setLayout(new BorderLayout()); // Set layout to BorderLayout for GameRoomView
        setBackground(new Color(195, 217, 255)); // Set light blue background for entire view

        // Create a container panel for the game screen
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false); // Make container transparent to show the GameRoomView background

        // Initialize the Game Panel with 4 players (adjust the number of players as needed)
        gamePanel = new GamePanel(4);

        // Add the game panel to the center of the container
        container.add(gamePanel, BorderLayout.CENTER);

        // Back button styling
        JButton backButton = new JButton("Back to Welcome");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setBackground(new Color(138, 43, 226)); // Blueviolet
        backButton.setForeground(Color.BLACK); // Black text color
        backButton.setFocusPainted(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.setPreferredSize(new Dimension(150, 40));

        // Back button action listener
        backButton.addActionListener(e -> {
            new WelcomeView(); // Open the welcome page
        });

        // Top panel to hold the back button on the left
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false); // Transparent background to show main background color
        topPanel.add(backButton, BorderLayout.WEST); // Add back button to the left side

        // Add the top panel to the top (NORTH) of the main container
        container.add(topPanel, BorderLayout.NORTH);

        // Add the container to the center of GameRoomView
        add(container, BorderLayout.CENTER);

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("SpyDle - Game Over");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(500, 500); // Full-screen dimensions
            frame.setLocationRelativeTo(null);
            frame.add(new GameRoomView());
            frame.setVisible(true);
        });
    }
}
