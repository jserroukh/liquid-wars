package model;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * Stocke le champ de gradients pour un joueur (distance à la cible sur la grille).
 */
public class GradientField {
    private final int playerId;
    private final int width;
    private final int height;
    private final AtomicIntegerArray[] gradients;
    private static final int INFINITY = Integer.MAX_VALUE / 2;
    
    /**
     * Crée un champ de gradients pour un joueur donné.
     */
    public GradientField(int playerId, int width, int height) {
        this.playerId = playerId;
        this.width = width;
        this.height = height;
        this.gradients = new AtomicIntegerArray[width];
        
        for (int x = 0; x < width; x++) {
            gradients[x] = new AtomicIntegerArray(height);
            for (int y = 0; y < height; y++) {
                gradients[x].set(y, INFINITY);
            }
        }
    }
    
    // Retourne la valeur du gradient en (x, y)
    public int getValue(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return gradients[x].get(y);
        }
        return INFINITY;
    }
    
    // Modifie la valeur du gradient en (x, y)
    public void setValue(int x, int y, int value) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            gradients[x].set(y, value);
        }
    }
    
    // Réinitialise tout le champ à l'infini
    public void reset() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                gradients[x].set(y, INFINITY);
            }
        }
    }
    
    // Retourne l'id du joueur associé
    public int getPlayerId() { return playerId; }
    // Retourne la largeur du champ
    public int getWidth() { return width; }
    // Retourne la hauteur du champ
    public int getHeight() { return height; }
    
    // Retourne le champ sous forme de tableau 2D
    public int[][] toArray() {
        int[][] result = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                result[x][y] = gradients[x].get(y);
            }
        }
        return result;
    }
}
