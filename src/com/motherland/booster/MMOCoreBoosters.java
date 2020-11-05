package com.motherland.booster;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
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
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import me.clip.placeholderapi.PlaceholderAPI;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerExperienceGainEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.luckperms.api.node.Node;
import net.luckperms.api.query.QueryOptions;

public class MMOCoreBoosters extends JavaPlugin implements Listener, CommandExecutor {
    private List<String> help = new ArrayList<>();
    public static Map<String, String> msgs = new HashMap<>();
    public Map<String, String> profess = new HashMap<>();
    public Map<String, String> classes = new HashMap<>();
    private List<String> types = new ArrayList<>();
    private LuckPerms api;
    private ItemStack item;
    private EventSubscription<NodeRemoveEvent> sub;
    private Boolean debug;

    public void onEnable() {
	Bukkit.getPluginManager().registerEvents(this, this);
	this.getCommand("mmocoreb").setExecutor(this);
	this.saveDefaultConfig();
	help = this.getConfig().getStringList("mmocoreb");
	debug = this.getConfig().getBoolean("debug");
	MMOCore.plugin.classManager.getAll().forEach(clas -> {
	    classes.put(clas.getId().toLowerCase(), clas.getId());
	    types.add(clas.getId().toLowerCase());
	});
	MMOCore.plugin.professionManager.getAll().forEach(pro -> {
	    profess.put(pro.getId().toLowerCase().replace("_", "-").replace(" ", "-"), pro.getId());
	    types.add(pro.getId().toLowerCase().replace("_", "-").replace(" ", "-"));
	});
	new BoosterExpansion(this, types).register();
	item = loadItem();
	for (String s : this.getConfig().getConfigurationSection("messages").getKeys(false)) {
	    msgs.put(s, this.getConfig().getString("messages." + s));
	}
	RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
	if (provider != null) {
	    api = provider.getProvider();
	    EventBus eventBus = api.getEventBus();
	    sub = eventBus.subscribe(NodeRemoveEvent.class, this::expire);
	}
    }

    public void onDisable() {
	sub.close();
    }

    public List<Booster> getBoosters(Player p, String type) {
	List<Booster> boost = new ArrayList<>();
	for (Node s : getApi().getUserManager().getUser(p.getName())
		.resolveInheritedNodes(QueryOptions.nonContextual())) {
	    if (s.getKey().contains("mmocoreboosters.xp")) {
		String[] split = s.getKey().split("\\.");
		if (profess.containsKey(split[2]) && !type.equalsIgnoreCase("class")) {
		    if (s.hasExpiry()) {
			boost.add(new Booster(split[2], s.getExpiryDuration().getSeconds() / 60l,
				Double.valueOf(split[3] + (split.length == 5 ? ("." + split[4]) : ".0")), s));
		    } else {
			boost.add(new Booster(split[2], -1l, Double.valueOf(split[3] + (split.length == 5 ? ("." + split[4]) : ".0")), s));
		    }
		} else if (classes.containsKey(split[2]) && !type.equalsIgnoreCase("profess")) {
		    if (s.hasExpiry()) {
			boost.add(new Booster(split[2], s.getExpiryDuration().getSeconds() / 60l,
				Double.valueOf(split[3] + (split.length == 5 ? ("." + split[4]) : ".0")), s));
		    } else {
			boost.add(new Booster(split[2], -1l, Double.valueOf(split[3] + (split.length == 5 ? ("." + split[4]) : ".0")), s));
		    }
		}
	    }
	}
	return boost;
    }

    public ItemStack loadItem() {
	if (Material.getMaterial(this.getConfig().getString("booster.mat").toUpperCase()) != null) {
	    CustomItem item = new CustomItem(
		    Material.getMaterial(this.getConfig().getString("booster.mat").toUpperCase()))
			    .setName(this.getConfig().getString("booster.name"))
			    .setLore(this.getConfig().getStringList("booster.lore"));
	    if (this.getConfig().getBoolean("booster.glow"))
		item.addGlow();
	    return item.build();
	}
	return new ItemStack(Material.EMERALD);
    }

    private String toTitleCase(String str) {
	if (str == null || str.isEmpty())
	    return "";
	if (str.length() == 1)
	    return str.toUpperCase();
	String[] parts = str.split(" ");
	StringBuilder sb = new StringBuilder(str.length());
	for (String part : parts) {
	    if (part.length() > 1)
		sb.append(part.substring(0, 1).toUpperCase()).append(part.substring(1).toLowerCase());
	    else
		sb.append(part.toUpperCase());
	    sb.append(" ");
	}
	return sb.toString().trim();
    }

