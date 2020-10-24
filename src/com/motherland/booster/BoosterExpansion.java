package com.motherland.booster;

import java.util.List;
import org.bukkit.entity.Player;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.luckperms.api.node.Node;

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
	    return "";
	if (identifier.equals("total")) {
	    int boost = 0;
	    plugin.getPlayers().get(player.getUniqueId()).getBoosters().removeIf(b -> b.hasExpired());
	    for (Booster b : plugin.getPlayers().get(player.getUniqueId()).getBoosters()) {
		boost += b.getPercent();
	    }
	    for (Node s : plugin.getApi().getUserManager().getUser(player.getName()).data().toCollection()) {
		if (s.getKey().contains("mmocoreboosters.xp")) {
		    String[] split = s.getKey().split("\\.");
		    boost += Integer.valueOf(split[3]);
		}
	    }
	    return boost + "";
	} else if (identifier.equals("class")) {
	    int boost = 0;
	    plugin.getPlayers().get(player.getUniqueId()).getBoosters().removeIf(b -> b.hasExpired());
	    for (Booster b : plugin.getPlayers().get(player.getUniqueId()).getBoosters()) {
		if (plugin.classes.containsKey(b.getType())) {
		    boost += b.getPercent();
		}
	    }
	    for (Node s : plugin.getApi().getUserManager().getUser(player.getName()).data().toCollection()) {
		if (s.getKey().contains("mmocoreboosters.xp")) {
		    String[] split = s.getKey().split("\\.");
		    if (plugin.classes.containsKey(split[2])) {
			boost += Integer.valueOf(split[3]);
		    }
		}
	    }
	    return boost + "";
	} else if (identifier.equals("profession")) {
	    int boost = 0;
	    plugin.getPlayers().get(player.getUniqueId()).getBoosters().removeIf(b -> b.hasExpired());
	    for (Booster b : plugin.getPlayers().get(player.getUniqueId()).getBoosters()) {
		if (plugin.profess.containsKey(b.getType())) {
		    boost += b.getPercent();
		}
	    }
	    for (Node s : plugin.getApi().getUserManager().getUser(player.getName()).data().toCollection()) {
		if (s.getKey().contains("mmocoreboosters.xp")) {
		    String[] split = s.getKey().split("\\.");
		    if (plugin.profess.containsKey(split[2])) {
			boost += Integer.valueOf(split[3]);
		    }
		}
	    }
	    return boost + "";
	} else if (types.contains(identifier)) {
	    int boost = 0;
	    plugin.getPlayers().get(player.getUniqueId()).getBoosters().removeIf(b -> b.hasExpired());
	    for (Booster b : plugin.getPlayers().get(player.getUniqueId()).getBoosters()) {
		if (b.getType().equalsIgnoreCase(identifier)) {
		    boost += b.getPercent();
		}
	    }
	    for (Node s : plugin.getApi().getUserManager().getUser(player.getName()).data().toCollection()) {
		if (s.getKey().contains("mmocoreboosters.xp")) {
		    String[] split = s.getKey().split("\\.");
		    if (split[2].equalsIgnoreCase(identifier)) {
			boost += Integer.valueOf(split[3]);
		    }
		}
	    }
	    return boost + "";
	}
	return "";
    }

}
