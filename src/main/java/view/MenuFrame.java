package view;

import controller.GameLoop;
import model.GameState;

import javax.swing.*;
import java.awt.*;

/**
 * Fenêtre de navigation des menus du jeu (accueil, options, équipes, etc).
 */
public class MenuFrame extends JFrame {

    private final CardLayout cards = new CardLayout();
    private final JPanel pages = new JPanel(cards);

    // Barre du bas : Quitter (visible sur toutes les pages menu)
    private final JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private final JButton quitter = new JButton("Quitter");

    private int largeur = 60;
    private int hauteur = 40;
    private int cellSize = 12;

    private final TeamConfig teamConfig = new TeamConfig();

    /**
     * Initialise la fenêtre de menu et affiche la page d'accueil.
     */
    public MenuFrame() {
        super("Liquid Wars");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Bottom bar
        bottomBar.setOpaque(true);
        bottomBar.add(quitter);
        quitter.addActionListener(e -> System.exit(0));

        // Pages
        pages.add(new MainMenuPanel(this), "MENU");
        pages.add(new MapPanel(this), "TABLEAU");
        pages.add(new TeamsPanel(this), "EQUIPES");

        add(pages, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);

        showMenu();
        applyWindowSize();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ---- Taille identique au jeu ----
    // Calcule la taille de la fenêtre en pixels selon la grille
    private Dimension tailleJeuPx() {
        return new Dimension(largeur * cellSize, hauteur * cellSize);
    }

    // Applique la taille calculée à la fenêtre
    private void applyWindowSize() {
        Dimension d = tailleJeuPx();
        pages.setPreferredSize(d);
        pages.revalidate();
        pack();
        setLocationRelativeTo(null);
    }

    // ---- Navigation ----
    public void showMenu() {
        bottomBar.setVisible(true);
        cards.show(pages, "MENU");
    }

    public void showTableau() {
        bottomBar.setVisible(true);
        cards.show(pages, "TABLEAU");
    }

    public void showEquipes() {
        bottomBar.setVisible(true);
        cards.show(pages, "EQUIPES");
    }

    // ---- Paramètres ----
    public int getLargeur() { return largeur; }
    public int getHauteur() { return hauteur; }
    public int getCellSize() { return cellSize; }
    public TeamConfig getTeamConfig() { return teamConfig; }

    public void setMapSize(int largeur, int hauteur) {
        this.largeur = largeur;
        this.hauteur = hauteur;
        applyWindowSize();
    }

    public void setCellSize(int cellSize) {
        this.cellSize = cellSize;
        applyWindowSize();
    }

    // ---- Lancer le jeu ----
    public void lancerJeuLocal() {
        // Bouton quitter absent en mode jeu
        bottomBar.setVisible(false);

        GameState state = new GameState(
                largeur, hauteur,
                teamConfig.getNombreJoueurs(),
                teamConfig.getParticulesParJoueur()
        );

        GameView gameView = new GameView(largeur, hauteur, cellSize);
        pages.add(gameView, "JOUER");
        cards.show(pages, "JOUER");

        applyWindowSize(); // fenêtre = même taille que le jeu

        GameLoop loop = new GameLoop(state, gameView);
        loop.start();

        SwingUtilities.invokeLater(gameView::requestFocusInWindow);
    }
}

