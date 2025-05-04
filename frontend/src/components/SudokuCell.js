import React from 'react';
import './SudokuCell.css';

const SudokuCell = ({ value, onChange, row, col, isOriginal, isInvalid }) => {
  // Handle input change
  const handleChange = (e) => {
    const inputValue = e.target.value;

    // Only allow empty string or numbers 1-9
    if (inputValue === '' || (inputValue >= '1' && inputValue <= '9')) {
      onChange(inputValue);
    }
  };

  // Determine cell classes for styling
  const getCellClasses = () => {
    const classes = ['sudoku-cell'];

    // Add border classes
    if (row % 3 === 0) classes.push('border-top');
    if (row === 8) classes.push('border-bottom');
    if (col % 3 === 0) classes.push('border-left');
    if (col === 8) classes.push('border-right');

    // Add solved class if the cell is not original and has a value
    if (!isOriginal && value !== 0) {
      classes.push('solved-cell');
    }

    // Add invalid class if the cell is part of a validation error
    if (isInvalid) {
      classes.push('invalid-cell');
    }

    return classes.join(' ');
  };

  return (
    <input
      type="text"
      className={getCellClasses()}
      value={value === 0 ? '' : value}
      onChange={handleChange}
      maxLength="1"
    />
  );
};

export default SudokuCell;
