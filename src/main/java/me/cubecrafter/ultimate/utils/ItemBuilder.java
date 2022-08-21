package me.cubecrafter.ultimate.utils;

import com.cryptomorin.xseries.SkullUtils;
import com.cryptomorin.xseries.XMaterial;
import me.cubecrafter.ultimate.UltimatePlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.MaterialData;

import java.util.List;

public class ItemBuilder {

    private ItemStack item;

    public ItemBuilder(String material) {
        item = XMaterial.matchXMaterial(material).get().parseItem();
    }

    public ItemBuilder(ItemStack item) {
        this.item = item;
    }

    public ItemBuilder setDisplayName(String name) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(TextUtil.color(name));
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        ItemMeta meta = item.getItemMeta();
        meta.setLore(TextUtil.color(lore));
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder setGlow(boolean glow) {
        if (glow) {
            ItemMeta meta = item.getItemMeta();
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder setTexture(String identifier) {
        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof SkullMeta)) return this;
        item.setItemMeta(SkullUtils.applySkin(meta, identifier));
        return this;
    }

    public ItemBuilder setTag(String key, String value) {
        item = UltimatePlugin.getInstance().getBedWars().getVersionSupport().setTag(item, key, value);
        return this;
    }

    public ItemBuilder hideAttributes() {
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder setUnbreakable() {
        ItemMeta meta = item.getItemMeta();
        UltimatePlugin.getInstance().getBedWars().getVersionSupport().setUnbreakable(meta);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return this;
    }

    public ItemStack build() {
        return item;
    }

    public static ItemBuilder fromConfig(ConfigurationSection section) {
        ItemBuilder builder = new ItemBuilder(section.getString("material"));
        if (section.contains("displayname")) builder.setDisplayName(section.getString("displayname"));
        if (section.contains("lore")) builder.setLore(section.getStringList("lore"));
        if (section.contains("texture")) builder.setTexture(section.getString("texture"));
        return builder;
    }

}
