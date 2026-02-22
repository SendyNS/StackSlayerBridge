package me.sendy;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.EventPriority;

import me.usainsrht.ujobs.UJobsPlugin;
import me.usainsrht.ujobs.managers.JobManager;
import me.usainsrht.ujobs.models.Action;
import me.usainsrht.ujobs.models.BuiltInActions;
import me.usainsrht.ujobs.models.Job;

import java.lang.reflect.Method;

public class StackSlayerBridge extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("StackSlayerBridge enabled!");
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
    
        LivingEntity entity = event.getEntity();
    
        if (!(entity.getKiller() instanceof Player player)) return;
    
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) return;
    
        if (!hasStackSlayerEnchant(item)) return;
    
        Plugin roseStacker = Bukkit.getPluginManager().getPlugin("RoseStacker");
        if (roseStacker == null) return;
    
        try {
            Class<?> apiClass = Class.forName("dev.rosewood.rosestacker.api.RoseStackerAPI");
            Object api = apiClass.getMethod("getInstance").invoke(null);
    
            Object stackedEntity = apiClass
                    .getMethod("getStackedEntity", LivingEntity.class)
                    .invoke(api, entity);
    
            if (stackedEntity == null) return;
    
            int stackSize = (int) stackedEntity
                    .getClass()
                    .getMethod("getStackSize")
                    .invoke(stackedEntity);
    
            if (stackSize <= 1) return;
    
            // 1 death already counted by Bukkit
            int extraKills = stackSize - 1;
    
            awardUJobsKills(player, entity, extraKills);
    
            // Now wipe remaining stack
            stackedEntity
                    .getClass()
                    .getMethod("killEntireStack")
                    .invoke(stackedEntity);
    
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
            
    private boolean hasStackSlayerEnchant(ItemStack item) {
        if (!item.hasItemMeta()) return false;
    
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
    
        NamespacedKey enchantsKey = new NamespacedKey("superenchants", "enchantments");
    
        if (!container.has(enchantsKey, PersistentDataType.TAG_CONTAINER))
            return false;
    
        PersistentDataContainer enchantsContainer =
                container.get(enchantsKey, PersistentDataType.TAG_CONTAINER);
    
        if (enchantsContainer == null)
            return false;
    
        NamespacedKey stackSlayerKey = new NamespacedKey("superenchants", "stackslayer");
    
        return enchantsContainer.has(stackSlayerKey, PersistentDataType.INTEGER);
    }
    
        private void awardUJobsKills(Player player, LivingEntity entity, int amount) {
    
        Plugin plugin = Bukkit.getPluginManager().getPlugin("UJobs");
        if (plugin == null) return;
    
        try {
            // Cast safely
            me.usainsrht.ujobs.UJobsPlugin ujobs =
                    (me.usainsrht.ujobs.UJobsPlugin) plugin;
    
            JobManager jobManager = ujobs.getJobManager();
    
            if (jobManager.shouldIgnore(player)) return;
    
            Action killAction = BuiltInActions.Entity.KILL;
    
            for (Job job : jobManager.getJobsWithAction(killAction)) {
                jobManager.processAction(
                        player,
                        killAction,
                        entity.getType().name(),
                        job,
                        amount
                );
            }
    
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
