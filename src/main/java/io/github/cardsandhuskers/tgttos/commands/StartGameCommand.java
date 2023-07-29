package io.github.cardsandhuskers.tgttos.commands;

import io.github.cardsandhuskers.teams.objects.Team;
import io.github.cardsandhuskers.tgttos.TGTTOS;
import io.github.cardsandhuskers.tgttos.handlers.GameStageHandler;
import io.github.cardsandhuskers.tgttos.objects.Arena;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class StartGameCommand implements CommandExecutor {

    private TGTTOS plugin;
    private GameStageHandler gameStageHandler;
    public StartGameCommand(TGTTOS plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof Player p) {
            if(!p.isOp()) {
                p.sendMessage(ChatColor.RED + "You do not have permission to do this");
                return true;
            }
            if (args.length > 0) {
                try {
                    TGTTOS.multiplier = Double.parseDouble(args[0]);
                    startGame();
                } catch (NumberFormatException e) {
                    p.sendMessage(ChatColor.RED + "ERROR: argument must be a double");
                }
            } else {
                startGame();
            }
        } else {
            if (args.length > 0) {
                try {
                    TGTTOS.multiplier = Double.parseDouble(args[0]);
                    startGame();
                } catch (NumberFormatException e) {
                    System.out.println(ChatColor.RED + "ERROR: argument must be a double");
                }
            } else {
                startGame();
            }
        }

        return true;
    }

    /**
     * Builds the arena list and calls the stageHandler, initializes some things
     */
    public void startGame() {
        int counter = 1;
        TGTTOS.currentRound = 0;
        ArrayList<Arena> arenas = new ArrayList<>();
        while(plugin.getConfig().get("Arenas." + counter) != null) {
            Arena arena = new Arena(counter, plugin);
            arenas.add(arena);
            counter++;
        }
        //System.out.println(arenas);
        //RESETS TEMP POINTS, IMPORTANT!
        for(Team t:TGTTOS.handler.getTeams()) {
            t.resetTempPoints();
        }

        //makes GameStageHandler and starts game
        gameStageHandler = new GameStageHandler(arenas, plugin);
        gameStageHandler.startGame();
    }

    public boolean cancelTimers() {
        if(gameStageHandler == null) return false;
        return gameStageHandler.cancelTimers();
    }
}
