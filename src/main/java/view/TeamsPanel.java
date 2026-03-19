package view;

import javax.swing.*;
import java.awt.*;

/**
 * Menu pour configurer les équipes et le nombre de particules par joueur.
 */
public class TeamsPanel extends StyledMenuScreen {

    /**
     * Construit le panneau de configuration des équipes.
     */
    public TeamsPanel(MenuFrame frame) {
        super();

        TeamConfig cfg = frame.getTeamConfig();

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 10, 10, 10);

        JLabel title = styledLabel("Equipes : multijoueur local");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JSpinner nbJoueurs = new JSpinner(new SpinnerNumberModel(cfg.getNombreJoueurs(), 1, 6, 1));
        JSpinner particules = new JSpinner(new SpinnerNumberModel(cfg.getParticulesParJoueur(), 10, 3000, 10));

        JButton appliquer = styledButton("Appliquer");
        JButton retour = styledButton("Retour");

        appliquer.addActionListener(e -> {
            cfg.setNombreJoueurs((Integer) nbJoueurs.getValue());
            cfg.setParticulesParJoueur((Integer) particules.getValue());
        });

        retour.addActionListener(e -> frame.showMenu());

        int row = 0;

        c.gridx = 0; c.gridy = row++; c.gridwidth = 2;
        content.add(Box.createRigidArea(new Dimension(1, 155)), c);
        c.gridy = row++;
        content.add(title, c);

        c.gridwidth = 1;

        c.gridy = row++; c.gridx = 0; content.add(styledLabel("Nombre joueurs :"), c);
        c.gridx = 1; content.add(nbJoueurs, c);

        c.gridy = row++; c.gridx = 0; content.add(styledLabel("Particules/joueur :"), c);
        c.gridx = 1; content.add(particules, c);

        c.gridy = row++; c.gridx = 0; content.add(appliquer, c);
        c.gridx = 1; content.add(retour, c);
    }
}
