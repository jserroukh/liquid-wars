package model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Collections;

/**
 * Représente l'état complet d'une partie (grille, joueurs, gradients, énergie, etc).
 */
public class GameState {
    private final Grid grid;
    private final List<Player> players;
    private final List<GradientField> gradientFields;
    private boolean gameRunning;
    private int totalEnergy;
    private final int[] teamPools;

    /**
     * Initialise l'état du jeu avec la grille, les joueurs et les particules.
     */
    public GameState(int width, int height, int numPlayers, int particlesPerPlayer) {
        this.grid = new Grid(width, height);
        this.players = new CopyOnWriteArrayList<>();
        this.gradientFields = new ArrayList<>();
        this.gameRunning = true;
        this.totalEnergy = 0;
        this.teamPools = new int[numPlayers];
        for (int i = 0; i < teamPools.length; i++)
            teamPools[i] = 0;

        initializePlayers(numPlayers);
        initializeParticles(particlesPerPlayer);
        initializeGradientFields();
        recalcTotalEnergy();
    }

    // Crée les joueurs et place leur cible au centre
    private void initializePlayers(int numPlayers) {
        for (int i = 0; i < numPlayers; i++) {
            Player player = new Player(i, i == 0);
            players.add(player);

            player.setTargetX(grid.getWidth() / 2);
            player.setTargetY(grid.getHeight() / 2);
        }
    }

    // Place les particules de chaque joueur sur la grille
    private void initializeParticles(int particlesPerPlayer) {
        int gridWidth = grid.getWidth();
        int gridHeight = grid.getHeight();

        for (Player player : players) {
            int baseX, baseY;
            switch (player.getId()) {
                case 0 -> {
                    baseX = gridWidth / 4;
                    baseY = 3 * gridHeight / 4;
                }
                case 1 -> {
                    baseX = 3 * gridWidth / 4;
                    baseY = gridHeight / 4;
                }
                case 2 -> {
                    baseX = 3 * gridWidth / 4;
                    baseY = 3 * gridHeight / 4;
                }
                case 3 -> {
                    baseX = gridWidth / 4;
                    baseY = gridHeight / 4;
                }
                default -> {
                    baseX = gridWidth / 2;
                    baseY = gridHeight / 2;
                }
            }

            int created = spawnPlayerParticlesDeterministic(player.getId(), particlesPerPlayer, baseX, baseY);

            System.out.printf("Joueur %d (%s): %d particules créées (%d,%d)%n",
                    player.getId(), player.isHuman() ? "Humain" : "IA", created, baseX, baseY);
        }
    }

    private int spawnPlayerParticlesDeterministic(int playerId, int count, int baseX, int baseY) {
        int w = grid.getWidth();
        int h = grid.getHeight();

        // 1) On tente d'abord dans une zone locale autour de la base
        int radius = 6; // ajuste si besoin (plus grand = plus facile de placer)
        List<int[]> empty = new ArrayList<>();

        for (int x = Math.max(1, baseX - radius); x <= Math.min(w - 2, baseX + radius); x++) {
            for (int y = Math.max(1, baseY - radius); y <= Math.min(h - 2, baseY + radius); y++) {
                if (grid.getCell(x, y) == Cell.EMPTY) {
                    empty.add(new int[] { x, y });
                }
            }
        }

        // 2) Fallback global si la zone est trop petite / bloquée par obstacles
        if (empty.size() < count) {
            empty.clear();
            for (int x = 1; x < w - 1; x++) {
                for (int y = 1; y < h - 1; y++) {
                    if (grid.getCell(x, y) == Cell.EMPTY) {
                        empty.add(new int[] { x, y });
                    }
                }
            }
        }

        if (empty.size() < count) {
            throw new IllegalStateException(
                    "Impossible de placer " + count + " particules pour le joueur " + playerId +
                            " (cases vides disponibles=" + empty.size() + ")");
        }

        Collections.shuffle(empty);

        for (int i = 0; i < count; i++) {
            int[] pos = empty.get(i);
            Particle p = new Particle(playerId, pos[0], pos[1]);
            grid.addParticle(p);
        }

        return count;
    }

    // Crée un champ de gradients pour chaque joueur
    private void initializeGradientFields() {
        for (Player player : players) {
            gradientFields.add(new GradientField(player.getId(), grid.getWidth(), grid.getHeight()));
        }
    }

    // Recalcule l'énergie totale de la partie
    private void recalcTotalEnergy() {
        totalEnergy = 0;
        for (Particle p : grid.getParticles())
            totalEnergy += p.getEnergy();
    }

    /**
     * À appeler à chaque frame : énergie totale doit rester constante si règles
     * correctes.
     */
    public synchronized void update() {
        recalcTotalEnergy();

        // Victoire: nombre de joueurs ayant au moins 1 particule > 0
        int joueursAvecParticules = 0;
        for (Player pl : players) {
            boolean aDesParticules = grid.getParticles().stream()
                    .anyMatch(p -> p.getPlayerId() == pl.getId());
            if (aDesParticules)
                joueursAvecParticules++;
        }

        if (joueursAvecParticules <= 1) {
            gameRunning = false;
        }
    }

    public Grid getGrid() {
        return grid;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public List<GradientField> getGradientFields() {
        return gradientFields;
    }

    public boolean isGameRunning() {
        return gameRunning;
    }

    public int getTotalEnergy() {
        return totalEnergy;
    }

    public Player getPlayer(int id) {
        return players.stream().filter(p -> p.getId() == id).findFirst().orElse(null);
    }

    public GradientField getGradientFieldForPlayer(int playerId) {
        return gradientFields.stream().filter(gf -> gf.getPlayerId() == playerId).findFirst().orElse(null);
    }

    public int computeTotalEnergy() {
        int sum = 0;
        for (Particle p : grid.getParticles())
            sum += p.getEnergy();
        return sum;
    }

    public int countParticles() {
        return grid.getParticles().size();
    }

    public int getPool(int playerId) {
        return teamPools[playerId];
    }

    public void addToPool(int playerId, int amount) {
        if (amount <= 0)
            return;
        teamPools[playerId] += amount;
    }

    public int takeFromPool(int playerId, int amount) {
        int take = Math.min(amount, teamPools[playerId]);
        teamPools[playerId] -= take;
        return take;
    }
}