package me.cubecrafter.ultimate;

import com.andrei1058.bedwars.api.BedWars;
import lombok.Getter;
import me.cubecrafter.ultimate.config.FileManager;
import me.cubecrafter.ultimate.listeners.ArenaListener;
import me.cubecrafter.ultimate.listeners.BuilderListener;
import me.cubecrafter.ultimate.listeners.DemolitionListener;
import me.cubecrafter.ultimate.listeners.FrozoListener;
import me.cubecrafter.ultimate.listeners.GathererListener;
import me.cubecrafter.ultimate.listeners.HealerListener;
import me.cubecrafter.ultimate.listeners.InventoryListener;
import me.cubecrafter.ultimate.listeners.KangarooListener;
import me.cubecrafter.ultimate.listeners.SwordsmanListener;
import me.cubecrafter.ultimate.ultimates.UltimateManager;
import me.cubecrafter.ultimate.utils.BlockingUtil;
import me.cubecrafter.xutils.Events;
import me.cubecrafter.xutils.ReflectionUtil;
import me.cubecrafter.xutils.Tasks;
import me.cubecrafter.xutils.XUtils;
import me.cubecrafter.xutils.item.TagHandler;
import org.bstats.bukkit.Metrics;
import org.bukkit.inventory.ItemStack;
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

        Events.register(
                new ArenaListener(this),
                new InventoryListener(this)
        );

        if (ReflectionUtil.VERSION > 9) {
            Events.register(new BlockingUtil());
        }

        XUtils.setCustomTagHandler(new TagHandler() {

            @Override
            public ItemStack set(ItemStack itemStack, String key, String value) {
                return bedWars.getVersionSupport().setTag(itemStack, key, value);
            }

            @Override
            public String get(ItemStack item, String key) {
                return bedWars.getVersionSupport().getTag(item, key);
            }

        });

        new Metrics(this, 15611);
    }

    @Override
    public void onDisable() {
        Tasks.cancelAll();
    }

}
