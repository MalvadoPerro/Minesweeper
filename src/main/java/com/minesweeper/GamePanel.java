package com.minesweeper;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.BiConsumer;

/**
 * Панель с игровым полем, таймером, счётчиком мин и кнопкой перезапуска.
 */
public class GamePanel extends JPanel {
    private MinesweeperGame game;
    private JLabel mineCounterLabel;   // счётчик оставшихся мин
    private JLabel timeLabel;          // таймер
    private JButton restartButton;     // кнопка перезапуска
    private JPanel gridPanel;
    private CellButton[][] buttons;
    private Timer swingTimer;
    private long startTime;
    private BiConsumer<Boolean, Integer> gameFinishedCallback;
    private Runnable restartAction;
    private boolean gameActive;

    public GamePanel(MinesweeperGame game,
                     BiConsumer<Boolean, Integer> gameFinishedCallback,
                     Runnable restartAction) {
        this.game = game;
        this.gameFinishedCallback = gameFinishedCallback;
        this.restartAction = restartAction;
        this.gameActive = true;
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        // Верхняя панель: счётчик мин, кнопка рестарта, таймер
        add(createTopPanel(), BorderLayout.NORTH);

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

        // Начальное отображение счётчика
        updateMineCounter();
    }

    /**
     * Создаёт верхнюю панель в стиле табло: счётчик мин слева, кнопка рестарта по центру, таймер справа.
     */
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Стиль табло: чёрный фон, зелёный шрифт, вдавленная рамка
        Border loweredBorder = BorderFactory.createLoweredBevelBorder();
        Font digitalFont = new Font("Courier New", Font.BOLD, 20);

        // ----- Левый счётчик мин -----
        mineCounterLabel = new JLabel("", JLabel.CENTER);
        mineCounterLabel.setOpaque(true);
        mineCounterLabel.setBackground(Color.BLACK);
        mineCounterLabel.setForeground(Color.GREEN);
        mineCounterLabel.setFont(digitalFont);
        mineCounterLabel.setBorder(loweredBorder);
        mineCounterLabel.setPreferredSize(new Dimension(70, 36));
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        leftPanel.add(mineCounterLabel);

        // ----- Центральная кнопка рестарта -----
        restartButton = new JButton("↺");
        restartButton.setFont(new Font("Arial", Font.BOLD, 18));
        restartButton.setFocusPainted(false);
        restartButton.setPreferredSize(new Dimension(50, 36));
        restartButton.addActionListener(e -> restartGame());
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        centerPanel.add(restartButton);

        // ----- Правый таймер -----
        timeLabel = new JLabel("0", JLabel.CENTER);
        timeLabel.setOpaque(true);
        timeLabel.setBackground(Color.BLACK);
        timeLabel.setForeground(Color.GREEN);
        timeLabel.setFont(digitalFont);
        timeLabel.setBorder(loweredBorder);
        timeLabel.setPreferredSize(new Dimension(70, 36));
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        rightPanel.add(timeLabel);

        topPanel.add(leftPanel, BorderLayout.WEST);
        topPanel.add(centerPanel, BorderLayout.CENTER);
        topPanel.add(rightPanel, BorderLayout.EAST);

        return topPanel;
    }

    /**
     * Перезапускает игру: останавливает таймер и вызывает внешний колбэк restartAction.
     */
    private void restartGame() {
        if (swingTimer != null) {
            swingTimer.stop();
        }
        restartAction.run();
    }

    private void updateTime() {
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        timeLabel.setText(String.valueOf(elapsed));
    }

    /**
     * Обновляет счётчик мин: totalMines - количество установленных флажков.
     */
    private void updateMineCounter() {
        int remaining = game.getTotalMines() - game.getFlaggedCount();
        mineCounterLabel.setText(String.valueOf(remaining));
    }

    private void cellClicked(int row, int col, boolean leftClick) {
        if (!gameActive) return;

        if (!game.isInitialized()) {
            game.initialize(row, col);
            startTime = System.currentTimeMillis();
            swingTimer.start();
        }

        Cell cell = game.getCell(row, col);

        if (leftClick) {
            if (cell.isOpen()) {
                handleChord(row, col);
            } else {
                handleOpen(row, col);
            }
        } else {
            game.toggleFlag(row, col);
            updateButton(row, col);
            updateMineCounter();   // счётчик мог измениться
        }
    }

    private void handleOpen(int row, int col) {
        Cell cell = game.getCell(row, col);
        if (cell.isFlagged()) return;

        MinesweeperGame.ActionResult result = game.openCell(row, col);
        if (result == MinesweeperGame.ActionResult.MINE) {
            gameOver(false);
        } else {
            updateAllButtons();
            updateMineCounter();
            checkWin();
        }
    }

    private void handleChord(int row, int col) {
        MinesweeperGame.ActionResult chordResult = game.chordCell(row, col);
        if (chordResult == MinesweeperGame.ActionResult.MINE) {
            gameOver(false);
        } else {
            updateAllButtons();
            updateMineCounter();
            checkWin();
        }
    }

    private void checkWin() {
        if (game.isWin()) {
            gameActive = false;
            swingTimer.stop();
            long elapsed = (System.currentTimeMillis() - startTime) / 1000;
            gameFinishedCallback.accept(true, (int) elapsed);
        }
    }

    private void gameOver(boolean win) {
        gameActive = false;
        swingTimer.stop();
        if (!win) {
            game.revealAll();
        }
        updateAllButtons();
        updateMineCounter();
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        gameFinishedCallback.accept(win, (int) elapsed);
    }

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