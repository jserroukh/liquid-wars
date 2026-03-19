package view;

import model.GameState;
import model.Player;
import model.Particle;
import model.Cell;
import model.Grid;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class GameView extends JPanel {
    private final int cellSize;
    private final int gridWidth;
    private final int gridHeight;
    private GameState gameState;
    private Point mousePosition;
    private boolean showGrid = false;

    /**
     * Vue principale du jeu : affiche la grille, les particules, les joueurs et l'interface.
     */
    private final Color BACKGROUND_COLOR = new Color(10, 10, 20); // Noir bleuté
    private final Color GRID_COLOR = new Color(40, 40, 60, 50); // Bleu foncé transparent
    private final Color OBSTACLE_COLOR = new Color(30, 30, 40); // Gris foncé
    private final Color OBSTACLE_HIGHLIGHT = new Color(60, 60, 80); // Gris clair pour relief
    private final Color TEXT_COLOR = new Color(220, 220, 255); // Blanc bleuté

    // Étoiles pour fond
    private final int[] starX;
    private final int[] starY;
    private final float[] starBrightness;

    /**
     * Initialise la vue du jeu avec la taille de la grille et des cellules.
     */
    public GameView(int gridWidth, int gridHeight, int cellSize) {
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.cellSize = cellSize;
        this.mousePosition = new Point(gridWidth * cellSize / 2, gridHeight * cellSize / 2);

        setPreferredSize(new Dimension(gridWidth * cellSize, gridHeight * cellSize));
        setBackground(BACKGROUND_COLOR);

        // Initialiser les étoiles
        starX = new int[100];
        starY = new int[100];
        starBrightness = new float[100];
        for (int i = 0; i < 100; i++) {
            starX[i] = (int) (Math.random() * gridWidth * cellSize);
            starY[i] = (int) (Math.random() * gridHeight * cellSize);
            starBrightness[i] = (float) (Math.random() * 0.8 + 0.2);
        }

        setupMouseListeners();
        setupKeyListeners();
    }

    // Ajoute les listeners souris pour déplacer la cible
    private void setupMouseListeners() {
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mousePosition = e.getPoint();
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                mouseMoved(e);
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mousePosition = e.getPoint();
                repaint();
            }
        });
    }

    // Ajoute le listener clavier pour les raccourcis (grille, quitter)
    private void setupKeyListeners() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_N:
                        showGrid = !showGrid;
                        repaint();
                        break;
                    case KeyEvent.VK_ESCAPE:
                        System.exit(0);
                        break;
                }
            }
        });

        setFocusable(true);
        // requestFocusInWindow(); // on évite de voler le focus au démarrage
    }

    // Retourne la position de la souris dans la grille
    public Point getMousePositionInGrid() {
        int x = mousePosition.x / cellSize;
        int y = mousePosition.y / cellSize;
        x = Math.max(0, Math.min(gridWidth - 1, x));
        y = Math.max(0, Math.min(gridHeight - 1, y));
        return new Point(x, y);
    }

    // Met à jour l'état du jeu et rafraîchit l'affichage
    public void update(GameState gameState) {
        this.gameState = gameState;
        repaint();
    }

    @Override
    // Dessine tout l'affichage du jeu (grille, particules, UI...)
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        if (gameState != null) {
            drawBackground(g2d);

            //

            if (showGrid) {
                drawGridLines(g2d);
            }

            drawObstacles(g2d);
            drawParticles(g2d);
            drawTargets(g2d);
            drawUI(g2d);
        }

        g2d.dispose();
    }

    // Dessine le fond étoilé
    private void drawBackground(Graphics2D g2d) {
        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(5, 5, 15),
                getWidth(), getHeight(), new Color(15, 15, 30));
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        for (int i = 0; i < 100; i++) {
            float brightness = starBrightness[i]
                    * (0.7f + 0.3f * (float) Math.sin(System.currentTimeMillis() / 1000.0 + i));
            g2d.setColor(new Color(1f, 1f, 1f, brightness * 0.5f));
            int starSize = (int) (brightness * 2);
            g2d.fillRect(starX[i], starY[i], starSize, starSize);
        }
    }

    // Dessine les lignes de la grille
    private void drawGridLines(Graphics2D g2d) {
        g2d.setColor(GRID_COLOR);
        g2d.setStroke(new BasicStroke(0.5f));

        for (int x = 0; x <= gridWidth; x++) {
            g2d.drawLine(x * cellSize, 0, x * cellSize, gridHeight * cellSize);
        }
        for (int y = 0; y <= gridHeight; y++) {
            g2d.drawLine(0, y * cellSize, gridWidth * cellSize, y * cellSize);
        }
    }

    // Dessine les obstacles sur la grille
    private void drawObstacles(Graphics2D g2d) {
        Grid grid = gameState.getGrid();

        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                if (grid.getCell(x, y) == Cell.OBSTACLE) {
                    int rectX = x * cellSize;
                    int rectY = y * cellSize;

                    g2d.setColor(OBSTACLE_COLOR);
                    g2d.fillRect(rectX, rectY, cellSize, cellSize);

                    g2d.setColor(OBSTACLE_HIGHLIGHT);
                    g2d.setStroke(new BasicStroke(1.5f));
                    g2d.drawRect(rectX, rectY, cellSize - 1, cellSize - 1);

                    g2d.setColor(new Color(20, 20, 30));
                    g2d.drawLine(rectX + 2, rectY + 2, rectX + cellSize - 3, rectY + cellSize - 3);
                    g2d.drawLine(rectX + cellSize - 3, rectY + 2, rectX + 2, rectY + cellSize - 3);

                    g2d.setColor(new Color(40, 40, 60));
                    g2d.fillOval(rectX + cellSize / 4, rectY + cellSize / 4, 2, 2);
                    g2d.fillOval(rectX + 3 * cellSize / 4 - 2, rectY + cellSize / 4, 2, 2);
                    g2d.fillOval(rectX + cellSize / 4, rectY + 3 * cellSize / 4 - 2, 2, 2);
                    g2d.fillOval(rectX + 3 * cellSize / 4 - 2, rectY + 3 * cellSize / 4 - 2, 2, 2);
                }
            }
        }
    }



    /** MODIF: on dessine toutes les particules (plus de isAlive()) */
    // Dessine toutes les particules du jeu
    private void drawParticles(Graphics2D g2d) {
        List<Particle> particles = gameState.getGrid().getParticles();

        for (Particle particle : particles) {
            Player player = gameState.getPlayer(particle.getPlayerId());
            if (player != null) {
                drawParticle(g2d, particle, player);
            }
        }
    }

    // Dessine une particule individuelle
    private void drawParticle(Graphics2D g2d, Particle particle, Player player) {
        int x = particle.getX() * cellSize;
        int y = particle.getY() * cellSize;
        float energyRatio = (float) particle.getEnergy() / Particle.MAX_ENERGY;

        Color baseColor = player.getColor();
        float brightness = 0.5f + 0.5f * energyRatio;
        float saturation = 0.7f + 0.3f * energyRatio;

        float[] hsb = Color.RGBtoHSB(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), null);
        Color particleColor = Color.getHSBColor(hsb[0], saturation, brightness);
        Color glowColor = new Color(
                particleColor.getRed(),
                particleColor.getGreen(),
                particleColor.getBlue(),
                100);

        g2d.setColor(glowColor);
        int glowSize = (int) (cellSize * 1.5);
        int glowX = x - (glowSize - cellSize) / 2;
        int glowY = y - (glowSize - cellSize) / 2;
        g2d.fillOval(glowX, glowY, glowSize, glowSize);

        g2d.setColor(particleColor);
        g2d.fillOval(x, y, cellSize, cellSize);

        g2d.setColor(particleColor.darker().darker());
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawOval(x, y, cellSize, cellSize);

        if (cellSize > 8) {
            GradientPaint innerGlow = new GradientPaint(
                    x, y, new Color(255, 255, 255, 100),
                    x + cellSize / 2, y + cellSize / 2, new Color(255, 255, 255, 0));
            g2d.setPaint(innerGlow);
            g2d.fillOval(x + cellSize / 4, y + cellSize / 4, cellSize / 2, cellSize / 2);
        }

        if (cellSize > 10) {
            g2d.setFont(new Font("Monospaced", Font.BOLD, Math.max(8, cellSize / 2)));
            String energyStr = String.valueOf(particle.getEnergy());
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(energyStr);
            int textHeight = fm.getHeight();

            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRoundRect(
                    x + (cellSize - textWidth) / 2 - 2,
                    y + cellSize - textHeight + 2,
                    textWidth + 4,
                    textHeight - 4,
                    3, 3);

            g2d.setColor(Color.WHITE);
            g2d.drawString(energyStr,
                    x + (cellSize - textWidth) / 2,
                    y + cellSize - 4);
        }

        if (energyRatio > 0.8) {
            float pulse = 0.5f + 0.5f * (float) Math.sin(System.currentTimeMillis() / 200.0);
            g2d.setColor(new Color(255, 255, 255, (int) (pulse * 100)));
            g2d.setStroke(new BasicStroke(1 + pulse));
            g2d.drawOval(x - 2, y - 2, cellSize + 4, cellSize + 4);
        }
    }

    /*
     * private void drawTargets(Graphics2D g2d) {
     * Point mouseGridPos = getMousePositionInGrid();
     * List<Player> players = gameState.getPlayers();
     * 
     * for (Player player : players) {
     * Color targetColor = player.getColor();
     * 
     * int targetX, targetY;
     * if (player.isHuman()) {
     * int mx = mouseGridPos.x;
     * int my = mouseGridPos.y;
     * 
     * int[] libre = gameState.getGrid().trouverCaseLibreLaPlusProche(mx, my);
     * if (libre != null) {
     * player.setTargetX(libre[0]);
     * player.setTargetY(libre[1]);
     * }
     * 
     * targetX = player.getTargetX();
     * targetY = player.getTargetY();
     * } else {
     * targetX = player.getTargetX();
     * targetY = player.getTargetY();
     * }
     * 
     * drawTarget(g2d, targetX, targetY, targetColor, player.isHuman(),
     * player.getId());
     * }
     * }
     */
    // Dessine les cibles de chaque joueur
    private void drawTargets(Graphics2D g2d) {
        List<Player> players = gameState.getPlayers();

        for (Player player : players) {
            int targetX = player.getTargetX();
            int targetY = player.getTargetY();
            drawTarget(g2d, targetX, targetY, player.getColor(), player.isHuman(), player.getId());
        }
    }

    // Dessine la cible d'un joueur
    private void drawTarget(Graphics2D g2d, int targetX, int targetY, Color color, boolean isHuman, int playerId) {
        int x = targetX * cellSize;
        int y = targetY * cellSize;

        float pulse = 0.5f + 0.5f * (float) Math.sin(System.currentTimeMillis() / 300.0);
        int pulseSize = (int) (pulse * 5);

        g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 80));
        g2d.setStroke(new BasicStroke(2 + pulse));
        int outerSize = cellSize * 3 + pulseSize * 2;
        g2d.drawOval(x - outerSize / 2 + cellSize / 2, y - outerSize / 2 + cellSize / 2, outerSize, outerSize);

        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(2));
        int crossSize = cellSize * 2;

        GradientPaint lineGradient = new GradientPaint(
                x - crossSize, y, new Color(color.getRed(), color.getGreen(), color.getBlue(), 150),
                x + crossSize, y, new Color(color.getRed(), color.getGreen(), color.getBlue(), 255));
        g2d.setPaint(lineGradient);

        g2d.drawLine(x - crossSize, y, x + crossSize, y);
        g2d.drawLine(x, y - crossSize, x, y + crossSize);

        g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 150));
        g2d.fillOval(x - 3, y - 3, 6, 6);

        String label = isHuman ? "VOUS" : "IA " + playerId;
        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(label);
        int textHeight = fm.getHeight();

        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRoundRect(
                x + crossSize + 8,
                y - textHeight / 2 + 2,
                textWidth + 8,
                textHeight + 2,
                5, 5);

        g2d.setColor(color);
        g2d.drawString(label, x + crossSize + 12, y + 5);

        g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 100));
        g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[] { 5, 5 }, 0));
        g2d.drawLine(x + crossSize, y, x + crossSize + 5, y);
    }

    // Dessine l'interface utilisateur (infos, scores, contrôles...)
    private void drawUI(Graphics2D g2d) {
        int uiWidth = 280;
        int uiHeight = 180;
        int cornerRadius = 15;

        g2d.setColor(new Color(0, 0, 0, 120));
        g2d.fillRoundRect(10, 10, uiWidth, uiHeight, cornerRadius, cornerRadius);

        g2d.setColor(new Color(100, 100, 150, 80));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(10, 10, uiWidth, uiHeight, cornerRadius, cornerRadius);

        GradientPaint titleGradient = new GradientPaint(
                20, 25, new Color(100, 150, 255),
                20, 45, new Color(50, 100, 220));
        g2d.setPaint(titleGradient);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("LIQUID WARS", 20, 35);

        g2d.setColor(new Color(180, 180, 255));
        g2d.setFont(new Font("Arial", Font.ITALIC, 10));
        g2d.drawString("Fluid Strategy Game", 22, 50);

        g2d.setColor(TEXT_COLOR);
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 11));

        int infoY = 70;
        g2d.drawString("Énergie totale: " + String.format("%,d", gameState.getTotalEnergy()), 20, infoY);
        infoY += 15;
        g2d.drawString("Particules: " + gameState.getGrid().getParticles().size(), 20, infoY);
        infoY += 15;

        // MODIF: on compte sans isAlive()
        for (Player player : gameState.getPlayers()) {
            long particleCount = gameState.getGrid().getParticles().stream()
                    .filter(p -> p.getPlayerId() == player.getId())
                    .count();

            g2d.setColor(player.getColor());
            String playerType = player.isHuman() ? "HUMAIN" : "IA";
            String playerInfo = String.format("%s %d: %3d | Score: %3d",
                    playerType, player.getId(), particleCount, player.getScore());
            g2d.drawString(playerInfo, 20, infoY);
            infoY += 15;

            g2d.setColor(TEXT_COLOR);
            String poolInfo = "Pool: " + gameState.getPool(player.getId());
            g2d.drawString(poolInfo, 30, infoY); // 30 = léger indent
            infoY += 15;
        }

        g2d.setColor(new Color(200, 200, 255));
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));

        int controlsY = getHeight() - 80;
        g2d.drawString("CONTROLES:", 20, controlsY);
        controlsY += 15;
        g2d.drawString("• Souris: Déplacer votre cible", 30, controlsY);
        controlsY += 12;
        //
        //
        g2d.drawString("• N: Grille ON/OFF", 30, controlsY);
        controlsY += 12;
        g2d.drawString("• ESC: Quitter", 30, controlsY);

        int indicatorX = getWidth() - 150;
        int indicatorY = getHeight() - 60;

        //


        if (!gameState.isGameRunning()) {
            drawGameOver(g2d);
        }
    }

    // Affiche l'écran de fin de partie
    private void drawGameOver(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int boxWidth = 400;
        int boxHeight = 200;

        GradientPaint boxGradient = new GradientPaint(
                centerX - boxWidth / 2, centerY - boxHeight / 2, new Color(20, 20, 40, 220),
                centerX + boxWidth / 2, centerY + boxHeight / 2, new Color(40, 40, 60, 240));
        g2d.setPaint(boxGradient);
        g2d.fillRoundRect(centerX - boxWidth / 2, centerY - boxHeight / 2, boxWidth, boxHeight, 20, 20);

        float pulse = 0.5f + 0.5f * (float) Math.sin(System.currentTimeMillis() / 200.0);
        g2d.setColor(new Color(255, 50, 50, (int) (pulse * 200)));
        g2d.setStroke(new BasicStroke(3 + pulse * 2));
        g2d.drawRoundRect(centerX - boxWidth / 2, centerY - boxHeight / 2, boxWidth, boxHeight, 20, 20);

        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        FontMetrics fm = g2d.getFontMetrics();
        String gameOverText = "GAME OVER";
        int textWidth = fm.stringWidth(gameOverText);

        GradientPaint textGradient = new GradientPaint(
                centerX - textWidth / 2, centerY - 50, Color.RED,
                centerX + textWidth / 2, centerY - 50, Color.ORANGE);
        g2d.setPaint(textGradient);
        g2d.drawString(gameOverText, centerX - textWidth / 2, centerY - 30);

        // MODIF: vainqueur sans isAlive()
        Player winner = null;
        long maxParticles = 0;

        for (Player player : gameState.getPlayers()) {
            long count = gameState.getGrid().getParticles().stream()
                    .filter(p -> p.getPlayerId() == player.getId())
                    .count();
            if (count > maxParticles) {
                maxParticles = count;
                winner = player;
            }
        }

        if (winner != null) {
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            g2d.setColor(winner.getColor());
            String winnerText = "VAINQUEUR: " + (winner.isHuman() ? "VOUS!" : "IA " + winner.getId());
            textWidth = g2d.getFontMetrics().stringWidth(winnerText);
            g2d.drawString(winnerText, centerX - textWidth / 2, centerY + 20);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.PLAIN, 18));
            String scoreText = "Score: " + winner.getScore() + " | Particules: " + maxParticles;
            textWidth = g2d.getFontMetrics().stringWidth(scoreText);
            g2d.drawString(scoreText, centerX - textWidth / 2, centerY + 50);
        }

        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        String restartText = "Appuyez sur ESC pour quitter";
        textWidth = g2d.getFontMetrics().stringWidth(restartText);
        g2d.drawString(restartText, centerX - textWidth / 2, centerY + 80);
    }

    // Retourne la taille d'une cellule
    public int getCellSize() {
        return cellSize;
    }
}
