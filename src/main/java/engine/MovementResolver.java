package engine;

import model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Résout les déplacements des particules à chaque tour de jeu.
 */

public class MovementResolver {
    private static final int[][] VOISINS_4 = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
    private static final int TRANSFERT = 5;
    private static final int VOL = 15;
    private static final int POOL_CHUNK = 5; // 1 si tu veux très fin

    public void resolve(GameState gameState) {
        Grid grid = gameState.getGrid();
        List<Particle> particles = new ArrayList<>(grid.getParticles());

        for (Particle particle : particles) {
            appliquerReglesPourUneParticule(particle, gameState);
        }

        redistribuerPools(gameState);
    }

    private void appliquerReglesPourUneParticule(Particle particle, GameState gameState) {
        if (!particle.canAct())
            return;

        Grid grid = gameState.getGrid();
        GradientField champ = gameState.getGradientFieldForPlayer(particle.getPlayerId());
        if (champ == null)
            return;

        int x = particle.getX();
        int y = particle.getY();

        int gIci = champ.getValue(x, y);
        if (gIci >= Integer.MAX_VALUE / 2)
            return; // pas de chemin

        // Construire les options sur les 4 voisins
        List<Option> options = new ArrayList<>();
        int gMin = Integer.MAX_VALUE;

        for (int[] d : VOISINS_4) {
            int nx = x + d[0], ny = y + d[1];
            if (!grid.isValidPosition(nx, ny))
                continue;

            int g = champ.getValue(nx, ny);
            if (g >= Integer.MAX_VALUE / 2)
                continue;

            Cell cell = grid.getCell(nx, ny);
            Contenu contenu = contenuCase(nx, ny, particle.getPlayerId(), grid);

            options.add(new Option(nx, ny, g, cell, contenu));
            if (g < gMin)
                gMin = g;
        }

        if (options.isEmpty())
            return;

        final int gMinFinal = gMin;

        List<Option> principales = options.stream().filter(o -> o.gradient() == gMinFinal).toList();
        List<Option> bonnes = options.stream().filter(o -> o.gradient() < gIci).toList();
        List<Option> acceptables = options.stream().filter(o -> o.gradient() == gIci).toList();

        // 1) Si une direction principale est libre, on y va.
        Option o = premierLibre(principales);
        if (o != null) {
            grid.moveParticle(particle, o.x(), o.y());
            return;
        }

        // 2) Si une bonne direction est libre, on y va.
        o = premierLibre(bonnes);
        if (o != null) {
            grid.moveParticle(particle, o.x(), o.y());
            return;
        }

        // 3) Si une direction acceptable est libre, on y va.
        o = premierLibre(acceptables);
        if (o != null) {
            grid.moveParticle(particle, o.x(), o.y());
            return;
        }

        // 4) Si une direction principale est occupée par un ennemi, on l’attaque.
        o = premierEnnemi(principales);
        if (o != null) {
            attaquer(particle, o.x(), o.y(), gameState);
            return;
        }

        // 5) Si une bonne direction est occupée par un ennemi, on l’attaque.
        o = premierEnnemi(bonnes);
        if (o != null) {
            attaquer(particle, o.x(), o.y(), gameState);
            return;
        }

        // 6) Si une direction principale est occupée par un ami, on transfère.
        o = premierAmi(principales);
        if (o != null) {
            transferer(particle, o.x(), o.y(), gameState.getGrid());
            return;
        }

        // 7) Sinon, si collé à un ennemi : attaque “au contact”
        for (int[] d : VOISINS_4) {
            int nx = x + d[0], ny = y + d[1];
            if (!grid.isValidPosition(nx, ny))
                continue;
            if (contenuCase(nx, ny, particle.getPlayerId(), grid) == Contenu.ENNEMI) {
                attaquer(particle, nx, ny, gameState);
                return;
            }
        }
    }

    private Option premierLibre(List<Option> opts) {
        return opts.stream().filter(o -> o.contenu() == Contenu.VIDE).findFirst().orElse(null);
    }

    private Option premierEnnemi(List<Option> opts) {
        return opts.stream().filter(o -> o.contenu() == Contenu.ENNEMI).findFirst().orElse(null);
    }

    private Option premierAmi(List<Option> opts) {
        return opts.stream().filter(o -> o.contenu() == Contenu.AMI).findFirst().orElse(null);
    }

    private Contenu contenuCase(int x, int y, int idJoueur, Grid grid) {
        Cell c = grid.getCell(x, y);
        if (c == Cell.OBSTACLE)
            return Contenu.OBSTACLE;
        if (c == Cell.EMPTY)
            return Contenu.VIDE;

        List<Particle> ps = grid.getParticlesAt(x, y);
        if (ps.isEmpty())
            return Contenu.VIDE; // sécurité
        return (ps.get(0).getPlayerId() == idJoueur) ? Contenu.AMI : Contenu.ENNEMI;
    }