    public ItemMeta updateLore(Player p, ItemStack item, int time, double percent, String type) {
	List<String> lore = item.getItemMeta().getLore();
	List<String> update = new ArrayList<>();
	for (String s : lore) {
	    s = s.replaceAll("%percent%", percent + "");
	    s = s.replaceAll("%time%", time + "");
	    s = s.replaceAll("%type%", toTitleCase(type));
	    update.add(s);
	}
	ItemMeta meta = item.getItemMeta();
	String name = this.getConfig().getString("booster.name");
	meta.setDisplayName(PlaceholderAPI.setPlaceholders(p, name));
	meta.getPersistentDataContainer().set(NamespacedKey.minecraft("booster"), PersistentDataType.STRING,
		type + "," + time + "," + (percent + ""));
	meta.setLore(update);
	return meta;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
	if (sender instanceof Player)
	    if (cmd.getName().equalsIgnoreCase("mmocoreb")) {
		Player p = (Player) sender;
		if (args.length == 0) {
		    log(sender, help);
		} else if (args.length == 1) {
		    if (args[0].equalsIgnoreCase("reload")) {
			if (sender.hasPermission("mmocoreboosters.admin")) {
			    reloadConfig();
			    getConfig().options().copyDefaults(true);
			    saveDefaultConfig();
			    item = loadItem();
			    log(sender, msgs.get("reload"));
			} else {
			    log(sender, msgs.get("invalid-perms"));
			}
		    } else if (args[0].equalsIgnoreCase("debug")) {
			if (sender.hasPermission("mmocoreboosters.admin")) {
			    this.debug = this.debug ? false : true;
			    this.getConfig().set("debug", debug);
			    this.saveConfig();
			    log(sender, msgs.get("debug"));
			} else {
			    log(sender, msgs.get("invalid-perms"));
			}
		    } else if (args[0].equalsIgnoreCase("boosters")) {
			log(sender, msgs.get("boost-list-header"));
			for (Booster b : getBoosters((Player) sender, "all")) {
			    log(sender,
				    msgs.get("boost-list").replaceAll("%type%", toTitleCase(b.getType()))
					    .replaceAll("%timeleft%", b.getNode().hasExpiry()
						    ? Math.max(b.getNode().getExpiryDuration().getSeconds() / 60L, 1)
							    + ""
						    : "permanent")
					    .replaceAll("%percent%", (b.getPercent() + "").replaceAll("\\.0", "")));
			}
		    } else {
			log(sender, msgs.get("invalid-cmd"));
		    }
		} else if (args.length == 4) {
		    if (args[0].equalsIgnoreCase("give")) {
			if (sender.hasPermission("mmocoreboosters.admin")) {
			    if (Bukkit.getPlayer(args[1]) != null) {
				if (types.contains(args[2].toLowerCase())) {
				    try {
					Double.parseDouble(args[3]);
					api.getUserManager().getUser(Bukkit.getPlayer(args[1]).getUniqueId()).data()
						.add(Node.builder("mmocoreboosters.xp." + args[2].toLowerCase() + "."
							+ args[3]).build());
					api.getUserManager().saveUser(
						api.getUserManager().getUser(Bukkit.getPlayer(args[1]).getUniqueId()));
					log(sender, msgs.get("perm-add"));
				    } catch (Exception e) {
					log(sender, msgs.get("invalid-cmd"));
				    }
				} else {
				    log(sender, msgs.get("invalid-cmd"));
				}
			    } else {
				log(sender, msgs.get("invalid-cmd"));
			    }
			} else {
			    log(sender, msgs.get("invalid-perms"));
			}
		    } else if (args[0].equalsIgnoreCase("remove")) {
			if (sender.hasPermission("mmocoreboosters.admin")) {
			    if (Bukkit.getPlayer(args[1]) != null) {
				if (types.contains(args[2].toLowerCase())) {
				    try {
					for (Booster b : getBoosters(Bukkit.getPlayer(args[1]), "all")) {
					    Double.parseDouble(args[3]);
					    if (b.getNode() == Node.builder("mmocoreboosters.xp."
						    + args[2].toLowerCase() + "." + args[3])
						    .build()) {
						api.getUserManager().getUser(Bukkit.getPlayer(args[1]).getUniqueId())
							.data().remove(b.getNode());
						api.getUserManager().saveUser(api.getUserManager()
							.getUser(Bukkit.getPlayer(args[1]).getUniqueId()));
						log(sender, msgs.get("perm-add"));
						return true;
					    }
					}
					log(sender, msgs.get("invalid-boost"));
				    } catch (Exception e) {
					log(sender, msgs.get("invalid-cmd"));
				    }
				} else {
				    log(sender, msgs.get("invalid-cmd"));
				}
			    } else {
				log(sender, msgs.get("invalid-cmd"));
			    }
			} else {
			    log(sender, msgs.get("invalid-perms"));
			}
		    } else {
			log(sender, msgs.get("invalid-cmd"));
		    }
		} else if (args.length == 5) {
		    if (args[0].equalsIgnoreCase("give")) {
			if (sender.hasPermission("mmocoreboosters.admin")) {
			    if (Bukkit.getPlayer(args[1]) != null) {
				if (types.contains(args[2].toLowerCase())) {
				    try {
					if (args[4].chars().allMatch(Character::isDigit)) {
					    ItemStack give = item.clone();
					    give.setItemMeta(updateLore(p, give, Integer.valueOf(args[4]),
						    Double.parseDouble(args[3]), args[2].toLowerCase()));
					    Bukkit.getPlayer(args[1]).getInventory().addItem(give);
					    log(sender, msgs.get("boost-add"));
					} else {
					    log(sender, msgs.get("invalid-cmd"));
					}
				    } catch (Exception e) {
					log(sender, msgs.get("invalid-cmd"));
				    }
				} else {
				    log(sender, msgs.get("invalid-cmd"));
				}
			    } else {
				log(sender, msgs.get("invalid-cmd"));
			    }
			} else {
			    log(sender, msgs.get("invalid-perms"));
			}
		    } else {
			log(sender, msgs.get("invalid-cmd"));
		    }
		}
	    }
	return true;
    }

