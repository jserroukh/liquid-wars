package controller;

import engine.GradientCalculator;
import engine.MovementResolver;
import model.GameState;
import model.GradientField;
import model.Player;
import view.GameView;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Boucle principale du jeu : met à jour l'état, l'IA et l'affichage à chaque frame.
 */
public class GameLoop {
    private final GameState gameState;
    private final GameView gameView;
    private final AIController aiController;
    private final GradientCalculator gradientCalculator;
    private final MovementResolver movementResolver;

    // Thread de boucle
    private final ScheduledExecutorService loopExecutor;

    // Pool gradients
    private final ExecutorService gradientExecutor;

    private static final int FPS = 60;
    private static final long FRAME_MS = 1000L / FPS;

    private volatile boolean running = true;
    private long frameCount = 0;

    // Futures du batch gradient en cours
    private volatile List<Future<Void>> gradientBatch = null;

    /**
     * Initialise la boucle de jeu avec l'état et la vue.
     */
    public GameLoop(GameState gameState, GameView gameView) {
        this.gameState = gameState;
        this.gameView = gameView;
        this.aiController = new AIController(gameState);
        this.gradientCalculator = new GradientCalculator();
        this.movementResolver = new MovementResolver();

        // Important: 1 seul thread pour la logique
        this.loopExecutor = Executors.newSingleThreadScheduledExecutor();

        // Pool dédié pour gradients
        int threads = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
        this.gradientExecutor = Executors.newFixedThreadPool(threads);
    }

    // Démarre la boucle de jeu (appelée à chaque frame)
    public void start() {
        System.out.println("Démarrage de la boucle de jeu à " + FPS + " FPS");
        // Avec fixedDelay: pas d'empilement si une frame prend plus longtemps
        loopExecutor.scheduleWithFixedDelay(this::gameUpdate, 0, FRAME_MS, TimeUnit.MILLISECONDS);
    }

    // Met à jour la logique du jeu à chaque frame
    private void gameUpdate() {
        if (!running || !gameState.isGameRunning()) {
            stop();
            return;
        }

        try {
            frameCount++;
            // 0) Cible du joueur humain (thread logique uniquement)
            updateHumanTargetFromMouse();

            // 1) IA (séquentiel)
            aiController.update();

            // 2) Si le batch de gradients précédent est terminé, on le "valide"
            // (ici, on ne swap rien: tes GradientField sont directement écrits,
            // donc la "validation" = s'assurer que c'est fini avant d'utiliser
            // des gradients potentiellement en cours d'écriture)
            boolean gradientsReady = isGradientBatchDone(gradientBatch);

            // 3) Si aucun batch en cours (ou terminé), on lance un nouveau batch EN
            // BACKGROUND
            if (gradientBatch == null || gradientsReady) {
                gradientBatch = submitGradientBatch();
                gradientsReady = false; // ils viennent de repartir
            }

            // 4) Mouvement: on utilise les gradients disponibles.
            // Si un batch est en cours, on utilise ceux de la frame précédente.
            // (On ne bloque jamais.)
            movementResolver.resolve(gameState);

            // 5) Update état
            gameState.update();

            /*DEBUG ÉNERGIE */
            if (frameCount % 60 == 0) {
                int total = gameState.computeTotalEnergy();
                System.out.println("[DEBUG] TOTAL ENERGY = " + total);
            }

            if (frameCount % 40 == 0) { // ~1s à 40 FPS
                int eParticles = gameState.computeTotalEnergy();
                int ePools = 0;
                for (var pl : gameState.getPlayers()) {
                    ePools += gameState.getPool(pl.getId());
                }
                System.out.println(
                        "[CHECK] particles=" + eParticles + " pools=" + ePools + " TOTAL=" + (eParticles + ePools));
            }
            // 6) Rendu
            if (frameCount % 2 == 0) {
                SwingUtilities.invokeLater(() -> gameView.update(gameState));
            }

            if (frameCount % 120 == 0) {
                printDebugStats();
            }

        } catch (Exception e) {
            System.err.println("Erreur dans la boucle de jeu:");
            e.printStackTrace();
            stop();
        }
    }

    private List<Future<Void>> submitGradientBatch() {
        List<Future<Void>> futures = new ArrayList<>();
        for (Player player : gameState.getPlayers()) {
            GradientField field = gameState.getGradientFieldForPlayer(player.getId());
            if (field == null)
                continue;

            int tx = player.getTargetX();
            int ty = player.getTargetY();

            futures.add(gradientExecutor.submit(() -> {
                // IMPORTANT: Grid en lecture seule pendant ce calcul (pas de move ici)
                gradientCalculator.calculateGradient(field, gameState.getGrid(), tx, ty);
                return null;
            }));
        }
        return futures;
    }

    private boolean isGradientBatchDone(List<Future<Void>> batch) {
        if (batch == null)
            return true;
        for (Future<Void> f : batch) {
            if (!f.isDone())
                return false;
            try {
                f.get(); // remonte les exceptions si une tâche a crash
            } catch (Exception e) {
                System.err.println("Erreur dans une tâche de gradient:");
                e.printStackTrace();
                return true; // on considère fini pour relancer un batch propre
            }
        }
        return true;
    }

    private void updateHumanTargetFromMouse() {
        Player human = gameState.getPlayers().stream().filter(Player::isHuman).findFirst().orElse(null);
        if (human == null)
            return;

        // Lire la souris depuis la vue (read-only). Point est immutable-ish ici.
        java.awt.Point mouse = gameView.getMousePositionInGrid();

        // Clamp + trouver case libre si obstacle/occupée
        int mx = mouse.x;
        int my = mouse.y;

        int[] libre = gameState.getGrid().trouverCaseLibreLaPlusProche(mx, my);
        if (libre != null) {
            human.setTargetX(libre[0]);
            human.setTargetY(libre[1]);
        }
    }

    private void printDebugStats() {
        System.out.println("\n=== STATS (" + frameCount + " frames) ===");
        for (Player player : gameState.getPlayers()) {
            long particleCount = gameState.getGrid().getParticles().stream()
                    .filter(p -> p.getPlayerId() == player.getId())
                    .count();
            System.out.printf("Joueur %d: %d particules, cible: (%d,%d)%n",
                    player.getId(), particleCount, player.getTargetX(), player.getTargetY());
        }
    }

    // Arrête la boucle de jeu
    public void stop() {
        running = false;
        loopExecutor.shutdownNow();
        gradientExecutor.shutdownNow();
    }
}

