package engine;

import model.Cell;
import model.GradientField;
import model.Grid;

import java.util.ArrayDeque;

/**
 * Calcule le champ de gradients pour chaque joueur (distance à la cible).
 */
public class GradientCalculator {

    // Calcule le champ de gradients à partir d'une cible
    public void calculateGradient(GradientField field, Grid grid, int targetX, int targetY) {
        if (!grid.isValidPosition(targetX, targetY))
            return;

        // Reset field
        field.reset();

        // Si la cible est un obstacle -> stop
        if (grid.getCell(targetX, targetY) == Cell.OBSTACLE)
            return;

        // Si la cible est occupée (particule), on cherche une case EMPTY proche
        if (grid.getCell(targetX, targetY) != Cell.EMPTY) {
            int[] libre = grid.trouverCaseLibreLaPlusProche(targetX, targetY);
            if (libre == null)
                return;
            targetX = libre[0];
            targetY = libre[1];
        }

        ArrayDeque<int[]> queue = new ArrayDeque<>();
        field.setValue(targetX, targetY, 0);
        queue.add(new int[] { targetX, targetY });

        final int[][] dirs = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };

        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            int x = cur[0];
            int y = cur[1];
            int d = field.getValue(x, y);

            for (int[] dir : dirs) {
                int nx = x + dir[0];
                int ny = y + dir[1];

                if (!grid.isValidPosition(nx, ny))
                    continue;

                // Seuls les obstacles bloquent le gradient
                if (grid.getCell(nx, ny) == Cell.OBSTACLE)
                    continue;

                int nd = d + 1;
                if (field.getValue(nx, ny) <= nd)
                    continue;

                field.setValue(nx, ny, nd);
                queue.add(new int[] { nx, ny });
            }
        }
    }
}