    public void expire(NodeRemoveEvent e) {
	if (e.getNode().hasExpiry() && e.getNode().getExpiry().isBefore(Instant.now())) {
	    if (e.getNode().getKey().contains("mmocoreboosters.xp")) {
		String[] split = e.getNode().getKey().split("\\.");
		log(Bukkit.getPlayer(e.getTarget().getFriendlyName()), msgs.get("boost-expire")
			.replaceAll("%percent%", (split[3] + (split.length == 5 ? ("." + split[4]) : ".0"))).replaceAll("%type%", split[2]));
		if (debug)
		    System.out.println(e.getTarget().getFriendlyName() + "'s x" +(split[3] + (split.length == 5 ? ("." + split[4]) : ".0"))
			    + split[2] + " booster has expired!");
	    }
	}
    }

    @EventHandler
    public void tabComplete(TabCompleteEvent e) {
	long spaces = e.getBuffer().chars().filter(c -> c == ' ').count();
	List<String> Players = new ArrayList<>();
	for (Player p : Bukkit.getOnlinePlayers()) {
	    Players.add(p.getName());
	}
	if (e.getBuffer().toLowerCase().contains("/mmocoreb ")) {
	    if (e.getSender().hasPermission("mmocoreboosters.admin")) {
		List<String> complete = Arrays.asList("give", "remove", "reload", "boosters", "debug");
		e.setCompletions(complete);
		if (e.getBuffer().toLowerCase().contains("/mmocoreb reload ")) {
		    e.setCompletions(new ArrayList<>());
		} else if (e.getBuffer().toLowerCase().contains("/mmocoreb give ")
			|| e.getBuffer().toLowerCase().contains("/mmocoreb remove ")) {
		    if (spaces == 2) {
			e.setCompletions(Players);
		    } else if (spaces == 3) {
			e.setCompletions(types);
		    } else if (spaces < 6) {
			e.setCompletions(Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"));
		    } else if (spaces > 5) {
			e.setCompletions(Arrays.asList(""));
		    }
		} else if (e.getBuffer().toLowerCase().contains("/mmocoreb boosters")) {
		    e.setCompletions(new ArrayList<>());
		}
	    } else {
		if (spaces == 1) {
		    e.setCompletions(Arrays.asList("boosters"));
		} else if (spaces > 1) {
		    e.setCompletions(new ArrayList<>());
		}
	    }
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
			    getApi().getUserManager().getUser(e.getPlayer().getUniqueId()).data()
				    .add(Node
					    .builder("mmocoreboosters.xp." + split[0].toLowerCase() + "."
						    + split[2])
					    .expiry(Long.valueOf(split[1]), TimeUnit.MINUTES).build());
			    api.getUserManager().saveUser(api.getUserManager()
					.getUser(e.getPlayer().getUniqueId()));
			    item.setAmount(item.getAmount() - 1);
			    log(e.getPlayer(), msgs.get("boost-use"));
			    if (debug)
				System.out.println(e.getPlayer().getName() + "has activated a x"
					+ split[2] + split[0] + " booster!");
			}

    }

    @EventHandler
    public void xpGain(PlayerExperienceGainEvent e) {
	int exp = e.getExperience();
	int add = 0;
	for (Booster b : getBoosters(e.getPlayer(), "all")) {
	    if (e.getProfession() != null && e.getProfession().getId().equalsIgnoreCase(b.getType())) {
		add += (exp * b.getPercent());
	    } else if (PlayerData.get(e.getPlayer().getUniqueId()).getProfess().getId().equalsIgnoreCase(b.getType())) {
		add += (exp * b.getPercent());
	    }
	}
	if (debug)
	    System.out.println(e.getPlayer().getName() + "'s boosters added +" + add + "xp from " + exp + "! Total = "
		    + (exp + add));
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

}
