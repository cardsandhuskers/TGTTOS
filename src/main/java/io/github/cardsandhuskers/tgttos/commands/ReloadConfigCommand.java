package io.github.cardsandhuskers.tgttos.commands;

import io.github.cardsandhuskers.tgttos.TGTTOS;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ReloadConfigCommand implements CommandExecutor {
    private TGTTOS plugin;
    public ReloadConfigCommand(TGTTOS plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(commandSender instanceof Player p && p.isOp()) {
            plugin.reloadConfig();
            p.sendMessage(ChatColor.GREEN + "Config Reloaded");
        } else if(commandSender instanceof Player p) {
            p.sendMessage(ChatColor.RED + "You don't have permissions");
        } else {
            plugin.reloadConfig();
            plugin.getLogger().info("Config Reloaded");
        }
        return true;
    }
}
