package io.github.cardsandhuskers.tgttos.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerTrampleListener implements Listener {

    @EventHandler
    public void onPlayerTrample(PlayerInteractEvent e) {
        if(e.getClickedBlock() == null) return;
        if(e.getAction() == Action.PHYSICAL && e.getClickedBlock().getType() == Material.FARMLAND) {
            e.setCancelled(true);
        }
    }
}
