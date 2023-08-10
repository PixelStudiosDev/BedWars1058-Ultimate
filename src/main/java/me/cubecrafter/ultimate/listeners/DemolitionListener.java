package me.cubecrafter.ultimate.listeners;

import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.events.player.PlayerBedBreakEvent;
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent;
import me.cubecrafter.ultimate.UltimatePlugin;
import me.cubecrafter.ultimate.ultimates.Ultimate;
import me.cubecrafter.ultimate.utils.Cooldown;
import me.cubecrafter.ultimate.utils.Utils;
import me.cubecrafter.xutils.Tasks;
import me.cubecrafter.xutils.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.SpawnEgg;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;

public class DemolitionListener implements Listener, Runnable {

    private final UltimatePlugin plugin;

    private final Map<Player, Integer> killCount = new HashMap<>();
    private final Map<Player, Cooldown> cooldowns = new HashMap<>();

    public DemolitionListener(UltimatePlugin plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        Tasks.repeat(this, 0, 1L);
    }

    @Override
    public void run() {
        for (Map.Entry<Player, Cooldown> entry : cooldowns.entrySet()) {
            Cooldown cooldown = entry.getValue();
            Player player = entry.getKey();
            if (cooldown.isExpired()) {
                resetCooldown(player);
                player.setAllowFlight(false);
                continue;
            }
            player.setExp(cooldown.getPercentageLeft());
            player.setLevel(cooldown.getSecondsLeft());
        }
    }

    @EventHandler
    public void onIgnite(BlockIgniteEvent e) {
        if (e.getCause() != BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL) return;
        Player player = e.getPlayer();
        IArena arena = plugin.getBedWars().getArenaUtil().getArenaByPlayer(player);
        if (!Utils.isUltimateArena(arena)) return;
        if (plugin.getUltimateManager().getUltimate(player) != Ultimate.DEMOLITION) return;
        e.setCancelled(true);
        if (cooldowns.containsKey(player)) return;
        cooldowns.put(player, new Cooldown(10));
        Block ignited = e.getBlock().getRelative(BlockFace.DOWN);
        if (!ignited.getType().toString().endsWith("WOOL") || !arena.isBlockPlaced(ignited)) return;
        handleDemolition(player, arena, ignited);
    }

    @EventHandler
    public void onKill(PlayerKillEvent event) {
        Player player = event.getVictim();
        IArena arena = plugin.getBedWars().getArenaUtil().getArenaByPlayer(player);
        if (!Utils.isUltimateArena(arena)) return;
        if (plugin.getUltimateManager().getUltimate(player) == Ultimate.DEMOLITION) {
            TNTPrimed tnt = (TNTPrimed) player.getWorld().spawnEntity(player.getLocation(), EntityType.PRIMED_TNT);
            tnt.setIsIncendiary(false);
            tnt.setMetadata("ultimate", new FixedMetadataValue(plugin, arena.getTeam(player).getName()));
        }
        Player killer = event.getKiller();
        if (killer == null) return;
        if (plugin.getUltimateManager().getUltimate(killer) == Ultimate.DEMOLITION) {
            killCount.merge(killer, 1, Integer::sum);
            if (killCount.get(killer) == 3) {
                killCount.remove(killer);
                killer.getInventory().addItem(new Random().nextBoolean() ? new ItemStack(Material.FIREBALL) : new ItemStack(Material.TNT));
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof TNTPrimed) || !(e.getEntity() instanceof Player)) return;
        Player player = (Player) e.getEntity();
        TNTPrimed tnt = (TNTPrimed) e.getDamager();
        IArena arena = plugin.getBedWars().getArenaUtil().getArenaByPlayer(player);
        if (!Utils.isUltimateArena(arena)) return;
        if (!tnt.hasMetadata("ultimate")) return;
        if (tnt.getMetadata("ultimate").get(0).asString().equals(arena.getTeam(player).getName())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBedBreak(PlayerBedBreakEvent e) {
        Player player = e.getPlayer();
        IArena arena = plugin.getBedWars().getArenaUtil().getArenaByPlayer(player);
        if (!Utils.isUltimateArena(arena)) return;
        if (plugin.getUltimateManager().getUltimate(player) != Ultimate.DEMOLITION) return;
        ItemStack egg = new ItemBuilder("CREEPER_SPAWN_EGG").build();
        player.getInventory().addItem(egg);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getBlockFace() != BlockFace.UP) return;
        Player player = e.getPlayer();
        if (plugin.getUltimateManager().getUltimate(player) != Ultimate.DEMOLITION) return;
        IArena arena = plugin.getBedWars().getArenaUtil().getArenaByPlayer(player);
        if (!Utils.isUltimateArena(arena)) return;
        if (e.getItem() == null) return;
        if (e.getItem().getType() != Material.MONSTER_EGG) return;
        SpawnEgg egg = (SpawnEgg) e.getItem().getData();
        if (egg.getSpawnedType() != EntityType.CREEPER) return;
        e.setCancelled(true);
        e.getClickedBlock().getWorld().spawnEntity(e.getClickedBlock().getLocation().add(0, 1, 0), EntityType.CREEPER);
        player.getInventory().remove(e.getItem());
    }

    private void handleDemolition(Player player, IArena arena, Block block) {
        Set<Block> burning = new HashSet<>();
        Set<Block> blocks = new HashSet<>();
        blocks.add(block);
        for (int i = 0; i < 20; i++) {
            Set<Block> temp = new HashSet<>(blocks);
            blocks.clear();
            for (Block wool : temp) {
                Block up = wool.getRelative(BlockFace.UP);
                if (up.getType() == Material.AIR && arena.isBlockPlaced(wool)) {
                    up.setType(Material.FIRE);
                    burning.add(wool);
                }
                blocks.addAll(getNearbyWool(wool));
            }
        }
        Random random = new Random();
        for (Block wool : burning) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> wool.setType(Material.AIR), random.nextInt(30) + 10);
        }
    }

    private Set<Block> getNearbyWool(Block block) {
        Set<Block> wool = new HashSet<>();
        for (BlockFace face : BlockFace.values()) {
            if (face == BlockFace.SELF) continue;
            if (block.getRelative(face).getType() == Material.WOOL) {
                wool.add(block.getRelative(face));
            }
        }
        return wool;
    }

    public void resetCooldown(Player player) {
        cooldowns.remove(player);
        player.setExp(0);
        player.setLevel(0);
    }

}
