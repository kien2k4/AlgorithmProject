package com.rmit.sudoku;

/**
 * RMIT_Sudoku_Solver class for solving 9x9 Sudoku puzzles.
 * Uses a backtracking algorithm to efficiently find solutions.
 */
public class RMIT_Sudoku_Solver {
    
    private static final int GRID_SIZE = 9;
    
    /**
     * Solves a Sudoku puzzle.
     *
     * @param board 2D array representing the Sudoku puzzle (0 for empty cells, 1-9 for filled cells)
     * @return The solved puzzle as a 2D array, or null if no solution exists
     */
    public int[][] solve(int[][] board) {
        // Create a copy of the input board to avoid modifying the original
        int[][] workingBoard = new int[GRID_SIZE][GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            System.arraycopy(board[i], 0, workingBoard[i], 0, GRID_SIZE);
        }

        // Solve the puzzle
        if (solveBoard(workingBoard)) {
            return workingBoard;
        } else {
            return null; // No solution exists
        }
    }
    
    /**
     * Recursive backtracking algorithm to solve the Sudoku puzzle.
     *
     * @param board The current state of the board
     * @return true if a solution is found, false otherwise
     */
    private boolean solveBoard(int[][] board) {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                // Find an empty cell
                if (board[row][col] == 0) {
                    // Try placing numbers 1-9
                    for (int numberToTry = 1; numberToTry <= GRID_SIZE; numberToTry++) {
                        if (isValidPlacement(board, numberToTry, row, col)) {
                            // Place the number
                            board[row][col] = numberToTry;
                            
                            // Recursively try to solve the rest of the board
                            if (solveBoard(board)) {
                                return true;
                            }
                            
                            // If placing the number doesn't lead to a solution, backtrack
                            board[row][col] = 0;
                        }
                    }
                    // If no number can be placed in this cell, the puzzle is unsolvable
                    return false;
                }
            }
        }
        // If we've filled all cells, we've solved the puzzle
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
    private boolean isValidPlacement(int[][] board, int number, int row, int col) {
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
    
    /**
     * Utility method to print a Sudoku board.
     * 
     * @param board The board to print
     */
    public void printBoard(int[][] board) {
        for (int row = 0; row < GRID_SIZE; row++) {
            if (row % 3 == 0 && row != 0) {
                System.out.println("---------------------");
            }
            for (int col = 0; col < GRID_SIZE; col++) {
                if (col % 3 == 0 && col != 0) {
                    System.out.print("| ");
                }
                System.out.print(board[row][col] == 0 ? ". " : board[row][col] + " ");
            }
            System.out.println();
        }
    }
    
    /**
     * Test method with a sample Sudoku puzzle.
     */
    public static void main(String[] args) {
        int[][] board = {
            {5, 3, 0, 0, 7, 0, 0, 0, 0},
            {6, 0, 0, 1, 9, 5, 0, 0, 0},
            {0, 9, 8, 0, 0, 0, 0, 6, 0},
            {8, 0, 0, 0, 6, 0, 0, 0, 3},
            {4, 0, 0, 8, 0, 3, 0, 0, 1},
            {7, 0, 0, 0, 2, 0, 0, 0, 6},
            {0, 6, 0, 0, 0, 0, 2, 8, 0},
            {0, 0, 0, 4, 1, 9, 0, 0, 5},
            {0, 0, 0, 0, 8, 0, 0, 7, 9}
        };
        
        RMIT_Sudoku_Solver solver = new RMIT_Sudoku_Solver();
        System.out.println("Unsolved Puzzle:");
        solver.printBoard(board);
        
        int[][] solvedBoard = solver.solve(board);
        
        if (solvedBoard != null) {
            System.out.println("\nSolved Puzzle:");
            solver.printBoard(solvedBoard);
        } else {
            System.out.println("\nNo solution exists for this puzzle.");
        }
    }
}
