package players;

import game.*;
import java.util.List;

public class Player20240808701 extends Player {
    public Player20240808701(Board board) {
        super(board);
    }

    @Override
    public Move nextMove() {
        List<Move> moves = board.getPossibleMoves();
        if (moves.isEmpty()) return null;

        Move bestMove = null;
        double maxHeuristic = -Double.MAX_VALUE;

        for (Move move : moves) {
            // 1. Hücre değerini ve hedef konumu al
            int stepSize = getJumpDistance(move);
            int targetRow = board.getPlayerRow() + move.getDRow() * stepSize;
            int targetCol = board.getPlayerCol() + move.getDCol() * stepSize;

            if (!isPositionValid(targetRow, targetCol)) {
                continue;
            }

            int currentScore = board.getValueAt(targetRow, targetCol);
            
            // 2. Gelecek hamleleri simüle et
            Board simulatedBoard = new Board(board);
            simulatedBoard.applyMove(move);
            double futureScore = simulateFutureMoves(simulatedBoard, 2);

            // 3. Heuristic hesapla
            double heuristic = currentScore + (futureScore * Math.sqrt(moves.size()));

            if (heuristic > maxHeuristic) {
                maxHeuristic = heuristic;
                bestMove = move;
            }
        }
        return bestMove;
    }

    // ✅ EKSİK METODU EKLEDİK
    private double simulateFutureMoves(Board board, int depth) {
        if (depth == 0) return 0;
        
        List<Move> futureMoves = board.getPossibleMoves();
        if (futureMoves.isEmpty()) return 0;

        double totalScore = 0;
        for (Move move : futureMoves) {
            int stepSize = getJumpDistance(move);
            int targetRow = board.getPlayerRow() + move.getDRow() * stepSize;
            int targetCol = board.getPlayerCol() + move.getDCol() * stepSize;
            
            if (!isPositionValid(targetRow, targetCol)) {
                continue;
            }

            Board newSimulation = new Board(board);
            newSimulation.applyMove(move);
            int score = board.getValueAt(targetRow, targetCol);
            totalScore += score + simulateFutureMoves(newSimulation, depth - 1);
        }
        return totalScore / futureMoves.size();
    }

    private int getJumpDistance(Move move) {
        int nextRow = board.getPlayerRow() + move.getDRow();
        int nextCol = board.getPlayerCol() + move.getDCol();
        if (!isPositionValid(nextRow, nextCol)) {
            return 0;
        }
        return board.getValueAt(nextRow, nextCol);
    }

    private boolean isPositionValid(int row, int col) {
        return row >= 0 && row < board.getSize() && col >= 0 && col < board.getSize();
    }
}