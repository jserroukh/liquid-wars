package view;

import javax.swing.*;
import java.awt.*;

/**
 * Menu principal du jeu : permet de lancer une partie, accéder au tableau ou aux équipes.
 */
public class MainMenuPanel extends StyledMenuScreen {

    /**
     * Construit le menu principal avec les boutons d'accès aux différentes pages.
     */
    public MainMenuPanel(MenuFrame frame) {
        super();

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 10, 10, 10);

        JButton jouer = styledButton("Jouer");
        JButton tableau = styledButton("Tableau");
        JButton equipes = styledButton("Equipes");

        jouer.addActionListener(e -> frame.lancerJeuLocal());
        tableau.addActionListener(e -> frame.showTableau());
        equipes.addActionListener(e -> frame.showEquipes());

        int row = 0;
        c.gridy = row++; content.add(Box.createRigidArea(new Dimension(1, 140)), c);
        c.gridy = row++; content.add(jouer, c);
        c.gridy = row++; content.add(tableau, c);
        c.gridy = row++; content.add(equipes, c);
    }
}
