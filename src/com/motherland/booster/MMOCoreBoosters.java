package com.motherland.booster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerExperienceGainEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.node.Node;

public class MMOCoreBoosters extends JavaPlugin implements Listener, CommandExecutor {
    private List<String> help = new ArrayList<>();
    private Map<String, String> msgs = new HashMap<>();
    public Map<String, String> profess = new HashMap<>();
    public Map<String, String> classes = new HashMap<>();
    private Map<UUID, PlayerDataConfig> players = new HashMap<>();
    private List<String> types = new ArrayList<>();
    private LuckPerms api;
    private ItemStack item;

    public void onEnable() {
	this.getServer().getPluginManager().registerEvents(this, this);
	this.getCommand("mmocoreb").setExecutor(this);
	this.saveDefaultConfig();
	help = this.getConfig().getStringList("mmocoreb");
	item = loadItem();
	MMOCore.plugin.classManager.getAll().forEach(clas -> {
	    classes.put(clas.getId().toLowerCase(), clas.getId());
	    types.add(clas.getId().toLowerCase());
	});
	MMOCore.plugin.professionManager.getAll().forEach(pro -> {
	    profess.put(pro.getId().toLowerCase().replace("_", "-").replace(" ", "-"), pro.getId());
	    types.add(pro.getId().toLowerCase().replace("_", "-").replace(" ", "-"));
	});
	new BoosterExpansion(this, types).register();
	for (String s : this.getConfig().getConfigurationSection("messages").getKeys(false)) {
	    msgs.put(s, this.getConfig().getString("messages." + s));
	}
	for (Player p : Bukkit.getOnlinePlayers()) {
	    if (!players.containsKey(p.getUniqueId())) {
		players.put(p.getUniqueId(), new PlayerDataConfig(this, p.getUniqueId()));
	    }
	}
	RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
	if (provider != null) {
	    api = provider.getProvider();
	}
    }

    public ItemStack loadItem() {
	if (Material.getMaterial(this.getConfig().getString("booster.mat").toUpperCase()) != null)
	    return new CustomItem(Material.getMaterial(this.getConfig().getString("booster.mat").toUpperCase()))
		    .setName(this.getConfig().getString("booster.name"))
		    .setLore(this.getConfig().getStringList("booster.lore")).build();
	return new ItemStack(Material.EMERALD);
    }

    public ItemMeta updateLore(ItemStack item, int time, int percent, String type) {
	List<String> lore = item.getItemMeta().getLore();
	List<String> update = new ArrayList<>();
	for (String s : lore) {
	    s = s.replaceAll("%percent%", percent + "");
	    s = s.replaceAll("%time%", time + "");
	    s = s.replaceAll("%type%", type.toUpperCase());
	    update.add(s);
	}
	ItemMeta meta = item.getItemMeta();
	meta.getPersistentDataContainer().set(NamespacedKey.minecraft("booster"), PersistentDataType.STRING,
		type + "," + time + "," + percent);
	meta.setLore(update);
	return meta;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
	if (sender instanceof Player)
	    if (cmd.getName().equalsIgnoreCase("mmocoreb") && sender.hasPermission("mmocoreboosters.admin")) {
		if (args.length == 0) {
		    log(sender, help);
		} else if (args.length == 1) {
		    if (args[0].equalsIgnoreCase("reload")) {
			for (PlayerDataConfig config : players.values()) {
			    config.save();
			    config.reload();
			}
			reloadConfig();
			getConfig().options().copyDefaults(true);
			saveDefaultConfig();
			item = loadItem();
			log(sender, msgs.get("reload"));
		    } else {
			log(sender, msgs.get("invalid-cmd"));
		    }
		} else if (args.length == 4) {
		    if (args[0].equalsIgnoreCase("give")) {
			if (Bukkit.getPlayer(args[1]) != null) {
			    if (classes.containsKey(args[2].toLowerCase())) {
				if (args[3].chars().allMatch(Character::isDigit)) {
				    api.getUserManager().getUser(Bukkit.getPlayer(args[1]).getUniqueId()).data().add(
					    Node.builder("mmocoreboosters.xp." + args[2].toLowerCase() + "." + args[3])
						    .build());
				    api.getUserManager().saveUser(
					    api.getUserManager().getUser(Bukkit.getPlayer(args[1]).getUniqueId()));
				    log(sender, msgs.get("perm-add"));
				} else {
				    log(sender, msgs.get("invalid-cmd"));
				}
			    } else if (profess.containsKey(args[2].toLowerCase())) {
				if (args[3].chars().allMatch(Character::isDigit)) {
				    api.getUserManager().getUser(Bukkit.getPlayer(args[1]).getUniqueId()).data().add(
					    Node.builder("mmocoreboosters.xp." + args[2].toLowerCase() + "." + args[3])
						    .build());
				    api.getUserManager().saveUser(
					    api.getUserManager().getUser(Bukkit.getPlayer(args[1]).getUniqueId()));
				    log(sender, msgs.get("perm-add"));
				} else {
				    log(sender, msgs.get("invalid-cmd"));
				}
			    } else {
				log(sender, msgs.get("invalid-cmd"));
			    }
			} else {
			    log(sender, msgs.get("invalid-cmd"));
			}
		    } else {
			log(sender, msgs.get("invalid-cmd"));
		    }
		} else if (args.length == 5) {
		    if (args[0].equalsIgnoreCase("give")) {
			if (Bukkit.getPlayer(args[1]) != null) {
			    if (classes.containsKey(args[2].toLowerCase())) {
				if (args[3].chars().allMatch(Character::isDigit)) {
				    if (args[4].chars().allMatch(Character::isDigit)) {
					ItemStack give = item.clone();
					give.setItemMeta(updateLore(give, Integer.valueOf(args[4]),
						Integer.valueOf(args[3]), args[2].toLowerCase()));
					Bukkit.getPlayer(args[1]).getInventory().addItem(give);
					log(sender, msgs.get("boost-add"));
				    } else {
					log(sender, msgs.get("invalid-cmd"));
				    }
				} else {
				    log(sender, msgs.get("invalid-cmd"));
				}
			    } else if (profess.containsKey(args[2].toLowerCase())) {
				if (args[3].chars().allMatch(Character::isDigit)) {
				    if (args[4].chars().allMatch(Character::isDigit)) {
					ItemStack give = item.clone();
					give.setItemMeta(updateLore(give, Integer.valueOf(args[4]),
						Integer.valueOf(args[3]), args[2].toLowerCase()));
					Bukkit.getPlayer(args[1]).getInventory().addItem(give);
					log(sender, msgs.get("boost-add"));
				    } else {
					log(sender, msgs.get("invalid-cmd"));
				    }
				} else {
				    log(sender, msgs.get("invalid-cmd"));
				}
			    } else {
				log(sender, msgs.get("invalid-cmd"));
			    }
			} else {
			    log(sender, msgs.get("invalid-cmd"));
			}
		    } else {
			log(sender, msgs.get("invalid-cmd"));
		    }
		}

	    }
	return true;
    }

