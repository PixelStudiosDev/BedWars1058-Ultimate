package me.cubecrafter.ultimate.config;

import lombok.Getter;
import me.cubecrafter.ultimate.UltimatePlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class FileManager {

    @Getter
    private final YamlConfiguration config;

    public FileManager(UltimatePlugin plugin) {
        File folder = new File("plugins/BedWars1058/Addons/Ultimate");
        if (!folder.exists()) folder.mkdirs();
        File configFile = new File(folder, "config.yml");
        if (!configFile.exists()) {
            saveResource(plugin.getResource("config.yml"), configFile);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    private void saveResource(InputStream in, File destination) {
        try {
            Files.copy(in, destination.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
