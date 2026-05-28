package com.minesweeper;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Главное окно игры. Стартовый экран с выбором размера поля и процента мин.
 * Игровой экран с кнопкой рестарта, счётчиком мин и таймером.
 */
public class GameWindow extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JPanel startPanel;
    private JPanel gamePanelContainer;
    private GamePanel gamePanel;
    private MinesweeperGame game;

    private JComboBox<String> sizeCombo;
    private JComboBox<String> percentCombo;

    private int lastSize = 20;      // последний выбранный размер
    private int lastPercent = 10;   // последний выбранный процент

    public GameWindow() {
        setTitle("Сапёр");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setMinimumSize(new Dimension(600, 650));
        initUI();
        setSize(700, 700);
        setLocationRelativeTo(null);
    }

    private void initUI() {
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        startPanel = createStartPanel();

        gamePanelContainer = new JPanel(new BorderLayout());
        gamePanelContainer.add(new JLabel("Игровое поле появится здесь", JLabel.CENTER));

        mainPanel.add(startPanel, "start");
        mainPanel.add(gamePanelContainer, "game");

        add(mainPanel);
    }

    private JPanel createStartPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // ---------- Верхняя панель с настройками ----------
        JPanel settingsPanel = new JPanel(new GridBagLayout());
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Подпись и выпадающий список для размера поля
        JLabel sizeLabel = new JLabel("Размер поля:");
        sizeLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        settingsPanel.add(sizeLabel, gbc);

        String[] sizes = {"10x10", "15x15", "20x20", "25x25", "30x30", "40x40", "50x50", "60x60"};
        sizeCombo = new JComboBox<>(sizes);
        sizeCombo.setSelectedIndex(1);   // 20x20
        gbc.gridx = 1;
        gbc.gridy = 0;
        settingsPanel.add(sizeCombo, gbc);

        // Подпись и выпадающий список для процента мин
        JLabel percentLabel = new JLabel("Процент мин:");
        percentLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 1;
        settingsPanel.add(percentLabel, gbc);

        String[] percents = {"5%", "10%", "15%", "20%", "25%", "30%", "35%", "40%", "45%", "50%"};
        percentCombo = new JComboBox<>(percents);
        percentCombo.setSelectedIndex(1);  // 10%
        gbc.gridx = 1;
        gbc.gridy = 1;
        settingsPanel.add(percentCombo, gbc);

        panel.add(settingsPanel, BorderLayout.NORTH);

        // ---------- Картинка в центре ----------
        ImageIcon imageIcon = createStartImage();
        JLabel imageLabel = new JLabel(imageIcon, JLabel.CENTER);
        panel.add(imageLabel, BorderLayout.CENTER);

        // ---------- Кнопка "Начать игру" снизу ----------
        JButton playButton = new JButton("Начать игру");
        playButton.setFont(new Font("Arial", Font.BOLD, 18));
        playButton.addActionListener(e -> startGame());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(playButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private ImageIcon createStartImage() {
        int width = 200;
        int height = 150;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(new Color(70, 130, 180));
        g2d.fillRect(0, 0, width, height);

        g2d.setColor(Color.BLACK);
        g2d.fillOval(50, 30, 100, 100);
        g2d.setColor(Color.YELLOW);
        g2d.fillRect(90, 15, 20, 30);
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(95, 15, 85, 5);
        g2d.drawLine(105, 15, 115, 5);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        String text = "САПЁР";
        FontMetrics fm = g2d.getFontMetrics();
        int textX = (width - fm.stringWidth(text)) / 2;
        g2d.drawString(text, textX, 140);

        g2d.dispose();
        return new ImageIcon(image);
    }

    /**
     * Запускает новую игру с параметрами со стартового экрана.
     */
    private void startGame() {
        String selectedSize = (String) sizeCombo.getSelectedItem();
        int size = Integer.parseInt(selectedSize.split("x")[0]);
        lastSize = size;

        String selectedPercent = (String) percentCombo.getSelectedItem();
        int percent = Integer.parseInt(selectedPercent.replace("%", ""));
        lastPercent = percent;

        int totalCells = size * size;
        int mines = Math.max(1, totalCells * percent / 100);

        game = new MinesweeperGame(size, size, mines);

        switchToGamePanel();
    }

    /**
     * Перезапускает игру с теми же lastSize и lastPercent.
     */
    private void restartGame() {
        int totalCells = lastSize * lastSize;
        int mines = Math.max(1, totalCells * lastPercent / 100);

        game = new MinesweeperGame(lastSize, lastSize, mines);

        switchToGamePanel();
    }

    /**
     * Заменяет игровую панель на новую и показывает её.
     */
    private void switchToGamePanel() {
        if (gamePanel != null) {
            gamePanelContainer.remove(gamePanel);
        }
        gamePanel = new GamePanel(game, this::onGameFinished, this::restartGame);
        gamePanelContainer.add(gamePanel, BorderLayout.CENTER);
        gamePanelContainer.revalidate();
        gamePanelContainer.repaint();
        cardLayout.show(mainPanel, "game");
    }

    /**
     * Вызывается при завершении игры.
     */
    private void onGameFinished(boolean win, int timeElapsed) {
        String message;
        if (win) {
            message = "Поздравляем! Вы выиграли!\nВремя: " + timeElapsed + " сек.";
        } else {
            message = "Вы подорвались на мине!\nВремя: " + timeElapsed + " сек.";
        }
        JOptionPane.showMessageDialog(this, message, "Игра окончена", JOptionPane.INFORMATION_MESSAGE);
        cardLayout.show(mainPanel, "start");
    }
}