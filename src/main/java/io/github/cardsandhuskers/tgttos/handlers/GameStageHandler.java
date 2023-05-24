package io.github.cardsandhuskers.tgttos.handlers;

import io.github.cardsandhuskers.teams.objects.Team;
import io.github.cardsandhuskers.teams.objects.TempPointsHolder;
import io.github.cardsandhuskers.tgttos.TGTTOS;
import io.github.cardsandhuskers.tgttos.listeners.*;
import io.github.cardsandhuskers.tgttos.objects.Arena;
import io.github.cardsandhuskers.tgttos.objects.Countdown;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static io.github.cardsandhuskers.tgttos.TGTTOS.*;

public class GameStageHandler {
    private ArrayList<Arena> arenas;
    private TGTTOS plugin;
    private Countdown pregameTimer;
    private Arena currentArena;
    private Countdown roundTimer;
    public GameStageHandler(ArrayList<Arena> arenas, TGTTOS plugin) {
        this.arenas = arenas;
        this.plugin = plugin;
    }

    /**
     * Initializes listeners and starts timer
     */
    public void startGame() {
        plugin.getServer().getPluginManager().registerEvents(new BlockBreakListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new BlockPlaceListener(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ButtonPressListener(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerDamageListener(plugin, this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerTrampleListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerJoinListener(plugin, this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ItemThrowListener(), plugin);

        pregameTimer();
    }

    /**
     * Pregame countdown, chat messages mostly
     */
    public void pregameTimer() {
        pregameTimer = new Countdown((JavaPlugin)plugin,
                //should be 60
                plugin.getConfig().getInt("PregameTime"),
                //Timer Start
                () -> {
                    TGTTOS.gameState = TGTTOS.State.GAME_STARTING;
                    totalPlayers = Bukkit.getOnlinePlayers().size();
                    for(Player p:Bukkit.getOnlinePlayers()) {
                        p.teleport(plugin.getConfig().getLocation("Spawn"));
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()->{
                            p.setHealth(20);
                            p.setFoodLevel(20);
                            p.setSaturation(20);
                            if(handler.getPlayerTeam(p) == null) {
                                p.setGameMode(GameMode.SPECTATOR);
                            }
                        },2L);
                    }
                },

                //Timer End
                () -> {
                    for(Player p:Bukkit.getOnlinePlayers()) {
                        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2F);
                        p.sendTitle(ChatColor.GREEN + ">GO!<", "", 2, 16, 2);
                    }
                    preRoundTimer();
                },

                //Each Second
                (t) -> {
                    if(t.getSecondsLeft() == t.getTotalSeconds() - 2) {
                        Bukkit.broadcastMessage(ChatColor.STRIKETHROUGH + "----------------------------------------");
                        Bukkit.broadcastMessage(StringUtils.center(ChatColor.GOLD + "" + ChatColor.BOLD + "To Get to the Other Side and Click a Button", 30));
                        Bukkit.broadcastMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "How To Play:");
                        Bukkit.broadcastMessage("Welcome to TGTTOSACAB!" +
                                "\nThere are 6 levels, you will have " + ChatColor.YELLOW + "" + ChatColor.BOLD + 2 + ChatColor.RESET + " minutes to complete each level!" +
                                "\nThe goal is to get across the opening. Each level is unique, with different obstacles and modes of travel." +
                                "\nWhen you reach the end, find a button and press it to complete the level.");
                        Bukkit.broadcastMessage(ChatColor.STRIKETHROUGH + "----------------------------------------");

                        for(Player p:Bukkit.getOnlinePlayers()) {
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
                        }
                    }

                    if(t.getSecondsLeft() == t.getTotalSeconds() - 12) {
                        Bukkit.broadcastMessage(ChatColor.STRIKETHROUGH + "----------------------------------------");
                        Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "How the game is Scored (for each level):");
                        Bukkit.broadcastMessage("1st Place: " + ChatColor.GOLD + (int)(plugin.getConfig().getInt("maxPoints") * multiplier) + ChatColor.RESET + " points" +
                                "\nPoint Drop-off: " + ChatColor.GOLD + (-plugin.getConfig().getDouble("dropoff") * multiplier) + ChatColor.RESET + " points for each player ahead");
                        Bukkit.broadcastMessage(ChatColor.STRIKETHROUGH + "----------------------------------------");

                        for(Player p:Bukkit.getOnlinePlayers()) {
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
                        }
                    }

                    if(t.getSecondsLeft() == 5) {
                        for(Player p:Bukkit.getOnlinePlayers()) {
                            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1F);
                            //p.sendTitle(ChatColor.GREEN + ">" + t.getSecondsLeft() + "<", "", 2, 16, 2);
                        }
                    }
                    timeVar = t.getSecondsLeft();
                }
        );

