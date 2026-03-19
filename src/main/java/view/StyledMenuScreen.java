package view;

import javax.swing.*;
import java.awt.*;

/**
 * Classe de base pour les écrans de menu stylisés (fond, titre, boîte centrale).
 */
public abstract class StyledMenuScreen extends StyledScreenPanel {

    protected final JPanel content = new JPanel();

    /**
     * Initialise le panneau de menu stylisé avec une boîte centrale.
     */
    protected StyledMenuScreen() {
        super();
        setLayout(new GridBagLayout());

        content.setOpaque(false);
        content.setLayout(new GridBagLayout());

        add(content, new GridBagConstraints());
    }

    @Override
    // Dessine le fond et la boîte centrale du menu
    protected void drawContent(Graphics2D g2d) {
        drawTitle(g2d, "LIQUID WARS", 90);

        // Carte centrale (style proche de l’UI de GameView)
        int boxW = 520;
        int boxH = 420;
        int x = (getWidth() - boxW) / 2;
        int y = 140;

        g2d.setColor(new Color(0, 0, 0, 140));
        g2d.fillRoundRect(x, y, boxW, boxH, 20, 20);

        g2d.setColor(new Color(100, 100, 150, 90));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(x, y, boxW, boxH, 20, 20);
    }

    // Crée un bouton stylisé pour le menu
    protected JButton styledButton(String txt) {
        JButton b = new JButton(txt);
        b.setFocusPainted(false);
        b.setFont(new Font("Arial", Font.BOLD, 16));
        b.setPreferredSize(new Dimension(260, 42));
        return b;
    }

    // Crée un label stylisé pour le menu
    protected JLabel styledLabel(String txt) {
        JLabel l = new JLabel(txt);
        l.setForeground(new Color(220, 220, 255));
        l.setFont(new Font("Arial", Font.PLAIN, 14));
        return l;
    }
}

