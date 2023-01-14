package io.github.cardsandhuskers.tgttos.listeners;

import io.github.cardsandhuskers.tgttos.handlers.GameStageHandler;
import io.github.cardsandhuskers.tgttos.objects.Arena;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public class PlayerMoveListener implements Listener {
    private GameStageHandler gameStageHandler;
    public PlayerMoveListener(GameStageHandler gameStageHandler) {
        this.gameStageHandler = gameStageHandler;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Arena arena = gameStageHandler.getCurrentArena();
        if(arena != null && arena.hasFallen(e.getTo())) {
            e.getPlayer().teleport(arena.getSpawn());
            e.getPlayer().sendMessage(ChatColor.GRAY + "You fell off");
        }
    }
}
