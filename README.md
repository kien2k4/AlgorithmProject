# RMIT Sudoku Solver

A comprehensive Sudoku solver application with a Java backend and React frontend, featuring multiple solving algorithms and puzzle generation capabilities.

## Features

- Solve 9x9 Sudoku puzzles using two efficient algorithms:
  - Backtracking algorithm (classic approach)
  - Dancing Links (DLX) algorithm (Knuth's Algorithm X implementation)
- Generate Sudoku puzzles with varying difficulty levels:
  - Easy: 35-45 filled cells
  - Medium: 28-34 filled cells
  - Hard: 22-27 filled cells
  - Expert: 17-21 filled cells
- Performance metrics display:
  - Time complexity (operations count and milliseconds)
  - Space complexity (constant O(1) for 9x9 grid)
- Interactive web interface with:
  - Solve: Solves the current puzzle
  - Unsolve: Reverts to the original puzzle state
  - Generate: Creates new puzzles with selected difficulty
  - Clear: Clears all cells in the grid

## Project Structure

- `src/main/java`: Java backend code
  - `com.rmit.sudoku.RMIT_Sudoku_Solver`: Main solver class
  - `com.rmit.sudoku.solver`: Solver implementations
    - `BacktrackingSudokuSolver`: Classic backtracking algorithm
    - `dlx`: Dancing Links implementation package
      - `DancingLinks`: Core DLX algorithm implementation
      - `SolutionHandler`: Interface for handling DLX solutions
      - `SudokuDLXSolver`: Sudoku-specific DLX solver
  - `com.rmit.sudoku.generator`: Puzzle generation
    - `SudokuGenerator`: Creates puzzles with varying difficulties
  - `com.rmit.sudoku.metrics`: Performance tracking
    - `SudokuMetrics`: Tracks time and space complexity
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
   - The system will solve the puzzle using the Dancing Links algorithm by default
   - Performance metrics will be displayed showing operations count and time taken
3. Click "Unsolve" to revert to the original puzzle
4. Click "Generate" to create a new puzzle
   - Select difficulty level from the dropdown (Easy, Medium, Hard, Expert)
5. Click "Clear" to clear all cells

## Implementation Details

### Recent Updates

#### Enhanced Dancing Links (DLX) Implementation
The Dancing Links algorithm has been completely refactored with the following improvements:

1. **Modular Architecture**:
   - Separated into three main components: DancingLinks, SolutionHandler, and SudokuDLXSolver
   - Improved code organization and maintainability

2. **Performance Optimizations**:
   - Enhanced column selection heuristics for faster solving
   - Improved memory management during the solving process
   - Better timeout handling to prevent long-running operations

3. **Improved Integration**:
   - Seamless integration with the existing metrics system
   - Default algorithm for the web interface
   - Comprehensive error handling and validation

### Solving Algorithms

#### Backtracking Algorithm
The classic approach to solving Sudoku puzzles:
1. Find an empty cell
2. Try placing numbers 1-9 in the cell
3. Check if the number is valid in that position
4. If valid, recursively try to solve the rest of the puzzle
5. If the recursive call returns false, backtrack and try the next number
6. If all numbers 1-9 have been tried and none work, the puzzle is unsolvable

#### Dancing Links (DLX) Algorithm
An optimized implementation of Donald Knuth's Algorithm X using the Dancing Links technique:
1. Represents the Sudoku puzzle as an exact cover problem
2. Uses a sparse matrix representation with doubly-linked lists
3. Efficiently finds solutions through recursive search with column selection heuristics
4. Provides an alternative solving method that can be more efficient for certain puzzles
5. Implemented with a modular design:
   - Core DancingLinks algorithm that can solve any exact cover problem
   - SolutionHandler interface for processing solutions
   - SudokuDLXSolver that converts Sudoku puzzles to exact cover problems

### Puzzle Generation
The puzzle generator creates valid Sudoku puzzles with unique solutions:
1. Generates a fully solved puzzle
2. Systematically removes numbers while ensuring a unique solution remains
3. Adjusts the number of filled cells based on the selected difficulty level

### Performance Metrics
The application tracks and displays comprehensive performance metrics:
- Time complexity: Number of operations and milliseconds taken
- Space complexity: Constant O(1) for the 9x9 grid (81 cells)
- Maximum recursion depth during solving
- Memory usage statistics

Both solving algorithms (Backtracking and Dancing Links) provide detailed metrics, allowing for performance comparison between the two approaches. The Dancing Links algorithm typically performs better on more complex puzzles with fewer initial clues.
The frontend communicates with the backend via a REST API to solve puzzles and generate new ones.

### Team Members & Contributions
| Student ID | Full Name              | Points |
|------------|------------------------|--------|
| S3927205   | Le Ngoc Hieu           | 4      |
| S3979510   | Dang Trung Kien        | 7      |
| S3983370   | Le Kim Quyen           | 5      |
| S3921846   | Nguyen Dai Thanh       | 5      |
| S3979772   | Ngo Nguyen Minh Tri    | 4      |
**Total Contribution Points:** 25