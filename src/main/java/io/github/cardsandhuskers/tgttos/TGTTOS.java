package io.github.cardsandhuskers.tgttos;

import io.github.cardsandhuskers.teams.Teams;
import io.github.cardsandhuskers.teams.handlers.TeamHandler;
import io.github.cardsandhuskers.tgttos.commands.*;
import io.github.cardsandhuskers.tgttos.objects.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class TGTTOS extends JavaPlugin {

    public static int timeVar = 0;
    public static TeamHandler handler;
    public static State gameState = State.GAME_STARTING;
    public static int currentRound;
    public static double multiplier = 1;
    public static int playersCompleted = 0;
    public static int totalPlayers = 0;
//create pos1 and pos2 that are arena corners, set everything inside to air
    @Override
    public void onEnable() {
        // Plugin startup logic

        //Placeholder API validation
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            /*
             * We register the EventListener here, when PlaceholderAPI is installed.
             * Since all events are in the main class (this class), we simply use "this"
             */
            new Placeholder(this).register();

        } else {
            /*
             * We inform about the fact that PlaceholderAPI isn't installed and then
             * disable this plugin to prevent issues.
             */
            System.out.println("Could not find PlaceholderAPI! This plugin is required.");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        getCommand("setTGTTOSPos1").setExecutor(new SetPos1Command(this));
        getCommand("setTGTTOSPos2").setExecutor(new SetPos2Command(this));
        getCommand("setTGTTOSSpawnPoint").setExecutor(new SetSpawnCommand(this));
        getCommand("setTGTTOSArenaSpawnPoint").setExecutor(new SetArenaSpawnCommand(this));
        StartGameCommand startGameCommand = new StartGameCommand(this);
        getCommand("startTGTTOS").setExecutor(startGameCommand);
        getCommand("setTGTTOSLobby").setExecutor(new SetLobbyCommand(this));
        getCommand("reloadTGTTOS").setExecutor(new ReloadConfigCommand(this));
        getCommand("cancelTGTTOS").setExecutor(new CancelGameCommand(this, startGameCommand));

        handler = Teams.handler;

        saveDefaultConfig();
        getConfig().options().copyDefaults(true);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    public enum State {
        GAME_STARTING,
        ROUND_ACTIVE,
        BETWEEN_ROUND,
        ROUND_OVER,
        GAME_OVER
    }
}
