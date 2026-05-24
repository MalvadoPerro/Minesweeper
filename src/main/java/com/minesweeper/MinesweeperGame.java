package com.minesweeper;

import java.util.Random;

/**
 * Логика игры "Сапёр".
 */
public class MinesweeperGame {
    public enum ActionResult {
        OK, ALREADY_OPEN, MINE, FLAGGED
    }

    private final int rows;
    private final int cols;
    private final int totalMines;
    private Cell[][] field;
    private boolean initialized;
    private Random random;

    public MinesweeperGame(int rows, int cols, int totalMines) {
        this.rows = rows;
        this.cols = cols;
        this.totalMines = totalMines;
        this.initialized = false;
        this.random = new Random();
        field = new Cell[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                field[r][c] = new Cell();
            }
        }
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public Cell getCell(int row, int col) { return field[row][col]; }
    public boolean isInitialized() { return initialized; }

    /**
     * Расставляет мины после первого клика, гарантируя, что клетка (safeRow, safeCol) не будет миной.
     */
    public void initialize(int safeRow, int safeCol) {
        int minesPlaced = 0;
        while (minesPlaced < totalMines) {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);
            if ((r == safeRow && c == safeCol) || field[r][c].isMine()) {
                continue;
            }
            field[r][c].setMine(true);
            minesPlaced++;
        }

        // Подсчёт мин вокруг каждой клетки
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (!field[r][c].isMine()) {
                    int count = countNeighborMines(r, c);
                    field[r][c].setNeighborMines(count);
                }
            }
        }
        initialized = true;
    }

    private int countNeighborMines(int row, int col) {
        int count = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int nr = row + dr;
                int nc = col + dc;
                if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && field[nr][nc].isMine()) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Открывает клетку. Если клетка с 0 мин, рекурсивно открывает соседей.
     * @return ActionResult с результатом операции
     */
    public ActionResult openCell(int row, int col) {
        Cell cell = field[row][col];
        if (cell.isOpen()) return ActionResult.ALREADY_OPEN;
        if (cell.isFlagged()) return ActionResult.FLAGGED;
        if (cell.isMine()) {
            cell.setOpen(true);
            return ActionResult.MINE;
        }

        cell.setOpen(true);
        if (cell.getNeighborMines() == 0) {
            openNeighbors(row, col);
        }
        return ActionResult.OK;
    }

    private void openNeighbors(int row, int col) {
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int nr = row + dr;
                int nc = col + dc;
                if (nr >= 0 && nr < rows && nc >= 0 && nc < cols) {
                    Cell neighbor = field[nr][nc];
                    if (!neighbor.isOpen() && !neighbor.isFlagged() && !neighbor.isMine()) {
                        openCell(nr, nc);  // рекурсивное открытие
                    }
                }
            }
        }
    }

    /**
     * Переключает флажок на закрытой клетке.
     */
    public void toggleFlag(int row, int col) {
        Cell cell = field[row][col];
        if (!cell.isOpen()) {
            cell.setFlagged(!cell.isFlagged());
        }
    }

    /**
     * Показывает все мины (при проигрыше).
     */
    public void revealAll() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (field[r][c].isMine()) {
                    field[r][c].setOpen(true);
                }
            }
        }
    }

    /**
     * Проверяет, открыты ли все клетки без мин.
     */
    public boolean isWin() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = field[r][c];
                if (!cell.isMine() && !cell.isOpen()) {
                    return false;
                }
            }
        }
        return true;
    }
}