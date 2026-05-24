package com.minesweeper;

/**
 * Модель одной клетки игрового поля.
 */
public class Cell {
    private boolean mine;        // есть ли мина
    private boolean open;        // открыта ли
    private boolean flagged;     // помечена ли флажком
    private int neighborMines;   // количество мин вокруг

    public Cell() {
        this.mine = false;
        this.open = false;
        this.flagged = false;
        this.neighborMines = 0;
    }

    public boolean isMine() { return mine; }
    public void setMine(boolean mine) { this.mine = mine; }

    public boolean isOpen() { return open; }
    public void setOpen(boolean open) { this.open = open; }

    public boolean isFlagged() { return flagged; }
    public void setFlagged(boolean flagged) { this.flagged = flagged; }

    public int getNeighborMines() { return neighborMines; }
    public void setNeighborMines(int neighborMines) { this.neighborMines = neighborMines; }
}