        // Start scheduling, don't use the "run" method unless you want to skip a second
        pregameTimer.scheduleTimer();
    }


    /**
     * Countdown before the start of the round
     * Inits a bunch of stuff
     */
    public void preRoundTimer() {
        int time = plugin.getConfig().getInt("PreroundTime");
        Countdown preroundTimer = new Countdown((JavaPlugin)plugin,
                //should be 20
                time,
                //Timer Start
                () -> {
                    //increment current round first, it starts as 0, so this brings it to 1
                    currentRound++;
                    gameState = TGTTOS.State.BETWEEN_ROUND;
                    currentArena = arenas.get(currentRound - 1);
                    playersCompleted = 0;
                    totalPlayers = 0;

                    currentArena.buildWall(Material.BARRIER);

                    Bukkit.broadcastMessage(ChatColor.AQUA + "Round: " + ChatColor.GREEN + ChatColor.BOLD + currentRound + ChatColor.RESET + ChatColor.AQUA + " on Map: " + ChatColor.GREEN + ChatColor.BOLD +
                                            currentArena.getMapName() + ChatColor.RESET + ChatColor.AQUA + " begins in " + ChatColor.GREEN + ChatColor.BOLD + time + ChatColor.RESET + ChatColor.AQUA + " seconds.");
                    for(Player p:Bukkit.getOnlinePlayers()) {
                        p.teleport(currentArena.getSpawn());
                        Inventory inv = p.getInventory();
                        inv.clear();

                        p.sendTitle(ChatColor.AQUA + "Round " + currentRound, ChatColor.GREEN + currentArena.getMapName(), 5, 10, 5);

                        if(handler.getPlayerTeam(p) != null) {
                            p.setGameMode(GameMode.SURVIVAL);
                            giveItems(p);

                            totalPlayers++;
                        } else {
                            p.setGameMode(GameMode.SPECTATOR);
                        }
                    }
                },

                //Timer End
                () -> {
                    currentArena.buildWall(Material.AIR);
                    roundTimer();
                    for(Player p:Bukkit.getOnlinePlayers()) {
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
                        p.sendTitle(">GO!<", "", 5, 10, 5);
                    }
                },

                //Each Second
                (t) -> {
                    timeVar = t.getSecondsLeft();
                    if(t.getSecondsLeft() < 5) {
                        for(Player p:Bukkit.getOnlinePlayers()) {
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                            p.sendTitle(">" + t.getSecondsLeft() + "<", "", 5, 10, 5);
                        }
                    }
                }
        );

        // Start scheduling, don't use the "run" method unless you want to skip a second
        preroundTimer.scheduleTimer();
    }

    /**
     *
     */
    public void giveItems(Player p) {
        Inventory inv = p.getInventory();
        if(currentArena.hasBlocks()) {
            ItemStack shears = new ItemStack(Material.SHEARS);
            ItemMeta shearsMeta = shears.getItemMeta();
            shearsMeta.setUnbreakable(true);
            shears.setItemMeta(shearsMeta);
            inv.setItem(0, new ItemStack(handler.getPlayerTeam(p).getWoolColor(), 64));
            inv.setItem(1, shears);
        }
        if(currentArena.hasElytra()) {
            ItemStack elytra = new ItemStack(Material.ELYTRA);
            ItemMeta elytraMeta = elytra.getItemMeta();
            elytraMeta.setUnbreakable(true);
            elytra.setItemMeta(elytraMeta);
            p.getEquipment().setChestplate(elytra);
        }
        if(currentArena.hasTrident()) {
            ItemStack trident = new ItemStack(Material.TRIDENT);
            ItemMeta tridentMeta = trident.getItemMeta();
            tridentMeta.addEnchant(Enchantment.RIPTIDE, 3, true);
            tridentMeta.setUnbreakable(true);
            trident.setItemMeta(tridentMeta);
            inv.setItem(2, trident);
        }
    }

    /**
     * Time for each round
     */
    public void roundTimer() {
        roundTimer = new Countdown((JavaPlugin)plugin,
                //should be 120
                plugin.getConfig().getInt("RoundTime"),
                //Timer Start
                () -> {
                    gameState = TGTTOS.State.ROUND_ACTIVE;
                },

                //Timer End
                () -> {
                    postRoundTimer();
                    Bukkit.broadcastMessage(ChatColor.AQUA + "Round Over!");
                    for(Player p:Bukkit.getOnlinePlayers()) {
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                        p.sendTitle(ChatColor.AQUA + "Round Over!", "", 5,20,5);
                    }
                },

                //Each Second
                (t) -> {
                    timeVar = t.getSecondsLeft();
                }
        );

        // Start scheduling, don't use the "run" method unless you want to skip a second
        roundTimer.scheduleTimer();
    }

    /**
     * Post round timer, just a time buffer
     */
    public void postRoundTimer() {
        Countdown postroundTimer = new Countdown(plugin,
                plugin.getConfig().getInt("PostroundTime"),
                //Timer Start
                () -> {
                    gameState = State.ROUND_OVER;
                    for(Player p:Bukkit.getOnlinePlayers()) {
                        p.setGameMode(GameMode.SPECTATOR);
                    }
                },

                //Timer End
                () -> {
                    if(currentRound == arenas.size()) {
                        gameEndTimer();
                    } else {
                        preRoundTimer();
                    }
                },

                //Each Second
                (t) -> {
                    timeVar = t.getSecondsLeft();
                }
        );
        postroundTimer.scheduleTimer();
    }

    /**
     * Timer for very end of game, displays results and returns players to main lobby
     */
    public void gameEndTimer() {
        Countdown gameEndTimer = new Countdown((JavaPlugin)plugin,
                //should be 30
                plugin.getConfig().getInt("GameEndTime"),
                //Timer Start
                () -> {
                    gameState = TGTTOS.State.GAME_OVER;
                },

                //Timer End
                () -> {
                    for(Player p:Bukkit.getOnlinePlayers()) {
                        p.teleport(plugin.getConfig().getLocation("Lobby"));
                    }
                    HandlerList.unregisterAll(plugin);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "startRound");

                },

                //Each Second
                (t) -> {
                    timeVar = t.getSecondsLeft();
                    //top 5 players
                    if(t.getSecondsLeft() == t.getTotalSeconds()) {
                        ArrayList<TempPointsHolder> tempPointsList = new ArrayList<>();
                        for(Team team: handler.getTeams()) {
                            for(Player p:team.getOnlinePlayers()) {
                                tempPointsList.add(team.getPlayerTempPoints(p));
                                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                            }
                        }

                        Collections.sort(tempPointsList, Comparator.comparing(TempPointsHolder::getPoints));
                        Collections.reverse(tempPointsList);

                        int max;
                        if(tempPointsList.size() >= 5) {
                            max = 4;
                        } else {
                            max = tempPointsList.size() - 1;
                        }

                        Bukkit.broadcastMessage("\n" + ChatColor.RED + "" + ChatColor.BOLD + "Top 5 Players:");
                        Bukkit.broadcastMessage(ChatColor.DARK_RED + "------------------------------");
                        int number = 1;
                        for(int i = 0; i <= max; i++) {
                            TempPointsHolder h = tempPointsList.get(i);
                            Bukkit.broadcastMessage(number + ". " + handler.getPlayerTeam(h.getPlayer()).color + h.getPlayer().getName() + ChatColor.RESET + "    Points: " +  h.getPoints());
                            number++;
                        }
                        Bukkit.broadcastMessage(ChatColor.DARK_RED + "------------------------------");
                    }

                    //team players performance
                    if(t.getSecondsLeft() == t.getTotalSeconds() - 10) {
                        for (Team team : handler.getTeams()) {
                            ArrayList<TempPointsHolder> tempPointsList = new ArrayList<>();
                            for (Player p : team.getOnlinePlayers()) {
                                if (team.getPlayerTempPoints(p) != null) {
                                    tempPointsList.add(team.getPlayerTempPoints(p));
                                }
                            }
                            Collections.sort(tempPointsList, Comparator.comparing(TempPointsHolder::getPoints));
                            Collections.reverse(tempPointsList);

                            for (Player p : team.getOnlinePlayers()) {
                                p.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Your Team Standings:");
                                p.sendMessage(ChatColor.DARK_BLUE + "------------------------------");
                                int number = 1;
                                for (TempPointsHolder h : tempPointsList) {
                                    p.sendMessage(number + ". " + handler.getPlayerTeam(p).color + h.getPlayer().getName() + ChatColor.RESET + "    Points: " + h.getPoints());
                                    number++;
                                }
                                p.sendMessage(ChatColor.DARK_BLUE + "------------------------------\n");
                                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                            }
                        }
                    }

                    //game leaderboard (each team's points)
                    if(t.getSecondsLeft() == t.getTotalSeconds() - 20) {
                        ArrayList<Team> teamList = handler.getTeams();
                        Collections.sort(teamList, Comparator.comparing(Team::getTempPoints));
                        Collections.reverse(teamList);

                        Bukkit.broadcastMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "Team Leaderboard:");
                        Bukkit.broadcastMessage(ChatColor.GREEN + "------------------------------");
                        int counter = 1;
                        for(Team team:teamList) {
                            Bukkit.broadcastMessage(counter + ". " + team.color + ChatColor.BOLD +  team.getTeamName() + ChatColor.RESET + " Points: " + team.getTempPoints());
                            counter++;
                        }
                        Bukkit.broadcastMessage(ChatColor.GREEN + "------------------------------");
                        for(Player p: Bukkit.getOnlinePlayers()) {
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                        }
                    }
                }
        );

        // Start scheduling, don't use the "run" method unless you want to skip a second
        gameEndTimer.scheduleTimer();
    }

    /**
     * Determine if a round is in progress
     * Used for the blockPlaceListener to determine if they're allowed to place blocks yet
     * @return
     */
    public boolean isGameActive() {
        if(gameState == State.ROUND_ACTIVE) {
            return true;
        }
        return false;
    }

    /**
     * If the finishing player is the last one, starts a new round
     */
    public void playerFinish() {
        if(playersCompleted == totalPlayers) {
            if(roundTimer != null) {
                roundTimer.cancelTimer();
                postRoundTimer();
            }
        }
    }
    public Arena getCurrentArena() {
        return currentArena;
    }

}
