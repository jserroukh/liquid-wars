package engine;

import model.Cell;
import model.GradientField;
import model.Grid;
import model.Particle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GradientCalculatorTest {

    private static final int INFINITY = Integer.MAX_VALUE / 2;

    private Grid grilleVide(int width, int height) {
        return new Grid(width, height, false); // grille vide, sans obstacles aléatoires
    }

    @Test
    void gradientSansObstacleCorrespondALaDistanceManhattan() {
        int w = 30, h = 30;
        Grid grille = grilleVide(w, h);
        GradientField champ = new GradientField(0, w, h);

        GradientCalculator calc = new GradientCalculator();
        calc.calculateGradient(champ, grille, 10, 10);

        assertEquals(0, champ.getValue(10, 10));
        assertEquals(1, champ.getValue(10, 9));
        assertEquals(1, champ.getValue(9, 10));
        assertEquals(2, champ.getValue(8, 10));
        assertEquals(20, champ.getValue(0, 0)); // |0-10| + |0-10|
    }

    @Test
    void obstacleForceUnDetourEtAugmenteLaDistance() {
        int w = 30, h = 30;
        Grid grille = grilleVide(w, h);
        GradientField champ = new GradientField(0, w, h);

        // obstacle juste "au-dessus" de la cible
        grille.setCell(10, 9, Cell.OBSTACLE);

        GradientCalculator calc = new GradientCalculator();
        calc.calculateGradient(champ, grille, 10, 10);

        // Sans obstacle, (10,8) serait à 2.
        // Avec obstacle en (10,9), détour minimal :
        // (10,10)->(9,10)->(9,9)->(9,8)->(10,8) = 4
        assertEquals(4, champ.getValue(10, 8));

        // La case obstacle reste à l'infini
        assertEquals(INFINITY, champ.getValue(10, 9));
    }

    @Test
    void particuleNeBloquePasLeGradient() {
        int w = 30, h = 30;
        int playerId = 0;

        Grid grille = grilleVide(w, h);
        GradientField champ = new GradientField(playerId, w, h);

        // Cible en (10,10), on met une particule ENNEMIE sur (11,10)
        grille.addParticle(new Particle(1, 11, 10));

        GradientCalculator calc = new GradientCalculator();
        calc.calculateGradient(champ, grille, 10, 10);

        // Avec "obstacles only", (11,10) doit être à 1, pas INF
        assertEquals(1, champ.getValue(11, 10));

        // Et (12,10) doit être à 2
        assertEquals(2, champ.getValue(12, 10));
    }

    @Test
    void particuleAmieNeBloquePasLeGradient() {
        int w = 30, h = 30;
        int playerId = 0;

        Grid grille = grilleVide(w, h);
        GradientField champ = new GradientField(playerId, w, h);

        // Particule AMIE sur le chemin
        grille.addParticle(new Particle(playerId, 11, 10));

        GradientCalculator calc = new GradientCalculator();
        calc.calculateGradient(champ, grille, 10, 10);

        assertEquals(1, champ.getValue(11, 10));
        assertEquals(2, champ.getValue(12, 10));
    }

}
