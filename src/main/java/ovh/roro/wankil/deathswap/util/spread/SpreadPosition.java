package ovh.roro.wankil.deathswap.util.spread;

import java.util.Random;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.BlockPosition.MutableBlockPosition;
import net.minecraft.server.v1_16_R3.EnumDirection;
import net.minecraft.server.v1_16_R3.IBlockData;
import net.minecraft.server.v1_16_R3.Material;
import net.minecraft.server.v1_16_R3.MathHelper;
import net.minecraft.server.v1_16_R3.WorldAccess;

public class SpreadPosition {

    public double x;
    public double z;

    public double dist(SpreadPosition other) {
        double xDistance = this.x - other.x;
        double zDistance = this.z - other.z;

        return Math.sqrt(xDistance * xDistance + zDistance * zDistance);
    }

    public void normalize() {
        double length = this.getLength();

        this.x /= length;
        this.z /= length;
    }

    float getLength() {
        return MathHelper.sqrt(this.x * this.x + this.z * this.z);
    }

    public void moveAway(SpreadPosition position) {
        this.x -= position.x;
        this.z -= position.z;
    }

    public boolean clamp(double minX, double minZ, double maxX, double maxZ) {
        boolean changed = false;

        if (this.x < minX) {
            this.x = minX;
            changed = true;
        } else if (this.x > maxX) {
            this.x = maxX;
            changed = true;
        }

        if (this.z < minZ) {
            this.z = minZ;
            changed = true;
        } else if (this.z > maxZ) {
            this.z = maxZ;
            changed = true;
        }

        return changed;
    }

    public int getSpawnY(WorldAccess level, int maxHeight) {
        MutableBlockPosition pos = new MutableBlockPosition(this.x, maxHeight + 1, this.z);

        boolean headAir = level.getType(pos).isAir();
        pos.c(EnumDirection.DOWN);
        boolean feetAir = level.getType(pos).isAir();

        while (pos.getY() > 0) {
            pos.c(EnumDirection.DOWN);

            boolean air = level.getType(pos).isAir();

            if (!air && headAir && feetAir) {
                return pos.getY() + 1;
            }

            headAir = feetAir;
            feetAir = air;
        }

        return maxHeight + 1;
    }

    public boolean isSafe(WorldAccess level, int maxHeight) {
        BlockPosition pos = new BlockPosition(this.x, this.getSpawnY(level, maxHeight) - 1, this.z);
        IBlockData blockState = level.getType(pos);
        Material material = blockState.getMaterial();
        return pos.getY() < maxHeight && !material.isLiquid() && material != Material.FIRE;
    }

    public void randomize(Random random, double minX, double minZ, double maxX, double maxZ) {
        this.x = MathHelper.a(random, minX, maxX);
        this.z = MathHelper.a(random, minZ, maxZ);
    }

    @Override
    public String toString() {
        return "SpreadPosition{" +
                "x=" + this.x +
                ", z=" + this.z +
                '}';
    }
}
