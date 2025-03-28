package io.github.cardsandhuskers.tgttos.objects;

import io.github.cardsandhuskers.teams.handlers.TeamHandler;
import io.github.cardsandhuskers.teams.objects.Team;
import io.github.cardsandhuskers.teams.objects.TempPointsHolder;
import io.github.cardsandhuskers.tgttos.TGTTOS;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.*;

import static io.github.cardsandhuskers.teams.Teams.handler;
import static io.github.cardsandhuskers.tgttos.TGTTOS.multiplier;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_BLUE;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;
import static net.kyori.adventure.text.format.TextDecoration.STRIKETHROUGH;

public class GameMessages {

    private static final Map<String, NamedTextColor> COLOR_MAP = new HashMap<>();

    static {
        COLOR_MAP.put("&0", NamedTextColor.BLACK);
        COLOR_MAP.put("&1", NamedTextColor.DARK_BLUE);
        COLOR_MAP.put("&2", NamedTextColor.DARK_GREEN);
        COLOR_MAP.put("&3", NamedTextColor.DARK_AQUA);
        COLOR_MAP.put("&4", NamedTextColor.DARK_RED);
        COLOR_MAP.put("&5", NamedTextColor.DARK_PURPLE);
        COLOR_MAP.put("&6", NamedTextColor.GOLD);
        COLOR_MAP.put("&7", NamedTextColor.GRAY);
        COLOR_MAP.put("&8", NamedTextColor.DARK_GRAY);
        COLOR_MAP.put("&9", NamedTextColor.BLUE);
        COLOR_MAP.put("&a", NamedTextColor.GREEN);
        COLOR_MAP.put("&b", NamedTextColor.AQUA);
        COLOR_MAP.put("&c", NamedTextColor.RED);
        COLOR_MAP.put("&d", LIGHT_PURPLE);
        COLOR_MAP.put("&e", NamedTextColor.YELLOW);
        COLOR_MAP.put("&f", WHITE);
    }

    /**
     *
     * @return String to announce for game rules
     */
    public static Component gameDescription() {
        String GAME_DESCRIPTION =
                ChatColor.STRIKETHROUGH + "----------------------------------------" +
                StringUtils.center(ChatColor.GOLD + "" + ChatColor.BOLD + "\nTo Get to the Other Side and Click a Button", 30) +
                ChatColor.BLUE + "" + ChatColor.BOLD + "\nHow To Play:" + ChatColor.RESET +
                "\nWelcome to TGTTOSACAB!" +
                "\nThere are 6 levels, you will have " + ChatColor.YELLOW + "" + ChatColor.BOLD + 2 + ChatColor.RESET + " minutes to complete each level!" +
                "\nThe goal is to get across the opening. Each level is unique, with different obstacles and modes of travel." +
                "\nWhen you reach the end, find a button and press it to complete the level." +
                ChatColor.STRIKETHROUGH + "\n----------------------------------------";

        return Component.text()
                .append(Component.text("----------------------------------------\n", WHITE, STRIKETHROUGH))
                .append(Component.text(StringUtils.center("Survival Games", 40), LIGHT_PURPLE, BOLD))
                .append(Component.text("\nHow To Play:", BLUE, BOLD))
                .append(Component.text("""
                        
                        Welcome to TGTTOSACAB!
                        There are 6 levels, you will have\s""")).append(Component.text(2, YELLOW, BOLD)).append(Component.text(" minutes to complete each level!"))
                .append(Component.text("""
                        
                        The goal is to get across the opening. Each level is unique, with different obstacles and modes of travel.
                        When you reach the end, find a button and press it to complete the level."""))
                .append(Component.text("\n----------------------------------------", WHITE, STRIKETHROUGH))
                .build();
    }

    /**
     *
     * @param plugin
     * @return String to announce for points
     */
        public static Component pointsDescription(TGTTOS plugin) {

        double firstPlace = plugin.getConfig().getDouble("maxPoints") * multiplier;
        double dropOff = -plugin.getConfig().getDouble("dropoff") * multiplier;

        String message = "<st>----------------------------------------</st>" +
                "<br><gold><bold>How the game is Scored (for each level): </bold></gold>" +
                "<br>1st Place: <gold>{firstPlace}</gold> points" +
                "<br>Point Drop-off: <gold>{dropOff}</gold> points for each player ahead" +
                "<br><st>----------------------------------------</st>";
        message = message.replace("{firstPlace}", firstPlace + "").replace("{dropOff}", dropOff + "");

        return MiniMessage.miniMessage().deserialize(message);
    }


