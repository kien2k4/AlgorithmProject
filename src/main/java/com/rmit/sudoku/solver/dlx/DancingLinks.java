package com.rmit.sudoku.solver.dlx;
import java.util.*;
import com.rmit.sudoku.metrics.SudokuMetrics;
import com.rmit.sudoku.solver.SudokuTimeoutException;

/**
 * Implementation of Donald Knuth's Dancing Links algorithm for solving problems.
 */
public class DancingLinks {

    private static final boolean VERBOSE = false;

    private ColumnNode header;
    private int solutions = 0;
    private int updates = 0;
    private SolutionHandler handler;
    private List<DancingNode> answer;
    private SudokuMetrics metrics;

    /**
     * A node in the Dancing Links matrix.
     */
    public class DancingNode {
        DancingNode L, R, U, D;
        ColumnNode C;

        // Hooks node n1 below current node
        void hookDown(DancingNode n1) {
            assert (this.C == n1.C);
            n1.D = this.D;
            n1.D.U = n1;
            n1.U = this;
            this.D = n1;
        }

        // Hooks a node n1 to the right of this node
        DancingNode hookRight(DancingNode n1) {
            n1.R = this.R;
            n1.R.L = n1;
            n1.L = this;
            this.R = n1;
            return n1;
        }

        void unlinkLR() {
            this.L.R = this.R;
            this.R.L = this.L;
            updates++;
            if (metrics != null) metrics.incrementOperationCount();
        }

        void relinkLR() {
            this.L.R = this.R.L = this;
            updates++;
            if (metrics != null) metrics.incrementOperationCount();
        }

        void unlinkUD() {
            this.U.D = this.D;
            this.D.U = this.U;
            updates++;
            if (metrics != null) metrics.incrementOperationCount();
        }

        void relinkUD() {
            this.U.D = this.D.U = this;
            updates++;
            if (metrics != null) metrics.incrementOperationCount();
        }

        public DancingNode() {
            L = R = U = D = this;
        }

        public DancingNode(ColumnNode c) {
            this();
            C = c;
        }
    }

    /**
     * A special type of DancingNode that represents a column header.
     */
    public class ColumnNode extends DancingNode {
        int size; // number of ones in current column
        String name;

        public ColumnNode(String n) {
            super();
            size = 0;
            name = n;
            C = this;
        }

        void cover() {
            unlinkLR();
            for (DancingNode i = this.D; i != this; i = i.D) {
                for (DancingNode j = i.R; j != i; j = j.R) {
                    j.unlinkUD();
                    j.C.size--;
                }
            }
            header.size--; // not part of original
        }

        void uncover() {
            for (DancingNode i = this.U; i != this; i = i.U) {
                for (DancingNode j = i.L; j != i; j = j.L) {
                    j.C.size++;
                    j.relinkUD();
                }
            }
            relinkLR();
            header.size++; // not part of original
        }
    }

    /**
     * Creates a new DancingLinks solver for the given exact cover grid.
     *
     * @param grid    The exact cover grid (1s and 0s)
     * @param h       The solution handler
     * @param metrics The metrics tracker (can be null)
     */
    public DancingLinks(int[][] grid, SolutionHandler h, SudokuMetrics metrics) {
        header = makeDLXBoard(grid);
        handler = h;
        this.metrics = metrics;
    }

    /**
     * Runs the solver to find all solutions.
     */
    public void runSolver() throws SudokuTimeoutException {
        solutions = 0;
        updates = 0;
        answer = new LinkedList<>();
        search(0);
        if (VERBOSE) showInfo();
    }

    /**
     * The heart of the algorithm - recursively searches for solutions.
     */
    private void search(int k) throws SudokuTimeoutException {
        if (metrics != null) {
            metrics.incrementRecursionDepth();
            metrics.updatePeakMemoryUsage();

            // Check for timeout
            if (metrics.hasExceededTimeLimit(120_000)) { // 2 minutes timeout
                throw new SudokuTimeoutException("Solving took longer than 120s");
            }
        }

        if (header.R == header) { // all columns removed
            if (VERBOSE) {
                System.out.println("-----------------------------------------");
                System.out.println("Solution #" + solutions + "\n");
            }
            handler.handleSolution(answer);
            if (VERBOSE) {
                System.out.println("-----------------------------------------");
            }
            solutions++;
            if (metrics != null) metrics.decrementRecursionDepth();
            return;
        }

        ColumnNode c = selectColumnNodeHeuristic();
        c.cover();

        for (DancingNode r = c.D; r != c; r = r.D) {
            answer.add(r);

            for (DancingNode j = r.R; j != r; j = j.R) {
                j.C.cover();
            }

            search(k + 1);

            r = answer.remove(answer.size() - 1);
            c = r.C;

            for (DancingNode j = r.L; j != r; j = j.L) {
                j.C.uncover();
            }
        }
        c.uncover();
        if (metrics != null) metrics.decrementRecursionDepth();
    }

    /**
     * Selects the column with the fewest nodes (most constrained).
     */
    private ColumnNode selectColumnNodeHeuristic() {
        int min = Integer.MAX_VALUE;
        ColumnNode ret = null;
        for (ColumnNode c = (ColumnNode) header.R; c != header; c = (ColumnNode) c.R) {
            if (c.size < min) {
                min = c.size;
                ret = c;
            }
        }
        return ret;
    }

    /**
     * Creates the DLX board from a grid of 0s and 1s.
     */
    private ColumnNode makeDLXBoard(int[][] grid) {
        final int COLS = grid[0].length;
        final int ROWS = grid.length;

        ColumnNode headerNode = new ColumnNode("header");
        ArrayList<ColumnNode> columnNodes = new ArrayList<>();

        for (int i = 0; i < COLS; i++) {
            ColumnNode n = new ColumnNode(Integer.toString(i));
            columnNodes.add(n);
            headerNode = (ColumnNode) headerNode.hookRight(n);
        }
        headerNode = headerNode.R.C;

        for (int i = 0; i < ROWS; i++) {
            DancingNode prev = null;
            for (int j = 0; j < COLS; j++) {
                if (grid[i][j] == 1) {
                    ColumnNode col = columnNodes.get(j);
                    DancingNode newNode = new DancingNode(col);
                    if (prev == null)
                        prev = newNode;
                    col.U.hookDown(newNode);
                    prev = prev.hookRight(newNode);
                    col.size++;
                }
            }
        }

        headerNode.size = COLS;

        return headerNode;
    }

    /**
     * Shows information about the solving process.
     */
    private void showInfo() {
        System.out.println("Number of updates: " + updates);
    }

    /**
     * Gets the number of solutions found.
     */
    public int getSolutionCount() {
        return solutions;
    }
}
