package me.cubecrafter.ultimate.listeners;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.cryptomorin.xseries.ReflectionUtils;
import me.cubecrafter.ultimate.UltimatePlugin;
import me.cubecrafter.ultimate.config.Config;
import me.cubecrafter.ultimate.ultimates.Ultimate;
import me.cubecrafter.ultimate.utils.Utils;
import me.cubecrafter.xutils.ReflectionUtil;
import me.cubecrafter.xutils.SoundUtil;
import me.cubecrafter.xutils.Tasks;
import me.cubecrafter.xutils.item.ItemUtil;
import me.cubecrafter.xutils.text.TextUtil;
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
        Tasks.repeat(this, 0, 20L);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null) return;

        Player player = event.getPlayer();
        IArena arena = plugin.getBedWars().getArenaUtil().getArenaByPlayer(player);
        if (!Utils.isUltimateArena(arena)) return;

        Ultimate ultimate = plugin.getUltimateManager().getUltimate(player);
        if (ultimate != Ultimate.BUILDER) return;

        String tag = ItemUtil.getTag(item, "ultimate");
        if (tag == null) return;

        if (tag.equals("wall-item")) {
            if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
                player.getInventory().setItem(player.getInventory().getHeldItemSlot(), Utils.getBridgeItem());
            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (Utils.isBed(event.getClickedBlock())) {
                    protectBed(player, arena, event.getClickedBlock());
                } else {
                    if (arena.isProtected(event.getClickedBlock().getRelative(event.getBlockFace()).getLocation())) {
                        TextUtil.sendMessage(player, Config.CANT_PLACE.asString());
                        return;
                    }
                    buildWall(player, event.getClickedBlock(), arena, event.getBlockFace());
                }
            }
            player.updateInventory();

        } else if (tag.equals("bridge-item")) {
            if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
                player.getInventory().setItem(player.getInventory().getHeldItemSlot(), Utils.getWallItem());
            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (Utils.isBed(event.getClickedBlock())) {
                    protectBed(player, arena, event.getClickedBlock());
                } else {
                    if (arena.isProtected(event.getClickedBlock().getRelative(event.getBlockFace()).getLocation())) {
                        TextUtil.sendMessage(player, Config.CANT_PLACE.asString());
                        return;
                    }
                    buildBridge(player, event.getClickedBlock(), arena, event.getBlockFace());
                }
            }
            player.updateInventory();
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        IArena arena = plugin.getBedWars().getArenaUtil().getArenaByPlayer(player);

        if (!Utils.isUltimateArena(arena)) return;
        if (plugin.getUltimateManager().getUltimate(player) != Ultimate.BUILDER) return;

        if (Utils.isUltimateItem(event.getItemInHand())) {
            event.setCancelled(true);
        }
    }

    private void buildBridge(final Player player, final Block clicked, final IArena arena, final BlockFace clickedFace) {
        BlockFace face = Utils.yawToFace(player.getEyeLocation().getYaw());
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
                    TextUtil.sendMessage(player, Config.CANT_BUILD_BRIDGE.asString());
                    cancel();
                    return;
                }
                if (!block.getType().equals(Material.AIR)) {
                    cancel();
                    return;
                }
                if (!block.getWorld().getNearbyEntities(block.getLocation(), 0.5, 0.5, 0.5).isEmpty()) {
                    cancel();
                    return;
                }
                // Remove wool from inventory
                int slot = getFirstWoolSlot(player);
                if (slot != -1) {
                    ItemStack item = player.getInventory().getItem(slot);
                    if (item.getAmount() > 1) {
                        item.setAmount(item.getAmount() - 1);
                    } else {
                        player.getInventory().clear(slot);
                    }
                } else {
                    TextUtil.sendMessage(player, Config.RAN_OUT_OF_WOOL_BLOCKS.asString());
                    cancel();
                    return;
                }

                if (ReflectionUtil.supports(13)) {
                    block.setType(Utils.getWoolMaterial(team.getColor()));
                } else {
                    block.setType(Material.WOOL);
                    block.setData(team.getColor().itemByte());
                }

                arena.addPlacedBlock(block);
                SoundUtil.play(player, "ENTITY_CHICKEN_EGG");

                if (++placed == 10) {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 2L);
    }

    private void buildWall(final Player player, final Block clicked, final IArena arena, final BlockFace clickedFace) {
        if (clickedFace != BlockFace.UP) return;

        BlockFace face = Utils.yawToFace(player.getEyeLocation().getYaw());
        ITeam team = arena.getTeam(player);

        List<Block> row = new ArrayList<>();

        Block bottom = clicked.getRelative(BlockFace.UP);
        row.add(bottom);

        switch (face) {
            case NORTH:
            case SOUTH:
                row.add(bottom.getRelative(BlockFace.EAST));
                row.add(bottom.getRelative(BlockFace.WEST));
                row.add(bottom.getRelative(BlockFace.EAST, 2));
                row.add(bottom.getRelative(BlockFace.WEST, 2));
                break;
            case EAST:
            case WEST:
                row.add(bottom.getRelative(BlockFace.NORTH));
                row.add(bottom.getRelative(BlockFace.SOUTH));
                row.add(bottom.getRelative(BlockFace.NORTH, 2));
                row.add(bottom.getRelative(BlockFace.SOUTH, 2));
                break;
        }

        new BukkitRunnable() {

            int rows = 0;

            @Override
            public void run() {
                for (Block block : row) {
                    if (arena.isProtected(block.getLocation())) {
                        TextUtil.sendMessage(player, Config.CANT_WALL_BRIDGE.asString());
                        cancel();
                        return;
                    }
                    if (block.getType() != Material.AIR) {
                        continue;
                    }
                    // Remove wool from inventory
                    int slot = getFirstWoolSlot(player);
                    if (slot != -1) {
                        ItemStack item = player.getInventory().getItem(slot);
                        if (item.getAmount() > 1) {
                            item.setAmount(item.getAmount() - 1);
                        } else {
                            player.getInventory().clear(slot);
                        }
                    } else {
                        TextUtil.sendMessage(player, Config.RAN_OUT_OF_WOOL_BLOCKS.asString());
                        cancel();
                        return;
                    }

                    if (ReflectionUtils.supports(13)) {
                        block.setType(Utils.getWoolMaterial(team.getColor()));
                    } else {
                        block.setType(Material.WOOL);
                        block.setData(team.getColor().itemByte());
                    }

                    arena.addPlacedBlock(block);
                }

                SoundUtil.play(player, "ENTITY_CHICKEN_EGG");

                if (++rows == 5) {
                    cancel();
                    return;
                }
                // Prepare next row
                row.replaceAll(block -> block.getRelative(BlockFace.UP));
            }
        }.runTaskTimer(plugin, 0, 2L);
    }

    private void protectBed(Player player, IArena arena, Block block) {
        ITeam team = arena.getTeam(player);

        if (team.getBed().distanceSquared(block.getLocation()) > 1) {
            TextUtil.sendMessage(player, Config.CANT_PROTECT_BED.asString());
            return;
        }

        if (getFirstWoolSlot(player) == -1) {
            TextUtil.sendMessage(player, Config.RAN_OUT_OF_WOOL_BLOCKS.asString());
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

        SoundUtil.play(player, "ENTITY_CHICKEN_EGG");

        for (BlockFace check : new BlockFace[]{ BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP }) {
            for (Block part : new Block[]{ block, secondBlock }) {
                Block checked = part.getRelative(check);

                if (!checked.getType().equals(Material.AIR)) continue;

                int slot = getFirstWoolSlot(player);
                if (slot != -1) {
                    ItemStack item = player.getInventory().getItem(slot);
                    if (item.getAmount() > 1) {
                        item.setAmount(item.getAmount() - 1);
                    } else {
                        player.getInventory().clear(slot);
                    }
                } else {
                    TextUtil.sendMessage(player, Config.RAN_OUT_OF_WOOL_BLOCKS.asString());
                    return;
                }

                if (ReflectionUtils.supports(13)) {
                    checked.setType(Utils.getWoolMaterial(team.getColor()));
                } else {
                    checked.setType(Material.WOOL);
                    checked.setData(team.getColor().itemByte());
                }

                arena.addPlacedBlock(checked);
            }
        }
    }

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

    public int getFirstWoolSlot(Player player) {
        for (int i = 0; i < 36; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null || item.getType() == Material.AIR) continue;

            if (item.getType().toString().endsWith("WOOL")) {
                return i;
            }
        }
        return -1;
    }

}
