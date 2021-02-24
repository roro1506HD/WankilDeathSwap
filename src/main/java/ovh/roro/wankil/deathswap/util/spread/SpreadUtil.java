package ovh.roro.wankil.deathswap.util.spread;

import java.util.Arrays;
import java.util.Random;
import net.minecraft.server.v1_16_R3.Vec2F;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SpreadUtil {

    private static final Random RANDOM = new Random();
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int MAX_HEIGHT = 230;

    public static SpreadPosition[] createSpawns(WorldServer level, Vec2F center, int amount, float spreadDistance, float maxRange) {
        double minX = center.i - maxRange;
        double minZ = center.j - maxRange;
        double maxX = center.i + maxRange;
        double maxZ = center.j + maxRange;

        SpreadPosition[] initialPositions = SpreadUtil.createInitialPositions(amount, minX, minZ, maxX, maxZ);

        SpreadUtil.spreadPositions(center, spreadDistance, level, minX, minZ, maxX, maxZ, initialPositions);

        LOGGER.info("Created " + initialPositions.length + " spawn positions.");

        return initialPositions;
    }

    private static SpreadPosition[] createInitialPositions(int amount, double minX, double minZ, double maxX, double maxZ) {
        SpreadPosition[] positions = new SpreadPosition[amount];

        for (int i = 0; i < positions.length; i++) {
            (positions[i] = new SpreadPosition()).randomize(RANDOM, minX, minZ, maxX, maxZ);
        }

        LOGGER.info(Arrays.toString(positions));

        return positions;
    }

    private static void spreadPositions(Vec2F center, float spreadDistance, WorldServer level, double minX, double minZ, double maxX, double maxZ, SpreadPosition[] positions) {
        boolean shouldContinue = true;
        double distance = Double.MAX_VALUE;
        int tries;

        for (tries = 0; tries < 10000 && shouldContinue; tries++) {
            shouldContinue = false;
            distance = Double.MAX_VALUE;

            for (int i = 0; i < positions.length; i++) {
                SpreadPosition position = positions[i];
                int closePositions = 0;
                SpreadPosition tempPosition = new SpreadPosition();

                for (int j = 0; j < positions.length; j++) {
                    if (j == i) {
                        continue;
                    }

                    SpreadPosition otherPosition = positions[j];
                    double dist = position.dist(otherPosition);

                    distance = Math.min(dist, distance);
                    if (dist >= spreadDistance) {
                        continue;
                    }

                    closePositions++;

                    tempPosition.x = tempPosition.x + (otherPosition.x - position.x);
                    tempPosition.z = tempPosition.z + (otherPosition.z - position.z);
                }

                if (closePositions > 0) {
                    tempPosition.x = tempPosition.x / (double) closePositions;
                    tempPosition.z = tempPosition.z / (double) closePositions;

                    double length = tempPosition.getLength();
                    if (length > 0.0D) {
                        tempPosition.normalize();
                        position.moveAway(tempPosition);
                    } else {
                        position.randomize(RANDOM, minX, minZ, maxX, maxZ);
                    }

                    shouldContinue = true;
                }

                if (!position.clamp(minX, minZ, maxX, maxZ)) {
                    continue;
                }

                shouldContinue = true;
            }

            if (shouldContinue) {
                continue;
            }

            for (SpreadPosition position : positions) {
                if (position.isSafe(level, MAX_HEIGHT)) {
                    continue;
                }

                position.randomize(RANDOM, minX, minZ, maxX, maxZ);
                shouldContinue = true;
            }
        }

        if (distance == Double.MAX_VALUE) {
            distance = 0.0D;
        }

        if (tries >= 10000) {
            LOGGER.info("Could not spread {} entities around {}, {} (too many entities for space - try using spread of at most {})", positions.length, center.i, center.j, String.format("%.2f", distance));
        }
    }
}
