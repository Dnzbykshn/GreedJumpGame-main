package players;

import game.*;
import java.util.*;
import java.util.concurrent.*;

public class Player20240808701 extends Player {
    // Configuration
    private static final int MAX_DEPTH = 6;
    private static final int MONTE_CARLO_SIMS = 200;
    private static final double TIME_LIMIT = 0.95;
    
    private final ExecutorService executor = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors());
    private long startTime;

    public Player20240808701(Board board) {
        super(board);
    }

    @Override
    public Move nextMove() {
        startTime = System.currentTimeMillis();
        List<Move> possibleMoves = board.getPossibleMoves();
        if (possibleMoves.isEmpty()) return null;

        // Evaluate all moves in parallel
        List<Future<MoveEval>> futures = new ArrayList<>();
        for (Move move : possibleMoves) {
            futures.add(executor.submit(() -> evaluateMove(move)));
        }

        // Find best move from results
        Move bestMove = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        try {
            for (Future<MoveEval> future : futures) {
                if (timeLeft() <= 0) break;
                MoveEval result = future.get(timeLeft(), TimeUnit.MILLISECONDS);
                if (result.score > bestScore) {
                    bestScore = result.score;
                    bestMove = result.move;
                }
            }
        } catch (Exception e) {
            System.err.println("Error during evaluation: " + e.getMessage());
        }

        return bestMove != null ? bestMove : possibleMoves.get(0);
    }

    // Combines strategic and random simulations to score a move
    private MoveEval evaluateMove(Move move) {
        Board newBoard = new Board(board);
        newBoard.applyMove(move);
        double score = alphaBeta(newBoard, MAX_DEPTH, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)
                    + monteCarloSimulation(newBoard);
        return new MoveEval(move, score);
    }

    // Minimax algorithm with pruning
    private double alphaBeta(Board board, int depth, double alpha, double beta) {
        if (depth == 0 || board.isGameOver() || timeLeft() <= 0) {
            return evaluateBoardState(board);
        }

        double value = Double.NEGATIVE_INFINITY;
        for (Move move : board.getPossibleMoves()) {
            Board newBoard = new Board(board);
            newBoard.applyMove(move);
            value = Math.max(value, alphaBeta(newBoard, depth - 1, alpha, beta));
            alpha = Math.max(alpha, value);
            if (alpha >= beta) break; // Prune
        }
        return value;
    }

    // Simulates random games from current position
    private double monteCarloSimulation(Board board) {
        double totalScore = 0;
        int simulations = 0;
        while (simulations < MONTE_CARLO_SIMS && timeLeft() > 0) {
            Board simBoard = new Board(board);
            while (!simBoard.isGameOver() && timeLeft() > 0) {
                List<Move> moves = simBoard.getPossibleMoves();
                simBoard.applyMove(moves.get(ThreadLocalRandom.current().nextInt(moves.size())));
            }
            totalScore += simBoard.getScore();
            simulations++;
        }
        return simulations > 0 ? (totalScore / simulations) : 0;
    }

    // Scores board based on current progress, mobility and position
    private double evaluateBoardState(Board board) {
        return board.getScore() * 0.6 + 
               board.getPossibleMoves().size() * 0.3 + 
               (1 - (distanceFromCenter(board) / maxDistance(board))) * 0.1;
    }

    // Helper methods
    private double distanceFromCenter(Board board) {
        int center = board.getSize() / 2;
        int row = board.getPlayerRow();
        int col = board.getPlayerCol();
        return Math.abs(row - center) + Math.abs(col - center);
    }
    
    private double maxDistance(Board board) {
        return board.getSize();
    }
    
    private long timeLeft() {
        return (long)(TIME_LIMIT * 1000) - (System.currentTimeMillis() - startTime);
    }

    // Simple struct to hold move-score pairs
    private static class MoveEval {
        final Move move;
        final double score;
        MoveEval(Move move, double score) {
            this.move = move;
            this.score = score;
        }
    }
}