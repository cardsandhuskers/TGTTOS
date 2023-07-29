package io.github.cardsandhuskers.tgttos.listeners;

import io.github.cardsandhuskers.tgttos.handlers.GameStageHandler;
import io.github.cardsandhuskers.tgttos.objects.Arena;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import static io.github.cardsandhuskers.tgttos.TGTTOS.handler;

public class BlockPlaceListener implements Listener {
    private GameStageHandler gameStageHandler;

    public BlockPlaceListener(GameStageHandler gameStageHandler) {
        this.gameStageHandler = gameStageHandler;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Arena arena = gameStageHandler.getCurrentArena();
        Location l = e.getBlock().getLocation();
        //if arena is null (not yet initialized), player is not in currentArena or game is not active yet, cancel the place attempt
        if(arena == null || !arena.isInArena(l) || !gameStageHandler.isGameActive()) {
            e.setCancelled(true);
        } else {
            Inventory inv = e.getPlayer().getInventory();
            Material wool = handler.getPlayerTeam(e.getPlayer()).getWoolColor();
            for(int i = 0; i <=35; i++) {
                if(inv.getItem(i) != null && inv.getItem(i).getType() == wool) {
                    inv.setItem(i, new ItemStack(wool, 64));
                }
            }

            if(e.getPlayer().getInventory().getItemInOffHand().getType() == wool) {
                e.getPlayer().getInventory().setItemInOffHand(new ItemStack(wool, 64));
            }
        }
    }
}
