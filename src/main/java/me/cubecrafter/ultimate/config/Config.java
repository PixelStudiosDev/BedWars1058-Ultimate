package me.cubecrafter.ultimate.config;

import lombok.RequiredArgsConstructor;
import me.cubecrafter.ultimate.UltimatePlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;

@RequiredArgsConstructor
public enum Config {

    ARENA_GROUPS("arena-groups"),
    DISABLE_FALL_DAMAGE("disable-fall-damage"),
    SWITCH_COOLDOWN("switch-cooldown"),
    ULTIMATES_ENABLED_DELAY("arena-enable-ultimates-delay"),
    ULTIMATES_ENABLED_SOUND("arena-enable-ultimates-sound"),
    KANGAROO_ENABLED("ultimates.kangaroo.enabled"),
    KANGAROO_DISPLAYNAME("ultimates.kangaroo.displayname"),
    SWORDSMAN_ENABLED("ultimates.swordsman.enabled"),
    SWORDSMAN_DISPLAYNAME("ultimates.swordsman.displayname"),
    SWORDSMAN_LOADING_SOUND("ultimates.swordsman.loading-sound"),
    HEALER_ENABLED("ultimates.healer.enabled"),
    HEALER_DISPLAYNAME("ultimates.healer.displayname"),
    HEALER_ITEM_NAME("ultimates.healer.ultimate-item-name"),
    FROZO_ENABLED("ultimates.frozo.enabled"),
    FROZO_DISPLAYNAME("ultimates.frozo.displayname"),
    FROZO_ITEM_NAME("ultimates.frozo.ultimate-item-name"),
    BUILDER_ENABLED("ultimates.builder.enabled"),
    BUILDER_DISPLAYNAME("ultimates.builder.displayname"),
    BUILDER_WALL_ITEM_NAME("ultimates.builder.wall-item-name"),
    BUILDER_BRIDGE_ITEM_NAME("ultimates.builder.bridge-item-name"),
    DEMOLITION_ENABLED("ultimates.demolition.enabled"),
    DEMOLITION_DISPLAYNAME("ultimates.demolition.displayname"),
    DEMOLITION_ITEM_NAME("ultimates.demolition.ultimate-item-name"),
    GATHERER_ENABLED("ultimates.gatherer.enabled"),
    GATHERER_DISPLAYNAME("ultimates.gatherer.displayname"),
    GATHERER_ITEM_NAME("ultimates.gatherer.ultimate-item-name"),
    SWITCH_SOUND("shop-category.switch-sound"),
    ALREADY_SELECTED_SOUND("shop-category.already-selected-sound"),
    CATEGORY_TITLE("shop-category.title"),
    ULTIMATE_SELECTED("shop-category.ultimate-selected"),
    ULTIMATE_UNSELECTED("shop-category.ultimate-unselected"),
    CATEGORY_ITEM("shop-category.category-item"),
    KANGAROO_ITEM("shop-category.kangaroo-item"),
    KANGAROO_ITEM_SLOT("shop-category.kangaroo-item.slot"),
    SWORDSMAN_ITEM("shop-category.swordsman-item"),
    SWORDSMAN_ITEM_SLOT("shop-category.swordsman-item.slot"),
    HEALER_ITEM("shop-category.healer-item"),
    HEALER_ITEM_SLOT("shop-category.healer-item.slot"),
    FROZO_ITEM("shop-category.frozo-item"),
    FROZO_ITEM_SLOT("shop-category.frozo-item.slot"),
    BUILDER_ITEM("shop-category.builder-item"),
    BUILDER_ITEM_SLOT("shop-category.builder-item.slot"),
    DEMOLITION_ITEM("shop-category.demolition-item"),
    DEMOLITION_ITEM_SLOT("shop-category.demolition-item.slot"),
    GATHERER_ITEM("shop-category.gatherer-item"),
    GATHERER_ITEM_SLOT("shop-category.gatherer-item.slot"),
    CANT_PLACE("messages.cant-place-blocks"),
    CANT_BUILD_BRIDGE("messages.cant-build-bridge"),
    CANT_WALL_BRIDGE("messages.cant-build-wall"),
    CANT_PROTECT_BED("messages.You can only protect your team bed"),
    RAN_OUT_OF_WOOL_BLOCKS("messages.ran-out-of-wool-blocks"),
    MESSAGE_ULTIMATE_ALREADY_SELECTED("messages.ultimate-already-selected"),
    MESSAGE_ULTIMATE_SELECTED("messages.ultimate-selected"),
    MESSAGE_SWITCH_COOLDOWN("messages.switch-cooldown"),
    MESSAGE_ULTIMATES_ENABLED("messages.ultimates-enabled");

    private static final YamlConfiguration config = UltimatePlugin.getInstance().getFileManager().getConfig();
    private final String path;

    public String getAsString() {
        return config.getString(path);
    }

    public int getAsInt() {
        return config.getInt(path);
    }

    public double getAsDouble() {
        return config.getDouble(path);
    }

    public List<String> getAsStringList() {
        return config.getStringList(path);
    }

    public boolean getAsBoolean() {
        return config.getBoolean(path);
    }

    public ConfigurationSection getAsConfigSection() {
        return config.getConfigurationSection(path);
    }

}
