package players;

import game.*;
import java.util.*;

public class Player20240808701 extends Player {
    // Depth limits for different board sizes
    private static final int DEPTH_LIMIT_10x10 = 5;  // Deeper search for small boards
    private static final int DEPTH_LIMIT_25x25 = 5;  // Medium search depth
    private static final int TIME_LIMIT_MS = 900;    // Max time per move (ms)

    public Player20240808701(Board board) {
        super(board);
    }
    //odev
    @Override
    public Move nextMove() {
        int size = board.getSize();
        
        // Strategy selection based on board size
        if (size <= 10) {
            return depthLimitedSearch(DEPTH_LIMIT_10x10);  // Deep search for small boards
        } else if (size <= 25) {
            return depthLimitedSearch(DEPTH_LIMIT_25x25);  // Medium search for medium boards
        } else {
            return largeGridStrategy();  // Optimized strategy for large boards
        }
    }

    /**
     * Performs depth-limited search for optimal move
     * @param maxDepth How many moves ahead to look
     * @return Best found move
     */
    private Move depthLimitedSearch(int maxDepth) {
        List<Move> moves = board.getPossibleMoves();
        if (moves.isEmpty()) return null;

        // Sort moves by jump distance (highest first)
        moves.sort((m1, m2) -> Integer.compare(getJumpDistance(m2), getJumpDistance(m1)));

        Move bestMove = moves.get(0);  // Default to first move if all evaluations fail
        double maxScore = -Double.MAX_VALUE;

        // Evaluate each possible move
        for (Move move : moves) {
            Board simulated = new Board(board);
            simulated.applyMove(move);
            
            double score = evaluateMove(move, simulated, maxDepth);
            
            if (score > maxScore) {
                maxScore = score;
                bestMove = move;
            }
        }
        
        return bestMove;
    }

    /**
     * Recursively evaluates a move's potential
     * @param move Current move being evaluated
     * @param board Current board state
     * @param depth Remaining search depth
     * @return Evaluation score
     */
    private double evaluateMove(Move move, Board board, int depth) {
        // Base case: return current score when depth limit reached
        if (depth == 0) {
            return board.getScore();
        }

        List<Move> futureMoves = board.getPossibleMoves();
        if (futureMoves.isEmpty()) return 0;  // No moves means game over

        // Sort future moves by potential
        futureMoves.sort((m1, m2) -> Integer.compare(getJumpDistance(m2, board), getJumpDistance(m1, board)));

        double totalScore = 0;
        int evaluated = 0;
        
        // Evaluate top 3 future moves to save computation time
        for (int i = 0; i < Math.min(3, futureMoves.size()); i++) {
            Move futureMove = futureMoves.get(i);
            Board newSim = new Board(board);
            newSim.applyMove(futureMove);
            
            totalScore += evaluateMove(futureMove, newSim, depth - 1);
            evaluated++;
        }
        
        return evaluated > 0 ? (totalScore / evaluated) : 0;  // Average score of evaluated paths
    }

    /**
     * Optimized strategy for large grids (50x50+)
     * Focuses on immediate gains with limited lookahead
     * @return Best move found
     */
    private Move largeGridStrategy() {
        List<Move> moves = board.getPossibleMoves();
        if (moves.isEmpty()) return null;

        Move bestMove = null;
        double maxHeuristic = -Double.MAX_VALUE;

        // Evaluate each move using heuristic function
        for (Move move : moves) {
            int stepSize = getJumpDistance(move);
            int targetRow = board.getPlayerRow() + move.getDRow() * stepSize;
            int targetCol = board.getPlayerCol() + move.getDCol() * stepSize;

            if (!isPositionValid(targetRow, targetCol)) {
                continue;  // Skip invalid positions
            }

            int currentScore = board.getValueAt(targetRow, targetCol);
            
            // Simulate future moves to estimate potential
            Board simulatedBoard = new Board(board);
            simulatedBoard.applyMove(move);
            double futureScore = simulateFutureMoves(simulatedBoard, 2);

            // Heuristic: current score + weighted future potential
            double heuristic = currentScore + (futureScore * Math.sqrt(moves.size()));

            if (heuristic > maxHeuristic) {
                maxHeuristic = heuristic;
                bestMove = move;
            }
        }
        return bestMove;
    }

    /**
     * Simulates future moves to estimate board potential
     * @param board Current board state
     * @param depth How many moves to look ahead
     * @return Estimated future score potential
     */
    private double simulateFutureMoves(Board board, int depth) {
        if (depth == 0) return 0;  // Base case
        
        List<Move> futureMoves = board.getPossibleMoves();
        if (futureMoves.isEmpty()) return 0;  // Game over

        double totalScore = 0;
        int validMoves = 0;
        
        // Evaluate all possible future moves
        for (Move move : futureMoves) {
            int stepSize = getJumpDistance(move, board);
            int targetRow = board.getPlayerRow() + move.getDRow() * stepSize;
            int targetCol = board.getPlayerCol() + move.getDCol() * stepSize;
            
            if (!isPositionValid(targetRow, targetCol, board)) {
                continue;  // Skip invalid moves
            }

            Board newSimulation = new Board(board);
            newSimulation.applyMove(move);
            int score = board.getValueAt(targetRow, targetCol);
            totalScore += score + simulateFutureMoves(newSimulation, depth - 1);
            validMoves++;
        }
        return validMoves > 0 ? (totalScore / validMoves) : 0;  // Average potential
    }

    // Helper method to get jump distance for current board
    private int getJumpDistance(Move move) {
        return getJumpDistance(move, this.board);
    }

    // Calculates jump distance for a move on given board
    private int getJumpDistance(Move move, Board board) {
        int nextRow = board.getPlayerRow() + move.getDRow();
        int nextCol = board.getPlayerCol() + move.getDCol();
        return isPositionValid(nextRow, nextCol, board) ? 
               board.getValueAt(nextRow, nextCol) : 0;  // 0 for invalid moves
    }

    // Position validation for current board
    private boolean isPositionValid(int row, int col) {
        return isPositionValid(row, col, this.board);
    }

    // Checks if position is within board boundaries
    private boolean isPositionValid(int row, int col, Board board) {
        return row >= 0 && row < board.getSize() && col >= 0 && col < board.getSize();
    }
}