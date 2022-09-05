package me.cubecrafter.ultimate;

import com.andrei1058.bedwars.api.BedWars;
import lombok.Getter;
import me.cubecrafter.ultimate.config.FileManager;
import me.cubecrafter.ultimate.listeners.*;
import me.cubecrafter.ultimate.ultimates.UltimateManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class UltimatePlugin extends JavaPlugin {

    @Getter
    private static UltimatePlugin instance;
    private FileManager fileManager;
    private UltimateManager ultimateManager;
    private KangarooListener kangarooListener;
    private SwordsmanListener swordsmanListener;
    private HealerListener healerListener;
    private FrozoListener frozoListener;
    private BuilderListener builderListener;
    private DemolitionListener demolitionListener;
    private GathererListener gathererListener;
    private BedWars bedWars;

    @Override
    public void onEnable() {
        instance = this;
        bedWars = getServer().getServicesManager().getRegistration(BedWars.class).getProvider();
        fileManager = new FileManager(this);
        ultimateManager = new UltimateManager();
        kangarooListener = new KangarooListener(this);
        swordsmanListener = new SwordsmanListener(this);
        healerListener = new HealerListener(this);
        frozoListener = new FrozoListener(this);
        builderListener = new BuilderListener(this);
        demolitionListener = new DemolitionListener(this);
        gathererListener = new GathererListener(this);
        getServer().getPluginManager().registerEvents(new ArenaListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        new Metrics(this, 15611);
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
    }

}
