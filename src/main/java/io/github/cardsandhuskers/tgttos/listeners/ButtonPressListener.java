package io.github.cardsandhuskers.tgttos.listeners;

import io.github.cardsandhuskers.tgttos.TGTTOS;
import io.github.cardsandhuskers.tgttos.handlers.GameStageHandler;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import static io.github.cardsandhuskers.tgttos.TGTTOS.*;

public class ButtonPressListener implements Listener {
    private GameStageHandler gameStageHandler;
    private TGTTOS plugin = (TGTTOS) Bukkit.getPluginManager().getPlugin("TGTTOS");
    public ButtonPressListener(GameStageHandler gameStageHandler) {
        this.gameStageHandler = gameStageHandler;
    }
    @EventHandler
    public void onButtonPress(PlayerInteractEvent e) {
        if(e.getClickedBlock() != null) {
            Material mat = e.getClickedBlock().getType();
            Player p = e.getPlayer();
            if (isButton(mat) && p.getGameMode() != GameMode.SPECTATOR) {
                playersCompleted++;

                int maxPoints = plugin.getConfig().getInt("maxPoints");
                double dropoff = plugin.getConfig().getDouble("dropoff");
                double points = (multiplier * (maxPoints - (playersCompleted - 1) * dropoff));


                //1st,2nd,and 3rd are special cases
                switch (playersCompleted) {
                    case 1:
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (player.equals(p)) {
                                p.sendMessage(ChatColor.GREEN + "You finished in " + ChatColor.YELLOW + ChatColor.BOLD + "1st " + ChatColor.RESET + ChatColor.GREEN + "place [" + ChatColor.YELLOW + ChatColor.BOLD + "+" + points + ChatColor.RESET + ChatColor.GREEN + "] points");
                            } else {
                                player.sendMessage(handler.getPlayerTeam(p).color + p.getName() + ChatColor.GREEN + " finished in " + ChatColor.YELLOW + ChatColor.BOLD + "1st " + ChatColor.RESET + ChatColor.GREEN + "place");
                            }
                        }
                        break;
                    case 2:
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (player.equals(p)) {
                                p.sendMessage(ChatColor.GREEN + "You finished in " + ChatColor.YELLOW + ChatColor.BOLD + "2nd " + ChatColor.RESET + ChatColor.GREEN + "place [" + ChatColor.YELLOW + ChatColor.BOLD + "+" + points + ChatColor.RESET + ChatColor.GREEN + "] points");
                            } else {
                                player.sendMessage(handler.getPlayerTeam(p).color + p.getName() + ChatColor.GREEN + " finished in " + ChatColor.YELLOW + ChatColor.BOLD + "2nd " + ChatColor.RESET + ChatColor.GREEN + "place");
                            }
                        }
                        break;
                    case 3:
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (player.equals(p)) {
                                p.sendMessage(ChatColor.GREEN + "You finished in " + ChatColor.YELLOW + ChatColor.BOLD + "3rd " + ChatColor.RESET + ChatColor.GREEN + "place [" + ChatColor.YELLOW + ChatColor.BOLD + "+" + points + ChatColor.RESET + ChatColor.GREEN + "] points");
                            } else {
                                player.sendMessage(handler.getPlayerTeam(p).color + p.getName() + ChatColor.GREEN + " finished in " + ChatColor.YELLOW + ChatColor.BOLD + "3rd " + ChatColor.RESET + ChatColor.GREEN + "place");
                            }
                        }
                        break;
                    default:
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (player.equals(p)) {
                                p.sendMessage(ChatColor.GREEN + "You finished in " + ChatColor.YELLOW + ChatColor.BOLD + playersCompleted + "th " + ChatColor.RESET + ChatColor.GREEN + "place [" + ChatColor.YELLOW + ChatColor.BOLD + "+" + points + ChatColor.RESET + ChatColor.GREEN + "] points");
                            } else {
                                player.sendMessage(handler.getPlayerTeam(p).color + p.getName() + ChatColor.GREEN + " finished in " + ChatColor.YELLOW + ChatColor.BOLD + playersCompleted + "th " + ChatColor.RESET + ChatColor.GREEN + "place");
                            }
                        }
                        break;
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
