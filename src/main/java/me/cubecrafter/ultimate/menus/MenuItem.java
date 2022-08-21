package me.cubecrafter.ultimate.menus;

import lombok.Getter;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Getter
public class MenuItem {

    private final ItemStack item;
    private final Map<Consumer<InventoryClickEvent>, ClickType[]> actions = new HashMap<>();
    private static final ClickType[] defaultClickTypes = new ClickType[]{ClickType.LEFT, ClickType.RIGHT, ClickType.SHIFT_LEFT, ClickType.SHIFT_RIGHT};

    public MenuItem(ItemStack item) {
        this.item = item;
    }

    public MenuItem addAction(Consumer<InventoryClickEvent> action, ClickType... clickTypes) {
        if (clickTypes.length == 0) {
            clickTypes = defaultClickTypes;
        }
        actions.put(action, clickTypes);
        return this;
    }

}
