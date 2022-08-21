package me.cubecrafter.ultimate.menus;

import com.andrei1058.bedwars.shop.ShopCache;
import com.andrei1058.bedwars.shop.ShopManager;
import com.andrei1058.bedwars.shop.main.ShopCategory;
import com.andrei1058.bedwars.shop.main.ShopIndex;
import com.andrei1058.bedwars.shop.quickbuy.PlayerQuickBuyCache;
import me.cubecrafter.ultimate.UltimatePlugin;
import me.cubecrafter.ultimate.config.Configuration;
import me.cubecrafter.ultimate.ultimates.Ultimate;
import me.cubecrafter.ultimate.ultimates.UltimateManager;
import me.cubecrafter.ultimate.utils.ItemBuilder;
import me.cubecrafter.ultimate.utils.TextUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UltimateMenu extends Menu {

    public UltimateMenu(Player player) {
        super(player);
    }

    @Override
    public String getTitle() {
        return Configuration.CATEGORY_TITLE.getAsString();
    }

    @Override
    public int getRows() {
        return 6;
    }

    @Override
    public Map<Integer, MenuItem> getItems() {
        UltimateManager manager = UltimatePlugin.getInstance().getUltimateManager();
        Map<Integer, MenuItem> items = new HashMap<>();
        ShopIndex shop = ShopManager.getShop();
        shop.addSeparator(player, getInventory());
        getInventory().setItem(8, ItemBuilder.fromConfig(Configuration.CATEGORY_ITEM.getAsConfigSection()).build());
        getInventory().setItem(17, shop.getSelectedItem(player));
        items.put(shop.getQuickBuyButton().getSlot(), new MenuItem(shop.getQuickBuyButton().getItemStack(player)).addAction(e -> shop.open(player, PlayerQuickBuyCache.getQuickBuyCache(player.getUniqueId()), false)));
        for (ShopCategory category : shop.getCategoryList()) {
            items.put(category.getSlot(), new MenuItem(category.getItemStack(player)).addAction(e -> category.open(player, shop, ShopCache.getShopCache(player.getUniqueId()))));
        }
        if (Configuration.KANGAROO_ENABLED.getAsBoolean()) {
            items.put(Configuration.KANGAROO_ITEM_SLOT.getAsInt(), new MenuItem(getUltimateItem(Ultimate.KANGAROO)).addAction(e -> manager.setUltimate(player, Ultimate.KANGAROO)));
        }
        if (Configuration.SWORDSMAN_ENABLED.getAsBoolean()) {
            items.put(Configuration.SWORDSMAN_ITEM_SLOT.getAsInt(), new MenuItem(getUltimateItem(Ultimate.SWORDSMAN)).addAction(e -> manager.setUltimate(player, Ultimate.SWORDSMAN)));
        }
        if (Configuration.HEALER_ENABLED.getAsBoolean()) {
            items.put(Configuration.HEALER_ITEM_SLOT.getAsInt(), new MenuItem(getUltimateItem(Ultimate.HEALER)).addAction(e -> manager.setUltimate(player, Ultimate.HEALER)));
        }
        if (Configuration.FROZO_ENABLED.getAsBoolean()) {
            items.put(Configuration.FROZO_ITEM_SLOT.getAsInt(), new MenuItem(getUltimateItem(Ultimate.FROZO)).addAction(e -> manager.setUltimate(player, Ultimate.FROZO)));
        }
        if (Configuration.BUILDER_ENABLED.getAsBoolean()) {
            items.put(Configuration.BUILDER_ITEM_SLOT.getAsInt(), new MenuItem(getUltimateItem(Ultimate.BUILDER)).addAction(e -> manager.setUltimate(player, Ultimate.BUILDER)));
        }
        if (Configuration.DEMOLITION_ENABLED.getAsBoolean()) {
            items.put(Configuration.DEMOLITION_ITEM_SLOT.getAsInt(), new MenuItem(getUltimateItem(Ultimate.DEMOLITION)).addAction(e -> manager.setUltimate(player, Ultimate.DEMOLITION)));
        }
        if (Configuration.GATHERER_ENABLED.getAsBoolean()) {
            items.put(Configuration.GATHERER_ITEM_SLOT.getAsInt(), new MenuItem(getUltimateItem(Ultimate.GATHERER)).addAction(e -> manager.setUltimate(player, Ultimate.GATHERER)));
        }
        return items;
    }

    public ItemStack getUltimateItem(Ultimate ultimate) {
        Ultimate current = UltimatePlugin.getInstance().getUltimateManager().getUltimate(player);
        ItemStack item = ItemBuilder.fromConfig(Configuration.valueOf(ultimate.toString() + "_ITEM").getAsConfigSection()).setGlow(current == ultimate).hideAttributes().build();
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore()) return item;
        List<String> lore = meta.getLore();
        lore.replaceAll(line -> line.replace("{status}", current == ultimate ? Configuration.ULTIMATE_SELECTED.getAsString() : Configuration.ULTIMATE_UNSELECTED.getAsString()));
        meta.setLore(TextUtil.color(lore));
        item.setItemMeta(meta);
        return item;
    }

}
