package me.cubecrafter.ultimate.listeners;

import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.events.player.PlayerBedBreakEvent;
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent;
import com.andrei1058.bedwars.api.events.player.PlayerReSpawnEvent;
import me.cubecrafter.ultimate.UltimatePlugin;
import me.cubecrafter.ultimate.config.Config;
import me.cubecrafter.ultimate.ultimates.Ultimate;
import me.cubecrafter.ultimate.utils.Cooldown;
import me.cubecrafter.ultimate.utils.Utils;
import me.cubecrafter.xutils.NumberUtil;
import me.cubecrafter.xutils.SoundUtil;
import me.cubecrafter.xutils.Tasks;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class KangarooListener implements Listener, Runnable {
    
    private final UltimatePlugin plugin;

    private final Map<Player, Cooldown> cooldowns = new HashMap<>();
    private final Map<Player, ItemStack[]> inventoryContents = new HashMap<>();

    public KangarooListener(UltimatePlugin plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        Tasks.repeat(this, 0, 1L);
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();

        if (!event.isFlying()) return;
        if (plugin.getUltimateManager().getUltimate(player) != Ultimate.KANGAROO) return;

        IArena arena = plugin.getBedWars().getArenaUtil().getArenaByPlayer(player);
        if (arena.isReSpawning(player)) return;
        if (!Utils.isUltimateArena(arena)) return;

        event.setCancelled(true);
        player.setAllowFlight(false);

        if (cooldowns.containsKey(player)) return;

        cooldowns.put(player, new Cooldown(10));

        player.setVelocity(player.getLocation().getDirection().multiply(0.5).setY(1));
        SoundUtil.play(player, "ENTITY_BAT_TAKEOFF");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();

        IArena arena = plugin.getBedWars().getArenaUtil().getArenaByPlayer(victim);
        if (!Utils.isUltimateArena(arena)) return;

        if (plugin.getUltimateManager().getUltimate(victim) == Ultimate.KANGAROO) {
            if (!NumberUtil.testChance(Config.KANGAROO_KEEP_RESOURCES_CHANCE.asInt())) {
                return;
            }
            PlayerInventory inventory = victim.getInventory();
            List<Integer> slots = new ArrayList<>();

            for (int i = 0; i < 36; i++) {
                ItemStack item = inventory.getItem(i);
                if (item == null || item.getType() == Material.AIR) continue;

                slots.add(i);
            }

            event.getDrops().clear();

            int slotsToKeep = slots.size() / 2;
            for (int i = 0; i < slotsToKeep; i++) {
                int index = ThreadLocalRandom.current().nextInt(slots.size());
                int slot = slots.get(index);

                event.getDrops().add(inventory.getItem(slot));
                inventory.clear(slot);

                slots.remove(index);
            }

            inventoryContents.put(victim, inventory.getContents());
        }
    }

    @EventHandler
    public void onKill(PlayerKillEvent event) {
        IArena arena = event.getArena();
        if (!Utils.isUltimateArena(arena)) return;

        Player killer = event.getKiller();
        if (killer == null) return;

        if (plugin.getUltimateManager().getUltimate(killer) == Ultimate.KANGAROO) {
            killer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1, false, false));
        }
    }

    @EventHandler
    public void onRespawn(PlayerReSpawnEvent event) {
        if (!Utils.isUltimateArena(event.getArena())) return;

        Player player = event.getPlayer();

        if (plugin.getUltimateManager().getUltimate(player) != Ultimate.KANGAROO) return;

        if (!inventoryContents.containsKey(player)) return;

        player.getInventory().setContents(inventoryContents.remove(player));
    }

    @EventHandler
    public void onBedBreak(PlayerBedBreakEvent event) {
        if (!Utils.isUltimateArena(event.getArena())) return;

        Player player = event.getPlayer();

        if (plugin.getUltimateManager().getUltimate(player) != Ultimate.KANGAROO) return;

        player.getInventory().addItem(new ItemStack(Material.MILK_BUCKET));
    }

    @Override
    public void run() {
        for (Map.Entry<Player, Cooldown> entry : cooldowns.entrySet()) {
            Cooldown cooldown = entry.getValue();
            Player player = entry.getKey();

            if (cooldown.isExpired()) {
                resetCooldown(player);
                player.setAllowFlight(true);
                continue;
            }

            player.setExp(cooldown.getPercentageLeft());
            player.setLevel(cooldown.getSecondsLeft());
        }
    }

    public void resetCooldown(Player player) {
        cooldowns.remove(player);
        player.setExp(0);
        player.setLevel(0);
    }

}
