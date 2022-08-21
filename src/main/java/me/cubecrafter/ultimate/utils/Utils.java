package me.cubecrafter.ultimate.utils;

import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.TeamColor;
import com.cryptomorin.xseries.XMaterial;
import lombok.experimental.UtilityClass;
import me.cubecrafter.ultimate.UltimatePlugin;
import me.cubecrafter.ultimate.config.Configuration;
import me.cubecrafter.ultimate.ultimates.Ultimate;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffectType;

@UtilityClass
public class Utils {

    public boolean isUltimateArena(IArena arena) {
        return arena != null && UltimatePlugin.getInstance().getUltimateManager().isUltimateArena(arena);
    }

    public String getTag(ItemStack item, String key) {
        String tag = UltimatePlugin.getInstance().getBedWars().getVersionSupport().getTag(item, key);
        return tag == null ? "" : tag;
    }

    public boolean isUltimateItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        return !getTag(item, "ultimate").equals("");
    }

    public void removeActiveCooldowns(Player player) {
        UltimatePlugin plugin = UltimatePlugin.getInstance();
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
        Utils.removeActiveCooldowns(player);
        Utils.clearUltimateItems(player);
        switch (ultimate) {
            case KANGAROO:
                player.setAllowFlight(true);
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
                player.getInventory().addItem(new ItemBuilder("FLINT_AND_STEEL").setDisplayName(Configuration.DEMOLITION_ITEM_NAME.getAsString()).setUnbreakable().setTag("ultimate", "demolition-item").build());
                break;
            case GATHERER:
                player.getInventory().addItem(new ItemBuilder("ENDER_CHEST").setDisplayName(Configuration.GATHERER_ITEM_NAME.getAsString()).setTag("ultimate", "gatherer-item").build());
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
        ItemStack item;
        if (XMaterial.SPLASH_POTION.isSupported()) {
            item = XMaterial.SPLASH_POTION.parseItem();
            PotionMeta meta = (PotionMeta) item.getItemMeta();
            meta.setMainEffect(PotionEffectType.REGENERATION);
            item.setItemMeta(meta);
        } else {
            Potion potion = new Potion(16385);
            potion.setSplash(true);
            item = potion.toItemStack(1);
        }
        return new ItemBuilder(item).setDisplayName(Configuration.HEALER_ITEM_NAME.getAsString()).setTag("ultimate", "healer-item").build();
    }

    public ItemStack getWallItem() {
        return new ItemBuilder("BRICKS").setDisplayName(Configuration.BUILDER_WALL_ITEM_NAME.getAsString()).setTag("ultimate", "wall-item").build();
    }

    public ItemStack getBridgeItem() {
        return new ItemBuilder("BRICKS").setDisplayName(Configuration.BUILDER_BRIDGE_ITEM_NAME.getAsString()).setTag("ultimate", "bridge-item").build();
    }

    public ItemStack getFrozoPotion() {
        ItemStack item;
        if (XMaterial.SPLASH_POTION.isSupported()) {
            item = XMaterial.SPLASH_POTION.parseItem();
            PotionMeta meta = (PotionMeta) item.getItemMeta();
            meta.setMainEffect(PotionEffectType.SLOW);
            item.setItemMeta(meta);
        } else {
            Potion potion = new Potion(16394);
            potion.setSplash(true);
            item = potion.toItemStack(1);
        }
        return new ItemBuilder(item).setDisplayName(Configuration.FROZO_ITEM_NAME.getAsString()).setTag("ultimate", "frozo-item").build();
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

    public Material getMaterial(TeamColor color) {
        if (color == TeamColor.AQUA) {
            return Material.valueOf("CYAN_WOOL");
        }
        return color.woolMaterial();
    }

}
