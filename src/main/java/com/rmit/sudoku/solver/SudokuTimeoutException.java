package com.rmit.sudoku.solver;

/**
 * Exception thrown when a Sudoku puzzle cannot be solved within the time limit.
 */
public class SudokuTimeoutException extends Exception {
    
    public SudokuTimeoutException(String message) {
        super(message);
    }
    
    public SudokuTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
