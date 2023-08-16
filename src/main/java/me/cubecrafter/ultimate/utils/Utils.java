package me.cubecrafter.ultimate.utils;

import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.TeamColor;
import lombok.experimental.UtilityClass;
import me.cubecrafter.ultimate.UltimatePlugin;
import me.cubecrafter.ultimate.config.Config;
import me.cubecrafter.ultimate.ultimates.Ultimate;
import me.cubecrafter.xutils.item.ItemBuilder;
import me.cubecrafter.xutils.item.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@UtilityClass
public class Utils {

    private static final BlockFace[] AXIS = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };

    public BlockFace yawToFace(float yaw) {
        return AXIS[Math.round(yaw / 90f) & 0x3].getOppositeFace();
    }

    public boolean isUltimateArena(IArena arena) {
        return arena != null && UltimatePlugin.getInstance().getUltimateManager().isUltimateArena(arena);
    }

    public boolean isUltimateItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        return ItemUtil.getTag(item, "ultimate") != null;
    }

    public boolean isBed(Block block) {
        return UltimatePlugin.getInstance().getBedWars().getVersionSupport().isBed(block.getType());
    }

    public void resetCooldowns(Player player) {
        UltimatePlugin plugin = UltimatePlugin.getInstance();

        plugin.getDemolitionListener().resetCooldown(player);
        plugin.getKangarooListener().resetCooldown(player);
        plugin.getSwordsmanListener().resetCooldown(player);
        plugin.getHealerListener().resetCooldown(player);
        plugin.getHealerListener().stopTasks(player);
    }

    public void clearUltimateItems(Player player) {
        player.setAllowFlight(false);
        player.setHealthScale(20);
        player.setMaxHealth(20);

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (Utils.isUltimateItem(item)) {
                player.getInventory().clear(i);
            }
        }
    }

    public void giveUltimateItems(Player player) {
        Ultimate ultimate = UltimatePlugin.getInstance().getUltimateManager().getUltimate(player);
        if (ultimate == null) return;

        IArena arena = UltimatePlugin.getInstance().getBedWars().getArenaUtil().getArenaByPlayer(player);
        if (!Utils.isUltimateArena(arena)) return;

        Utils.resetCooldowns(player);
        Utils.clearUltimateItems(player);

        switch (ultimate) {
            case KANGAROO:
                Bukkit.getScheduler().runTaskLater(UltimatePlugin.getInstance(), () -> player.setAllowFlight(true), 1L);
                break;
            case HEALER:
                player.getInventory().addItem(getHealerPotion());
                player.setHealthScale(30);
                player.setMaxHealth(30);
                player.setHealth(player.getMaxHealth());
                break;
            case FROZO:
                player.getInventory().addItem(getFrozoPotion());
                break;
            case BUILDER:
                player.getInventory().addItem(getWallItem());
                break;
            case DEMOLITION:
                player.getInventory().addItem(new ItemBuilder("FLINT_AND_STEEL").setDisplayName(Config.DEMOLITION_ITEM_NAME.asString()).setUnbreakable(true).setTag("ultimate", "demolition-item").build());
                break;
            case GATHERER:
                player.getInventory().addItem(new ItemBuilder("ENDER_CHEST").setDisplayName(Config.GATHERER_ITEM_NAME.asString()).setTag("ultimate", "gatherer-item").build());
                break;
        }
    }

    public int getAmount(ItemStack item, PlayerInventory inventory) {
        int amount = 0;
        for (ItemStack content : inventory.getContents()) {
            if (content != null && content.isSimilar(item)) {
                amount += content.getAmount();
            }
        }
        return amount;
    }

    public ItemStack getHealerPotion() {
        return new ItemBuilder("SPLASH_POTION")
                .setDisplayName(Config.HEALER_ITEM_NAME.asString())
                .addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 0, 0))
                .setTag("ultimate", "healer-item")
                .build();
    }

    public ItemStack getWallItem() {
        return new ItemBuilder("BRICKS")
                .setDisplayName(Config.BUILDER_WALL_ITEM_NAME.asString())
                .setTag("ultimate", "wall-item")
                .build();
    }

    public ItemStack getBridgeItem() {
        return new ItemBuilder("BRICKS")
                .setDisplayName(Config.BUILDER_BRIDGE_ITEM_NAME.asString())
                .setTag("ultimate", "bridge-item")
                .build();
    }

    public ItemStack getFrozoPotion() {
        return new ItemBuilder("SPLASH_POTION")
                .setDisplayName(Config.FROZO_ITEM_NAME.asString())
                .addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 0, 0))
                .setTag("ultimate", "frozo-item")
                .build();
    }

    public ItemStack getWool(TeamColor color) {
        String material = null;
        switch (color) {
            case RED:
                material = "RED_WOOL";
                break;
            case BLUE:
                material = "BLUE_WOOL";
                break;
            case AQUA:
                material = "CYAN_WOOL";
                break;
            case GRAY:
                material = "LIGHT_GRAY_WOOL";
                break;
            case PINK:
                material = "PINK_WOOL";
                break;
            case GREEN:
                material = "LIME_WOOL";
                break;
            case WHITE:
                material = "WHITE_WOOL";
                break;
            case YELLOW:
                material = "YELLOW_WOOL";
                break;
            case DARK_GRAY:
                material = "GRAY_WOOL";
                break;
            case DARK_GREEN:
                material = "GREEN_WOOL";
                break;
        }
        return new ItemBuilder(material).build();
    }

    public Material getWoolMaterial(TeamColor color) {
        if (color == TeamColor.AQUA) {
            return Material.valueOf("CYAN_WOOL");
        }
        return color.woolMaterial();
    }

}
