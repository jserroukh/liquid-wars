package model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Représente la grille du jeu, avec obstacles et particules.
 */
public class Grid {

    private final int width;
    private final int height;
    private final Cell[][] cells;
    private final List<Particle> particles;
    private final Random random;

    /** Constructeur NORMAL (jeu réel) : obstacles + random */
    /**
     * Crée une grille normale avec obstacles.
     */
    public Grid(int width, int height) {
        this(width, height, true);
    }

    /**
     * Constructeur TESTABLE
     * @param withObstacles true = comportement normal du jeu
     *                      false = grille vide (tests)
     */
    /**
     * Crée une grille testable (avec ou sans obstacles).
     */
    public Grid(int width, int height, boolean withObstacles) {
        this.width = width;
        this.height = height;
        this.cells = new Cell[width][height];
        this.particles = new CopyOnWriteArrayList<>();
        this.random = new Random();

        // Initialisation vide
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                cells[x][y] = Cell.EMPTY;
            }
        }

        if (withObstacles) {
            addStructuredObstacles();
        }
    }

    /*OBSTACLES*/

    // Ajoute des obstacles structurés sur la grille
    private void addStructuredObstacles() {
        if (width <= 10 || height <= 10) return;

        // Bordures
        for (int x = 0; x < width; x++) {
            cells[x][0] = Cell.OBSTACLE;
            cells[x][height - 1] = Cell.OBSTACLE;
        }
        for (int y = 0; y < height; y++) {
            cells[0][y] = Cell.OBSTACLE;
            cells[width - 1][y] = Cell.OBSTACLE;
        }

        int centerX = width / 2;
        int centerY = height / 2;
        createSquareObstacle(centerX - 5, centerY - 5, 10, 10);

        createSquareObstacle(width / 4, height / 4, 8, 8);
        createSquareObstacle(3 * width / 4, height / 4, 8, 8);
        createSquareObstacle(width / 4, 3 * height / 4, 8, 8);
        createSquareObstacle(3 * width / 4, 3 * height / 4, 8, 8);

        for (int i = 0; i < 15; i++) {
            int x = random.nextInt(width - 10) + 5;
            int y = random.nextInt(height - 10) + 5;
            int size = random.nextInt(5) + 3;
            createSquareObstacle(x, y, size, size);
        }
    }

    private void createSquareObstacle(int startX, int startY, int w, int h) {
        for (int x = startX; x < startX + w && x < width; x++) {
            for (int y = startY; y < startY + h && y < height; y++) {
                if (x >= 0 && y >= 0) {
                    cells[x][y] = Cell.OBSTACLE;
                }
            }
        }
    }

    /*PARTICULES*/

    public synchronized void addParticle(Particle particle) {
        int x = particle.getX();
        int y = particle.getY();

        if (!isValidPosition(x, y)) return;
        if (cells[x][y] != Cell.EMPTY) return;

        cells[x][y] = Cell.PARTICLE;
        particles.add(particle);
    }

    public synchronized boolean moveParticle(Particle particle, int newX, int newY) {
        if (!isValidPosition(newX, newY)) return false;
        if (cells[newX][newY] != Cell.EMPTY) return false;

        cells[particle.getX()][particle.getY()] = Cell.EMPTY;
        cells[newX][newY] = Cell.PARTICLE;

        particle.setX(newX);
        particle.setY(newY);
        return true;
    }

    public synchronized void removeParticle(Particle particle) {
        int x = particle.getX();
        int y = particle.getY();

        if (isValidPosition(x, y) && cells[x][y] == Cell.PARTICLE) {
            cells[x][y] = Cell.EMPTY;
        }
        particles.remove(particle);
    }

    public synchronized void updateParticleCell(Particle particle) {
        if (isValidPosition(particle.getX(), particle.getY())) {
            cells[particle.getX()][particle.getY()] = Cell.PARTICLE;
        }
    }

    /* =======================
       ACCESSEURS
       ======================= */

    public Cell getCell(int x, int y) {
        if (!isValidPosition(x, y)) return Cell.OBSTACLE;
        return cells[x][y];
    }

    public void setCell(int x, int y, Cell cell) {
        if (isValidPosition(x, y)) cells[x][y] = cell;
    }

    public boolean isValidPosition(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public List<Particle> getParticles() {
        return particles;
    }

    public List<Particle> getParticlesAt(int x, int y) {
        List<Particle> result = new ArrayList<>();
        for (Particle p : particles) {
            if (p.getX() == x && p.getY() == y) result.add(p);
        }
        return result;
    }

    /*UTILITAIRE IA*/

    public int[] trouverCaseLibreLaPlusProche(int startX, int startY) {
        if (!isValidPosition(startX, startY)) return null;
        if (getCell(startX, startY) != Cell.OBSTACLE) return new int[]{startX, startY};

        boolean[][] seen = new boolean[width][height];
        ArrayDeque<int[]> q = new ArrayDeque<>();
        q.add(new int[]{startX, startY});
        seen[startX][startY] = true;

        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
        while (!q.isEmpty()) {
            int[] cur = q.poll();
            for (int[] d : dirs) {
                int nx = cur[0] + d[0];
                int ny = cur[1] + d[1];
                if (!isValidPosition(nx, ny) || seen[nx][ny]) continue;
                seen[nx][ny] = true;

                if (getCell(nx, ny) != Cell.OBSTACLE) return new int[]{nx, ny};
                q.add(new int[]{nx, ny});
            }
        }
        return null;
    }
    public boolean[][] snapshotObstacles() {
    boolean[][] obs = new boolean[width][height];
    for (int x = 0; x < width; x++) {
        for (int y = 0; y < height; y++) {
            obs[x][y] = (cells[x][y] == Cell.OBSTACLE);
        }
    }
    return obs;
}

}
