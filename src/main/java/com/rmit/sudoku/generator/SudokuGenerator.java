package com.rmit.sudoku.generator;

import com.rmit.sudoku.solver.BacktrackingSudokuSolver;
import com.rmit.sudoku.solver.SudokuSolver;
import com.rmit.sudoku.solver.SudokuTimeoutException;
import com.rmit.sudoku.validator.SudokuValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Class for generating Sudoku puzzles with varying difficulty levels.
 */
public class SudokuGenerator {

    private static final int GRID_SIZE = 9;
    private static final int BOX_SIZE = 3;
    private final Random random;
    private final SudokuSolver solver;


    /**
     * Difficulty levels for Sudoku puzzles.
     */
    public enum Difficulty {
        EASY(35, 45),      // 35-45 filled cells (36-46 empty cells)
        MEDIUM(28, 34),    // 28-34 filled cells (47-53 empty cells)
        HARD(22, 27),      // 22-27 filled cells (54-59 empty cells)
        EXPERT(17, 21);    // 17-21 filled cells (60-64 empty cells)

        private final int minFilled;
        private final int maxFilled;

        Difficulty(int minFilled, int maxFilled) {
            this.minFilled = minFilled;
            this.maxFilled = maxFilled;
        }

        public int getMinFilled() {
            return minFilled;
        }

        public int getMaxFilled() {
            return maxFilled;
        }
    }

    /**
     * Creates a new SudokuGenerator with a random seed.
     */
    public SudokuGenerator() {
        this(new Random().nextLong());
    }

    /**
     * Creates a new SudokuGenerator with a specific seed for reproducible puzzles.
     *
     * @param seed The random seed
     */
    public SudokuGenerator(long seed) {
        this.random = new Random(seed);
        this.solver = new BacktrackingSudokuSolver(false); // Disable metrics printing
    }

    /**
     * Generates a new Sudoku puzzle with the specified difficulty.
     *
     * @param difficulty The difficulty level
     * @return A 9x9 array representing the puzzle (0 for empty cells)
     */
    public int[][] generate(Difficulty difficulty) {
        // Generate a fully solved puzzle
        int[][] solvedPuzzle = generateSolvedPuzzle();

        // Create a copy to work with
        int[][] puzzle = new int[GRID_SIZE][GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            System.arraycopy(solvedPuzzle[i], 0, puzzle[i], 0, GRID_SIZE);
        }

        // Determine how many cells to keep filled
        int cellsToKeep = random.nextInt(difficulty.getMaxFilled() - difficulty.getMinFilled() + 1)
                + difficulty.getMinFilled();

        // Create a list of all cell positions
        List<Integer> positions = new ArrayList<>(GRID_SIZE * GRID_SIZE);
        for (int i = 0; i < GRID_SIZE * GRID_SIZE; i++) {
            positions.add(i);
        }

        // Shuffle the positions
        Collections.shuffle(positions, random);

        // Keep only the specified number of cells
        int cellsToRemove = GRID_SIZE * GRID_SIZE - cellsToKeep;
        for (int i = 0; i < cellsToRemove; i++) {
            int position = positions.get(i);
            int row = position / GRID_SIZE;
            int col = position % GRID_SIZE;

            // Store the original value
            int originalValue = puzzle[row][col];

            // Try removing the cell
            puzzle[row][col] = 0;

            // Check if the puzzle still has a unique solution
            if (!hasUniqueSolution(puzzle)) {
                // If not, restore the value
                puzzle[row][col] = originalValue;
            }
        }

        return puzzle;
    }

    /**
     * Generates a fully solved Sudoku puzzle.
     *
     * @return A 9x9 array with a valid Sudoku solution
     */
    private int[][] generateSolvedPuzzle() {
        int[][] puzzle = new int[GRID_SIZE][GRID_SIZE];

        // Fill the diagonal boxes first (these can be filled independently)
        for (int box = 0; box < GRID_SIZE; box += BOX_SIZE) {
            fillBox(puzzle, box, box);
        }

        // Solve the rest of the puzzle
        solvePuzzle(puzzle);

        return puzzle;
    }

    /**
     * Fills a 3x3 box with random values.
     *
     * @param puzzle The puzzle to fill
     * @param boxRow The starting row of the box
     * @param boxCol The starting column of the box
     */
    private void fillBox(int[][] puzzle, int boxRow, int boxCol) {
        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= GRID_SIZE; i++) {
            numbers.add(i);
        }
        Collections.shuffle(numbers, random);

