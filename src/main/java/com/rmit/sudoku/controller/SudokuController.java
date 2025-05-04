package com.rmit.sudoku.controller;

import com.rmit.sudoku.RMIT_Sudoku_Solver;
import com.rmit.sudoku.model.SudokuRequest;
import com.rmit.sudoku.model.SudokuResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sudoku")
public class SudokuController {

    private final RMIT_Sudoku_Solver solver = new RMIT_Sudoku_Solver();

    @PostMapping("/solve")
    public SudokuResponse solveSudoku(@RequestBody SudokuRequest request) {
        int[][] board = request.getBoard();
        int[][] solvedBoard = solver.solve(board);
        
        SudokuResponse response = new SudokuResponse();
        if (solvedBoard != null) {
            response.setBoard(solvedBoard);
            response.setSolved(true);
        } else {
            response.setBoard(board);
            response.setSolved(false);
            response.setMessage("No solution exists for this puzzle.");
        }
        
        return response;
    }
}
