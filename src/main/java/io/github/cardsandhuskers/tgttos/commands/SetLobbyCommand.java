package io.github.cardsandhuskers.tgttos.commands;

import io.github.cardsandhuskers.tgttos.TGTTOS;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SetLobbyCommand implements CommandExecutor {
    TGTTOS plugin;
    public SetLobbyCommand(TGTTOS plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(sender instanceof Player p) {
            if(p.isOp()) {
                Location location = p.getLocation();

                plugin.getConfig().set("Lobby", location);
                plugin.saveConfig();
                p.sendMessage("Lobby set at:\nWorld: " + location.getWorld() + "\nX: " + location.getX() + " Y: " + location.getY() + " Z: " + location.getZ());
            } else {
                p.sendMessage(ChatColor.RED + "You do not have permission to do this");
            }
        } else {
            System.out.println("ERROR: cannot run from console.");
        }
        return true;
    }

}
