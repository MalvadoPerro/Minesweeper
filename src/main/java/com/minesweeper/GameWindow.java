package com.minesweeper;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Главное окно игры. Управляет переключением между стартовым экраном и игровым полем.
 * Содержит кнопку рестарта на игровом поле.
 */
public class GameWindow extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JPanel startPanel;
    private JPanel gamePanelContainer;
    private GamePanel gamePanel;
    private MinesweeperGame game;

    private JComboBox<String> sizeCombo;
    private int lastSize = 20;   // последний выбранный размер, по умолчанию 20

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

        // Панель выбора размера
        JPanel sizePanel = new JPanel(new FlowLayout());
        sizePanel.add(new JLabel("Размер поля:"));
        String[] sizes = {"10x10", "15x15", "20x20", "25x25", "30x30", "40x40", "50x50", "60x60"};
        sizeCombo = new JComboBox<>(sizes);
        sizeCombo.setSelectedIndex(1);   // 20x20 по умолчанию
        sizePanel.add(sizeCombo);
        panel.add(sizePanel, BorderLayout.NORTH);

        // Картинка
        ImageIcon imageIcon = createStartImage();
        JLabel imageLabel = new JLabel(imageIcon, JLabel.CENTER);
        panel.add(imageLabel, BorderLayout.CENTER);

        // Кнопка "Начать игру"
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
     * Запускает новую игру с настройками, выбранными на стартовом экране.
     */
    private void startGame() {
        String selected = (String) sizeCombo.getSelectedItem();
        int size = Integer.parseInt(selected.split("x")[0]);
        lastSize = size;   // запоминаем последний размер

        int totalCells = size * size;
        int mines = Math.max(1, totalCells / 10);

        game = new MinesweeperGame(size, size, mines);

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
     * Перезапускает игру с тем же размером поля, не возвращаясь на стартовый экран.
     */
    private void restartGame() {
        int totalCells = lastSize * lastSize;
        int mines = Math.max(1, totalCells / 10);

        game = new MinesweeperGame(lastSize, lastSize, mines);

        if (gamePanel != null) {
            gamePanelContainer.remove(gamePanel);
        }
        gamePanel = new GamePanel(game, this::onGameFinished, this::restartGame);
        gamePanelContainer.add(gamePanel, BorderLayout.CENTER);
        gamePanelContainer.revalidate();
        gamePanelContainer.repaint();
        // Карточка не переключается, остаёмся на игровом экране
    }

    /**
     * Обработчик завершения игры (вызывается из GamePanel).
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