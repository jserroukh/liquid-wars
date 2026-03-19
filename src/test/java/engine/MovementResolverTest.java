package engine;

import model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MovementResolverTest {

    private static void clearToEmpty(Grid g) {
        g.getParticles().clear();
        for (int x = 0; x < g.getWidth(); x++) {
            for (int y = 0; y < g.getHeight(); y++) {
                g.setCell(x, y, Cell.EMPTY);
            }
        }
    }

    private static void computeGradients(GameState state) {
        Grid g = state.getGrid();
        GradientCalculator calc = new GradientCalculator();
        for (Player pl : state.getPlayers()) {
            GradientField f = state.getGradientFieldForPlayer(pl.getId());
            calc.calculateGradient(f, g, pl.getTargetX(), pl.getTargetY());
        }
    }

    @Test
    void energieTotaleEstConserveeSurPlusieursSteps() {
        int w = 20, h = 20;

        GameState state = new GameState(w, h, 2, 0);
        Grid g = state.getGrid();
        clearToEmpty(g);

        // 2 particules adjacentes
        Particle p0 = new Particle(0, 10, 10);
        Particle p1 = new Particle(1, 11, 10);
        g.addParticle(p0);
        g.addParticle(p1);

        // Cibles (peu importe tant que gradients calculables)
        state.getPlayer(0).setTargetX(15);
        state.getPlayer(0).setTargetY(10);
        state.getPlayer(1).setTargetX(5);
        state.getPlayer(1).setTargetY(10);

        computeGradients(state);

        MovementResolver resolver = new MovementResolver();

        int energyBefore = state.computeTotalEnergy();
        int countBefore  = state.countParticles();

        for (int i = 0; i < 50; i++) {
            computeGradients(state);
            resolver.resolve(state);
        }

        int energyAfter = state.computeTotalEnergy();
        int countAfter  = state.countParticles();

        assertEquals(countBefore, countAfter, "La population doit rester constante.");
        assertEquals(energyBefore, energyAfter, "L'énergie totale doit être conservée (transferts/vols).");
    }

    @Test
    void particuleSansEnergieNeDoitPasAgir() {
        int w = 10, h = 10;

        GameState state = new GameState(w, h, 1, 0);
        Grid g = state.getGrid();
        clearToEmpty(g);

        Particle p = new Particle(0, 5, 5);
        p.setEnergy(Particle.MIN_ENERGY - 1);
        g.addParticle(p);

        state.getPlayer(0).setTargetX(6);
        state.getPlayer(0).setTargetY(5);

        computeGradients(state);

        MovementResolver resolver = new MovementResolver();
        resolver.resolve(state);

        assertEquals(5, p.getX());
        assertEquals(5, p.getY());
    }

    @Test
    void attaqueAdjacentePeutConvertirUneParticuleEnnemie() {
        int w = 12, h = 12;

        GameState state = new GameState(w, h, 2, 0);
        Grid g = state.getGrid();
        clearToEmpty(g);

        // p0 (attaquant) à gauche, p1 (cible ennemie) à droite
        Particle p0 = new Particle(0, 5, 5);
        Particle p1 = new Particle(1, 6, 5);

        // On force une conversion facile :
        // si VOL=15 et p1 a 10, on vole 10 => p1 tombe à 0 (< MIN) => conversion.
        p0.setEnergy(50);
        p1.setEnergy(Particle.MIN_ENERGY); // 10

        g.addParticle(p0);
        g.addParticle(p1);

        // Cibles : sans importance, mais on calcule quand même les gradients
        state.getPlayer(0).setTargetX(10);
        state.getPlayer(0).setTargetY(5);
        state.getPlayer(1).setTargetX(1);
        state.getPlayer(1).setTargetY(5);

        computeGradients(state);

        MovementResolver resolver = new MovementResolver();
        resolver.resolve(state);

        assertEquals(0, p1.getPlayerId(),
                "La particule ennemie devrait être convertie si son énergie passe sous MIN.");
        assertTrue(p1.getEnergy() >= Particle.MIN_ENERGY,
                "Après conversion, l'énergie de la cible doit être au moins MIN (selon tes règles).");
    }
}

