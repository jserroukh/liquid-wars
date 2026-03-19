package view;

import javax.swing.*;
import java.awt.*;

/**
 * Panneau de base pour les écrans stylisés (fond étoilé, gestion du rendu).
 */
public abstract class StyledScreenPanel extends JPanel {

    protected final Color BACKGROUND_COLOR = new Color(10, 10, 20);
    protected final Color TEXT_COLOR = new Color(220, 220, 255);

    private final int[] starX;
    private final int[] starY;
    private final float[] starBrightness;

    /**
     * Initialise le panneau avec un fond étoilé.
     */
    protected StyledScreenPanel() {
        setBackground(BACKGROUND_COLOR);
        setOpaque(true);

        int nbStars = 120;
        starX = new int[nbStars];
        starY = new int[nbStars];
        starBrightness = new float[nbStars];

        // On initialise avec une taille "par défaut" ; les étoiles restent correctes même si la fenêtre est plus grande.
        // (Swing les redessine, et le fond reste homogène.)
        int w = 1200;
        int h = 800;
        for (int i = 0; i < nbStars; i++) {
            starX[i] = (int) (Math.random() * w);
            starY[i] = (int) (Math.random() * h);
            starBrightness[i] = (float) (Math.random() * 0.8 + 0.2);
        }
    }

    @Override
    // Gère le rendu du fond et du contenu
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        drawBackground(g2d);
        drawContent(g2d);

        g2d.dispose();
    }

    // Dessine le fond étoilé et le dégradé
    private void drawBackground(Graphics2D g2d) {
        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(5, 5, 15),
                getWidth(), getHeight(), new Color(15, 15, 30)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        long t = System.currentTimeMillis();
        for (int i = 0; i < starX.length; i++) {
            float brightness = starBrightness[i] * (0.7f + 0.3f * (float) Math.sin(t / 1000.0 + i));
            int size = Math.max(1, (int) (brightness * 2));
            g2d.setColor(new Color(1f, 1f, 1f, brightness * 0.5f));

            // On "wrap" au cas où la fenêtre est plus petite / plus grande
            int x = starX[i] % Math.max(1, getWidth());
            int y = starY[i] % Math.max(1, getHeight());
            g2d.fillRect(x, y, size, size);
        }
    }

    protected void drawTitle(Graphics2D g2d, String text, int y) {
        g2d.setFont(new Font("Arial", Font.BOLD, 42));
        FontMetrics fm = g2d.getFontMetrics();
        int w = fm.stringWidth(text);
        int x = (getWidth() - w) / 2;

        GradientPaint titleGradient = new GradientPaint(
                x, y - 35, new Color(100, 150, 255),
                x, y + 10, new Color(50, 100, 220)
        );
        g2d.setPaint(titleGradient);
        g2d.drawString(text, x, y);

        // Sous-titre discret
        g2d.setFont(new Font("Arial", Font.ITALIC, 12));
        g2d.setColor(new Color(180, 180, 255));
        String sub = "Fluid Strategy Game";
        int sw = g2d.getFontMetrics().stringWidth(sub);
        g2d.drawString(sub, (getWidth() - sw) / 2, y + 18);
    }

    /** Chaque écran menu dessine ses éléments ici (texte, cadres, etc.) */
    // Méthode à implémenter pour dessiner le contenu spécifique
    protected abstract void drawContent(Graphics2D g2d);
}
