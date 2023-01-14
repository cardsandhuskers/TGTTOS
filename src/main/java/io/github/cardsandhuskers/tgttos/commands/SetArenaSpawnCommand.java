package io.github.cardsandhuskers.tgttos.commands;

import io.github.cardsandhuskers.tgttos.TGTTOS;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetArenaSpawnCommand implements CommandExecutor {
    private TGTTOS plugin;
    public SetArenaSpawnCommand(TGTTOS plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof  Player p && p.isOp()) {
            if(args.length > 0) {
                Location l = p.getLocation();
                int level;
                try {
                    level = Integer.parseInt(args[0]);
                } catch (Exception e) {
                    p.sendMessage(ChatColor.RED + "ERROR: Argument must be an integer");
                    return false;
                }
                plugin.getConfig().set("Arenas." + level + ".Spawn", l);
                plugin.saveConfig();
                p.sendMessage("Location set to " + l.toString());

            } else {
                p.sendMessage(ChatColor.RED + "ERROR: Must specify a Level number");
            }
        } else if(sender instanceof Player p) {
            p.sendMessage(ChatColor.RED + "ERROR: You do not have sufficient permission to do this");
        } else {
            System.out.println(ChatColor.RED + "ERROR: Cannot run from console");
        }

        return true;
    }

}
