package io.github.cardsandhuskers.tgttos.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class ItemThrowListener implements Listener {

    @EventHandler
    public void onItemThrow(PlayerDropItemEvent e) {
        e.setCancelled(true);
    }
}
