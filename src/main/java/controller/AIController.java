package controller;

import model.GameState;
import model.Player;
import model.Particle;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Contrôleur pour l'IA : gère les décisions des joueurs non humains.
 */
public class AIController {
    private final GameState gameState;
    private int updateCounter = 0;
    private static final int UPDATE_FREQUENCY = 10;

    /**
     * Initialise le contrôleur IA avec l'état du jeu.
     */
    public AIController(GameState gameState) {
        this.gameState = gameState;
    }

    // Met à jour l'IA à intervalles réguliers
    public void update() {
        updateCounter++;

        if (updateCounter % UPDATE_FREQUENCY == 0) {
            for (Player player : gameState.getPlayers()) {
                if (!player.isHuman()) {
                    updateAIPlayer(player);
                }
            }
        }
    }

    // Met à jour la cible d'un joueur IA en fonction des ennemis
    private void updateAIPlayer(Player player) {
        // Particules ennemies (sans isAlive)
        List<Particle> enemyParticles = gameState.getGrid().getParticles().stream()
                .filter(p -> p.getPlayerId() != player.getId())
                .toList();

        if (!enemyParticles.isEmpty()) {
            int currentX = player.getTargetX();
            int currentY = player.getTargetY();

            Particle closestEnemy = null;
            int bestDist2 = Integer.MAX_VALUE;

            for (Particle enemy : enemyParticles) {
                int dx = enemy.getX() - currentX;
                int dy = enemy.getY() - currentY;
                int dist2 = dx * dx + dy * dy;

                if (dist2 < bestDist2) {
                    bestDist2 = dist2;
                    closestEnemy = enemy;
                }
            }

            if (closestEnemy != null) {
                int targetX = closestEnemy.getX();
                int targetY = closestEnemy.getY();

                // Petit offset aléatoire
                if (ThreadLocalRandom.current().nextDouble() < 0.3) {
                    targetX += ThreadLocalRandom.current().nextInt(-2, 3);
                    targetY += ThreadLocalRandom.current().nextInt(-2, 3);
                }

                // Clamp dans la grille (évite les bords obstacles)
                int gridWidth = gameState.getGrid().getWidth();
                int gridHeight = gameState.getGrid().getHeight();
                targetX = Math.max(1, Math.min(gridWidth - 2, targetX));
                targetY = Math.max(1, Math.min(gridHeight - 2, targetY));

                if (gameState.getGrid().getCell(targetX, targetY) != model.Cell.EMPTY) {
                    int[] libre = gameState.getGrid().trouverCaseLibreLaPlusProche(targetX, targetY);
                    if (libre != null) {
                        targetX = libre[0];
                        targetY = libre[1];
                    }
                }

                player.setTargetX(targetX);
                player.setTargetY(targetY);

                if (updateCounter % 100 == 0) {
                    System.out.printf("IA %d → cible (%d,%d)%n", player.getId(), targetX, targetY);
                }
            }
        } else {
            exploreRandomly(player);
        }
    }

    private void exploreRandomly(Player player) {
        if (ThreadLocalRandom.current().nextDouble() < 0.4) {
            int currentX = player.getTargetX();
            int currentY = player.getTargetY();

            int newX = currentX + ThreadLocalRandom.current().nextInt(-3, 4);
            int newY = currentY + ThreadLocalRandom.current().nextInt(-3, 4);

            int gridWidth = gameState.getGrid().getWidth();
            int gridHeight = gameState.getGrid().getHeight();
            newX = Math.max(1, Math.min(gridWidth - 2, newX));
            newY = Math.max(1, Math.min(gridHeight - 2, newY));

            // éviter obstacles
            if (gameState.getGrid().getCell(newX, newY) != model.Cell.EMPTY) {
                int[] libre = gameState.getGrid().trouverCaseLibreLaPlusProche(newX, newY);
                if (libre != null) {
                    newX = libre[0];
                    newY = libre[1];
                } else {
                    return; // abandonner ce tick, garde l'ancienne cible
                }
            }

            player.setTargetX(newX);
            player.setTargetY(newY);
        }
    }
}
