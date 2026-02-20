package me.sendy;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;

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

        Plugin roseStacker = Bukkit.getPluginManager().getPlugin("RoseStacker");
        if (roseStacker == null)
            return;

        try {
            Class<?> apiClass = Class.forName("dev.rosewood.rosestacker.api.RoseStackerAPI");
            Method getInstance = apiClass.getMethod("getInstance");
            Object api = getInstance.invoke(null);

            Method getStackedEntity = apiClass.getMethod("getStackedEntity", LivingEntity.class);
            Object stackedEntity = getStackedEntity.invoke(api, entity);

            if (stackedEntity == null)
                return;

            Method killEntireStack = stackedEntity.getClass().getMethod("killEntireStack");
            killEntireStack.invoke(stackedEntity);

        } catch (Exception e) {
            e.printStackTrace();
        }
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
