package com.rmit.sudoku.validator;

/**
 * Class for validating Sudoku puzzles and moves.
 */
public class SudokuValidator {
    
    private static final int GRID_SIZE = 9;
    
    /**
     * Validates a Sudoku board.
     * 
     * @param board The board to validate
     * @return true if the board is valid, false otherwise
     */
    public static boolean isValidBoard(int[][] board) {
        if (board == null || board.length != GRID_SIZE) {
            return false;
        }
        
        for (int i = 0; i < GRID_SIZE; i++) {
            if (board[i] == null || board[i].length != GRID_SIZE) {
                return false;
            }
            
            for (int j = 0; j < GRID_SIZE; j++) {
                if (board[i][j] < 0 || board[i][j] > 9) {
                    return false;
                }
                
                // Skip empty cells
                if (board[i][j] == 0) {
                    continue;
                }
                
                // Check if the current number is valid
                int temp = board[i][j];
                board[i][j] = 0; // Temporarily remove the number
                boolean isValid = isValidPlacement(board, temp, i, j);
                board[i][j] = temp; // Restore the number
                
                if (!isValid) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Checks if placing a number at a specific position is valid.
     * 
     * @param board The current state of the board
     * @param number The number to place
     * @param row The row index
     * @param col The column index
     * @return true if the placement is valid, false otherwise
     */
    public static boolean isValidPlacement(int[][] board, int number, int row, int col) {
        // Check row
        for (int i = 0; i < GRID_SIZE; i++) {
            if (board[row][i] == number) {
                return false;
            }
        }
        
        // Check column
        for (int i = 0; i < GRID_SIZE; i++) {
            if (board[i][col] == number) {
                return false;
            }
        }
        
        // Check 3x3 box
        int boxStartRow = row - row % 3;
        int boxStartCol = col - col % 3;
        
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[boxStartRow + i][boxStartCol + j] == number) {
                    return false;
                }
            }
        }
        
        // If we get here, the placement is valid
        return true;
    }
}
