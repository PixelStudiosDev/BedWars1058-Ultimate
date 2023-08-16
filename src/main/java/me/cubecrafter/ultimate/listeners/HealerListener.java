package me.cubecrafter.ultimate.listeners;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import me.cubecrafter.ultimate.UltimatePlugin;
import me.cubecrafter.ultimate.ultimates.Ultimate;
import me.cubecrafter.ultimate.utils.Cooldown;
import me.cubecrafter.ultimate.utils.Utils;
import me.cubecrafter.xutils.SoundUtil;
import me.cubecrafter.xutils.Tasks;
import me.cubecrafter.xutils.item.ItemUtil;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class HealerListener implements Listener, Runnable {

    private final UltimatePlugin plugin;

    private final Map<Player, Cooldown> cooldowns = new HashMap<>();
    private final Map<Player, BukkitTask> potionTasks = new HashMap<>();
    private final Map<Player, BukkitTask> healTasks = new HashMap<>();

    public HealerListener(UltimatePlugin plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        Tasks.repeat(this, 0L, 1L);
    }

    @EventHandler
    public void onSplash(PotionSplashEvent event) {
        ThrownPotion potion = event.getPotion();
        if (!(potion.getShooter() instanceof Player)) return;

        Player player = (Player) potion.getShooter();

        if (plugin.getUltimateManager().getUltimate(player) != Ultimate.HEALER) return;

        String tag = ItemUtil.getTag(potion.getItem(), "ultimate");
        if (tag == null || !tag.equals("healer-item")) return;

        event.setCancelled(true);

        IArena arena = plugin.getBedWars().getArenaUtil().getArenaByPlayer(player);
        ITeam team = arena.getTeam(player);
        Location location = potion.getLocation();
        Cooldown cooldown = new Cooldown(30);

        BukkitTask task = Tasks.repeat(() -> {
            potion.getNearbyEntities(2.5, 2.5, 2.5).stream()
                    .filter(entity -> entity.getType() == EntityType.PLAYER)
                    .map(entity -> (Player) entity)
                    .filter(team::isMember).filter(member -> !arena.isReSpawning(member))
                    .forEach(member -> {
                        member.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 0));
                    });

            for (double i = 0.3; i < 2.5; i += 0.4) {
                for (int degree = 0; degree < 360; degree += (30 / i)) {
                    double radians = Math.toRadians(degree);
                    double x = Math.cos(radians) * i;
                    double z = Math.sin(radians) * i;

                    location.getWorld().playEffect(location.clone().add(x, 0.1, z), Effect.HAPPY_VILLAGER, 1);
                }
            }

            if (cooldown.isExpired()) {
                potionTasks.get(player).cancel();
                potionTasks.remove(player);
            }
        }, 0L, 20L);

        potionTasks.put(player, task);
    }

    @Override
    public void run() {
        for (IArena arena : plugin.getBedWars().getArenaUtil().getArenas()) {
            if (arena.getStatus() != GameState.playing) continue;
            if (!Utils.isUltimateArena(arena)) continue;

            for (Player player : arena.getPlayers()) {
                if (!player.isBlocking()) continue;
                if (plugin.getUltimateManager().getUltimate(player) != Ultimate.HEALER) continue;
                if (cooldowns.containsKey(player)) continue;

                cooldowns.put(player, new Cooldown(20));
                SoundUtil.play(player, "NOTE_PLING");

                BukkitTask task = Tasks.repeat(() -> {
                    if (player.getHealth() < player.getMaxHealth()) {
                        player.setHealth(player.getHealth() + 1);
                    } else {
                        healTasks.get(player).cancel();
                        healTasks.remove(player);
                    }
                }, 0L, 20L);

                healTasks.put(player, task);
            }
        }

        for (Map.Entry<Player, Cooldown> entry : cooldowns.entrySet()) {
            Player player = entry.getKey();
            Cooldown cooldown = entry.getValue();

            if (cooldown.isExpired()) {
                resetCooldown(player);
                continue;
            }

            player.setExp(cooldown.getPercentageLeft());
            player.setLevel(cooldown.getSecondsLeft());
        }
    }

    public void resetCooldown(Player player) {
        cooldowns.remove(player);
        player.setExp(0);
        player.setLevel(0);
    }

    public void stopTasks(Player player) {
        if (potionTasks.containsKey(player)) {
            potionTasks.get(player).cancel();
            potionTasks.remove(player);
        }
        if (healTasks.containsKey(player)) {
            healTasks.get(player).cancel();
            healTasks.remove(player);
        }
    }

}