    /**
     * Announces the top 5 earning players in the game
     */
    public static void announceTopPlayers() {
        ArrayList<TempPointsHolder> tempPointsList = new ArrayList<>();
        for(Team team: handler.getTeams()) {
            for(Player p:team.getOnlinePlayers()) {
                tempPointsList.add(team.getPlayerTempPoints(p));
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            }
        }

        tempPointsList.sort(Comparator.comparing(TempPointsHolder::getPoints));
        Collections.reverse(tempPointsList);

        int max;
        if(tempPointsList.size() >= 5) {
            max = 4;
        } else {
            max = tempPointsList.size() - 1;
        }

        TextComponent.Builder builder = Component.text()
                .append(Component.text("\nTop 5 Players:", RED, BOLD))
                .append(Component.text("\n------------------------------", DARK_RED));

        int number = 1;
        for(int i = 0; i <= max; i++) {
            TempPointsHolder h = tempPointsList.get(i);
            Team team = TeamHandler.getInstance().getPlayerTeam(h.getPlayer());

            builder.append(Component.text("\n" + number + ". "))
                    .append(Component.text(h.getPlayer().getName(), COLOR_MAP.get(team.getConfigColor()), BOLD))
                    .append(Component.text(" Points: " + h.getPoints()));
            number++;
        }
        builder.append(Component.text("\n------------------------------", DARK_RED));
        Bukkit.broadcast(builder.build());
    }

    /**
     * Announces the leaderboard for players on your team based on points earned in the game
     */
    public static void announceTeamPlayers() {
        for (Team team : handler.getTeams()) {
            ArrayList<TempPointsHolder> tempPointsList = new ArrayList<>();
            for (Player p : team.getOnlinePlayers()) {
                if (team.getPlayerTempPoints(p) != null) {
                    tempPointsList.add(team.getPlayerTempPoints(p));
                }
            }
            tempPointsList.sort(Comparator.comparing(TempPointsHolder::getPoints));
            Collections.reverse(tempPointsList);
            NamedTextColor teamColor = COLOR_MAP.get(team.getConfigColor());

            TextComponent.Builder builder = Component.text()
                    .append(Component.text("\nYour Team Standings:", teamColor, BOLD))
                    .append(Component.text("\n------------------------------", teamColor));

            int number = 1;
            for (TempPointsHolder h : tempPointsList) {
                builder.append(Component.text("\n" + number + ". "))
                        .append(Component.text(h.getPlayer().getName(), teamColor, BOLD))
                        .append(Component.text(" Points: " + h.getPoints()));
                number++;
            }
            builder.append(Component.text("\n------------------------------", teamColor));


            for (Player p : team.getOnlinePlayers()) {
                p.sendMessage(builder.build());
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            }
        }
    }

    /**
     * Announces the leaderboard of teams based on points earned in the game
     */
    public static void announceTeamLeaderboard() {

        ArrayList<Team> teamList = TeamHandler.getInstance().getTeams();
        teamList.sort(Comparator.comparing(Team::getTempPoints));
        Collections.reverse(teamList);

        TextComponent.Builder builder = Component.text()
                .append(Component.text("\nTeam Leaderboard:", BLUE, BOLD))
                .append(Component.text("\n------------------------------", DARK_BLUE));

        int counter = 1;
        for(Team team:teamList) {
            builder.append(Component.text("\n" + counter + ". "))
                    .append(Component.text(team.getTeamName(), COLOR_MAP.get(team.getConfigColor()), BOLD))
                    .append(Component.text(" Points: " + team.getTempPoints()));

            counter++;
        }
        builder.append(Component.text("\n------------------------------", DARK_BLUE));

        Bukkit.broadcast(builder.build());
        for(Player p: Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
        }
    }

}
