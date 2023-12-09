package me.cubecrafter.ultimate.listeners;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent;
import me.cubecrafter.ultimate.UltimatePlugin;
import me.cubecrafter.ultimate.config.Config;
import me.cubecrafter.ultimate.ultimates.Ultimate;
import me.cubecrafter.ultimate.utils.BlockingUtil;
import me.cubecrafter.ultimate.utils.Cooldown;
import me.cubecrafter.ultimate.utils.Utils;
import me.cubecrafter.xutils.ReflectionUtil;
import me.cubecrafter.xutils.SoundUtil;
import me.cubecrafter.xutils.Tasks;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import java.sql.Ref;
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
        Tasks.repeat(this, 0L, 1L);
    }

    @EventHandler
    public void onHotbarSlotChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        if (blocking.containsKey(player)) {
            blocking.remove(player);
            player.setExp(0);
        }
    }

    @EventHandler
    public void onKill(PlayerKillEvent event) {
        if (event.getKiller() == null) return;
        if (!Utils.isUltimateArena(event.getArena())) return;

        Player player = event.getKiller();
        if (plugin.getUltimateManager().getUltimate(player) != Ultimate.SWORDSMAN) return;

        resetCooldown(player);
        player.setHealth(Math.min(player.getHealth() + 2, player.getMaxHealth()));
    }

    @Override
    public void run() {
        for (IArena arena : plugin.getBedWars().getArenaUtil().getArenas()) {
            if (arena.getStatus() != GameState.playing) continue;
            if (!Utils.isUltimateArena(arena)) continue;

            for (Player player : arena.getPlayers()) {
                if (plugin.getUltimateManager().getUltimate(player) != Ultimate.SWORDSMAN) continue;

                if (!cooldowns.containsKey(player)) {
                    if (BlockingUtil.isBlocking(player) && (!blocking.containsKey(player) || blocking.get(player) < 40)) {
                        blocking.merge(player, 1, Integer::sum);
                        player.setExp(blocking.get(player) / 40f);

                        if (blocking.get(player) % 5 == 0) {
                            SoundUtil.play(player, Config.SWORDSMAN_LOADING_SOUND.asString());
                        }
                    } else if (!BlockingUtil.isBlocking(player) && blocking.containsKey(player)) {
                        recall.put(player, player.getLocation());

                        player.setVelocity(player.getLocation().getDirection().multiply(blocking.get(player) * 0.08).setY(blocking.get(player) * 0.03));
                        SoundUtil.play(player, "FIREWORK_LAUNCH");

                        blocking.remove(player);
                        cooldowns.put(player, new Cooldown(10));
                    }
                } else if (cooldowns.get(player).getSecondsLeft() > 5 && BlockingUtil.isBlocking(player) && recall.containsKey(player)) {
                    player.teleport(recall.remove(player));
                    SoundUtil.play(player, "ENDERMAN_TELEPORT");
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
