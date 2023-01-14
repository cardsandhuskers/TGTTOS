package io.github.cardsandhuskers.tgttos.listeners;

import io.github.cardsandhuskers.tgttos.TGTTOS;
import io.github.cardsandhuskers.tgttos.handlers.GameStageHandler;
import io.github.cardsandhuskers.tgttos.objects.Arena;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import static io.github.cardsandhuskers.tgttos.TGTTOS.gameState;
import static io.github.cardsandhuskers.tgttos.TGTTOS.handler;

public class PlayerJoinListener implements Listener {
    private TGTTOS plugin;
    private GameStageHandler gameStageHandler;

    public PlayerJoinListener(TGTTOS plugin, GameStageHandler gameStageHandler) {
        this.plugin = plugin;
        this.gameStageHandler = gameStageHandler;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if(handler.getPlayerTeam(p) == null) {
            p.teleport(plugin.getConfig().getLocation("Spawn"));
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()-> {
                p.setGameMode(GameMode.SPECTATOR);
            }, 10L);
            return;
        }
        if(gameState == TGTTOS.State.GAME_STARTING) {
            p.teleport(plugin.getConfig().getLocation("Spawn"));
        }
        else {
            Arena currentArena = gameStageHandler.getCurrentArena();
            p.teleport(currentArena.getSpawn());
            gameStageHandler.giveItems(p);
        }
    }
}
