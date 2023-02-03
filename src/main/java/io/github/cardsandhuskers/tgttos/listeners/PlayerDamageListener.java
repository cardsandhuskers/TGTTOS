package io.github.cardsandhuskers.tgttos.listeners;

import io.github.cardsandhuskers.tgttos.TGTTOS;
import io.github.cardsandhuskers.tgttos.handlers.GameStageHandler;
import io.github.cardsandhuskers.tgttos.objects.Arena;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class PlayerDamageListener implements Listener {
    private TGTTOS plugin;
    private GameStageHandler gameStageHandler;
    public PlayerDamageListener(TGTTOS plugin, GameStageHandler gameStageHandler) {
        this.plugin = plugin;
        this.gameStageHandler = gameStageHandler;
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {
        if(e.getEntityType() != EntityType.PLAYER) return;
        if(e.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
            if(e.getEntity() instanceof Player p) {
                if(TGTTOS.gameState != TGTTOS.State.ROUND_ACTIVE) {
                    e.setCancelled(true);
                } else {
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, ()-> {
                        p.setHealth(20);
                    },1);
                }

            }
        } else if(e.getCause() == EntityDamageEvent.DamageCause.LAVA) {
            Arena arena = gameStageHandler.getCurrentArena();
            e.getEntity().teleport(arena.getSpawn());
            e.getEntity().sendMessage(ChatColor.GRAY + "You fell off");
            e.getEntity().setFireTicks(0);
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, ()-> {
                Player p = (Player)e.getEntity();
                p.setHealth(20);
            },1);
            //e.setCancelled(true);
        }else {
            e.setCancelled(true);
        }

    }
}
