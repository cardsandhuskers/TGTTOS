package io.github.cardsandhuskers.tgttos.listeners;

import io.github.cardsandhuskers.tgttos.TGTTOS;
import io.github.cardsandhuskers.tgttos.handlers.GameStageHandler;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.UUID;

import static io.github.cardsandhuskers.tgttos.TGTTOS.*;

public class ButtonPressListener implements Listener {
    private GameStageHandler gameStageHandler;
    private ArrayList<UUID> playersCompleted;


    private TGTTOS plugin = (TGTTOS) Bukkit.getPluginManager().getPlugin("TGTTOS");
    public ButtonPressListener(GameStageHandler gameStageHandler, ArrayList<UUID> playersCompleted) {
        this.gameStageHandler = gameStageHandler;
        this.playersCompleted = playersCompleted;
    }
    @EventHandler
    public void onButtonPress(PlayerInteractEvent e) {
        if(e.getClickedBlock() != null) {
            Material mat = e.getClickedBlock().getType();
            Player p = e.getPlayer();
            if (isButton(mat) && p.getGameMode() != GameMode.SPECTATOR) {
                numPlayersCompleted++;
                playersCompleted.add(p.getUniqueId());

                int maxPoints = plugin.getConfig().getInt("maxPoints");
                double dropoff = plugin.getConfig().getDouble("dropoff");
                double points = (multiplier * (maxPoints - (numPlayersCompleted - 1) * dropoff));

                for (Player player : Bukkit.getOnlinePlayers()) {
                    String message;
                    if(player.equals(p)) {
                        message = "You";
                    } else {
                        message = handler.getPlayerTeam(p).color + p.getName();
                    }
                    message += ChatColor.GREEN + " finished in " + ChatColor.YELLOW + ChatColor.BOLD;

                    if(numPlayersCompleted % 10 == 1) {
                        message += numPlayersCompleted + "st";
                    } else if(numPlayersCompleted % 10 == 2) {
                        message += numPlayersCompleted + "nd";
                    } else if(numPlayersCompleted % 10 == 3) {
                        message += numPlayersCompleted + "rd";
                    } else {
                        message += numPlayersCompleted + "th";
                    }
                    message += ChatColor.RESET + "" + ChatColor.GREEN + " place [" + ChatColor.YELLOW + "" + ChatColor.BOLD + "+" + points + ChatColor.RESET + ChatColor.GREEN + "] points";
                    player.sendMessage(message);
                }


                handler.getPlayerTeam(p).addTempPoints(p, points);

                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
                p.setGameMode(GameMode.SPECTATOR);
                gameStageHandler.playerFinish();
            }
        }
    }

    /**
     * checks if the block is any type of button (Buttons cannot be used in arena construction as a result of the way this is coded)
     * @param mat
     * @return if button
     */
    private boolean isButton(Material mat) {
        switch(mat) {
            case OAK_BUTTON:
            case BIRCH_BUTTON:
            case ACACIA_BUTTON:
            case CRIMSON_BUTTON:
            case DARK_OAK_BUTTON:
            case JUNGLE_BUTTON:
            case MANGROVE_BUTTON:
            case POLISHED_BLACKSTONE_BUTTON:
            case SPRUCE_BUTTON:
            case STONE_BUTTON:
            case WARPED_BUTTON:
                return true;
            default:
                return false;
        }
    }
}
