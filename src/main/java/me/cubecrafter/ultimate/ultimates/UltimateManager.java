package me.cubecrafter.ultimate.ultimates;

import com.andrei1058.bedwars.api.arena.IArena;
import com.cryptomorin.xseries.XSound;
import me.cubecrafter.ultimate.config.Configuration;
import me.cubecrafter.ultimate.utils.Cooldown;
import me.cubecrafter.ultimate.utils.TextUtil;
import me.cubecrafter.ultimate.utils.Utils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UltimateManager {

    private final Map<UUID, Ultimate> ultimates = new HashMap<>();
    private final Map<Player, Cooldown> cooldowns = new HashMap<>();
    private final List<IArena> arenas = new ArrayList<>();

    public void setUltimate(Player player, Ultimate type) {
        if (getUltimate(player) == type) {
            TextUtil.sendMessage(player, Configuration.MESSAGE_ULTIMATE_ALREADY_SELECTED.getAsString());
            XSound.play(player, Configuration.ALREADY_SELECTED_SOUND.getAsString());
            return;
        }
        if (cooldowns.containsKey(player)) {
            Cooldown cooldown = cooldowns.get(player);
            if (!cooldown.isExpired()) {
                TextUtil.sendMessage(player, Configuration.MESSAGE_SWITCH_COOLDOWN.getAsString().replace("{seconds}", String.valueOf(cooldown.getSecondsLeft())));
                XSound.play(player, Configuration.ALREADY_SELECTED_SOUND.getAsString());
                return;
            }
            cooldowns.remove(player);
        }
        cooldowns.put(player, new Cooldown(Configuration.SWITCH_COOLDOWN.getAsInt()));
        ultimates.put(player.getUniqueId(), type);
        Utils.giveUltimateItems(player);
        TextUtil.sendMessage(player, Configuration.MESSAGE_ULTIMATE_SELECTED.getAsString().replace("{ultimate}", type.getName()));
        XSound.play(player, Configuration.SWITCH_SOUND.getAsString());
    }

    public Ultimate getUltimate(Player player) {
        return ultimates.get(player.getUniqueId());
    }

    public void registerArena(IArena arena) {
        arenas.add(arena);
    }

    public void unregisterArena(IArena arena) {
        arenas.remove(arena);
    }

    public boolean isUltimateArena(IArena arena) {
        return arenas.contains(arena);
    }

}
