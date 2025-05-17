package com.rmit.sudoku.solver.dlx;
import java.util.List;

/**
 * Interface for handling solutions found by the DancingLinks algorithm.
 */
public interface SolutionHandler {
    /**
     * Called when a solution is found.
     *
     * @param solution The list of dancing nodes that form the solution
     */
    void handleSolution(List<DancingLinks.DancingNode> solution);
}
