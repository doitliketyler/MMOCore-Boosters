package com.motherland.booster;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class PlayerDataConfig {
    private final File playerData;
    private FileConfiguration config;
    private UUID uuid;
    private MMOCoreBoosters plugin;
    private List<Booster> boosters = new ArrayList<>();
    private File playerDataFolder;

    public PlayerDataConfig(MMOCoreBoosters plugin, UUID uuid) {
	this.uuid = uuid;
	this.plugin = plugin;
	this.playerDataFolder = new File(plugin.getDataFolder() + File.separator + "players");
	this.playerData = new File(this.playerDataFolder.getAbsoluteFile() + File.separator + uuid.toString() + ".yml");
	this.config = YamlConfiguration.loadConfiguration(playerData);
	if (!this.playerDataFolder.exists()) {
	    playerDataFolder.mkdirs();
	}
	if (!playerData.exists()) {
	    try {
		config.save(playerData);
	    } catch (IOException e) {

	    }
	}
	loadBoosters();
    }

    private void loadBoosters() {
	if (config.isSet("boosters"))
	    for (String s : config.getConfigurationSection("boosters").getKeys(false)) {
		String date = s.replaceAll("%d%", ".").replaceAll("%c%", ":");
		Booster boost = new Booster(Date.from(Instant.parse(date)), config.getInt("boosters." + s + ".percent"),
			config.getInt("boosters." + s + ".time"), config.getString("boosters." + s + ".type"), this);
		boosters.add(boost);
	    }
    }

    public void createBooster(int percent, int time, String type) {
	Date date = Date.from(Instant.now());
	boosters.add(new Booster(date, percent, time, type, this));
	String instant = date.toInstant().toString();
	instant = instant.replaceAll("\\.", "%d%").replace(":", "%c%");
	config.set("boosters." + instant + ".time", time);
	config.set("boosters." + instant + ".percent", percent);
	config.set("boosters." + instant + ".type", type);
	save();
    }

    public List<Booster> getBoosters() {
	return boosters;
    }

    public FileConfiguration getCustomConfig() {
	return this.config;
    }

    public void reload() {
	this.config = YamlConfiguration.loadConfiguration(this.playerData);
    }

    public void save() {
	try {
	    this.config.save(this.playerData);
	} catch (IOException e) {
	    this.plugin.getServer().getConsoleSender().sendMessage("Error saving " + uuid.toString() + ".yml");
	    e.printStackTrace();
	}
    }

    public void set(String path, Object object) {
	getCustomConfig().set(path, object);
	save();
    }

}
