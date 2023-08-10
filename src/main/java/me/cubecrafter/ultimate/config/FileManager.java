package me.cubecrafter.ultimate.config;

import lombok.Getter;
import me.cubecrafter.ultimate.UltimatePlugin;
import me.cubecrafter.xutils.FileUtil;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class FileManager {

    @Getter
    private final YamlConfiguration config;

    public FileManager(UltimatePlugin plugin) {
        File file = new File("plugins/BedWars1058/Addons/Ultimate/config.yml");
        if (!file.exists()) {
            FileUtil.copy(plugin.getResource("config.yml"), file);
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

}
