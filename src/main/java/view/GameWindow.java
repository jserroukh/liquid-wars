package view;

import controller.GameLoop;
import model.GameState;

import javax.swing.*;
import java.awt.*;

/**
 * Fenêtre principale du jeu : gère l'affichage des menus et du jeu.
 */
public class GameWindow extends JFrame implements MenuActions {

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    private StartMenuPanel menu;
    private GameView gameView;
    private GameLoop gameLoop;

    /**
     * Initialise la fenêtre et affiche le menu principal.
     */
    public GameWindow() {
        super("Liquid Wars");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        menu = new StartMenuPanel(this);
        root.add(menu, "MENU");

        setContentPane(root);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        cards.show(root, "MENU");
    }

    @Override
    // Lance une nouvelle partie avec les paramètres choisis
    public void onStartGame(int largeur, int hauteur, int nbJoueurs, int particulesParJoueur, int cellSize) {
        // 1) créer le modèle
        GameState gameState = new GameState(largeur, hauteur, nbJoueurs, particulesParJoueur);

        // 2) créer la vue (ta GameView existante)
        gameView = new GameView(largeur, hauteur, cellSize);

        // 3) créer la boucle de jeu
        gameLoop = new GameLoop(gameState, gameView);

        // 4) afficher l’écran de jeu
        root.add(gameView, "GAME");
        pack();
        cards.show(root, "GAME");

        // Focus clavier pour les touches G/H/N/Esc
        SwingUtilities.invokeLater(gameView::requestFocusInWindow);

        // 5) démarrer
        gameLoop.start();
    }

    @Override
    // Ferme l'application
    public void onQuit() {
        System.exit(0);
    }
}
