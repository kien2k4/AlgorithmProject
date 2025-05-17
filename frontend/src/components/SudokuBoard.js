import React, { useState } from 'react';
import axios from 'axios';
import './SudokuBoard.css';
import SudokuCell from './SudokuCell';

const SudokuBoard = () => {
  // Initialize empty 9x9 board with zeros
  const emptyBoard = Array(9).fill().map(() => Array(9).fill(0));

  // State variables
  const [board, setBoard] = useState(emptyBoard);
  const [originalBoard, setOriginalBoard] = useState(emptyBoard);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [validationError, setValidationError] = useState(null);
  const [isSolved, setIsSolved] = useState(false);
  const [generatingPuzzle, setGeneratingPuzzle] = useState(false);
  const [complexityMetrics, setComplexityMetrics] = useState(null);

  // Example Sudoku puzzle
  const examplePuzzle = [
    [5, 3, 0, 0, 7, 0, 0, 0, 0],
    [6, 0, 0, 1, 9, 5, 0, 0, 0],
    [0, 9, 8, 0, 0, 0, 0, 6, 0],
    [8, 0, 0, 0, 6, 0, 0, 0, 3],
    [4, 0, 0, 8, 0, 3, 0, 0, 1],
    [7, 0, 0, 0, 2, 0, 0, 0, 6],
    [0, 6, 0, 0, 0, 0, 2, 8, 0],
    [0, 0, 0, 4, 1, 9, 0, 0, 5],
    [0, 0, 0, 0, 8, 0, 0, 7, 9]
  ];

  // State to track invalid cells
  const [invalidCells, setInvalidCells] = useState([]);

  // Validate the board for duplicate numbers in rows, columns, and boxes
  const validateBoard = (board) => {
    const newInvalidCells = [];
    let errorMessage = null;

    // Check rows
    for (let row = 0; row < 9; row++) {
      const rowNumbers = new Map();
      for (let col = 0; col < 9; col++) {
        const num = board[row][col];
        if (num !== 0) {
          if (rowNumbers.has(num)) {
            // Mark both cells as invalid
            const prevCol = rowNumbers.get(num);
            newInvalidCells.push([row, prevCol]);
            newInvalidCells.push([row, col]);
            errorMessage = `Duplicate number ${num} in row ${row + 1}`;
          } else {
            rowNumbers.set(num, col);
          }
        }
      }
    }

    // Check columns
    for (let col = 0; col < 9; col++) {
      const colNumbers = new Map();
      for (let row = 0; row < 9; row++) {
        const num = board[row][col];
        if (num !== 0) {
          if (colNumbers.has(num)) {
            // Mark both cells as invalid
            const prevRow = colNumbers.get(num);
            newInvalidCells.push([prevRow, col]);
            newInvalidCells.push([row, col]);
            if (!errorMessage) {
              errorMessage = `Duplicate number ${num} in column ${col + 1}`;
            }
          } else {
            colNumbers.set(num, row);
          }
        }
      }
    }

    // Check 3x3 boxes
    for (let boxRow = 0; boxRow < 3; boxRow++) {
      for (let boxCol = 0; boxCol < 3; boxCol++) {
        const boxNumbers = new Map();
        for (let i = 0; i < 3; i++) {
          for (let j = 0; j < 3; j++) {
            const row = boxRow * 3 + i;
            const col = boxCol * 3 + j;
            const num = board[row][col];
            if (num !== 0) {
              if (boxNumbers.has(num)) {
                // Mark both cells as invalid
                const [prevI, prevJ] = boxNumbers.get(num);
                const prevRow = boxRow * 3 + prevI;
                const prevCol = boxCol * 3 + prevJ;
                newInvalidCells.push([prevRow, prevCol]);
                newInvalidCells.push([row, col]);
                if (!errorMessage) {
                  errorMessage = `Duplicate number ${num} in box at position (${boxRow + 1},${boxCol + 1})`;
                }
              } else {
                boxNumbers.set(num, [i, j]);
              }
            }
          }
        }
      }
    }

    // Update invalid cells state
    setInvalidCells(newInvalidCells);

    // Return error message if any
    return errorMessage;
  };

  // Handle cell value change
  const handleCellChange = (row, col, value) => {
    // Create a deep copy of the board
    const newBoard = board.map(row => [...row]);

    // Update the cell value (convert to number or 0 if empty)
    newBoard[row][col] = value === '' ? 0 : parseInt(value, 10);

    // Validate the board
    const validationResult = validateBoard(newBoard);
    setValidationError(validationResult);

    // Clear error message if validation errors are fixed
    if (!validationResult && error === 'Please fix the validation errors before solving.') {
      setError(null);
    }

    // Update the board state
    setBoard(newBoard);
  };

  // Toggle between solve and unsolve
  const toggleSolve = async () => {
    // If already solved, revert to original board
    if (isSolved) {
      setBoard(originalBoard.map(row => [...row]));
      setError(null);
      setValidationError(null);
      setInvalidCells([]);
      setIsSolved(false);

      setComplexityMetrics(null);
      return;
    }

    // Check for validation errors before solving
    if (validationError) {
      setError('Please fix the validation errors before solving.');
      return;
    }

    // Otherwise, solve the puzzle
    try {
      setLoading(true);
      setError(null);

      // Save the original board state before solving
      setOriginalBoard(board.map(row => [...row]));
      const response = await axios.post('/api/sudoku/solve', { board });

      // Update the board with the solution
      if (response.data.solved) {
        setBoard(response.data.board);
        setIsSolved(true);
        // Store complexity metrics
        setComplexityMetrics({
          operations: response.data.operationCount,
          time: response.data.timeTakenMs
        });
      } else {
        setError(response.data.message || 'Failed to solve the puzzle.');
        // Store complexity metrics even for failed attempts
        setComplexityMetrics({
          operations: response.data.operationCount,
          time: response.data.timeTakenMs
        });
      }
    } catch (err) {
      setError('Error connecting to the server. Please try again.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  // Load the example puzzle
  const loadExample = () => {
    setBoard(examplePuzzle.map(row => [...row]));
    setOriginalBoard(examplePuzzle.map(row => [...row]));
    setError(null);
    setValidationError(null);
    setInvalidCells([]);
    setIsSolved(false);
    setComplexityMetrics(null);

  };

  // Clear the board
  const clearBoard = () => {
    setBoard(emptyBoard.map(row => [...row]));
    setOriginalBoard(emptyBoard.map(row => [...row]));
    setError(null);
    setValidationError(null);
    setInvalidCells([]);
    setIsSolved(false);
    setComplexityMetrics(null);
  };

  // Generate a puzzle with the specified difficulty
  const generatePuzzle = async (difficulty) => {
    try {
      setGeneratingPuzzle(true);
      setError(null);
      setValidationError(null);
      setInvalidCells([]);
      setIsSolved(false);
      setComplexityMetrics(null);

      // Call the API to generate a puzzle
      const response = await axios.get(`/api/sudoku/generate/${difficulty}`);

      if (response.data.board) {
        // Update the board with the generated puzzle
        setBoard(response.data.board);
        setOriginalBoard(response.data.board.map(row => [...row]));
      } else {
        setError('Failed to generate puzzle.');
      }
    } catch (err) {
      setError('Error generating puzzle: ' + (err.response?.data?.message || err.message));
      console.error(err);
    } finally {
      setGeneratingPuzzle(false);
    }

  };

  return (
    <div className="sudoku-container">
      <div className="sudoku-board">
        {board.map((row, rowIndex) => (
          <div key={rowIndex} className="sudoku-row">
            {row.map((cell, colIndex) => (
              <SudokuCell
                key={`${rowIndex}-${colIndex}`}
                value={cell}
                onChange={(value) => handleCellChange(rowIndex, colIndex, value)}
                row={rowIndex}
                col={colIndex}
                isOriginal={!isSolved || originalBoard[rowIndex][colIndex] === cell}
                isInvalid={invalidCells.some(([r, c]) => r === rowIndex && c === colIndex)}
              />
            ))}
          </div>
        ))}
      </div>

      <div className="controls">
        <button onClick={toggleSolve} disabled={loading || generatingPuzzle}>
          {loading ? 'Solving...' : isSolved ? 'Unsolve' : 'Solve'}
        </button>
        <button onClick={loadExample} disabled={generatingPuzzle}>Load Example</button>
        <button onClick={clearBoard} disabled={generatingPuzzle}>Clear</button>
      </div>

      <div className="difficulty-controls">
        <p>Generate Puzzle:</p>
        <button onClick={() => generatePuzzle('EASY')} disabled={loading || generatingPuzzle}>Easy</button>
        <button onClick={() => generatePuzzle('MEDIUM')} disabled={loading || generatingPuzzle}>Medium</button>
        <button onClick={() => generatePuzzle('HARD')} disabled={loading || generatingPuzzle}>Hard</button>
        <button onClick={() => generatePuzzle('EXPERT')} disabled={loading || generatingPuzzle}>Expert</button>
      </div>

      {error && <div className="error-message">{error}</div>}
      {validationError && <div className="validation-error-message">Invalid input: {validationError}</div>}

      {complexityMetrics && (
        <div className="complexity-metrics">
          <p><strong>Time Complexity:</strong> {complexityMetrics.operations.toLocaleString()} operations in {complexityMetrics.time} ms</p>
          <p><strong>Space Complexity:</strong> O(1) - constant for 9x9 grid (81 cells)</p>
        </div>
      )}
    </div>
  );
};

export default SudokuBoard;
