package view;

import javax.swing.*;
import java.awt.*;

/**
 * Menu pour configurer la taille de la carte et des cellules.
 */
public class MapPanel extends StyledMenuScreen {

    /**
     * Construit le panneau de configuration de la carte.
     */
    public MapPanel(MenuFrame frame) {
        super();

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 10, 10, 10);

        JLabel title = styledLabel("Tableau : taille de la map");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JSpinner largeur = new JSpinner(new SpinnerNumberModel(frame.getLargeur(), 20, 200, 5));
        JSpinner hauteur = new JSpinner(new SpinnerNumberModel(frame.getHauteur(), 20, 200, 5));
        JSpinner cell = new JSpinner(new SpinnerNumberModel(frame.getCellSize(), 6, 30, 1));

        JButton appliquer = styledButton("Appliquer");
        JButton retour = styledButton("Retour");

        appliquer.addActionListener(e -> {
            frame.setMapSize((Integer) largeur.getValue(), (Integer) hauteur.getValue());
            frame.setCellSize((Integer) cell.getValue());
        });
        retour.addActionListener(e -> frame.showMenu());

        int row = 0;

        c.gridx = 0; c.gridy = row++; c.gridwidth = 2;
        content.add(Box.createRigidArea(new Dimension(1, 155)), c);
        c.gridy = row++;
        content.add(title, c);

        c.gridwidth = 1;

        c.gridy = row++; c.gridx = 0; content.add(styledLabel("Largeur :"), c);
        c.gridx = 1; content.add(largeur, c);

        c.gridy = row++; c.gridx = 0; content.add(styledLabel("Hauteur :"), c);
        c.gridx = 1; content.add(hauteur, c);

        c.gridy = row++; c.gridx = 0; content.add(styledLabel("Taille cellule :"), c);
        c.gridx = 1; content.add(cell, c);

        c.gridy = row++; c.gridx = 0; content.add(appliquer, c);
        c.gridx = 1; content.add(retour, c);
    }
}

