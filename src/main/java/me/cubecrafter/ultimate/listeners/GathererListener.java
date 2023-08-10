package me.cubecrafter.ultimate.listeners;

import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.events.player.PlayerGeneratorCollectEvent;
import me.cubecrafter.ultimate.UltimatePlugin;
import me.cubecrafter.ultimate.ultimates.Ultimate;
import me.cubecrafter.ultimate.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class GathererListener implements Listener {

    private final UltimatePlugin plugin;

    public GathererListener(UltimatePlugin plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getItem() == null) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        if (plugin.getUltimateManager().getUltimate(player) != Ultimate.GATHERER) return;

        IArena arena = plugin.getBedWars().getArenaUtil().getArenaByPlayer(player);
        if (!Utils.isUltimateArena(arena)) return;

        if (event.getItem().getType() != Material.ENDER_CHEST) return;

        event.setCancelled(true);
        player.openInventory(player.getEnderChest());
    }

    @EventHandler
    public void onGeneratorItemPickup(PlayerGeneratorCollectEvent event) {
        Player player = event.getPlayer();
        IArena arena = plugin.getBedWars().getArenaUtil().getArenaByPlayer(player);

        if (!Utils.isUltimateArena(arena)) return;
        if (plugin.getUltimateManager().getUltimate(player) != Ultimate.GATHERER) return;

        ItemStack item = event.getItemStack();
        if (item.getType() == Material.EMERALD || item.getType() == Material.DIAMOND) {
            player.getInventory().addItem(new ItemStack(item.getType(), item.getAmount()));
        }
    }

}
