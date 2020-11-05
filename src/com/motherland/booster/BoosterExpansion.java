package com.motherland.booster;

import java.util.List;
import org.bukkit.entity.Player;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class BoosterExpansion extends PlaceholderExpansion {
    private final MMOCoreBoosters plugin;
    private final List<String> types;

    public BoosterExpansion(MMOCoreBoosters plugin, List<String> types) {
	this.plugin = plugin;
	this.types = types;
    }

    public boolean persist() {
	return true;
    }

    public boolean canRegister() {
	return true;
    }

    public String getAuthor() {
	return this.plugin.getDescription().getAuthors().toString();
    }

    public String getIdentifier() {
	return "mmocoreboosters";
    }

    public String getVersion() {
	return this.plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
	if (player == null)
	    return "0";
	if (identifier.equals("total")) {
	    int total = 0;
	    for(Booster b : plugin.getBoosters(player, "all")) {
		total += b.getPercent();
	    }
	    return total + "";
	} else if (identifier.equals("class")) {
	    int total = 0;
	    for(Booster b : plugin.getBoosters(player, "class")) {
		total += b.getPercent();
	    }
	    return total + "";
	} else if (identifier.equals("profession")) {
	    int total = 0;
	    for(Booster b : plugin.getBoosters(player, "profess")) {
		total += b.getPercent();
	    }
	    return total + "";
	} else if (types.contains(identifier)) {
	    int boost = 0;
	    for (Booster b : plugin.getBoosters(player, "all")) {
		if (b.getType().equalsIgnoreCase(identifier)) {
		    boost += b.getPercent();
		}
	    }
	    return boost + "";
	}
	return "0";
    }

}
