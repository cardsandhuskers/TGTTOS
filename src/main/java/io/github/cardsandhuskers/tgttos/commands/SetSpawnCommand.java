package io.github.cardsandhuskers.tgttos.commands;

import io.github.cardsandhuskers.tgttos.TGTTOS;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SetSpawnCommand implements CommandExecutor {
    TGTTOS plugin;
    public SetSpawnCommand(TGTTOS plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof  Player p && p.isOp()) {
            Location l = p.getLocation();
            plugin.getConfig().set("Spawn", l);
            plugin.saveConfig();
            p.sendMessage("Location set to " + l.toString());


        } else if(sender instanceof Player p) {
            p.sendMessage(ChatColor.RED + "ERROR: You do not have sufficient permission to do this");
        } else {
            System.out.println(ChatColor.RED + "ERROR: Cannot run from console");
        }
        return true;
    }


}
