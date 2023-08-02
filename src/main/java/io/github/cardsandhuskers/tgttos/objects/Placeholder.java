package io.github.cardsandhuskers.tgttos.objects;


import io.github.cardsandhuskers.tgttos.TGTTOS;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

import static io.github.cardsandhuskers.tgttos.TGTTOS.*;

public class Placeholder extends PlaceholderExpansion {
    private final TGTTOS plugin;

    public Placeholder(TGTTOS plugin) {
        this.plugin = plugin;
    }


    @Override
    public String getIdentifier() {
        return "TGTTOS";
    }
    @Override
    public String getAuthor() {
        return "cardsandhuskers";
    }
    @Override
    public String getVersion() {
        return "1.0.0";
    }
    @Override
    public boolean persist() {
        return true;
    }


    @Override
    public String onRequest(OfflinePlayer p, String s) {

        if(s.equalsIgnoreCase("timer")) {
            int time = timeVar;
            int mins = time / 60;
            String seconds = String.format("%02d", time - (mins * 60));
            return mins + ":" + seconds;
        }

        if(s.equalsIgnoreCase("timerstage")) {
            switch(gameState) {
                case GAME_STARTING:
                    return "Game Starts";
                case ROUND_ACTIVE:
                        return "Round Ends";
                case BETWEEN_ROUND:
                        return "Round " + currentRound + " Starts";
                case ROUND_OVER:
                    return "Round " + currentRound + " Over";
                case GAME_OVER:
                    return "Game Over";
                default:
                    return "Game";
            }
        }
        if(s.equalsIgnoreCase("playersCompleted")) {
            return numPlayersCompleted + "";
            //return numPlayers + "/" + totalPlayers;
        }
        if(s.equalsIgnoreCase("totalPlayers")) {
            return totalPlayers + "";
            //return numPlayers + "/" + totalPlayers;
        }

        if(s.equalsIgnoreCase("round")) {
            return currentRound + "";
        }
        return null;
    }
}
