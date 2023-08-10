package me.cubecrafter.ultimate.listeners;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.events.gameplay.GameEndEvent;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent;
import com.andrei1058.bedwars.api.events.player.PlayerLeaveArenaEvent;
import com.andrei1058.bedwars.api.events.player.PlayerReSpawnEvent;
import lombok.RequiredArgsConstructor;
import me.cubecrafter.ultimate.UltimatePlugin;
import me.cubecrafter.ultimate.config.Config;
import me.cubecrafter.ultimate.utils.Utils;
import me.cubecrafter.xutils.SoundUtil;
import me.cubecrafter.xutils.Tasks;
import me.cubecrafter.xutils.text.TextUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

@RequiredArgsConstructor
public class ArenaListener implements Listener {

    private final UltimatePlugin plugin;

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!Config.DISABLE_FALL_DAMAGE.getAsBoolean()) return;
        if (!(event.getEntity() instanceof Player)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;

        Player player = (Player) event.getEntity();
        IArena arena = plugin.getBedWars().getArenaUtil().getArenaByPlayer(player);

        if (!Utils.isUltimateArena(arena)) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        IArena arena = plugin.getBedWars().getArenaUtil().getArenaByPlayer(event.getPlayer());
        if (!Utils.isUltimateArena(arena)) return;

        if (Utils.isUltimateItem(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onRespawn(PlayerReSpawnEvent event) {
        if (!Utils.isUltimateArena(event.getArena())) return;
        Player player = event.getPlayer();

        Utils.giveUltimateItems(player);
    }

    @EventHandler
    public void onGameStart(GameStateChangeEvent event) {
        if (event.getNewState() != GameState.playing) return;

        IArena arena = event.getArena();
        if (!Config.ARENA_GROUPS.getAsStringList().contains(arena.getGroup())) return;

        Tasks.later(() -> {
            plugin.getUltimateManager().registerArena(arena);
            arena.getPlayers().forEach(Utils::giveUltimateItems);

            TextUtil.sendMessages(arena.getPlayers(), Config.MESSAGE_ULTIMATES_ENABLED.getAsStringList());
            SoundUtil.play(arena.getPlayers(), Config.ULTIMATES_ENABLED_SOUND.getAsString());
        }, Config.ULTIMATES_ENABLED_DELAY.getAsInt() * 20L);
    }

    @EventHandler
    public void onGameEnd(GameEndEvent event) {
        IArena arena = event.getArena();
        if (!Utils.isUltimateArena(arena)) return;

        plugin.getUltimateManager().unregisterArena(arena);

        for (Player player : arena.getPlayers()) {
            Utils.clearUltimateItems(player);
            Utils.resetCooldowns(player);
        }
    }

    @EventHandler
    public void onDeath(PlayerKillEvent event) {
        IArena arena = event.getArena();
        if (!Utils.isUltimateArena(arena)) return;

        Utils.resetCooldowns(event.getVictim());
    }

    @EventHandler
    public void onLeave(PlayerLeaveArenaEvent event) {
        IArena arena = event.getArena();
        if (!Utils.isUltimateArena(arena)) return;

        Player player = event.getPlayer();

        Utils.clearUltimateItems(player);
        Utils.resetCooldowns(player);
    }

}