        int index = 0;
        for (int i = 0; i < BOX_SIZE; i++) {
            for (int j = 0; j < BOX_SIZE; j++) {
                puzzle[boxRow + i][boxCol + j] = numbers.get(index++);
            }
        }
    }

    /**
     * Solves a partially filled Sudoku puzzle.
     *
     * @param puzzle The puzzle to solve
     * @return true if the puzzle was solved, false otherwise
     */
    private boolean solvePuzzle(int[][] puzzle) {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (puzzle[row][col] == 0) {
                    List<Integer> numbers = new ArrayList<>();
                    for (int i = 1; i <= GRID_SIZE; i++) {
                        numbers.add(i);
                    }
                    Collections.shuffle(numbers, random);

                    for (int num : numbers) {
                        if (SudokuValidator.isValidPlacement(puzzle, num, row, col)) {
                            puzzle[row][col] = num;

                            if (solvePuzzle(puzzle)) {
                                return true;
                            }

                            puzzle[row][col] = 0;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks if a puzzle has a unique solution.
     *
     * @param puzzle The puzzle to check
     * @return true if the puzzle has a unique solution, false otherwise
     */
    private boolean hasUniqueSolution(int[][] puzzle) {
        // Create a copy of the puzzle
        int[][] puzzleCopy = new int[GRID_SIZE][GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            System.arraycopy(puzzle[i], 0, puzzleCopy[i], 0, GRID_SIZE);
        }

        try {
            // Try to solve the puzzle
            int[][] solution = solver.solve(puzzleCopy);

            // If no solution exists, return false
            if (solution == null) {
                return false;
            }

            // Check if there's a second solution
            return !hasSecondSolution(puzzle, solution);
        } catch (SudokuTimeoutException e) {
            // If solving times out, assume it's too difficult
            return false;
        }
    }

    /**
     * Checks if a puzzle has a second solution different from the given one.
     *
     * @param puzzle The puzzle to check
     * @param firstSolution The first solution
     * @return true if a second solution exists, false otherwise
     */
    private boolean hasSecondSolution(int[][] puzzle, int[][] firstSolution) {
        // This is a simplified check that doesn't guarantee finding all solutions
        // For a complete check, a full backtracking search would be needed

        // Create a copy of the puzzle
        int[][] puzzleCopy = new int[GRID_SIZE][GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            System.arraycopy(puzzle[i], 0, puzzleCopy[i], 0, GRID_SIZE);
        }

        // Find an empty cell
        int emptyRow = -1;
        int emptyCol = -1;
        outerLoop:
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (puzzleCopy[row][col] == 0) {
                    emptyRow = row;
                    emptyCol = col;
                    break outerLoop;
                }
            }
        }

        // If no empty cell, the puzzle is already solved
        if (emptyRow == -1) {
            return false;
        }

        // Try a different value than the one in the first solution
        int valueInFirstSolution = firstSolution[emptyRow][emptyCol];

        for (int num = 1; num <= GRID_SIZE; num++) {
            if (num != valueInFirstSolution && SudokuValidator.isValidPlacement(puzzleCopy, num, emptyRow, emptyCol)) {
                puzzleCopy[emptyRow][emptyCol] = num;

                try {
                    // Try to solve with this different value
                    if (solver.solve(puzzleCopy) != null) {
                        return true; // Found a second solution
                    }
                } catch (SudokuTimeoutException e) {
                    // If solving times out, continue with the next number
                }

                // Reset for the next attempt
                puzzleCopy[emptyRow][emptyCol] = 0;
            }
        }

        return false; // No second solution found
    }

    /**
     * Prints a Sudoku puzzle to the console.
     *
     * @param puzzle The puzzle to print
     */
    public void printPuzzle(int[][] puzzle) {
        for (int row = 0; row < GRID_SIZE; row++) {
            if (row % BOX_SIZE == 0 && row != 0) {
                System.out.println("---------------------");
            }
            for (int col = 0; col < GRID_SIZE; col++) {
                if (col % BOX_SIZE == 0 && col != 0) {
                    System.out.print("| ");
                }
                System.out.print(puzzle[row][col] == 0 ? ". " : puzzle[row][col] + " ");
            }
            System.out.println();
        }
    }

    /**
     * Main method to demonstrate the Sudoku generator.
     */
    public static void main(String[] args) {
        SudokuGenerator generator = new SudokuGenerator();

        System.out.println("Generating EASY puzzle:");
        int[][] easyPuzzle = generator.generate(Difficulty.EASY);
        generator.printPuzzle(easyPuzzle);

        System.out.println("\nGenerating MEDIUM puzzle:");
        int[][] mediumPuzzle = generator.generate(Difficulty.MEDIUM);
        generator.printPuzzle(mediumPuzzle);

        System.out.println("\nGenerating HARD puzzle:");
        int[][] hardPuzzle = generator.generate(Difficulty.HARD);
        generator.printPuzzle(hardPuzzle);

        System.out.println("\nGenerating EXPERT puzzle:");
        int[][] expertPuzzle = generator.generate(Difficulty.EXPERT);
        generator.printPuzzle(expertPuzzle);

        // Demonstrate solving with metrics (only once at the end)
        System.out.println("\nSolving the EXPERT puzzle with metrics:");
        try {
            BacktrackingSudokuSolver solverWithMetrics = new BacktrackingSudokuSolver(true);
            solverWithMetrics.solve(expertPuzzle);
        } catch (SudokuTimeoutException e) {
            System.out.println("Timeout while solving: " + e.getMessage());
        }
    }
}
