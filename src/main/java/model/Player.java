package model;

import java.awt.Color;

/**
 * Représente un joueur (humain ou IA) dans le jeu.
 */
public class Player {
    private int id;
    private Color color;
    private int targetX;
    private int targetY;
    private boolean isHuman;
    private int score;
    
    // Predefined colors for players
    private static final Color[] PLAYER_COLORS = {
        Color.BLUE,      // Player 0
        Color.RED,       // Player 1
        Color.GREEN,     // Player 2
        Color.YELLOW,    // Player 3
        Color.MAGENTA,   // Player 4
        Color.CYAN       // Player 5
    };
    
    /**
     * Crée un joueur avec un identifiant et le type (humain ou IA).
     */
    public Player(int id, boolean isHuman) {
        this.id = id;
        this.isHuman = isHuman;
        this.score = 0;
        this.targetX = 0;
        this.targetY = 0;
        
        // Assign color from predefined array
        this.color = PLAYER_COLORS[id % PLAYER_COLORS.length];
    }
    
    // Retourne l'identifiant du joueur
    public int getId() { return id; }
    // Retourne la couleur du joueur
    public Color getColor() { return color; }

    // Accès et modification de la cible X
    public int getTargetX() { return targetX; }
    public void setTargetX(int targetX) { this.targetX = targetX; }

    // Accès et modification de la cible Y
    public int getTargetY() { return targetY; }
    public void setTargetY(int targetY) { this.targetY = targetY; }

    // Indique si le joueur est humain
    public boolean isHuman() { return isHuman; }
    public void setIsHuman(boolean isHuman) { this.isHuman = isHuman; }

    // Accès et modification du score
    public int getScore() { return score; }
    public void incrementScore() { this.score++; }

    // Retourne la couleur d'énergie (ici, identique à la couleur du joueur)
    public Color getEnergyColor(int energy) {
        return color;
    }
}