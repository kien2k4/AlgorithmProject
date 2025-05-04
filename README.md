# RMIT Sudoku Solver

A Sudoku solver application with a Java backend and React frontend.

## Features

- Solve 9x9 Sudoku puzzles using an efficient backtracking algorithm
- Interactive grid for entering puzzles
- Four control buttons:
  - Solve: Solves the current puzzle
  - Unsolve: Reverts to the original puzzle state
  - Load Example: Loads a predefined Sudoku puzzle
  - Clear: Clears all cells in the grid

## Project Structure

- `src/main/java`: Java backend code
  - `com.rmit.sudoku.RMIT_Sudoku_Solver`: Main solver class
  - `com.rmit.sudoku.controller`: REST API controllers
  - `com.rmit.sudoku.model`: Data models
- `frontend`: React frontend code
  - `src/components`: React components for the Sudoku board and cells

## Running the Application

### Backend (Java)

1. Make sure you have Java 11+ and Maven installed
2. Navigate to the project root directory
3. Run the following command to start the Spring Boot application:

```bash
mvn spring-boot:run
```

The backend will start on http://localhost:8080

### Frontend (React)

1. Make sure you have Node.js and npm installed
2. Navigate to the `frontend` directory
3. Install dependencies:

```bash
npm install
```

4. Start the React development server:

```bash
npm start
```

The frontend will start on http://localhost:3000

## How to Use

1. Enter numbers (1-9) in the cells to create a Sudoku puzzle
   - Leave cells empty (or enter 0) for cells to be solved
2. Click "Solve" to solve the puzzle
3. Click "Unsolve" to revert to the original puzzle
4. Click "Load Example" to load a predefined puzzle
5. Click "Clear" to clear all cells

## Implementation Details

The Sudoku solver uses a backtracking algorithm to efficiently find solutions:
1. Find an empty cell
2. Try placing numbers 1-9 in the cell
3. Check if the number is valid in that position
4. If valid, recursively try to solve the rest of the puzzle
5. If the recursive call returns false, backtrack and try the next number
6. If all numbers 1-9 have been tried and none work, the puzzle is unsolvable

The frontend communicates with the backend via a REST API to solve puzzles.
