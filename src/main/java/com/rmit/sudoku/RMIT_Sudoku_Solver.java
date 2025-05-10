package com.rmit.sudoku;

import com.rmit.sudoku.solver.BacktrackingSudokuSolver;
import com.rmit.sudoku.solver.SudokuSolver;
import com.rmit.sudoku.solver.SudokuTimeoutException;

/**
 * RMIT_Sudoku_Solver class for solving 9x9 Sudoku puzzles.
 * Uses a backtracking algorithm to efficiently find solutions.
 * Tracks and reports time and space complexity metrics.
 */
public class RMIT_Sudoku_Solver {

    private static final int GRID_SIZE = 9;
    private final SudokuSolver solver;

    /**
     * Creates a new RMIT_Sudoku_Solver with the default solver.
     */
    public RMIT_Sudoku_Solver() {
        this.solver = new BacktrackingSudokuSolver();
    }

    /**
     * Creates a new RMIT_Sudoku_Solver with a custom solver.
     *
     * @param solver The solver to use
     */
    public RMIT_Sudoku_Solver(SudokuSolver solver) {
        this.solver = solver;
    }

    /**
     * Solves a Sudoku puzzle.
     *
     * @param board 2D array representing the Sudoku puzzle (0 for empty cells, 1-9 for filled cells)
     * @return The solved puzzle as a 2D array, or null if no solution exists
     * @throws RuntimeException if the puzzle cannot be solved within 2 minutes
     */
    public int[][] solve(int[][] board) {
        try {
            return solver.solve(board);
        } catch (SudokuTimeoutException e) {
            // Convert to RuntimeException to maintain backward compatibility
            throw new RuntimeException("Timeout: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the metrics from the solver.
     *
     * @return The metrics from the solver if it's a BacktrackingSudokuSolver, null otherwise
     */
    public com.rmit.sudoku.metrics.SudokuMetrics getMetrics() {
        if (solver instanceof BacktrackingSudokuSolver) {
            return ((BacktrackingSudokuSolver) solver).getMetrics();
        }
        return null;
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
                System.out.print(board[row][col] == 0 ? "0 " : board[row][col] + " ");
            }
            System.out.println();
        }
    }

    /**
     * Main method to demonstrate the Sudoku solver.
     */
    public static void main(String[] args) {
        // Easy puzzle
        int[][] easyBoard = {
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

        // Hard puzzle with more empty cells
        int[][] hardBoard = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 3, 0, 8, 5},
            {0, 0, 1, 0, 2, 0, 0, 0, 0},
            {0, 0, 0, 5, 0, 7, 0, 0, 0},
            {0, 0, 4, 0, 0, 0, 1, 0, 0},
            {0, 9, 0, 0, 0, 0, 0, 0, 0},
            {5, 0, 0, 0, 0, 0, 0, 7, 3},
            {0, 0, 2, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 4, 0, 0, 0, 9}
        };

        RMIT_Sudoku_Solver solver = new RMIT_Sudoku_Solver();

        // Solve easy puzzle
        System.out.println("Solving Easy Puzzle:");
        System.out.println("Unsolved Puzzle:");
        solver.printBoard(easyBoard);

        try {
            int[][] solvedEasyBoard = solver.solve(easyBoard);

            if (solvedEasyBoard != null) {
                System.out.println("\nSolved Puzzle:");
                solver.printBoard(solvedEasyBoard);
            } else {
                System.out.println("\nNo solution exists for this puzzle.");
            }
        } catch (RuntimeException e) {
            System.out.println("\nFailed to solve puzzle: " + e.getMessage());
        }

        // Solve hard puzzle
        System.out.println("\n\nSolving Hard Puzzle:");
        System.out.println("Unsolved Puzzle:");
        solver.printBoard(hardBoard);

        try {
            int[][] solvedHardBoard = solver.solve(hardBoard);

            if (solvedHardBoard != null) {
                System.out.println("\nSolved Puzzle:");
                solver.printBoard(solvedHardBoard);
            } else {
                System.out.println("\nNo solution exists for this puzzle.");
            }
        } catch (RuntimeException e) {
            System.out.println("\nFailed to solve puzzle: " + e.getMessage());
        }
    }
}
