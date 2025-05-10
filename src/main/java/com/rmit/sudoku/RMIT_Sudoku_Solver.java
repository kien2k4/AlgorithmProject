package com.rmit.sudoku;

import com.rmit.sudoku.solver.BacktrackingSudokuSolver;
import com.rmit.sudoku.solver.DancingLinksSudokuSolver;
import com.rmit.sudoku.solver.SudokuSolver;
import com.rmit.sudoku.solver.SudokuTimeoutException;

/**
 * RMIT_Sudoku_Solver class for solving 9x9 Sudoku puzzles.
 * Uses a backtracking algorithm to efficiently find solutions.
 * Tracks and reports time and space complexity metrics.
 */
public class RMIT_Sudoku_Solver {

    private static final int GRID_SIZE = 9;
    private final SudokuSolver backtrackingSolver;
    private final SudokuSolver dancingLinksSolver;

    /**
     * Creates a new RMIT_Sudoku_Solver with the default solvers.
     */
    public RMIT_Sudoku_Solver() {
        this.backtrackingSolver = new BacktrackingSudokuSolver();
        this.dancingLinksSolver = new DancingLinksSudokuSolver();
    }

    /**
     * Creates a new RMIT_Sudoku_Solver with custom solvers.
     *
     * @param backtrackingSolver The backtracking solver to use
     * @param dancingLinksSolver The dancing links solver to use
     */
    public RMIT_Sudoku_Solver(SudokuSolver backtrackingSolver, SudokuSolver dancingLinksSolver) {
        this.backtrackingSolver = backtrackingSolver;
        this.dancingLinksSolver = dancingLinksSolver;
    }

    /**
     * Solves a Sudoku puzzle using the backtracking algorithm.
     *
     * @param board 2D array representing the Sudoku puzzle (0 for empty cells, 1-9 for filled cells)
     * @return The solved puzzle as a 2D array, or null if no solution exists
     * @throws RuntimeException if the puzzle cannot be solved within 2 minutes
     */
    public int[][] solve(int[][] board) {
        try {
            return backtrackingSolver.solve(board);
        } catch (SudokuTimeoutException e) {
            // Convert to RuntimeException to maintain backward compatibility
            throw new RuntimeException("Timeout: " + e.getMessage(), e);
        }
    }

    /**
     * Solves a Sudoku puzzle using the Dancing Links (DLX) algorithm.
     *
     * @param board 2D array representing the Sudoku puzzle (0 for empty cells, 1-9 for filled cells)
     * @return The solved puzzle as a 2D array, or null if no solution exists
     * @throws RuntimeException if the puzzle cannot be solved within 2 minutes
     */
    public int[][] solveDLX(int[][] board) {
        try {
            return dancingLinksSolver.solve(board);
        } catch (SudokuTimeoutException e) {
            // Convert to RuntimeException to maintain backward compatibility
            throw new RuntimeException("Timeout: " + e.getMessage(), e);
        }
    }

    /**
     * Solves a Sudoku puzzle using both algorithms and compares their performance.
     *
     * @param board 2D array representing the Sudoku puzzle (0 for empty cells, 1-9 for filled cells)
     * @return The solved puzzle as a 2D array, or null if no solution exists
     * @throws RuntimeException if the puzzle cannot be solved within 2 minutes
     */
    public int[][] solveBoth(int[][] board) {
        int[][] solution = null;

        System.out.println("\nSolving with Backtracking algorithm:");
        try {
            solution = solve(board);
        } catch (RuntimeException e) {
            System.out.println("Backtracking solver failed: " + e.getMessage());
        }

        System.out.println("\nSolving with Dancing Links algorithm:");
        try {
            int[][] dlxSolution = solveDLX(board);
            if (solution == null) {
                solution = dlxSolution;
            }
        } catch (RuntimeException e) {
            System.out.println("Dancing Links solver failed: " + e.getMessage());
        }

        return solution;
    }

    /**
     * Gets the metrics from the backtracking solver.
     *
     * @return The metrics from the backtracking solver
     */
    public com.rmit.sudoku.metrics.SudokuMetrics getBacktrackingMetrics() {
        if (backtrackingSolver instanceof BacktrackingSudokuSolver) {
            return ((BacktrackingSudokuSolver) backtrackingSolver).getMetrics();
        }
        return null;
    }

    /**
     * Gets the metrics from the dancing links solver.
     *
     * @return The metrics from the dancing links solver
     */
    public com.rmit.sudoku.metrics.SudokuMetrics getDancingLinksMetrics() {
        if (dancingLinksSolver instanceof DancingLinksSudokuSolver) {
            return ((DancingLinksSudokuSolver) dancingLinksSolver).getMetrics();
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

        // Solve easy puzzle with both algorithms
        System.out.println("Solving Easy Puzzle with Both Algorithms:");
        System.out.println("Unsolved Puzzle:");
        solver.printBoard(easyBoard);

        try {
            int[][] solvedEasyBoard = solver.solveBoth(easyBoard);

            if (solvedEasyBoard != null) {
                System.out.println("\nSolved Puzzle:");
                solver.printBoard(solvedEasyBoard);
            } else {
                System.out.println("\nNo solution exists for this puzzle.");
            }
        } catch (RuntimeException e) {
            System.out.println("\nFailed to solve puzzle: " + e.getMessage());
        }

        // Solve hard puzzle with both algorithms
        System.out.println("\n\nSolving Hard Puzzle with Both Algorithms:");
        System.out.println("Unsolved Puzzle:");
        solver.printBoard(hardBoard);

        try {
            int[][] solvedHardBoard = solver.solveBoth(hardBoard);

            if (solvedHardBoard != null) {
                System.out.println("\nSolved Puzzle:");
                solver.printBoard(solvedHardBoard);
            } else {
                System.out.println("\nNo solution exists for this puzzle.");
            }
        } catch (RuntimeException e) {
            System.out.println("\nFailed to solve puzzle: " + e.getMessage());
        }

        // Generate and solve a puzzle with both algorithms
        System.out.println("\n\nGenerating and Solving a New Puzzle with Both Algorithms:");
        try {
            // Create a generator with a fixed seed for reproducibility
            com.rmit.sudoku.generator.SudokuGenerator generator =
                new com.rmit.sudoku.generator.SudokuGenerator(12345L);

            // Generate a medium difficulty puzzle
            int[][] generatedPuzzle = generator.generate(
                com.rmit.sudoku.generator.SudokuGenerator.Difficulty.MEDIUM);

            System.out.println("Generated Puzzle:");
            solver.printBoard(generatedPuzzle);

            // Solve with both algorithms
            int[][] solvedGeneratedPuzzle = solver.solveBoth(generatedPuzzle);

            if (solvedGeneratedPuzzle != null) {
                System.out.println("\nSolved Puzzle:");
                solver.printBoard(solvedGeneratedPuzzle);
            } else {
                System.out.println("\nNo solution exists for this puzzle.");
            }
        } catch (Exception e) {
            System.out.println("\nError generating or solving puzzle: " + e.getMessage());
        }
    }
}
