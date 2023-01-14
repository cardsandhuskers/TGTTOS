package io.github.cardsandhuskers.tgttos.commands;

import io.github.cardsandhuskers.tgttos.TGTTOS;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SetPos1Command implements CommandExecutor {
    private TGTTOS plugin;
    public SetPos1Command(TGTTOS plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof  Player p && p.isOp()) {
            if(args.length > 0) {
                Location l = p.getLocation();
                //moves position to player's head, making it easier to do this
                int level;
                try {
                    level = Integer.parseInt(args[0]);
                } catch (Exception e) {
                    p.sendMessage(ChatColor.RED + "ERROR: Argument must be an integer");
                    return false;
                }
                plugin.getConfig().set("Arenas." + level + ".pos1", l);
                plugin.saveConfig();
                p.sendMessage("Location set to " + l.toString());
                return true;

            } else {
                p.sendMessage(ChatColor.RED + "ERROR: Must specify a Level number");
            }
        } else if(sender instanceof Player p) {
            p.sendMessage(ChatColor.RED + "ERROR: You do not have sufficient permission to do this");
        } else {
            System.out.println(ChatColor.RED + "ERROR: Cannot run from console");
        }

        return false;
    }
}
