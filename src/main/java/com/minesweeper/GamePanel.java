package com.minesweeper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.BiConsumer;

/**
 * Панель с игровым полем, таймером и клетками-кнопками.
 * Поддержка больших полей через JScrollPane.
 * Обработка левого клика: закрытую клетку открывает, открытую — chord.
 */
public class GamePanel extends JPanel {
    private MinesweeperGame game;
    private JLabel timeLabel;
    private JPanel gridPanel;
    private CellButton[][] buttons;
    private Timer swingTimer;
    private long startTime;
    private BiConsumer<Boolean, Integer> gameFinishedCallback;
    private boolean gameActive;

    public GamePanel(MinesweeperGame game, BiConsumer<Boolean, Integer> gameFinishedCallback) {
        this.game = game;
        this.gameFinishedCallback = gameFinishedCallback;
        this.gameActive = true;
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        // Верхняя панель с таймером
        JPanel topPanel = new JPanel();
        timeLabel = new JLabel("Время: 0 сек");
        timeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        topPanel.add(timeLabel);
        add(topPanel, BorderLayout.NORTH);

        // Сетка кнопок внутри прокручиваемой области
        gridPanel = new JPanel(new GridLayout(game.getRows(), game.getCols(), 0, 0));
        buttons = new CellButton[game.getRows()][game.getCols()];
        for (int r = 0; r < game.getRows(); r++) {
            for (int c = 0; c < game.getCols(); c++) {
                CellButton btn = new CellButton(r, c);
                buttons[r][c] = btn;
                gridPanel.add(btn);
            }
        }

        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        swingTimer = new Timer(1000, e -> updateTime());
    }

    private void updateTime() {
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        timeLabel.setText("Время: " + elapsed + " сек");
    }

    /**
     * Обработчик клика по клетке (левая или правая кнопка).
     */
    private void cellClicked(int row, int col, boolean leftClick) {
        if (!gameActive) return;

        // Первый клик инициализирует минное поле и запускает таймер
        if (!game.isInitialized()) {
            game.initialize(row, col);
            startTime = System.currentTimeMillis();
            swingTimer.start();
        }

        Cell cell = game.getCell(row, col);

        if (leftClick) {
            // Левый клик
            if (cell.isOpen()) {
                // Если клетка уже открыта — попробовать chord
                handleChord(row, col);
            } else {
                // Обычное открытие закрытой клетки
                handleOpen(row, col);
            }
        } else {
            // Правый клик: переключение флажка
            game.toggleFlag(row, col);
            updateButton(row, col);
        }
    }

    /**
     * Открытие закрытой клетки.
     */
    private void handleOpen(int row, int col) {
        Cell cell = game.getCell(row, col);
        if (cell.isFlagged()) return;  // флажок блокирует открытие

        MinesweeperGame.ActionResult result = game.openCell(row, col);
        if (result == MinesweeperGame.ActionResult.MINE) {
            gameOver(false);
        } else {
            updateAllButtons();
            checkWin();
        }
    }

    /**
     * Выполнение chord по открытой клетке.
     */
    private void handleChord(int row, int col) {
        MinesweeperGame.ActionResult chordResult = game.chordCell(row, col);
        if (chordResult == MinesweeperGame.ActionResult.MINE) {
            gameOver(false);
        } else {
            updateAllButtons();  // могли открыться новые клетки
            checkWin();
        }
    }

    /**
     * Проверка условия победы и вызов колбэка.
     */
    private void checkWin() {
        if (game.isWin()) {
            gameActive = false;
            swingTimer.stop();
            long elapsed = (System.currentTimeMillis() - startTime) / 1000;
            gameFinishedCallback.accept(true, (int) elapsed);
        }
    }

    /**
     * Обработка проигрыша: остановка таймера, раскрытие мин, уведомление.
     */
    private void gameOver(boolean win) {
        gameActive = false;
        swingTimer.stop();
        if (!win) {
            game.revealAll();
        }
        updateAllButtons();
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        gameFinishedCallback.accept(win, (int) elapsed);
    }

    // ------- Обновление кнопок -------
    private void updateButton(int row, int col) {
        buttons[row][col].updateAppearance(game.getCell(row, col));
    }

    private void updateAllButtons() {
        for (int r = 0; r < game.getRows(); r++) {
            for (int c = 0; c < game.getCols(); c++) {
                updateButton(r, c);
            }
        }
    }

    // ----- Внутренний класс кнопки-клетки -----
    private class CellButton extends JButton {
        private int row, col;

        public CellButton(int row, int col) {
            this.row = row;
            this.col = col;
            setPreferredSize(new Dimension(28, 28));
            setFont(new Font("Arial", Font.BOLD, 12));
            setMargin(new Insets(0, 0, 0, 0));
            setFocusPainted(false);
            setBackground(Color.LIGHT_GRAY);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        cellClicked(row, col, true);
                    } else if (SwingUtilities.isRightMouseButton(e)) {
                        cellClicked(row, col, false);
                    }
                }
            });
        }

        public void updateAppearance(Cell cell) {
            if (cell.isOpen()) {
                setBackground(Color.WHITE);
                if (cell.isMine()) {
                    setText("M");
                    setForeground(Color.RED);
                } else {
                    int mines = cell.getNeighborMines();
                    if (mines > 0) {
                        setText(String.valueOf(mines));
                        switch (mines) {
                            case 1: setForeground(Color.BLUE); break;
                            case 2: setForeground(new Color(0, 128, 0)); break;
                            case 3: setForeground(Color.RED); break;
                            case 4: setForeground(new Color(0, 0, 128)); break;
                            case 5: setForeground(new Color(128, 0, 0)); break;
                            case 6: setForeground(Color.CYAN); break;
                            case 7: setForeground(Color.BLACK); break;
                            case 8: setForeground(Color.GRAY); break;
                            default: setForeground(Color.BLACK);
                        }
                    } else {
                        setText("");
                    }
                }
            } else {
                setBackground(Color.LIGHT_GRAY);
                if (cell.isFlagged()) {
                    setText("F");
                    setForeground(Color.RED);
                } else {
                    setText("");
                    setForeground(Color.BLACK);
                }
            }
        }
    }
}