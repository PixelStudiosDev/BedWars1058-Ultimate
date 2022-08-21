package me.cubecrafter.ultimate.listeners;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent;
import com.cryptomorin.xseries.XSound;
import me.cubecrafter.ultimate.UltimatePlugin;
import me.cubecrafter.ultimate.ultimates.Ultimate;
import me.cubecrafter.ultimate.utils.Cooldown;
import me.cubecrafter.ultimate.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;

import java.util.HashMap;
import java.util.Map;

public class SwordsmanListener implements Listener, Runnable {

    private final UltimatePlugin plugin;
    private final Map<Player, Cooldown> cooldowns = new HashMap<>();
    private final Map<Player, Integer> blocking = new HashMap<>();
    private final Map<Player, Location> recall = new HashMap<>();

    public SwordsmanListener(UltimatePlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        Bukkit.getScheduler().runTaskTimer(plugin, this, 0L, 1L);
    }

    @EventHandler
    public void onHotbarSlotChange(PlayerItemHeldEvent e) {
        Player player = e.getPlayer();
        if (blocking.containsKey(player)) {
            blocking.remove(player);
            player.setExp(0);
        }
    }

    @EventHandler
    public void onKill(PlayerKillEvent e) {
        if (e.getKiller() == null) return;
        Player player = e.getKiller();
        if (plugin.getUltimateManager().getUltimate(player) != Ultimate.SWORDSMAN) return;
        cooldowns.remove(player);
        player.setExp(0);
        player.setLevel(0);
        player.setHealth(player.getHealth() + 2);
    }

    @Override
    public void run() {
        for (IArena arena : plugin.getBedWars().getArenaUtil().getArenas()) {
            if (!Utils.isUltimateArena(arena)) continue;
            if (arena.getStatus() != GameState.playing) continue;
            for (Player player : arena.getPlayers()) {
                if (plugin.getUltimateManager().getUltimate(player) != Ultimate.SWORDSMAN) continue;
                if (!cooldowns.containsKey(player)) {
                    if (player.isBlocking() && (!blocking.containsKey(player) || blocking.get(player) < 40)) {
                        blocking.merge(player, 1, Integer::sum);
                        player.setExp(blocking.get(player) / 40f);
                    } else if (!player.isBlocking() && blocking.containsKey(player)) {
                        recall.put(player, player.getLocation());
                        player.setVelocity(player.getLocation().getDirection().multiply(blocking.get(player) * 0.08).setY(blocking.get(player) * 0.03));
                        XSound.play(player, "FIREWORK_LAUNCH");
                        blocking.remove(player);
                        cooldowns.put(player, new Cooldown(10));
                    }
                } else if (cooldowns.get(player).getSecondsLeft() > 5 && player.isBlocking() && recall.containsKey(player)) {
                    Location location = recall.get(player);
                    player.teleport(location);
                    recall.remove(player);
                    XSound.play(player, "ENDERMAN_TELEPORT");
                }
            }
        }
        for (Map.Entry<Player, Cooldown> entry : cooldowns.entrySet()) {
            Player player = entry.getKey();
            Cooldown cooldown = entry.getValue();
            if (cooldown.isExpired()) {
                resetCooldown(player);
                continue;
            }
            player.setExp(cooldown.getPercentageLeft());
            player.setLevel(cooldown.getSecondsLeft());
        }
    }

    public void resetCooldown(Player player) {
        cooldowns.remove(player);
        blocking.remove(player);
        recall.remove(player);
        player.setExp(0);
        player.setLevel(0);
    }

}
