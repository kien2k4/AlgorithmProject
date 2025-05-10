package com.rmit.sudoku.controller;

import com.rmit.sudoku.RMIT_Sudoku_Solver;
import com.rmit.sudoku.generator.SudokuGenerator;
import com.rmit.sudoku.metrics.SudokuMetrics;
import com.rmit.sudoku.model.SudokuRequest;
import com.rmit.sudoku.model.SudokuResponse;
import com.rmit.sudoku.solver.BacktrackingSudokuSolver;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sudoku")
public class SudokuController {

    private final RMIT_Sudoku_Solver solver = new RMIT_Sudoku_Solver();
    private final SudokuGenerator generator = new SudokuGenerator();

    @PostMapping("/solve")
    public SudokuResponse solveSudoku(@RequestBody SudokuRequest request) {
        int[][] board = request.getBoard();
        int[][] solvedBoard = null;
        SudokuResponse response = new SudokuResponse();

        try {
            solvedBoard = solver.solve(board);
        } catch (RuntimeException e) {
            // Handle timeout or other exceptions
            response.setBoard(board);
            response.setSolved(false);
            response.setMessage("Failed to solve puzzle: " + e.getMessage());
            return response;
        }

        // Add performance metrics to the response
        SudokuMetrics metrics = solver.getBacktrackingMetrics();
        if (metrics != null) {
            response.setOperationCount(metrics.getOperationCount());
            response.setTimeTakenMs(metrics.getTimeTaken());
            response.setMaxRecursionDepth(metrics.getMaxRecursionDepth());
            response.setMemoryUsed(metrics.getFormattedMemoryUsed());
        }

        if (solvedBoard != null) {
            response.setBoard(solvedBoard);
            response.setSolved(true);
            if (metrics != null) {
                response.setMessage("Puzzle solved successfully with backtracking in " + metrics.getTimeTaken() +
                        " ms using " + metrics.getOperationCount() + " operations. " +
                        "Space complexity: O(1) constant, Max recursion depth: " + metrics.getMaxRecursionDepth() +
                        ", Memory used: " + metrics.getFormattedMemoryUsed() + ".");
            } else {
                response.setMessage("Puzzle solved successfully.");
            }
        } else {
            response.setBoard(board);
            response.setSolved(false);
            if (metrics != null) {
                response.setMessage("No solution exists for this puzzle. Attempted for " +
                        metrics.getTimeTaken() + " ms using " + metrics.getOperationCount() + " operations. " +
                        "Space complexity: O(1) constant, Max recursion depth: " + metrics.getMaxRecursionDepth() +
                        ", Memory used: " + metrics.getFormattedMemoryUsed() + ".");
            } else {
                response.setMessage("No solution exists for this puzzle.");
            }
        }

        return response;
    }

    /**
     * Generates a new Sudoku puzzle with the specified difficulty.
     *
     * @param difficulty The difficulty level (EASY, MEDIUM, HARD, EXPERT)
     * @return A response containing the generated puzzle
     */
    @GetMapping("/generate/{difficulty}")
    public SudokuResponse generatePuzzle(@PathVariable String difficulty) {
        SudokuResponse response = new SudokuResponse();

        try {
            SudokuGenerator.Difficulty difficultyLevel = SudokuGenerator.Difficulty.valueOf(difficulty.toUpperCase());
            int[][] puzzle = generator.generate(difficultyLevel);

            response.setBoard(puzzle);
            response.setSolved(false);
            response.setMessage("Generated " + difficulty.toUpperCase() + " puzzle");
        } catch (IllegalArgumentException e) {
            // Handle invalid difficulty level
            int[][] emptyBoard = new int[9][9];
            response.setBoard(emptyBoard);
            response.setSolved(false);
            response.setMessage("Invalid difficulty level. Valid options are: EASY, MEDIUM, HARD, EXPERT");
        }

        return response;
    }

    /**
     * Solves a Sudoku puzzle using the Dancing Links algorithm.
     *
     * @param request The request containing the puzzle to solve
     * @return A response containing the solved puzzle
     */
    @PostMapping("/solve/dlx")
    public SudokuResponse solveSudokuWithDLX(@RequestBody SudokuRequest request) {
        int[][] board = request.getBoard();
        int[][] solvedBoard = null;
        SudokuResponse response = new SudokuResponse();

        try {
            solvedBoard = solver.solveDLX(board);
        } catch (RuntimeException e) {
            // Handle timeout or other exceptions
            response.setBoard(board);
            response.setSolved(false);
            response.setMessage("Failed to solve puzzle with DLX: " + e.getMessage());
            return response;
        }

        // Add performance metrics to the response
        SudokuMetrics metrics = solver.getDancingLinksMetrics();
        if (metrics != null) {
            response.setOperationCount(metrics.getOperationCount());
            response.setTimeTakenMs(metrics.getTimeTaken());
            response.setMaxRecursionDepth(metrics.getMaxRecursionDepth());
            response.setMemoryUsed(metrics.getFormattedMemoryUsed());
        }

        if (solvedBoard != null) {
            response.setBoard(solvedBoard);
            response.setSolved(true);
            if (metrics != null) {
                response.setMessage("Puzzle solved successfully with Dancing Links in " + metrics.getTimeTaken() +
                        " ms using " + metrics.getOperationCount() + " operations. " +
                        "Space complexity: O(1) constant, Max recursion depth: " + metrics.getMaxRecursionDepth() +
                        ", Memory used: " + metrics.getFormattedMemoryUsed() + ".");
            } else {
                response.setMessage("Puzzle solved successfully with Dancing Links.");
            }
        } else {
            response.setBoard(board);
            response.setSolved(false);
            if (metrics != null) {
                response.setMessage("No solution exists for this puzzle. Attempted with Dancing Links for " +
                        metrics.getTimeTaken() + " ms using " + metrics.getOperationCount() + " operations. " +
                        "Space complexity: O(1) constant, Max recursion depth: " + metrics.getMaxRecursionDepth() +
                        ", Memory used: " + metrics.getFormattedMemoryUsed() + ".");
            } else {
                response.setMessage("No solution exists for this puzzle.");
            }
        }

        return response;
    }

