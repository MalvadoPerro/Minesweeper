package com.minesweeper;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Главное окно игры. Управляет переключением между стартовым экраном и игровым полем.
 */
public class GameWindow extends JFrame {
    private CardLayout cardLayout;            // для переключения панелей
    private JPanel mainPanel;                 // контейнер с картами
    private JPanel startPanel;                // стартовая панель
    private JPanel gamePanelContainer;        // контейнер для игровой панели
    private GamePanel gamePanel;              // текущая игровая панель
    private MinesweeperGame game;             // текущая игра

    public GameWindow() {
        setTitle("Сапёр");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);                  // фиксированный размер
        initUI();
        setSize(600, 650);                    // размер окна
        setLocationRelativeTo(null);          // по центру экрана
    }

    /**
     * Инициализация пользовательского интерфейса.
     */
    private void initUI() {
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Создаём стартовую панель
        startPanel = createStartPanel();

        // Заглушка для игровой панели (будет заменена при старте игры)
        gamePanelContainer = new JPanel(new BorderLayout());
        gamePanelContainer.add(new JLabel("Игровое поле появится здесь", JLabel.CENTER));

        mainPanel.add(startPanel, "start");
        mainPanel.add(gamePanelContainer, "game");

        add(mainPanel);
    }

    /**
     * Формирует стартовую панель с картинкой и кнопкой "Начать игру".
     */
    private JPanel createStartPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Картинка, созданная программно
        ImageIcon imageIcon = createStartImage();
        JLabel imageLabel = new JLabel(imageIcon, JLabel.CENTER);
        panel.add(imageLabel, BorderLayout.CENTER);

        // Кнопка запуска игры
        JButton playButton = new JButton("Начать игру");
        playButton.setFont(new Font("Arial", Font.BOLD, 18));
        playButton.addActionListener(e -> startGame());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(playButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Создаёт изображение для стартового экрана (бомба и надпись "САПЁР").
     */
    private ImageIcon createStartImage() {
        int width = 200;
        int height = 150;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Фон
        g2d.setColor(new Color(70, 130, 180));
        g2d.fillRect(0, 0, width, height);

        // Рисуем бомбу
        g2d.setColor(Color.BLACK);
        g2d.fillOval(50, 30, 100, 100);
        g2d.setColor(Color.YELLOW);
        g2d.fillRect(90, 15, 20, 30);          // фитиль
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(95, 15, 85, 5);
        g2d.drawLine(105, 15, 115, 5);

        // Текст
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
     * Начинает новую игру: создаёт модель игры и игровую панель, переключается на неё.
     */
    private void startGame() {
        // Создаём модель: поле 20x20, 40 мин
        game = new MinesweeperGame(20, 20, 40);

        // Создаём игровую панель и передаём callback для завершения игры
        if (gamePanel != null) {
            gamePanelContainer.remove(gamePanel);
        }
        gamePanel = new GamePanel(game, this::onGameFinished);
        gamePanelContainer.add(gamePanel, BorderLayout.CENTER);
        gamePanelContainer.revalidate();
        gamePanelContainer.repaint();

        // Переключаемся на игровую панель
        cardLayout.show(mainPanel, "game");
    }

    /**
     * Обработчик завершения игры (вызывается из GamePanel).
     * @param win true, если игрок выиграл
     * @param timeElapsed затраченное время в секундах
     */
    private void onGameFinished(boolean win, int timeElapsed) {
        String message;
        if (win) {
            message = "Поздравляем! Вы выиграли!\nВремя: " + timeElapsed + " сек.";
        } else {
            message = "Вы подорвались на мине!\nВремя: " + timeElapsed + " сек.";
        }
        JOptionPane.showMessageDialog(this, message, "Игра окончена", JOptionPane.INFORMATION_MESSAGE);
        // Возвращаемся на стартовый экран
        cardLayout.show(mainPanel, "start");
    }
}