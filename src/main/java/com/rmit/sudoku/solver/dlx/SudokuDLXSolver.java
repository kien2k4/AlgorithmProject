package com.rmit.sudoku.solver.dlx;

import com.rmit.sudoku.metrics.SudokuMetrics;
import com.rmit.sudoku.solver.SudokuSolver;
import com.rmit.sudoku.solver.SudokuTimeoutException;
import java.util.List;

/**
 * Implementation of the SudokuSolver interface using the Dancing Links (DLX) algorithm.
 * This solver formulates Sudoku as an exact cover problem and solves it efficiently.
 */
public class SudokuDLXSolver implements SudokuSolver {

    private static final int GRID_SIZE = 9;   // grid size
    private static final int BOX_SIZE = 3;   // box size
    private static final int ROWS = GRID_SIZE * GRID_SIZE * GRID_SIZE;  // 729
    private static final int COLS = GRID_SIZE * GRID_SIZE * 4;          // 324

    private static final long DEFAULT_TIMEOUT_MS = 120_000;  // 2 minutes

    private final SudokuMetrics metrics;
    private final long timeoutMs;
    private final boolean printMetrics;

    /**
     * Creates a new SudokuDLXSolver with default settings.
     */
    public SudokuDLXSolver() {
        this(DEFAULT_TIMEOUT_MS, true);
    }

    /**
     * Creates a new SudokuDLXSolver with the specified timeout.
     *
     * @param timeoutMs The timeout in milliseconds
     */
    public SudokuDLXSolver(long timeoutMs) {
        this(timeoutMs, true);
    }

    /**
     * Creates a new SudokuDLXSolver with the specified metrics printing option.
     *
     * @param printMetrics Whether to print metrics after solving
     */
    public SudokuDLXSolver(boolean printMetrics) {
        this(DEFAULT_TIMEOUT_MS, printMetrics);
    }

    /**
     * Creates a new SudokuDLXSolver with the specified timeout and metrics printing option.
     *
     * @param timeoutMs    The timeout in milliseconds
     * @param printMetrics Whether to print metrics after solving
     */
    public SudokuDLXSolver(long timeoutMs, boolean printMetrics) {
        this.timeoutMs = timeoutMs;
        this.printMetrics = printMetrics;
        this.metrics = new SudokuMetrics();
    }

    @Override
    public int[][] solve(int[][] board) throws SudokuTimeoutException {
        // Basic dimension checks
        if (board == null || board.length != GRID_SIZE) {
            throw new IllegalArgumentException("Board must be a 9x9 grid");
        }
        for (int i = 0; i < GRID_SIZE; i++) {
            if (board[i] == null || board[i].length != GRID_SIZE) {
                throw new IllegalArgumentException("Board must be a 9x9 grid");
            }
            for (int j = 0; j < GRID_SIZE; j++) {
                if (board[i][j] < 0 || board[i][j] > GRID_SIZE) {
                    throw new IllegalArgumentException("Board values must be between 0 and 9");
                }
            }
        }

        // Validate the input board
        if (!isValidBoardIgnoringZeros(board)) {
            throw new IllegalArgumentException("Board contains duplicates in row, column, or box");
        }

        // Track performance metrics
        metrics.startTracking();

        // Build the exact cover grid
        int[][] cover = buildExactCoverGrid(board);

        // Create the solution handler
        SudokuSolutionHandler handler = new SudokuSolutionHandler();

        // Create and run the DancingLinks solver
        try {
            new DancingLinks(cover, handler, metrics).runSolver();
        } catch (SudokuTimeoutException e) {
            metrics.stopTracking();
            if (printMetrics) {
                metrics.printMetrics();
            }
            throw e;
        }

        // Get the solution
        int[][] solution = handler.getSolution();

        metrics.stopTracking();
        if (printMetrics) {
            metrics.printMetrics();
        }

        return solution;
    }

