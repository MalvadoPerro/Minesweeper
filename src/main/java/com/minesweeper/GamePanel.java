package com.minesweeper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.BiConsumer;

/**
 * Панель с игровым полем, таймером и клетками-кнопками.
 */
public class GamePanel extends JPanel {
    private MinesweeperGame game;
    private JLabel timeLabel;                       // отображение времени
    private JPanel gridPanel;                       // сетка кнопок
    private CellButton[][] buttons;                 // матрица кнопок
    private Timer swingTimer;                       // таймер для обновления времени раз в секунду
    private long startTime;                         // время начала игры
    private BiConsumer<Boolean, Integer> gameFinishedCallback; // (победа?, время)
    private boolean gameActive;                     // активна ли игра

    public GamePanel(MinesweeperGame game, BiConsumer<Boolean, Integer> gameFinishedCallback) {
        this.game = game;
        this.gameFinishedCallback = gameFinishedCallback;
        this.gameActive = true;
        setLayout(new BorderLayout());
        initComponents();
    }

    /**
     * Строит верхнюю панель с таймером и сетку кнопок.
     */
    private void initComponents() {
        // Верхняя панель
        JPanel topPanel = new JPanel();
        timeLabel = new JLabel("Время: 0 сек");
        timeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        topPanel.add(timeLabel);
        add(topPanel, BorderLayout.NORTH);

        // Сетка игрового поля
        gridPanel = new JPanel(new GridLayout(game.getRows(), game.getCols(), 0, 0));
        buttons = new CellButton[game.getRows()][game.getCols()];
        for (int r = 0; r < game.getRows(); r++) {
            for (int c = 0; c < game.getCols(); c++) {
                CellButton btn = new CellButton(r, c);
                buttons[r][c] = btn;
                gridPanel.add(btn);
            }
        }
        add(gridPanel, BorderLayout.CENTER);

        // Таймер запустится после первого клика
        swingTimer = new Timer(1000, e -> updateTime());
    }

    /**
     * Обновляет текст времени.
     */
    private void updateTime() {
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        timeLabel.setText("Время: " + elapsed + " сек");
    }

    /**
     * Обрабатывает клик левой или правой кнопкой мыши по клетке.
     * @param leftClick true, если левая кнопка
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
        if (cell.isOpen()) return;   // открытые клетки не обрабатываем

        if (leftClick) {
            // Левый клик: открыть клетку
            if (cell.isFlagged()) return;   // флажок блокирует открытие

            MinesweeperGame.ActionResult result = game.openCell(row, col);
            if (result == MinesweeperGame.ActionResult.MINE) {
                // Взрыв
                gameActive = false;
                swingTimer.stop();
                game.revealAll();
                updateAllButtons();
                long elapsed = (System.currentTimeMillis() - startTime) / 1000;
                gameFinishedCallback.accept(false, (int) elapsed);
            } else {
                // Успешное открытие
                updateAllButtons();   // обновляем все кнопки (учтёт каскадное открытие)
                if (game.isWin()) {
                    gameActive = false;
                    swingTimer.stop();
                    long elapsed = (System.currentTimeMillis() - startTime) / 1000;
                    gameFinishedCallback.accept(true, (int) elapsed);
                }
            }
        } else {
            // Правый клик: переключить флажок
            game.toggleFlag(row, col);
            updateButton(row, col);
        }
    }

    private void updateButton(int row, int col) {
        CellButton btn = buttons[row][col];
        btn.updateAppearance(game.getCell(row, col));
    }

    private void updateAllButtons() {
        for (int r = 0; r < game.getRows(); r++) {
            for (int c = 0; c < game.getCols(); c++) {
                updateButton(r, c);
            }
        }
    }

    // ----- Класс кнопки-клетки -----
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

            // Обработка левой и правой кнопок мыши
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

        /**
         * Обновляет внешний вид кнопки в зависимости от состояния клетки.
         */
        public void updateAppearance(Cell cell) {
            if (cell.isOpen()) {
                setBackground(Color.WHITE);
                if (cell.isMine()) {
                    setText("M");           // мина (можно заменить на другой символ)
                    setForeground(Color.RED);
                } else {
                    int mines = cell.getNeighborMines();
                    if (mines > 0) {
                        setText(String.valueOf(mines));
                        switch (mines) {    // раскраска чисел
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
                // Закрытая клетка
                setBackground(Color.LIGHT_GRAY);
                if (cell.isFlagged()) {
                    setText("F");           // флаг
                    setForeground(Color.RED);
                } else {
                    setText("");
                    setForeground(Color.BLACK);
                }
            }
        }
    }
}