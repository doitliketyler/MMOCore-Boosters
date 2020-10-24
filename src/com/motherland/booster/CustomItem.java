package com.motherland.booster;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class CustomItem {
    private ItemStack item;
    private ItemMeta meta;

    public CustomItem(Material mat) {
	item = new ItemStack(mat);
	meta = item.getItemMeta();
    }

    public CustomItem setName(String s) {
	meta.setDisplayName(color(s));
	return this;
    }

    public ItemStack build() {
	item.setItemMeta(meta);
	return item;
    }

    public CustomItem setLore(String... s) {
	List<String> loreList = new ArrayList<>();
	for (String lore : s) {
	    loreList.add(color(lore));
	}
	meta.setLore(loreList);
	return this;
    }

    public CustomItem setLore(List<String> s) {
	List<String> loreList = new ArrayList<>();
	for (String lore : s) {
	    loreList.add(color(lore));
	}
	meta.setLore(loreList);
	return this;
    }

    public CustomItem addLore(String s) {
	List<String> loreList = meta.getLore();
	loreList.add(color(s));
	meta.setLore(loreList);
	return this;
    }

    public CustomItem addDataContainer(String s, String o) {
	meta.getPersistentDataContainer().set(NamespacedKey.minecraft(s.toLowerCase()), PersistentDataType.STRING, o);
	return this;
    }

    public String color(String s) {
	return ChatColor.translateAlternateColorCodes('&', s);
    }
}
