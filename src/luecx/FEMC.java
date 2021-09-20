package luecx;

import luecx.volume.Materials;
import luecx.volume.Volume;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class FEMC extends JavaPlugin implements Listener {

    public static final HashMap<String, Volume> volumes = new HashMap<>();
    public static final HashMap<UUID, Location[]> TOOL_HOLDER = new HashMap<>();
    private static Plugin plugin;

    @Override
    public void onEnable() {
        plugin = this;
        PluginManager pluginManager = getServer().getPluginManager();
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (String s : volumes.keySet()) {
                volumes.get(s).render();
            }
        }, 0, 20);
        pluginManager.registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        for (String k : volumes.keySet()) {
            volumes.get(k).deleteFolder();
        }
        volumes.clear();
        plugin = null;
        try {
            FileUtils.deleteDirectory(new File("plugins/FEMC/"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        if (!(commandSender instanceof Player)) return false;

        String cmdName = command.getName().toLowerCase();
        String senderName = commandSender.getName();
        Player player = (Player) commandSender;
        UUID uuid = player.getUniqueId();


        switch (cmdName) {
            case "selector":
                player.setAllowFlight(true);
                player.setFlying(true);

                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 0f);

                player.sendMessage(" \n".repeat(100));

                // add stick
                ItemStack stack = new ItemStack(Material.STICK);
                ItemMeta meta = stack.getItemMeta();
                meta.setDisplayName("§l§7Selector");
                meta.setLore(Arrays.asList("Select an rectangular volume which will be used as the Computation Space"));
                meta.setUnbreakable(true);
                stack.setItemMeta(meta);

                // add quartz block
                ItemStack stack1 = new ItemStack(Material.BEETROOT_SEEDS);
                ItemMeta meta1 = stack1.getItemMeta();
                meta1.setDisplayName("§l§7Selector");
                meta1.setLore(Arrays.asList("Use this to click on a block to create a boundary condition"));
                meta1.setUnbreakable(true);
                stack1.setItemMeta(meta1);

                player.getInventory().clear();

                player.getInventory().setItem(8, stack);
                player.getInventory().setItem(7, stack1);

                TOOL_HOLDER.remove(uuid);
                TOOL_HOLDER.put(uuid, new Location[]{null, null});

                return true;
            case "createvolume":

                if (TOOL_HOLDER.get(uuid) == null) {
                    player.sendMessage("§6§l[FEMC] You are not registered");
                    player.sendMessage("§6§l[FEMC] Type /selector and select two blocks which will be your volume");
                    return true;
                }

                if (TOOL_HOLDER.get(uuid)[0] == null || TOOL_HOLDER.get(uuid)[1] == null) {
                    player.sendMessage("§6§l[FEMC] You have not selected two points with the selector");
                    player.sendMessage("§6§l[FEMC] Type /selector and select two blocks which will be your volume");
                    return true;
                }

                if (strings.length < 1) {
                    player.sendMessage("§6§l[FEMC] Please specify the volume name");
                    return false;
                }

                String name = strings[0];
                if (volumes.containsKey(name)) {
                    player.sendMessage("§6§l[FEMC] Volume with name: " + name + " already exists. Choose a different name!");
                    return true;
                }

                volumes.put(name, new Volume(name, TOOL_HOLDER.get(uuid)[0], TOOL_HOLDER.get(uuid)[1], player));
                player.sendMessage("§6§l[FEMC] Created volume: " + name);

                return true;
            case "deletevolume":
                if (strings.length < 1) {
                    player.sendMessage("§6§l[FEMC] Please specify the volume name");
                    return false;
                }
                name = strings[0];
                if (!volumes.containsKey(name)) {
                    player.sendMessage("§6§l[FEMC] No Volume named: " + name + " found!");
                    return true;
                }
                volumes.get(name).deleteFolder();
                volumes.remove(name);
                return true;
            case "compute":
                if (strings.length < 1) {
                    player.sendMessage("§6§l[FEMC] Please specify the volume name");
                    return false;
                }
                name = strings[0];
                if (!volumes.containsKey(name)) {
                    player.sendMessage("§6§l[FEMC] No Volume named: " + name + " found!");
                    return true;
                }
                volumes.get(name).compute(player);
                return true;
            case "scale":
                if (strings.length < 2) {
                    player.sendMessage("§6§l[FEMC] Please specify the volume name and a scaling factor");
                    return false;
                }
                name = strings[0];
                float scale = Float.parseFloat(strings[1]);
                if (!volumes.containsKey(name)) {
                    player.sendMessage("§6§l[FEMC] No Volume named: " + name + " found!");
                    return true;
                }
                volumes.get(name).displayDisplaced(scale);
        }

        return false;
    }

    @EventHandler
    public void onInventory(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();

        System.out.println("event called");

        System.out.println(e.getItem());
        if (TOOL_HOLDER.containsKey(uuid)) {
            if ((e.getItem() == null ? Material.AIR : e.getItem().getType()) == Material.STICK) {

                if (e.getClickedBlock() != null) {
                    if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
                        TOOL_HOLDER.get(uuid)[0] = e.getClickedBlock().getLocation();
                        p.sendMessage("§6§l[FEMC] Set first position to: "
                                + e.getClickedBlock().getLocation().getX() + " "
                                + e.getClickedBlock().getLocation().getY() + " "
                                + e.getClickedBlock().getLocation().getZ() + " ");
                    } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        TOOL_HOLDER.get(uuid)[1] = e.getClickedBlock().getLocation();
                        p.sendMessage("§6§l[FEMC] Set second position to: "
                                + e.getClickedBlock().getLocation().getX() + " "
                                + e.getClickedBlock().getLocation().getY() + " "
                                + e.getClickedBlock().getLocation().getZ() + " ");
                    }
                }

                e.setCancelled(true);
            } else if ((e.getItem() == null ? Material.AIR : e.getItem().getType()) == Material.BEETROOT_SEEDS) {
                if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
                    for (String s : volumes.keySet()) {
                        volumes.get(s).constraint(e.getClickedBlock(), true);
                    }
                } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    for (String s : volumes.keySet()) {
                        volumes.get(s).constraint(e.getClickedBlock(), false);
                    }
                }
                e.setCancelled(true);

            }
        }
    }
}
