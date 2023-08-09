package me.cubecrafter.ultimate.listeners;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.cryptomorin.xseries.ReflectionUtils;
import com.cryptomorin.xseries.XSound;
import me.cubecrafter.ultimate.UltimatePlugin;
import me.cubecrafter.ultimate.config.Configuration;
import me.cubecrafter.ultimate.ultimates.Ultimate;
import me.cubecrafter.ultimate.utils.TextUtil;
import me.cubecrafter.ultimate.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Bed;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class BuilderListener implements Listener, Runnable {

    private final UltimatePlugin plugin;

    public BuilderListener(UltimatePlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        Bukkit.getScheduler().runTaskTimer(plugin, this, 0, 20L);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        ItemStack item = e.getItem();
        if (item == null) return;
        Player player = e.getPlayer();
        IArena arena = plugin.getBedWars().getArenaUtil().getArenaByPlayer(player);
        if (!Utils.isUltimateArena(arena)) return;
        Ultimate ultimate = plugin.getUltimateManager().getUltimate(player);
        if (ultimate != Ultimate.BUILDER) return;
        String tag = Utils.getTag(item, "ultimate");
        if (tag.equals("wall-item")) {
            if (e.getAction().equals(Action.LEFT_CLICK_AIR) || e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                player.getInventory().setItem(player.getInventory().getHeldItemSlot(), Utils.getBridgeItem());
            } else if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                if (Utils.isBed(e.getClickedBlock().getType())) {
                    protectBed(player, arena, e.getClickedBlock());
                } else {
                    if (arena.isProtected(e.getClickedBlock().getRelative(e.getBlockFace()).getLocation())) {
                        TextUtil.sendMessage(player, Configuration.CANT_PLACE.getAsString());
                        return;
                    }
                    buildWall(player, e.getClickedBlock(), arena, e.getBlockFace());
                }
            }
            player.updateInventory();
        } else if (tag.equals("bridge-item")) {
            if (e.getAction().equals(Action.LEFT_CLICK_AIR) || e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                player.getInventory().setItem(player.getInventory().getHeldItemSlot(), Utils.getWallItem());
            } else if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                if (Utils.isBed(e.getClickedBlock().getType())) {
                    protectBed(player, arena, e.getClickedBlock());
                } else {
                    if (arena.isProtected(e.getClickedBlock().getRelative(e.getBlockFace()).getLocation())) {
                        TextUtil.sendMessage(player, Configuration.CANT_PLACE.getAsString());
                        return;
                    }
                    buildBridge(player, e.getClickedBlock(), arena, e.getBlockFace());
                }
            }
            player.updateInventory();
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        IArena arena = plugin.getBedWars().getArenaUtil().getArenaByPlayer(player);
        if (!Utils.isUltimateArena(arena)) return;
        if (plugin.getUltimateManager().getUltimate(player) != Ultimate.BUILDER) return;
        if (Utils.isUltimateItem(e.getItemInHand())) {
            e.setCancelled(true);
        }
    }

    private void buildBridge(final Player player, final Block clicked, final IArena arena, final BlockFace clickedFace) {
        BlockFace face = yawToFace(player.getEyeLocation().getYaw());
        ITeam team = arena.getTeam(player);
        new BukkitRunnable() {

            Block block = clicked;
            int placed = 0;

            @Override
            public void run() {
                if (clickedFace.equals(BlockFace.UP) || clickedFace.equals(BlockFace.DOWN)) {
                    block = block.getRelative(face);
                } else {
                    block = block.getRelative(clickedFace);
                }
                if (arena.isProtected(block.getLocation())) {
                    TextUtil.sendMessage(player, Configuration.CANT_BUILD_BRIDGE.getAsString());
                    cancel();
                    return;
                }
                if (!block.getType().equals(Material.AIR)) {
                    cancel();
                    return;
                }
                if (block.getWorld().getNearbyEntities(block.getLocation(), 0.5, 0.5, 0.5).size() > 0) {
                    cancel();
                    return;
                }
                int wool = player.getInventory().first(Material.WOOL);
                if (wool != -1) {
                    if (player.getInventory().getItem(wool).getAmount() > 1) {
                        player.getInventory().getItem(wool).setAmount(player.getInventory().getItem(wool).getAmount() - 1);
                    } else {
                        player.getInventory().setItem(wool, null);
                    }
                } else {
                    TextUtil.sendMessage(player, Configuration.RAN_OUT_OF_WOOL_BLOCKS.getAsString());
                    cancel();
                    return;
                }
                if (ReflectionUtils.supports(13)) {
                    block.setType(Utils.getMaterial(team.getColor()));
                } else {
                    block.setType(Material.WOOL);
                    block.setData(team.getColor().itemByte());
                }
                arena.addPlacedBlock(block);
                XSound.play(player, "ENTITY_CHICKEN_EGG");
                placed++;
                if (placed == 10) {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 2L);
    }

    private void buildWall(final Player player, final Block clicked, final IArena arena, final BlockFace clickedFace) {
        if (clickedFace != BlockFace.UP) return;
        BlockFace face = yawToFace(player.getEyeLocation().getYaw());
        ITeam team = arena.getTeam(player);
        List<Block> row = new ArrayList<>();
        Block bottomCenter = clicked.getRelative(BlockFace.UP);
        row.add(bottomCenter);
        switch (face) {
            case NORTH:
            case SOUTH:
                row.add(bottomCenter.getRelative(BlockFace.EAST));
                row.add(bottomCenter.getRelative(BlockFace.WEST));
                row.add(bottomCenter.getRelative(BlockFace.EAST, 2));
                row.add(bottomCenter.getRelative(BlockFace.WEST, 2));
                break;
            case EAST:
            case WEST:
                row.add(bottomCenter.getRelative(BlockFace.NORTH));
                row.add(bottomCenter.getRelative(BlockFace.SOUTH));
                row.add(bottomCenter.getRelative(BlockFace.NORTH, 2));
                row.add(bottomCenter.getRelative(BlockFace.SOUTH, 2));
                break;
        }
        new BukkitRunnable() {

            int rows = 0;

            @Override
            public void run() {
                for (Block block : row) {
                    if (arena.isProtected(block.getLocation())) {
                        TextUtil.sendMessage(player, Configuration.CANT_WALL_BRIDGE.getAsString());
                        cancel();
                        return;
                    }
                    if (!block.getType().equals(Material.AIR)) {
                        continue;
                    }
                    int wool = player.getInventory().first(Material.WOOL);
                    if (wool != -1) {
                        if (player.getInventory().getItem(wool).getAmount() > 1) {
                            player.getInventory().getItem(wool).setAmount(player.getInventory().getItem(wool).getAmount() - 1);
                        } else {
                            player.getInventory().setItem(wool, null);
                        }
                    } else {
                        TextUtil.sendMessage(player, Configuration.RAN_OUT_OF_WOOL_BLOCKS.getAsString());
                        cancel();
                        continue;
                    }
                    if (ReflectionUtils.supports(13)) {
                        block.setType(Utils.getMaterial(team.getColor()));
                    } else {
                        block.setType(Material.WOOL);
                        block.setData(team.getColor().itemByte());
                    }
                    arena.addPlacedBlock(block);
                }
                XSound.play(player, "ENTITY_CHICKEN_EGG");
                rows++;
                if (rows == 5) {
                    cancel();
                    return;
                }
                row.replaceAll(block -> block.getRelative(BlockFace.UP));
            }

        }.runTaskTimer(plugin, 0, 2L);
    }

    private void protectBed(Player player, IArena arena, Block block) {
        ITeam team = arena.getTeam(player);
        if (team.getBed().distanceSquared(block.getLocation()) > 1) {
            TextUtil.sendMessage(player, Configuration.CANT_PROTECT_BED.getAsString());
            return;
        }
        int slot = player.getInventory().first(Material.WOOL);
        if (slot == -1) {
            TextUtil.sendMessage(player, Configuration.RAN_OUT_OF_WOOL_BLOCKS.getAsString());
            return;
        }
        Bed bed = new Bed(block.getType(), block.getData());
        Block secondBlock;
        BlockFace face = bed.getFacing();
        if (bed.isHeadOfBed()) {
            secondBlock = block.getRelative(face.getOppositeFace());
        } else {
            secondBlock = block.getRelative(face);
        }
        XSound.play(player, "ENTITY_CHICKEN_EGG");
        for (BlockFace check : new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP}) {
            for (Block part : new Block[]{block, secondBlock}) {
                Block checked = part.getRelative(check);
                if (!checked.getType().equals(Material.AIR)) continue;
                int wool = player.getInventory().first(Material.WOOL);
                if (wool != -1) {
                    ItemStack item = player.getInventory().getItem(wool);
                    if (item.getAmount() > 1) {
                        item.setAmount(item.getAmount() - 1);
                    } else {
                        player.getInventory().clear(wool);
                    }
                } else {
                    TextUtil.sendMessage(player, Configuration.RAN_OUT_OF_WOOL_BLOCKS.getAsString());
                    return;
                }
                if (ReflectionUtils.supports(13)) {
                    checked.setType(Utils.getMaterial(team.getColor()));
                } else {
                    checked.setType(Material.WOOL);
                    checked.setData(team.getColor().itemByte());
                }
                arena.addPlacedBlock(checked);
            }
        }
    }

    public BlockFace yawToFace(float yaw) {
        return axis[Math.round(yaw / 90f) & 0x3].getOppositeFace();
    }

    private final BlockFace[] axis = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

    @Override
    public void run() {
        for (IArena arena : plugin.getBedWars().getArenaUtil().getArenas()) {
            if (arena.getStatus() != GameState.playing || !Utils.isUltimateArena(arena)) continue;
            for (Player player : arena.getPlayers()) {
                if (arena.isReSpawning(player) || plugin.getUltimateManager().getUltimate(player) != Ultimate.BUILDER) continue;
                ITeam team = arena.getTeam(player);
                player.getInventory().addItem(Utils.getWool(team.getColor()));
            }
        }
    }

}
