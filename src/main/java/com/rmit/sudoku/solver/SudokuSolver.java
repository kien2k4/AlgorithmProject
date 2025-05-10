package com.rmit.sudoku.solver;

/**
 * Interface for Sudoku solving algorithms.
 */
public interface SudokuSolver {
    
    /**
     * Solves a Sudoku puzzle.
     * 
     * @param board 2D array representing the Sudoku puzzle (0 for empty cells, 1-9 for filled cells)
     * @return The solved puzzle as a 2D array with values 1-9
     * @throws SudokuTimeoutException if the puzzle cannot be solved within the time limit
     * @throws IllegalArgumentException if the input board is invalid
     */
    int[][] solve(int[][] board) throws SudokuTimeoutException;
}