    /**
     * Checks that no non-zero appears more than once in any row, column, or 3×3 box.
     */
    private boolean isValidBoardIgnoringZeros(int[][] b) {
        // Rows and columns
        for (int i = 0; i < GRID_SIZE; i++) {
            boolean[] seenRow = new boolean[GRID_SIZE + 1];
            boolean[] seenCol = new boolean[GRID_SIZE + 1];
            for (int j = 0; j < GRID_SIZE; j++) {
                int vR = b[i][j], vC = b[j][i];
                if (vR != 0) {
                    if (seenRow[vR]) return false;
                    seenRow[vR] = true;
                }
                if (vC != 0) {
                    if (seenCol[vC]) return false;
                    seenCol[vC] = true;
                }
            }
        }

        // Boxes
        for (int boxRow = 0; boxRow < BOX_SIZE; boxRow++) {
            for (int boxCol = 0; boxCol < BOX_SIZE; boxCol++) {
                boolean[] seen = new boolean[GRID_SIZE + 1];
                for (int i = 0; i < BOX_SIZE; i++) {
                    for (int j = 0; j < BOX_SIZE; j++) {
                        int v = b[boxRow * BOX_SIZE + i][boxCol * BOX_SIZE + j];
                        if (v != 0) {
                            if (seen[v]) return false;
                            seen[v] = true;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Builds the exact cover grid for the Sudoku puzzle.
     */
    private static int[][] buildExactCoverGrid(int[][] puzzle) {
        int[][] cover = new int[ROWS][COLS];
        int rowPtr = 0;

        for (int r = 0; r < GRID_SIZE; r++)
            for (int c = 0; c < GRID_SIZE; c++)
                for (int d = 0; d < GRID_SIZE; d++) {

                    /*  Skip digits that collide with a given clue  */
                    if (puzzle[r][c] != 0 && puzzle[r][c] != d + 1) {
                        rowPtr++;
                        continue;
                    }

                    int box = (r / BOX_SIZE) * BOX_SIZE + (c / BOX_SIZE);

                    int cellCol = r * GRID_SIZE + c;                   // 0-80
                    int rowCol = 81 + r * GRID_SIZE + d;               // 81-161
                    int colCol = 162 + c * GRID_SIZE + d;              // 162-242
                    int boxCol = 243 + box * GRID_SIZE + d;            // 243-323

                    cover[rowPtr][cellCol] = 1;
                    cover[rowPtr][rowCol] = 1;
                    cover[rowPtr][colCol] = 1;
                    cover[rowPtr][boxCol] = 1;
                    rowPtr++;
                }
        return cover;
    }

    /**
     * Gets the metrics from the last solve operation.
     *
     * @return The metrics from the last solve operation
     */
    public SudokuMetrics getMetrics() {
        return metrics;
    }

    /**
     * Handler for Sudoku solutions found by the DancingLinks algorithm.
     */
    private static class SudokuSolutionHandler implements SolutionHandler {

        private final int[][] board = new int[GRID_SIZE][GRID_SIZE];
        private boolean filled = false;

        @Override
        public void handleSolution(List<DancingLinks.DancingNode> answer) {
            if (filled) return; // Only use the first solution

            // Convert the list of chosen rows back into a grid
            for (DancingLinks.DancingNode n : answer) {
                // Each row in the DLX matrix represents a cell-value placement
                // We need to extract the row, column, and digit from the constraints
                int row = -1, col = -1, digit = -1;

                // Walk the row ring to gather the four column indices
                for (DancingLinks.DancingNode j = n; ; j = j.R) {
                    int idx = Integer.parseInt(j.C.name);

                    if (idx < 81) {                 // cell constraint
                        row = idx / 9;
                        col = idx % 9;
                    } else if (idx < 162) {         // row-digit constraint
                        int rowDigitIdx = idx - 81;
                        digit = rowDigitIdx % 9;
                    }

                    if (j.R == n) break;  // We've gone all the way around
                }

                // If we found valid row, column, and digit, place it in the solution
                if (row >= 0 && col >= 0 && digit >= 0) {
                    board[row][col] = digit + 1;  // +1 because digits are 0-based in the constraints
                }
            }
            filled = true;
        }

        /**
         * Gets the solution if one was found.
         */
        int[][] getSolution() {
            return filled ? board : null;
        }
    }
}
