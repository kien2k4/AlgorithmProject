package com.rmit.sudoku.metrics;

/**
 * Class for tracking and reporting Sudoku solver performance metrics.
 */
public class SudokuMetrics {
    
    private long operationCount;
    private long startTime;
    private long endTime;
    private int currentRecursionDepth;
    private int maxRecursionDepth;
    private long memoryBefore;
    private long memoryAfter;
    private long peakMemoryUsage;
    
    /**
     * Initializes and starts tracking metrics.
     */
    public void startTracking() {
        operationCount = 0;
        currentRecursionDepth = 0;
        maxRecursionDepth = 0;
        startTime = System.currentTimeMillis();
        
        // Measure memory before solving
        System.gc(); // Request garbage collection to get more accurate memory measurement
        memoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        peakMemoryUsage = 0;
    }
    
    /**
     * Stops tracking metrics.
     */
    public void stopTracking() {
        endTime = System.currentTimeMillis();
        memoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }
    
    /**
     * Increments the operation count.
     */
    public void incrementOperationCount() {
        operationCount++;
    }
    
    /**
     * Increments the recursion depth and updates the maximum.
     */
    public void incrementRecursionDepth() {
        currentRecursionDepth++;
        if (currentRecursionDepth > maxRecursionDepth) {
            maxRecursionDepth = currentRecursionDepth;
        }
    }
    
    /**
     * Decrements the recursion depth.
     */
    public void decrementRecursionDepth() {
        currentRecursionDepth--;
    }
    
    /**
     * Updates the peak memory usage.
     */
    public void updatePeakMemoryUsage() {
        if (operationCount % 1000 == 0) {
            long currentMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            long memoryUsed = currentMemory - memoryBefore;
            if (memoryUsed > peakMemoryUsage) {
                peakMemoryUsage = memoryUsed;
            }
        }
    }
    
    /**
     * Prints the performance metrics to the console.
     */
    public void printMetrics() {
        System.out.println("\nPerformance Metrics:");
        System.out.println("Time Complexity:");
        System.out.println("  Operations performed: " + operationCount);
        System.out.println("  Time taken: " + getTimeTaken() + " milliseconds");
        System.out.println("\nSpace Complexity:");
        System.out.println("  Maximum recursion depth: " + maxRecursionDepth);
        System.out.println("  Memory used: " + getFormattedMemoryUsed());
        System.out.println("  Theoretical space complexity: O(1) - constant for 9x9 grid (81 cells)");
    }
    
    /**
     * Gets the number of operations performed.
     * 
     * @return The operation count
     */
    public long getOperationCount() {
        return operationCount;
    }
    
    /**
     * Gets the time taken in milliseconds.
     * 
     * @return The time in milliseconds
     */
    public long getTimeTaken() {
        return endTime - startTime;
    }
    
    /**
     * Gets the maximum recursion depth reached.
     * 
     * @return The maximum recursion depth
     */
    public int getMaxRecursionDepth() {
        return maxRecursionDepth;
    }
    
    /**
     * Gets the memory used in bytes.
     * 
     * @return Memory used in bytes
     */
    public long getMemoryUsed() {
        return Math.max(memoryAfter - memoryBefore, peakMemoryUsage);
    }
    
    /**
     * Gets the formatted memory usage string.
     * 
     * @return Formatted memory usage
     */
    public String getFormattedMemoryUsed() {
        return formatMemorySize(getMemoryUsed());
    }
    
    /**
     * Formats memory size in bytes to a more readable format (KB, MB).
     * 
     * @param bytes Memory size in bytes
     * @return Formatted string representing memory size
     */
    private String formatMemorySize(long bytes) {
        if (bytes < 1024) {
            return bytes + " bytes";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        }
    }
    
    /**
     * Checks if the solver has exceeded the time limit.
     * 
     * @param timeoutMillis The timeout in milliseconds
     * @return true if the time limit has been exceeded, false otherwise
     */
    public boolean hasExceededTimeLimit(long timeoutMillis) {
        return System.currentTimeMillis() - startTime > timeoutMillis;
    }
}