    /**
     * Solves a Sudoku puzzle using both algorithms and compares their performance.
     *
     * @param request The request containing the puzzle to solve
     * @return A response containing the solved puzzle and comparison metrics
     */
    @PostMapping("/solve/compare")
    public SudokuResponse compareSolvers(@RequestBody SudokuRequest request) {
        int[][] board = request.getBoard();
        SudokuResponse response = new SudokuResponse();

        // Solve with backtracking
        int[][] backtrackingSolution = null;
        SudokuMetrics backtrackingMetrics = null;
        String backtrackingMessage = "";

        try {
            backtrackingSolution = solver.solve(board);
            backtrackingMetrics = solver.getBacktrackingMetrics();
            if (backtrackingMetrics != null) {
                backtrackingMessage = "Backtracking: " + backtrackingMetrics.getTimeTaken() +
                        " ms, " + backtrackingMetrics.getOperationCount() + " operations, " +
                        "depth " + backtrackingMetrics.getMaxRecursionDepth() + ", " +
                        backtrackingMetrics.getFormattedMemoryUsed() + ".";
            }
        } catch (RuntimeException e) {
            backtrackingMessage = "Backtracking failed: " + e.getMessage();
        }

        // Solve with Dancing Links
        int[][] dlxSolution = null;
        SudokuMetrics dlxMetrics = null;
        String dlxMessage = "";

        try {
            dlxSolution = solver.solveDLX(board);
            dlxMetrics = solver.getDancingLinksMetrics();
            if (dlxMetrics != null) {
                dlxMessage = "Dancing Links: " + dlxMetrics.getTimeTaken() +
                        " ms, " + dlxMetrics.getOperationCount() + " operations, " +
                        "depth " + dlxMetrics.getMaxRecursionDepth() + ", " +
                        dlxMetrics.getFormattedMemoryUsed() + ".";
            }
        } catch (RuntimeException e) {
            dlxMessage = "Dancing Links failed: " + e.getMessage();
        }

        // Use whichever solution is available
        int[][] solution = backtrackingSolution != null ? backtrackingSolution : dlxSolution;

        if (solution != null) {
            response.setBoard(solution);
            response.setSolved(true);
            response.setMessage(backtrackingMessage + "\n" + dlxMessage);

            // Use metrics from the faster algorithm
            if (backtrackingMetrics != null && dlxMetrics != null) {
                if (backtrackingMetrics.getTimeTaken() <= dlxMetrics.getTimeTaken()) {
                    response.setOperationCount(backtrackingMetrics.getOperationCount());
                    response.setTimeTakenMs(backtrackingMetrics.getTimeTaken());
                    response.setMaxRecursionDepth(backtrackingMetrics.getMaxRecursionDepth());
                    response.setMemoryUsed(backtrackingMetrics.getFormattedMemoryUsed());
                } else {
                    response.setOperationCount(dlxMetrics.getOperationCount());
                    response.setTimeTakenMs(dlxMetrics.getTimeTaken());
                    response.setMaxRecursionDepth(dlxMetrics.getMaxRecursionDepth());
                    response.setMemoryUsed(dlxMetrics.getFormattedMemoryUsed());
                }
            } else if (backtrackingMetrics != null) {
                response.setOperationCount(backtrackingMetrics.getOperationCount());
                response.setTimeTakenMs(backtrackingMetrics.getTimeTaken());
                response.setMaxRecursionDepth(backtrackingMetrics.getMaxRecursionDepth());
                response.setMemoryUsed(backtrackingMetrics.getFormattedMemoryUsed());
            } else if (dlxMetrics != null) {
                response.setOperationCount(dlxMetrics.getOperationCount());
                response.setTimeTakenMs(dlxMetrics.getTimeTaken());
                response.setMaxRecursionDepth(dlxMetrics.getMaxRecursionDepth());
                response.setMemoryUsed(dlxMetrics.getFormattedMemoryUsed());
            }
        } else {
            response.setBoard(board);
            response.setSolved(false);
            response.setMessage("No solution exists for this puzzle.\n" + backtrackingMessage + "\n" + dlxMessage);
        }

        return response;
    }
}
