package me.cubecrafter.ultimate.listeners;

import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import lombok.RequiredArgsConstructor;
import me.cubecrafter.ultimate.UltimatePlugin;
import me.cubecrafter.ultimate.config.Config;
import me.cubecrafter.ultimate.menu.UltimateMenu;
import me.cubecrafter.xutils.item.ItemBuilder;
import me.cubecrafter.xutils.item.ItemUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class InventoryListener implements Listener {

    private final UltimatePlugin plugin;

    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        IArena arena = plugin.getBedWars().getArenaUtil().getArenaByPlayer(player);

        if (arena == null || !Config.ARENA_GROUPS.getAsStringList().contains(arena.getGroup())) return;
        if (!isShop(player, event.getView().getTitle())) return;

        event.getInventory().setItem(8, ItemBuilder.fromConfig(Config.CATEGORY_ITEM.getAsConfigSection()).setTag("ultimate", "category-item").build());
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        Player player = (Player) event.getWhoClicked();
        IArena arena = plugin.getBedWars().getArenaUtil().getArenaByPlayer(player);

        if (arena == null) return;

        ItemStack item = event.getCurrentItem();

        String tag = ItemUtil.getTag(item, "ultimate");
        if (tag == null) return;

        if (ItemUtil.getTag(item, "ultimate").equals("category-item")) {
            new UltimateMenu(player).open();
        } else if (event.getInventory().getType() != InventoryType.CRAFTING) {
            event.setCancelled(true);
        }
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

}
