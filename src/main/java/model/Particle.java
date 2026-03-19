package model;

/**
 * Représente une particule sur la grille (appartient à un joueur).
 */
public class Particle {

    private int playerId;
    private int energy;
    private int x;
    private int y;

    public static final int MAX_ENERGY = 100;
    public static final int MIN_ENERGY = 10;
    public static final int INITIAL_ENERGY = 100;

    /**
     * Crée une particule pour un joueur à une position donnée.
     */
    public Particle(int playerId, int x, int y) {
        this.playerId = playerId;
        this.energy = INITIAL_ENERGY;
        this.x = x;
        this.y = y;
    }

    // Identité et équipe de la particule

    // Retourne l'identifiant du joueur propriétaire
    public int getPlayerId() {
        return playerId;
    }
    // Change le joueur propriétaire
    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    // Gestion de l'énergie de la particule

    // Retourne l'énergie de la particule
    public int getEnergy() {
        return energy;
    }

    // L'énergie est toujours entre 0 et MAX, la particule n'est jamais supprimée
    // Modifie l'énergie (bornée entre 0 et MAX)
    public void setEnergy(int energy) {
        this.energy = Math.max(0, Math.min(energy, MAX_ENERGY));
    }

    // Ajoute ou retire de l'énergie à la particule
    public void addEnergy(int delta) {
        setEnergy(this.energy + delta);
    }

    // Une particule peut agir seulement si elle a assez d'énergie
    // Indique si la particule peut agir (assez d'énergie)
    public boolean canAct() {
        return energy >= MIN_ENERGY;
    }

    /*Position*/

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}

