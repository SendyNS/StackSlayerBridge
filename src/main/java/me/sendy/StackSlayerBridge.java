package me.sendy;

import dev.rosewood.rosestacker.api.RoseStackerAPI;
import dev.rosewood.rosestacker.api.stack.StackedEntity;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class StackSlayerBridge extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("StackSlayerBridge enabled!");
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {

        LivingEntity entity = event.getEntity();

        if (!(entity.getKiller() instanceof Player player))
            return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR)
            return;

        if (!hasStackSlayerLore(item))
            return;

        RoseStackerAPI api = RoseStackerAPI.getInstance();
        StackedEntity stackedEntity = api.getStackedEntity(entity);

        if (stackedEntity == null)
            return;

        stackedEntity.killEntireStack();
    }

    private boolean hasStackSlayerLore(ItemStack item) {
        if (!item.hasItemMeta())
            return false;

        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore())
            return false;

        return meta.getLore().stream()
                .anyMatch(line -> line.contains("StackSlayer"));
    }
}
