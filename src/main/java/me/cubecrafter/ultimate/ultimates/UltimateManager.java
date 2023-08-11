package me.cubecrafter.ultimate.ultimates;

import com.andrei1058.bedwars.api.arena.IArena;
import me.cubecrafter.ultimate.config.Config;
import me.cubecrafter.ultimate.utils.Cooldown;
import me.cubecrafter.ultimate.utils.Utils;
import me.cubecrafter.xutils.SoundUtil;
import me.cubecrafter.xutils.text.TextUtil;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UltimateManager {

    private final Map<UUID, Ultimate> ultimates = new HashMap<>();
    private final Map<UUID, Cooldown> cooldowns = new HashMap<>();
    private final List<IArena> arenas = new ArrayList<>();

    public void clearUltimate(Player player) {
        ultimates.remove(player.getUniqueId());
    }

    public void setUltimate(Player player, Ultimate type) {
        if (getUltimate(player) == type) {
            TextUtil.sendMessage(player, Config.MESSAGE_ULTIMATE_ALREADY_SELECTED.getAsString());
            SoundUtil.play(player, Config.ALREADY_SELECTED_SOUND.getAsString());
            return;
        }

        UUID uuid = player.getUniqueId();

        if (cooldowns.containsKey(uuid)) {
            Cooldown cooldown = cooldowns.get(uuid);
            if (!cooldown.isExpired()) {
                TextUtil.sendMessage(player, Config.MESSAGE_SWITCH_COOLDOWN.getAsString().replace("{seconds}", String.valueOf(cooldown.getSecondsLeft())));
                SoundUtil.play(player, Config.ALREADY_SELECTED_SOUND.getAsString());
                return;
            }
            cooldowns.remove(uuid);
        }

        cooldowns.put(uuid, new Cooldown(Config.SWITCH_COOLDOWN.getAsInt()));
        ultimates.put(player.getUniqueId(), type);

        Utils.giveUltimateItems(player);

        TextUtil.sendMessage(player, Config.MESSAGE_ULTIMATE_SELECTED.getAsString().replace("{ultimate}", type.getName()));
        SoundUtil.play(player, Config.SWITCH_SOUND.getAsString());
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
