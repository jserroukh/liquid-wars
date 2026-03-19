package view;

import javax.swing.*;
import java.awt.*;

/**
 * Panneau du menu de démarrage : permet de configurer et lancer une partie.
 */
public class StartMenuPanel extends JPanel {

    /**
     * Construit le menu de démarrage avec les champs de configuration.
     */
    public StartMenuPanel(MenuActions actions) {
        setLayout(new GridBagLayout());
        setBackground(new Color(10, 10, 20));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 10, 10, 10);

        JLabel title = new JLabel("LIQUID WARS", SwingConstants.CENTER);
        title.setForeground(new Color(220, 220, 255));
        title.setFont(new Font("Arial", Font.BOLD, 36));

        // Paramètres simples (tu peux enrichir)
        JSpinner largeur = new JSpinner(new SpinnerNumberModel(60, 20, 200, 5));
        JSpinner hauteur = new JSpinner(new SpinnerNumberModel(40, 20, 200, 5));
        JSpinner nbJoueurs = new JSpinner(new SpinnerNumberModel(2, 2, 6, 1));
        JSpinner particules = new JSpinner(new SpinnerNumberModel(200, 10, 2000, 10));
        JSpinner cellSize = new JSpinner(new SpinnerNumberModel(12, 6, 30, 1));

        JLabel l1 = label("Largeur grille");
        JLabel l2 = label("Hauteur grille");
        JLabel l3 = label("Nombre joueurs");
        JLabel l4 = label("Particules / joueur");
        JLabel l5 = label("Taille cellule");

        JButton start = button("Jouer");
        JButton quit = button("Quitter");

        start.addActionListener(e -> actions.onStartGame(
                (Integer) largeur.getValue(),
                (Integer) hauteur.getValue(),
                (Integer) nbJoueurs.getValue(),
                (Integer) particules.getValue(),
                (Integer) cellSize.getValue()
        ));

        quit.addActionListener(e -> actions.onQuit());

        int row = 0;

        c.gridy = row++; add(title, c);
        c.gridy = row++; add(rowPanel(l1, largeur), c);
        c.gridy = row++; add(rowPanel(l2, hauteur), c);
        c.gridy = row++; add(rowPanel(l3, nbJoueurs), c);
        c.gridy = row++; add(rowPanel(l4, particules), c);
        c.gridy = row++; add(rowPanel(l5, cellSize), c);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttons.setOpaque(false);
        buttons.add(start);
        buttons.add(quit);

        c.gridy = row++; add(buttons, c);
    }

    private static JLabel label(String txt) {
        JLabel l = new JLabel(txt);
        l.setForeground(new Color(200, 200, 255));
        l.setFont(new Font("Arial", Font.PLAIN, 14));
        return l;
    }

    private static JButton button(String txt) {
        JButton b = new JButton(txt);
        b.setFocusPainted(false);
        b.setFont(new Font("Arial", Font.BOLD, 14));
        return b;
    }

    private static JPanel rowPanel(JLabel label, JComponent input) {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        p.add(label, BorderLayout.WEST);
        p.add(input, BorderLayout.EAST);
        return p;
    }
}

