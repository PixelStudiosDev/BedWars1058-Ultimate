package me.cubecrafter.ultimate.listeners;

import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.events.player.PlayerBedBreakEvent;
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent;
import com.andrei1058.bedwars.api.events.player.PlayerReSpawnEvent;
import me.cubecrafter.ultimate.UltimatePlugin;
import me.cubecrafter.ultimate.ultimates.Ultimate;
import me.cubecrafter.ultimate.utils.Cooldown;
import me.cubecrafter.ultimate.utils.Utils;
import me.cubecrafter.xutils.SoundUtil;
import me.cubecrafter.xutils.Tasks;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

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
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        IArena arena = plugin.getBedWars().getArenaUtil().getArenaByPlayer(player);
        if (!Utils.isUltimateArena(arena)) return;

        event.setCancelled(true);
        player.setAllowFlight(false);

        if (cooldowns.containsKey(player)) return;

        cooldowns.put(player, new Cooldown(10));

        player.setVelocity(player.getLocation().getDirection().multiply(0.5).setY(1));
        SoundUtil.play(player, "ENTITY_BAT_TAKEOFF");
    }

    @EventHandler
    public void onKill(PlayerKillEvent event) {
        if (!Utils.isUltimateArena(event.getArena())) return;

        Player victim = event.getVictim();
        Player killer = event.getKiller();

        if (plugin.getUltimateManager().getUltimate(victim) == Ultimate.KANGAROO) {
            inventoryContents.put(victim, victim.getInventory().getContents());
        }

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
