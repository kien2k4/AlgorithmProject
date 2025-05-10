package com.rmit.sudoku.solver;

import com.rmit.sudoku.metrics.SudokuMetrics;
import com.rmit.sudoku.validator.SudokuValidator;

/**
 * Implementation of the SudokuSolver interface using the Dancing Links (DLX) algorithm.
 * This is an efficient algorithm for exact cover problems, which Sudoku can be formulated as.
 */
public class DancingLinksSudokuSolver implements SudokuSolver {

    private static final int GRID_SIZE = 9;
    private static final int BOX_SIZE = 3;
    private static final int CONSTRAINTS = 4; // Row, Column, Box, Cell
    private static final int COVER_SIZE = GRID_SIZE * GRID_SIZE * CONSTRAINTS;
    private static final int POSSIBILITIES = GRID_SIZE * GRID_SIZE * GRID_SIZE;

    private static final long DEFAULT_TIMEOUT_MS = 120000; // 2 minutes

    private final SudokuMetrics metrics;
    private final long timeoutMs;
    private final boolean printMetrics;

    // DLX data structures
    private DancingNode header;
    private int[][] solution;
    private boolean solutionFound;

    /**
     * Creates a new DancingLinksSudokuSolver with the default timeout.
     */
    public DancingLinksSudokuSolver() {
        this(DEFAULT_TIMEOUT_MS, true);
    }

    /**
     * Creates a new DancingLinksSudokuSolver with the default timeout and specified metrics printing option.
     *
     * @param printMetrics Whether to print metrics after solving
     */
    public DancingLinksSudokuSolver(boolean printMetrics) {
        this(DEFAULT_TIMEOUT_MS, printMetrics);
    }

    /**
     * Creates a new DancingLinksSudokuSolver with a custom timeout.
     *
     * @param timeoutMs The timeout in milliseconds
     */
    public DancingLinksSudokuSolver(long timeoutMs) {
        this(timeoutMs, true);
    }

