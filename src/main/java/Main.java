import javax.swing.SwingUtilities;
import view.MenuFrame;

/**
 * Point d'entrée principal de l'application Liquid Wars.
 * Lance l'interface graphique du menu principal.
 */
public class Main {
    // Lance l'application en affichant le menu principal
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MenuFrame::new);
    }
}

