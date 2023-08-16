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
import me.cubecrafter.ultimate.ultimates.Ultimate;
import me.cubecrafter.ultimate.utils.Utils;
import me.cubecrafter.xutils.NumberUtil;
import me.cubecrafter.xutils.SoundUtil;
import me.cubecrafter.xutils.Tasks;
import me.cubecrafter.xutils.text.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
public class ArenaListener implements Listener {

    private final UltimatePlugin plugin;

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!Config.DISABLE_FALL_DAMAGE.asBoolean()) return;
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
        if (!Config.ARENA_GROUPS.asStringList().contains(arena.getGroup())) return;

        Tasks.later(() -> {
            plugin.getUltimateManager().registerArena(arena);
            arena.getPlayers().forEach(Utils::giveUltimateItems);

            TextUtil.sendMessages(arena.getPlayers(), Config.MESSAGE_ULTIMATES_ENABLED.asStringList());
            SoundUtil.play(arena.getPlayers(), Config.ULTIMATES_ENABLED_SOUND.asString());
        }, Config.ULTIMATES_ENABLED_DELAY.asInt() * 20L);
    }

    @EventHandler
    public void onGameEnd(GameEndEvent event) {
        IArena arena = event.getArena();
        if (!Utils.isUltimateArena(arena)) return;

        plugin.getUltimateManager().unregisterArena(arena);

        for (Player player : arena.getPlayers()) {
            Utils.clearUltimateItems(player);
            Utils.resetCooldowns(player);

            plugin.getUltimateManager().clearUltimate(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onKill(PlayerKillEvent event) {
        Player player = event.getVictim();
        IArena arena = event.getArena();
        if (!Utils.isUltimateArena(arena)) return;

        Utils.resetCooldowns(player);

        if (event.getCause().isFinalKill()) {
            Utils.clearUltimateItems(player);
            plugin.getUltimateManager().clearUltimate(player);
        }
    }

    @EventHandler
    public void onLeave(PlayerLeaveArenaEvent event) {
        IArena arena = event.getArena();
        if (!Utils.isUltimateArena(arena)) return;

        Player player = event.getPlayer();

        Utils.clearUltimateItems(player);
        Utils.resetCooldowns(player);

        plugin.getUltimateManager().clearUltimate(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {
        event.getDrops().removeIf(Utils::isUltimateItem);
    }

}
