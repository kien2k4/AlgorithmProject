import React from 'react';
import './App.css';
import SudokuBoard from './components/SudokuBoard';

function App() {
  return (
    <div className="App">
      <header className="App-header">
        <h1>RMIT Sudoku Solver</h1>
      </header>
      <main>
        <SudokuBoard />
      </main>
      <footer className="App-footer">
        <p>© 2025 RMIT University</p>
      </footer>
    </div>
  );
}

export default App;
