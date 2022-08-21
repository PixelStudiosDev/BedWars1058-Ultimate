package me.cubecrafter.ultimate.listeners;

import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import me.cubecrafter.ultimate.UltimatePlugin;
import me.cubecrafter.ultimate.config.Configuration;
import me.cubecrafter.ultimate.menus.Menu;
import me.cubecrafter.ultimate.menus.MenuItem;
import me.cubecrafter.ultimate.menus.UltimateMenu;
import me.cubecrafter.ultimate.utils.ItemBuilder;
import me.cubecrafter.ultimate.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;

import java.util.Map;
import java.util.function.Consumer;

public class InventoryListener implements Listener {

    private final UltimatePlugin plugin;

    public InventoryListener(UltimatePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        Player player = (Player) e.getPlayer();
        IArena arena = plugin.getBedWars().getArenaUtil().getArenaByPlayer(player);
        if (arena == null || !Configuration.ARENA_GROUPS.getAsStringList().contains(arena.getGroup())) return;
        if (!isShop(player, e.getView().getTitle())) return;
        e.getInventory().setItem(8, ItemBuilder.fromConfig(Configuration.CATEGORY_ITEM.getAsConfigSection()).setTag("ultimate", "category-item").build());
    }

    public boolean isShop(Player player, String title) {
        return title.equals(Language.getMsg(player, Messages.SHOP_INDEX_NAME)) ||
                title.equals(Language.getMsg(player, Messages.SHOP_PATH + ".blocks-category.inventory-name")) ||
                title.equals(Language.getMsg(player, Messages.SHOP_PATH + ".melee-category.inventory-name")) ||
                title.equals(Language.getMsg(player, Messages.SHOP_PATH + ".armor-category.inventory-name")) ||
                title.equals(Language.getMsg(player, Messages.SHOP_PATH + ".tools-category.inventory-name")) ||
                title.equals(Language.getMsg(player, Messages.SHOP_PATH + ".ranged-category.inventory-name")) ||
                title.equals(Language.getMsg(player, Messages.SHOP_PATH + ".potions-category.inventory-name")) ||
                title.equals(Language.getMsg(player, Messages.SHOP_PATH + ".utility-category.inventory-name"));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;
        Player player = (Player) e.getWhoClicked();
        IArena arena = plugin.getBedWars().getArenaUtil().getArenaByPlayer(player);
        if (arena == null) return;
        if (e.getInventory().getHolder() instanceof Menu) {
            e.setCancelled(true);
            Menu menu = (Menu) e.getInventory().getHolder();
            MenuItem clicked = menu.getItems().get(e.getSlot());
            if (clicked == null) return;
            for (Map.Entry<Consumer<InventoryClickEvent>, ClickType[]> entry : clicked.getActions().entrySet()) {
                for (ClickType clickType : entry.getValue()) {
                    if (e.getClick() == clickType) {
                        entry.getKey().accept(e);
                        menu.updateMenu();
                    }
                }
            }
            return;
        }
        if (Utils.getTag(e.getCurrentItem(), "ultimate").equals("category-item")) {
            new UltimateMenu(player).openMenu();
        } else if (Utils.isUltimateItem(e.getCurrentItem()) && e.getInventory().getType() != InventoryType.CRAFTING) {
            e.setCancelled(true);
        }
    }

}
