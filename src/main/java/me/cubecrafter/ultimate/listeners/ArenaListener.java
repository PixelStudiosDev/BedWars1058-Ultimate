package me.cubecrafter.ultimate.listeners;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.events.gameplay.GameEndEvent;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent;
import com.andrei1058.bedwars.api.events.player.PlayerReSpawnEvent;
import com.cryptomorin.xseries.XSound;
import me.cubecrafter.ultimate.UltimatePlugin;
import me.cubecrafter.ultimate.config.Configuration;
import me.cubecrafter.ultimate.utils.TextUtil;
import me.cubecrafter.ultimate.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class ArenaListener implements Listener {

    private final UltimatePlugin plugin;

    public ArenaListener(UltimatePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!Configuration.DISABLE_FALL_DAMAGE.getAsBoolean()) return;
        if (!(e.getEntity() instanceof Player)) return;
        Player player = (Player) e.getEntity();
        IArena arena = plugin.getBedWars().getArenaUtil().getArenaByPlayer(player);
        if (!Utils.isUltimateArena(arena)) return;
        if (e.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent e) {
        IArena arena = plugin.getBedWars().getArenaUtil().getArenaByPlayer(e.getPlayer());
        if (!Utils.isUltimateArena(arena)) return;
        if (Utils.isUltimateItem(e.getItemDrop().getItemStack())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onRespawn(PlayerReSpawnEvent e) {
        if (!Utils.isUltimateArena(e.getArena())) return;
        Player player = e.getPlayer();
        Utils.giveUltimateItems(player);
    }

    @EventHandler
    public void onGameStart(GameStateChangeEvent e) {
        IArena arena = e.getArena();
        if (!Configuration.ARENA_GROUPS.getAsStringList().contains(arena.getGroup())) return;
        if (e.getNewState() == GameState.playing) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                plugin.getUltimateManager().registerArena(arena);
                arena.getPlayers().forEach(Utils::giveUltimateItems);
                TextUtil.sendMessage(arena.getPlayers(), Configuration.MESSAGE_ULTIMATES_ENABLED.getAsStringList());
                arena.getPlayers().forEach(player -> XSound.play(player, Configuration.ULTIMATES_ENABLED_SOUND.getAsString()));
            }, Configuration.ULTIMATES_ENABLED_DELAY.getAsInt() * 20L);
        }
    }

    @EventHandler
    public void onGameEnd(GameEndEvent e) {
        IArena arena = e.getArena();
        if (!Utils.isUltimateArena(arena)) return;
        plugin.getUltimateManager().unregisterArena(arena);
        for (Player player : arena.getPlayers()) {
            Utils.clearUltimateItems(player);
            Utils.removeActiveCooldowns(player);
        }
    }

    @EventHandler
    public void onDeath(PlayerKillEvent e) {
        IArena arena = e.getArena();
        if (!Utils.isUltimateArena(arena)) return;
        Player player = e.getVictim();
        Utils.removeActiveCooldowns(player);
    }

}