    @EventHandler
    public void tabComplete(TabCompleteEvent e) {
	if (e.getBuffer().toLowerCase().contains("/mmocoreb ")) {
	    List<String> complete = Arrays.asList("give", "reload");
	    e.setCompletions(complete);
	}
	if (e.getBuffer().toLowerCase().contains("/mmocoreb reload ")) {
	    e.setCompletions(new ArrayList<>());
	}
	if (e.getBuffer().toLowerCase().contains("/mmocoreb give ")) {
	    List<String> complete = new ArrayList<>();
	    for (Player p : Bukkit.getOnlinePlayers()) {
		complete.add(p.getName());
	    }
	    e.setCompletions(complete);
	}
	if (e.getBuffer().toLowerCase().contains("/mmocoreb give ") && e.getBuffer().split(" ").length >= 3
		&& Bukkit.getPlayer(e.getBuffer().split(" ")[2]) != null) {
	    e.setCompletions(types);
	}
	
	if (e.getBuffer().toLowerCase().contains("/mmocoreb give ") && e.getBuffer().split(" ").length >= 4) {
	    e.setCompletions(Arrays.asList("1","2","3","4","5","6","7","8","9","10"));
	}
	if (e.getBuffer().toLowerCase().contains("/mmocoreb give ") && e.getBuffer().split(" ").length >= 6) {
	    e.setCompletions(Arrays.asList(""));
	}

    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
	if (!players.containsKey(e.getPlayer().getUniqueId())) {
	    players.put(e.getPlayer().getUniqueId(), new PlayerDataConfig(this, e.getPlayer().getUniqueId()));
	}
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
	ItemStack item = e.getPlayer().getEquipment().getItemInMainHand();
	if (e.getHand().equals(EquipmentSlot.HAND))
	    if (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK))
		if (item != null)
		    if (item.hasItemMeta())
			if (item.getItemMeta().getPersistentDataContainer().has(NamespacedKey.minecraft("booster"),
				PersistentDataType.STRING)) {
			    String s = item.getItemMeta().getPersistentDataContainer()
				    .get(NamespacedKey.minecraft("booster"), PersistentDataType.STRING);
			    String[] split = s.split(",");
			    if (!players.containsKey(e.getPlayer().getUniqueId())) {
				players.put(e.getPlayer().getUniqueId(),
					new PlayerDataConfig(this, e.getPlayer().getUniqueId()));
			    }
			    players.get(e.getPlayer().getUniqueId()).createBooster(Integer.valueOf(split[2]),
				    Integer.valueOf(split[1]), split[0]);
			    item.setAmount(item.getAmount() - 1);
			}

    }

    @EventHandler
    public void xpGain(PlayerExperienceGainEvent e) {
	int exp = e.getExperience();
	int add = 0;
	players.get(e.getPlayer().getUniqueId()).getBoosters().removeIf(b -> b.hasExpired());
	for (Booster b : players.get(e.getPlayer().getUniqueId()).getBoosters()) {
	    if (e.getProfession() != null && e.getProfession().getId().equalsIgnoreCase(b.getType())) {
		add += (exp * (b.getPercent() / 100));
	    } else if (PlayerData.get(e.getPlayer().getUniqueId()).getProfess().getId().equalsIgnoreCase(b.getType())) {
		add += (exp * (b.getPercent() / 100));
	    }
	}
	for (Node s : api.getUserManager().getUser(e.getPlayer().getName()).data().toCollection()) {
	    if (s.getKey().contains("mmocoreboosters.xp")) {
		String[] split = s.getKey().split("\\.");
		if (e.getProfession() != null && e.getProfession().getId().equalsIgnoreCase(split[2])) {
		    add += (exp * (Integer.valueOf(split[3]) / 100));
		} else if (PlayerData.get(e.getPlayer().getUniqueId()).getProfess().getId()
			.equalsIgnoreCase(split[2])) {
		    add += (exp * (Integer.valueOf(split[3]) / 100));
		}
	    }
	}
	e.setExperience(exp + add);
    }

    public static void log(CommandSender sender, String... s) {
	for (String msg : s) {
	    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
	}
    }

    public static void log(CommandSender sender, List<String> s) {
	for (String msg : s) {
	    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
	}
    }

    public LuckPerms getApi() {
	return api;
    }

    public Map<UUID, PlayerDataConfig> getPlayers() {
	return players;
    }

}
