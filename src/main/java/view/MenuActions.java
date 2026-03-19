package view;

/**
 * Interface pour les actions de menu (démarrer une partie, quitter).
 */
public interface MenuActions {
    // Lance une nouvelle partie avec les paramètres donnés
    void onStartGame(int largeur, int hauteur, int nbJoueurs, int particulesParJoueur, int cellSize);
    // Ferme l'application
    void onQuit();
}
