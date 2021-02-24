package ovh.roro.wankil.deathswap.game.json;

import org.bukkit.inventory.ItemStack;

public final class JsonStarter {

    private ItemStack[] inventory;

    public ItemStack[] getInventory() {
        return this.inventory;
    }

    public void setInventory(ItemStack[] inventory) {
        this.inventory = inventory;
    }
}
