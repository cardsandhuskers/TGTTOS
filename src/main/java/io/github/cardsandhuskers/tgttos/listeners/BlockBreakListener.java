package io.github.cardsandhuskers.tgttos.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {

    public BlockBreakListener() {

    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        e.setCancelled(true);
        if(isWool(e.getBlock())) {
            e.getBlock().setType(Material.AIR);
        }
    }



    private boolean isWool(Block b) {
        switch(b.getType()) {
            case GREEN_WOOL:
            case LIGHT_BLUE_WOOL:
            case CYAN_WOOL:
            case PURPLE_WOOL:
            case ORANGE_WOOL:
            case LIGHT_GRAY_WOOL:
            case GRAY_WOOL:
            case BLUE_WOOL:
            case LIME_WOOL:
            case RED_WOOL:
            case MAGENTA_WOOL:
            case YELLOW_WOOL:
            case WHITE_WOOL: return true;
            default: return false;
        }
    }
}
