package com.rmit.sudoku.solver;

import com.rmit.sudoku.metrics.SudokuMetrics;
import com.rmit.sudoku.validator.SudokuValidator;

/**
 * Implementation of the SudokuSolver interface using a backtracking algorithm.
 */
public class BacktrackingSudokuSolver implements SudokuSolver {

    private static final int GRID_SIZE = 9;
    private static final long DEFAULT_TIMEOUT_MS = 120000; // 2 minutes

    private final SudokuMetrics metrics;
    private final long timeoutMs;
    private final boolean printMetrics;

    /**
     * Creates a new BacktrackingSudokuSolver with the default timeout.
     */
    public BacktrackingSudokuSolver() {
        this(DEFAULT_TIMEOUT_MS, true);
    }

    /**
     * Creates a new BacktrackingSudokuSolver with the default timeout and specified metrics printing option.
     *
     * @param printMetrics Whether to print metrics after solving
     */
    public BacktrackingSudokuSolver(boolean printMetrics) {
        this(DEFAULT_TIMEOUT_MS, printMetrics);
    }

    /**
     * Creates a new BacktrackingSudokuSolver with a custom timeout.
     *
     * @param timeoutMs The timeout in milliseconds
     */
    public BacktrackingSudokuSolver(long timeoutMs) {
        this(timeoutMs, true);
    }

    /**
     * Creates a new BacktrackingSudokuSolver with a custom timeout and metrics printing option.
     *
     * @param timeoutMs The timeout in milliseconds
     * @param printMetrics Whether to print metrics after solving
     */
    public BacktrackingSudokuSolver(long timeoutMs, boolean printMetrics) {
        this.metrics = new SudokuMetrics();
        this.timeoutMs = timeoutMs;
        this.printMetrics = printMetrics;
    }

    @Override
    public int[][] solve(int[][] board) throws SudokuTimeoutException {
        // Validate input
        if (board == null || board.length != GRID_SIZE) {
            throw new IllegalArgumentException("Board must be a 9x9 grid");
        }

        for (int i = 0; i < GRID_SIZE; i++) {
            if (board[i] == null || board[i].length != GRID_SIZE) {
                throw new IllegalArgumentException("Board must be a 9x9 grid");
            }

            for (int j = 0; j < GRID_SIZE; j++) {
                if (board[i][j] < 0 || board[i][j] > 9) {
                    throw new IllegalArgumentException("Board values must be between 0 and 9");
                }
            }
        }

        if (!SudokuValidator.isValidBoard(board)) {
            throw new IllegalArgumentException("Board contains invalid values");
        }

        // Start tracking metrics
        metrics.startTracking();

        // Create a copy of the input board to avoid modifying the original
        int[][] workingBoard = new int[GRID_SIZE][GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            System.arraycopy(board[i], 0, workingBoard[i], 0, GRID_SIZE);
        }

        // Solve the puzzle
        boolean solved = solveBoard(workingBoard);

        // Stop tracking metrics
        metrics.stopTracking();

        // Print metrics if enabled
        if (printMetrics) {
            metrics.printMetrics();
        }

        if (solved) {
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
     * @throws SudokuTimeoutException if the puzzle cannot be solved within the time limit
     */
    private boolean solveBoard(int[][] board) throws SudokuTimeoutException {
        // Check if we've exceeded the time limit
        if (metrics.hasExceededTimeLimit(timeoutMs)) {
            throw new SudokuTimeoutException("Solving took longer than " + (timeoutMs / 1000) + " seconds");
        }

        // Increment recursion depth
        metrics.incrementRecursionDepth();

        // Check current memory usage periodically
        metrics.updatePeakMemoryUsage();

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                // Find an empty cell
                if (board[row][col] == 0) {
                    // Try placing numbers 1-9
                    for (int numberToTry = 1; numberToTry <= GRID_SIZE; numberToTry++) {
                        // Increment operation count
                        metrics.incrementOperationCount();

                        if (SudokuValidator.isValidPlacement(board, numberToTry, row, col)) {
                            // Place the number
                            board[row][col] = numberToTry;
                            metrics.incrementOperationCount(); // Count the placement operation

                            // Recursively try to solve the rest of the board
                            try {
                                if (solveBoard(board)) {
                                    return true;
                                }
                            } catch (SudokuTimeoutException e) {
                                // Propagate the timeout exception
                                metrics.decrementRecursionDepth();
                                throw e;
                            }

                            // If placing the number doesn't lead to a solution, backtrack
                            board[row][col] = 0;
                            metrics.incrementOperationCount(); // Count the backtracking operation
                        }
                    }
                    // If no number can be placed in this cell, the puzzle is unsolvable
                    metrics.decrementRecursionDepth();
                    return false;
                }
            }
        }

        // If we've filled all cells, we've solved the puzzle
        metrics.decrementRecursionDepth();
        return true;
    }

    /**
     * Gets the metrics object.
     *
     * @return The metrics object
     */
    public SudokuMetrics getMetrics() {
        return metrics;
    }
}
