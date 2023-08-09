package me.cubecrafter.ultimate.listeners;

import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent;
import com.cryptomorin.xseries.XMaterial;
import me.cubecrafter.ultimate.UltimatePlugin;
import me.cubecrafter.ultimate.ultimates.Ultimate;
import me.cubecrafter.ultimate.utils.ItemBuilder;
import me.cubecrafter.ultimate.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.stream.Collectors;

public class FrozoListener implements Listener {

    private final UltimatePlugin plugin;

    public FrozoListener(UltimatePlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent e) {
        ThrownPotion potion = e.getPotion();
        if (!(potion.getShooter() instanceof Player)) return;
        Player player = (Player) potion.getShooter();
        IArena arena = plugin.getBedWars().getArenaUtil().getArenaByPlayer(player);
        if (!Utils.isUltimateArena(arena)) return;
        if (!Utils.getTag(potion.getItem(), "ultimate").equals("frozo-item")) return;
        if (plugin.getUltimateManager().getUltimate(player) != Ultimate.FROZO) return;
        e.setCancelled(true);
        ITeam team = plugin.getBedWars().getArenaUtil().getArenaByPlayer(player).getTeam(player);
        List<Player> players = e.getAffectedEntities().stream().filter(entity -> entity instanceof Player).map(entity -> (Player) entity).collect(Collectors.toList());
        for (Player affected : players) {
            if (team.isMember(affected)) {
                affected.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 600, 5));
            } else {
                affected.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 1200, 1));
            }
        }
    }

    @EventHandler
    public void onKill(PlayerKillEvent e) {
        if (e.getKiller() == null) return;
        if (!Utils.isUltimateArena(e.getArena())) return;
        Player player = e.getKiller();
        if (plugin.getUltimateManager().getUltimate(player) != Ultimate.FROZO) return;
        ItemStack snowball = XMaterial.SNOWBALL.parseItem();
        snowball = new ItemBuilder(snowball).setTag("ultimate", "snowball").build();
        for (int i = 0; i < 2; i++) {
            if (Utils.getAmount(snowball, player.getInventory()) >= 16) break;
            player.getInventory().addItem(snowball);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event){
        if(Utils.isUltimateItem(event.getItem())) event.setCancelled(true);
    }
}
