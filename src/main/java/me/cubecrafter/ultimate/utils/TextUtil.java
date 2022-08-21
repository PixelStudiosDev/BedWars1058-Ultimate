package me.cubecrafter.ultimate.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

@UtilityClass
public class TextUtil {

    public String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public List<String> color(List<String> lines) {
        lines.replaceAll(TextUtil::color);
        return lines;
    }

    public void sendMessage(Player player, String message) {
        player.sendMessage(color(message));
    }

    public void sendMessage(Player player, List<String> messages) {
        messages.forEach(message -> sendMessage(player, message));
    }

    public void sendMessage(List<Player> players, List<String> messages) {
        players.forEach(player -> sendMessage(player, messages));
    }

}
