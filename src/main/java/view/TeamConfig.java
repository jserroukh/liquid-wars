package view;

/**
 * Gère la configuration des équipes (nombre de joueurs, particules par joueur).
 */
public class TeamConfig {
    private int nombreJoueurs = 2;
    private int particulesParJoueur = 50;

    // Retourne le nombre de joueurs
    public int getNombreJoueurs() { return nombreJoueurs; }
    // Modifie le nombre de joueurs
    public void setNombreJoueurs(int n) { this.nombreJoueurs = n; }

    // Retourne le nombre de particules par joueur
    public int getParticulesParJoueur() { return particulesParJoueur; }
    // Modifie le nombre de particules par joueur
    public void setParticulesParJoueur(int n) { this.particulesParJoueur = n; }
}
