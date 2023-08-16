package me.cubecrafter.ultimate.listeners;

import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.events.player.PlayerBedBreakEvent;
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent;
import me.cubecrafter.ultimate.UltimatePlugin;
import me.cubecrafter.ultimate.ultimates.Ultimate;
import me.cubecrafter.ultimate.utils.Cooldown;
import me.cubecrafter.ultimate.utils.Utils;
import me.cubecrafter.xutils.BlockUtil;
import me.cubecrafter.xutils.Tasks;
import me.cubecrafter.xutils.item.ItemBuilder;
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

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
                continue;
            }

            player.setExp(cooldown.getPercentageLeft());
            player.setLevel(cooldown.getSecondsLeft());
        }
    }

    @EventHandler
    public void onIgnite(BlockIgniteEvent event) {
        if (event.getCause() != BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL) return;

        Player player = event.getPlayer();
        IArena arena = plugin.getBedWars().getArenaUtil().getArenaByPlayer(player);

        if (!Utils.isUltimateArena(arena)) return;
        if (plugin.getUltimateManager().getUltimate(player) != Ultimate.DEMOLITION) return;

        event.setCancelled(true);

        if (cooldowns.containsKey(player)) return;

        cooldowns.put(player, new Cooldown(10));

        BlockFace face = BlockUtil.getTargetedFace(event.getPlayer());
        Block ignited = event.getBlock().getRelative(face.getOppositeFace());
        if (!ignited.getType().toString().endsWith("WOOL") || !arena.isBlockPlaced(ignited)) return;

        handleDemolition(arena, ignited);
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
                killer.getInventory().addItem(new ItemStack(ThreadLocalRandom.current().nextBoolean() ? Material.FIREBALL : Material.TNT));
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof TNTPrimed) || !(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        TNTPrimed tnt = (TNTPrimed) event.getDamager();
        IArena arena = plugin.getBedWars().getArenaUtil().getArenaByPlayer(player);

        if (!Utils.isUltimateArena(arena)) return;
        if (!tnt.hasMetadata("ultimate")) return;

        if (tnt.getMetadata("ultimate").get(0).asString().equals(arena.getTeam(player).getName())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBedBreak(PlayerBedBreakEvent event) {
        Player player = event.getPlayer();
        IArena arena = plugin.getBedWars().getArenaUtil().getArenaByPlayer(player);

        if (!Utils.isUltimateArena(arena)) return;
        if (plugin.getUltimateManager().getUltimate(player) != Ultimate.DEMOLITION) return;

        ItemStack item = new ItemBuilder("CREEPER_SPAWN_EGG").build();
        player.getInventory().addItem(item);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getItem() == null || event.getItem().getType() != Material.MONSTER_EGG) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getBlockFace() != BlockFace.UP) return;

        Player player = event.getPlayer();
        if (plugin.getUltimateManager().getUltimate(player) != Ultimate.DEMOLITION) return;

        IArena arena = plugin.getBedWars().getArenaUtil().getArenaByPlayer(player);
        if (!Utils.isUltimateArena(arena)) return;

        SpawnEgg egg = (SpawnEgg) event.getItem().getData();
        if (egg.getSpawnedType() != EntityType.CREEPER) return;

        event.setCancelled(true);
        event.getClickedBlock().getWorld().spawnEntity(event.getClickedBlock().getLocation().add(0, 1, 0), EntityType.CREEPER);

        player.getInventory().remove(event.getItem());
    }

    private void handleDemolition(IArena arena, Block block) {
        Set<Block> burning = new HashSet<>();
        Set<Block> blocks = new HashSet<>();

        blocks.add(block);

        for (int i = 0; i < 20; i++) {
            Set<Block> temp = new HashSet<>(blocks);
            blocks.clear();

            for (Block wool : temp) {

                if (arena.isBlockPlaced(wool)) {
                    burning.add(wool);

                    Block fire = wool.getRelative(BlockFace.UP);

                    if (fire.getType() == Material.AIR) {
                        fire.setType(Material.FIRE);
                    }
                }

                blocks.addAll(getNearbyWool(wool));
            }
        }

        for (Block wool : burning) {
            Tasks.later(() -> wool.setType(Material.AIR), ThreadLocalRandom.current().nextInt(10, 40));
        }
    }

    private Set<Block> getNearbyWool(Block block) {
        Set<Block> blocks = new HashSet<>();
        for (BlockFace face : BlockFace.values()) {
            if (face == BlockFace.SELF) continue;

            if (block.getRelative(face).getType().toString().endsWith("WOOL")) {
                blocks.add(block.getRelative(face));
            }
        }
        return blocks;
    }

    public void resetCooldown(Player player) {
        cooldowns.remove(player);
        player.setExp(0);
        player.setLevel(0);
    }

}
