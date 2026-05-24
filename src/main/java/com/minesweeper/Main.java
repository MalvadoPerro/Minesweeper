package com.minesweeper;

import javax.swing.*;

/**
 * Точка входа в приложение.
 */
public class Main {
    public static void main(String[] args) {
        // Запуск графического интерфейса в потоке обработки событий Swing
        SwingUtilities.invokeLater(() -> {
            GameWindow window = new GameWindow();
            window.setVisible(true);
        });
    }
}