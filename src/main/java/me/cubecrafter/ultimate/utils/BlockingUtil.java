package me.cubecrafter.ultimate.utils;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;

public class BlockingUtil implements Listener {

    private static final Map<Player, Long> blocking = new HashMap<>();

    public static boolean isBlocking(Player player) {
        if (blocking.containsKey(player)) {
            long time = System.currentTimeMillis() - blocking.get(player);
            if (time > 250) {
                blocking.remove(player);
                return false;
            }
            return true;
        }
        return player.isBlocking();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getItem() == null) return;
        if (!event.getItem().getType().toString().endsWith("SWORD")) return;

        blocking.put(event.getPlayer(), System.currentTimeMillis());
    }

}