    /**
     * Creates a new DancingLinksSudokuSolver with a custom timeout and metrics printing option.
     *
     * @param timeoutMs The timeout in milliseconds
     * @param printMetrics Whether to print metrics after solving
     */
    public DancingLinksSudokuSolver(long timeoutMs, boolean printMetrics) {
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

        // Check if the initial board is valid according to Sudoku rules
        if (!SudokuValidator.isValidBoard(board)) {
            throw new IllegalArgumentException("Board contains invalid values that violate Sudoku rules");
        }

        // Start tracking metrics
        metrics.startTracking();

        // Create a copy of the input board to avoid modifying the original
        int[][] workingBoard = new int[GRID_SIZE][GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            System.arraycopy(board[i], 0, workingBoard[i], 0, GRID_SIZE);
        }

        // Initialize DLX data structures
        initializeDLX();

        // Add constraints for the given cells
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (workingBoard[row][col] != 0) {
                    // Add constraint for the given value
                    int value = workingBoard[row][col];
                    addConstraint(row, col, value);
                }
            }
        }

        // Initialize solution array
        solution = new int[GRID_SIZE][GRID_SIZE];
        solutionFound = false;

        // Solve the puzzle using DLX
        search(0);

        // Stop tracking metrics
        metrics.stopTracking();

        // Print metrics if enabled
        if (printMetrics) {
            metrics.printMetrics();
        }

        if (solutionFound) {
            return solution;
        } else {
            return null; // No solution exists
        }
    }

    /**
     * Initializes the Dancing Links data structure.
     */
    private void initializeDLX() {
        // Create the header node
        header = new DancingNode();
        header.left = header;
        header.right = header;
        header.up = header;
        header.down = header;

        // Create column headers
        DancingNode[] columnHeaders = new DancingNode[COVER_SIZE];
        for (int i = 0; i < COVER_SIZE; i++) {
            DancingNode columnHeader = new DancingNode();
            columnHeader.column = columnHeader;
            columnHeader.size = 0;

            // Link horizontally
            columnHeader.right = header.right;
            columnHeader.left = header;
            header.right.left = columnHeader;
            header.right = columnHeader;

            // Link vertically
            columnHeader.up = columnHeader;
            columnHeader.down = columnHeader;

            columnHeaders[i] = columnHeader;
        }

        // Create nodes for each possibility
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                for (int num = 1; num <= GRID_SIZE; num++) {
                    // Calculate box index
                    int box = (row / BOX_SIZE) * BOX_SIZE + (col / BOX_SIZE);

                    // Calculate constraint indices
                    int rowConstraint = row * GRID_SIZE + num - 1;
                    int colConstraint = GRID_SIZE * GRID_SIZE + col * GRID_SIZE + num - 1;
                    int boxConstraint = 2 * GRID_SIZE * GRID_SIZE + box * GRID_SIZE + num - 1;
                    int cellConstraint = 3 * GRID_SIZE * GRID_SIZE + row * GRID_SIZE + col;

                    // Create nodes for each constraint
                    DancingNode rowNode = createNode(rowConstraint);
                    DancingNode colNode = createNode(colConstraint);
                    DancingNode boxNode = createNode(boxConstraint);
                    DancingNode cellNode = createNode(cellConstraint);

                    // Link the nodes horizontally
                    rowNode.right = colNode;
                    colNode.right = boxNode;
                    boxNode.right = cellNode;
                    cellNode.right = rowNode;

                    rowNode.left = cellNode;
                    colNode.left = rowNode;
                    boxNode.left = colNode;
                    cellNode.left = boxNode;

                    // Store the row, column, and number in the nodes
                    rowNode.row = row;
                    rowNode.col = col;
                    rowNode.num = num;

                    colNode.row = row;
                    colNode.col = col;
                    colNode.num = num;

                    boxNode.row = row;
                    boxNode.col = col;
                    boxNode.num = num;

                    cellNode.row = row;
                    cellNode.col = col;
                    cellNode.num = num;

                    metrics.incrementOperationCount();
                }
            }
        }
    }

    /**
     * Creates a new node and links it to the specified column.
     *
     * @param columnIndex The index of the column
     * @return The new node
     */
    private DancingNode createNode(int columnIndex) {
        DancingNode columnHeader = getColumnHeader(columnIndex);
        DancingNode node = new DancingNode();

        // Set column reference
        node.column = columnHeader;

        // Link vertically
        node.up = columnHeader.up;
        node.down = columnHeader;
        columnHeader.up.down = node;
        columnHeader.up = node;

        // Increment column size
        columnHeader.size++;

        return node;
    }

    /**
     * Gets the column header at the specified index.
     *
     * @param index The index of the column
     * @return The column header
     */
    private DancingNode getColumnHeader(int index) {
        DancingNode current = header.right;
        for (int i = 0; i < index; i++) {
            current = current.right;
        }
        return current;
    }

    /**
     * Adds a constraint for a given cell value.
     *
     * @param row The row index
     * @param col The column index
     * @param value The value (1-9)
     */
    private void addConstraint(int row, int col, int value) {
        // Calculate constraint indices
        int box = (row / BOX_SIZE) * BOX_SIZE + (col / BOX_SIZE);
        int rowConstraint = row * GRID_SIZE + value - 1;
        int colConstraint = GRID_SIZE * GRID_SIZE + col * GRID_SIZE + value - 1;
        int boxConstraint = 2 * GRID_SIZE * GRID_SIZE + box * GRID_SIZE + value - 1;
        int cellConstraint = 3 * GRID_SIZE * GRID_SIZE + row * GRID_SIZE + col;

        // Cover the columns for these constraints
        coverColumn(getColumnHeader(rowConstraint));
        coverColumn(getColumnHeader(colConstraint));
        coverColumn(getColumnHeader(boxConstraint));
        coverColumn(getColumnHeader(cellConstraint));

        metrics.incrementOperationCount();
    }

    /**
     * Covers a column in the DLX matrix.
     *
     * @param column The column to cover
     */
    private void coverColumn(DancingNode column) {
        // Remove the column from the header list
        column.right.left = column.left;
        column.left.right = column.right;

        // Remove all rows that have a 1 in this column
        DancingNode row = column.down;
        while (row != column) {
            DancingNode rightNode = row.right;
            while (rightNode != row) {
                rightNode.up.down = rightNode.down;
                rightNode.down.up = rightNode.up;
                rightNode.column.size--;
                rightNode = rightNode.right;
                metrics.incrementOperationCount();
            }
            row = row.down;
        }
    }

    /**
     * Uncovers a column in the DLX matrix.
     *
     * @param column The column to uncover
     */
    private void uncoverColumn(DancingNode column) {
        // Restore all rows that have a 1 in this column
        DancingNode row = column.up;
        while (row != column) {
            DancingNode leftNode = row.left;
            while (leftNode != row) {
                leftNode.up.down = leftNode;
                leftNode.down.up = leftNode;
                leftNode.column.size++;
                leftNode = leftNode.left;
                metrics.incrementOperationCount();
            }
            row = row.up;
        }

        // Restore the column to the header list
        column.right.left = column;
        column.left.right = column;
    }

    /**
     * Recursive search function for the DLX algorithm.
     *
     * @param k The current depth of the search
     * @throws SudokuTimeoutException if the search takes too long
     */
    private void search(int k) throws SudokuTimeoutException {
        // Check if we've exceeded the time limit
        if (metrics.hasExceededTimeLimit(timeoutMs)) {
            throw new SudokuTimeoutException("Solving took longer than " + (timeoutMs / 1000) + " seconds");
        }

        // Increment recursion depth
        metrics.incrementRecursionDepth();

        // Check current memory usage periodically
        metrics.updatePeakMemoryUsage();

        // If there are no more columns to cover, we've found a solution
        if (header.right == header) {
            solutionFound = true;
            metrics.decrementRecursionDepth();
            return;
        }

        // Choose the column with the smallest size
        DancingNode column = chooseColumn();

        // Cover the column
        coverColumn(column);

        // Try each row in the column
        DancingNode row = column.down;
        while (row != column) {
            // Add this row to the solution
            DancingNode temp = row;
            do {
                if (temp.row >= 0 && temp.col >= 0 && temp.num > 0) {
                    solution[temp.row][temp.col] = temp.num;
                }
                temp = temp.right;
            } while (temp != row);

            // Cover all columns in this row
            temp = row.right;
            while (temp != row) {
                coverColumn(temp.column);
                temp = temp.right;
            }

            // Recursively search
            search(k + 1);

            // If a solution is found, stop searching
            if (solutionFound) {
                metrics.decrementRecursionDepth();
                return;
            }

            // Backtrack: uncover all columns in this row
            temp = row.left;
            while (temp != row) {
                uncoverColumn(temp.column);
                temp = temp.left;
            }

            row = row.down;
        }

        // Uncover the column
        uncoverColumn(column);

        metrics.decrementRecursionDepth();
    }

    /**
     * Chooses the column with the smallest size.
     *
     * @return The column with the smallest size
     */
    private DancingNode chooseColumn() {
        DancingNode bestColumn = null;
        int minSize = Integer.MAX_VALUE;

        DancingNode current = header.right;
        while (current != header) {
            if (current.size < minSize) {
                minSize = current.size;
                bestColumn = current;
            }
            current = current.right;
        }

        return bestColumn;
    }

    /**
     * Gets the metrics object.
     *
     * @return The metrics object
     */
    public SudokuMetrics getMetrics() {
        return metrics;
    }

    /**
     * Inner class representing a node in the Dancing Links data structure.
     */
    private static class DancingNode {
        DancingNode left;
        DancingNode right;
        DancingNode up;
        DancingNode down;
        DancingNode column;
        int size; // Used for column headers
        int row = -1;
        int col = -1;
        int num = -1;

        /**
         * Creates a new DancingNode.
         */
        DancingNode() {
            left = this;
            right = this;
            up = this;
            down = this;
            column = this;
            size = 0;
        }
    }
}
