package me.cubecrafter.ultimate.menu;

import com.andrei1058.bedwars.shop.ShopCache;
import com.andrei1058.bedwars.shop.ShopManager;
import com.andrei1058.bedwars.shop.main.ShopCategory;
import com.andrei1058.bedwars.shop.main.ShopIndex;
import com.andrei1058.bedwars.shop.quickbuy.PlayerQuickBuyCache;
import me.cubecrafter.ultimate.UltimatePlugin;
import me.cubecrafter.ultimate.config.Config;
import me.cubecrafter.ultimate.ultimates.Ultimate;
import me.cubecrafter.ultimate.ultimates.UltimateManager;
import me.cubecrafter.xutils.item.ItemBuilder;
import me.cubecrafter.xutils.menu.Menu;
import me.cubecrafter.xutils.menu.MenuItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

public class UltimateMenu extends Menu {

    public UltimateMenu(Player player) {
        super(player);

        setAutoUpdate(false);
        setParsePlaceholders(true);
    }

    @Override
    public String getTitle() {
        return Config.CATEGORY_TITLE.getAsString();
    }

    @Override
    public int getRows() {
        return 6;
    }

    @Override
    public void update() {
        UltimateManager manager = UltimatePlugin.getInstance().getUltimateManager();

        ShopIndex shop = ShopManager.getShop();
        shop.addSeparator(player, getInventory());

        setItem(new MenuItem(ItemBuilder.fromConfig(Config.CATEGORY_ITEM.getAsConfigSection())), 8);
        setItem(new MenuItem(shop.getSelectedItem(player)), 17);
        
        setItem(new MenuItem(shop.getQuickBuyButton().getItemStack(player)).addAction(() -> {
            shop.open(player, PlayerQuickBuyCache.getQuickBuyCache(player.getUniqueId()), false);
        }), shop.getQuickBuyButton().getSlot());
        
        for (ShopCategory category : shop.getCategoryList()) {
            setItem(new MenuItem(category.getItemStack(player)).addAction(() -> category.open(player, shop, ShopCache.getShopCache(player.getUniqueId()))), category.getSlot());
        }
        
        if (Config.KANGAROO_ENABLED.getAsBoolean()) {
            setItem(new MenuItem(getUltimateItem(Ultimate.KANGAROO)).addAction(() -> manager.setUltimate(player, Ultimate.KANGAROO)), Config.KANGAROO_ITEM_SLOT.getAsInt());
        }
        if (Config.SWORDSMAN_ENABLED.getAsBoolean()) {
            setItem(new MenuItem(getUltimateItem(Ultimate.SWORDSMAN)).addAction(() -> manager.setUltimate(player, Ultimate.SWORDSMAN)), Config.SWORDSMAN_ITEM_SLOT.getAsInt());
        }
        if (Config.HEALER_ENABLED.getAsBoolean()) {
            setItem(new MenuItem(getUltimateItem(Ultimate.HEALER)).addAction(() -> manager.setUltimate(player, Ultimate.HEALER)), Config.HEALER_ITEM_SLOT.getAsInt());
        }
        if (Config.FROZO_ENABLED.getAsBoolean()) {
            setItem(new MenuItem(getUltimateItem(Ultimate.FROZO)).addAction(() -> manager.setUltimate(player, Ultimate.FROZO)), Config.FROZO_ITEM_SLOT.getAsInt());
        }
        if (Config.BUILDER_ENABLED.getAsBoolean()) {
            setItem(new MenuItem(getUltimateItem(Ultimate.BUILDER)).addAction(() -> manager.setUltimate(player, Ultimate.BUILDER)), Config.BUILDER_ITEM_SLOT.getAsInt());
        }
        if (Config.DEMOLITION_ENABLED.getAsBoolean()) {
            setItem(new MenuItem(getUltimateItem(Ultimate.DEMOLITION)).addAction(() -> manager.setUltimate(player, Ultimate.DEMOLITION)), Config.DEMOLITION_ITEM_SLOT.getAsInt());
        }
        if (Config.GATHERER_ENABLED.getAsBoolean()) {
            setItem(new MenuItem(getUltimateItem(Ultimate.GATHERER)).addAction(() -> manager.setUltimate(player, Ultimate.GATHERER)), Config.GATHERER_ITEM_SLOT.getAsInt());
        }
    }

    public ItemBuilder getUltimateItem(Ultimate ultimate) {
        Ultimate selected = UltimatePlugin.getInstance().getUltimateManager().getUltimate(player);

        return ItemBuilder.fromConfig(Config.valueOf(ultimate.toString() + "_ITEM").getAsConfigSection())
                .setGlow(selected == ultimate).addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                .addPlaceholder("{status}", selected == ultimate ? Config.ULTIMATE_SELECTED.getAsString() : Config.ULTIMATE_UNSELECTED.getAsString());
    }

}