    /**
     * Attaque (version POOL) :
     * - on vole jusqu'à VOL, en pouvant descendre la cible jusqu'à 0
     * - l'énergie volée va :
     * 1) dans l'attaquant (jusqu'à MAX)
     * 2) le surplus va dans le pool de l'équipe
     * - aucune distribution aux adjacents
     * - conservation stricte : ce qui est retiré de la cible = (gain attaquant +
     * dépôt pool)
     */
    private void attaquer(Particle attaquant, int x, int y, GameState gameState) {
        Grid grid = gameState.getGrid();
        List<Particle> ps = grid.getParticlesAt(x, y);
        if (ps.isEmpty())
            return;

        Particle cible = ps.get(0);
        if (cible.getPlayerId() == attaquant.getPlayerId())
            return;

        // ✅ FIX : si la cible est déjà trop faible, on la capture même si on ne peut
        // plus voler
        if (cible.getEnergy() < Particle.MIN_ENERGY) {
            boolean converted = convertirSiSousMinimum(cible, attaquant, gameState);
            if (converted) {
                boosterConvertieDepuisPool(cible, attaquant, gameState);
            }
            return;
        }

        // 1) Combien peut-on voler ? (jusqu'à 0)
        int volable = cible.getEnergy();
        if (volable <= 0)
            return; // sécurité (normalement inutile maintenant)

        int vol = Math.min(VOL, volable);
        if (vol <= 0)
            return;

        // 2) Retirer tout de suite à la cible (conservation)
        cible.addEnergy(-vol);

        // 3) Mettre dans l'attaquant autant que possible
        int capAtt = Particle.MAX_ENERGY - attaquant.getEnergy();
        int prisAtt = Math.min(vol, Math.max(0, capAtt));
        if (prisAtt > 0) {
            attaquant.addEnergy(prisAtt);
        }

        // 4) Tout le surplus va dans le pool
        int surplus = vol - prisAtt;
        if (surplus > 0) {
            gameState.addToPool(attaquant.getPlayerId(), surplus);
        }

        // 5) Conversion si énergie < MIN
        boolean converted = convertirSiSousMinimum(cible, attaquant, gameState);

        // Boost depuis le pool (optionnel)
        if (converted) {
            boosterConvertieDepuisPool(cible, attaquant, gameState);
        }
    }

    private void boosterConvertieDepuisPool(Particle cible, Particle attaquant, GameState gameState) {
        if (cible.getPlayerId() != attaquant.getPlayerId())
            return;

        int manque = Particle.MIN_ENERGY - cible.getEnergy();
        if (manque <= 0)
            return;

        int pris = gameState.takeFromPool(attaquant.getPlayerId(), manque);
        if (pris > 0) {
            cible.addEnergy(pris);
        }
    }

    /**
     * Conversion : si énergie < MIN, la particule change d’équipe.
     * Retourne true si conversion effectuée.
     */
    private boolean convertirSiSousMinimum(Particle cible, Particle attaquant, GameState gameState) {
        if (cible.getEnergy() < Particle.MIN_ENERGY) {
            cible.setPlayerId(attaquant.getPlayerId());
            gameState.getGrid().updateParticleCell(cible);

            Player p = gameState.getPlayer(attaquant.getPlayerId());
            if (p != null)
                p.incrementScore();

            return true;
        }
        return false;
    }

    /**
     * Don = TRANSFERT (conservation), seulement si donneur > MIN et receveur < MAX.
     */
    private void transferer(Particle donneur, int x, int y, Grid grid) {
        List<Particle> ps = grid.getParticlesAt(x, y);
        if (ps.isEmpty())
            return;

        Particle receveur = ps.get(0);
        if (receveur.getPlayerId() != donneur.getPlayerId())
            return;

        // Ne donne pas si receveur déjà plein (déjà respecté ici)
        if (donneur.getEnergy() > Particle.MIN_ENERGY && receveur.getEnergy() < Particle.MAX_ENERGY) {
            int maxDon = donneur.getEnergy() - Particle.MIN_ENERGY;
            int maxRec = Particle.MAX_ENERGY - receveur.getEnergy();
            int t = Math.min(TRANSFERT, Math.min(maxDon, maxRec));
            if (t <= 0)
                return;

            donneur.addEnergy(-t);
            receveur.addEnergy(+t);
        }
    }

    private enum Contenu {
        VIDE, AMI, ENNEMI, OBSTACLE
    }

    private record Option(int x, int y, int gradient, Cell cell, Contenu contenu) {
    }

    private void redistribuerPools(GameState gameState) {
        Grid grid = gameState.getGrid();

        for (Player pl : gameState.getPlayers()) {
            int id = pl.getId();
            if (gameState.getPool(id) <= 0)
                continue;

            // Particules de l'équipe triées par énergie croissante
            List<Particle> candidates = grid.getParticles().stream()
                    .filter(p -> p.getPlayerId() == id)
                    .filter(p -> p.getEnergy() < Particle.MAX_ENERGY)
                    .sorted((a, b) -> Integer.compare(a.getEnergy(), b.getEnergy()))
                    .toList();

            for (Particle p : candidates) {
                if (gameState.getPool(id) <= 0)
                    break;

                //pas de recharge pool sur le front
                /*if (estAuContactDUnEnnemi(grid, p))
                    continue;*/

                int manque = Particle.MAX_ENERGY - p.getEnergy();
                if (manque <= 0)
                    continue;

                int demande = Math.min(POOL_CHUNK, manque);
                int pris = gameState.takeFromPool(id, demande);
                if (pris <= 0)
                    break;

                p.addEnergy(pris);
            }
        }
    }

}