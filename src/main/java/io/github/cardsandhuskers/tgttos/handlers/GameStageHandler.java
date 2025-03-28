package io.github.cardsandhuskers.tgttos.handlers;

import io.github.cardsandhuskers.teams.objects.Team;
import io.github.cardsandhuskers.tgttos.objects.Stats;
import io.github.cardsandhuskers.tgttos.TGTTOS;
import io.github.cardsandhuskers.tgttos.listeners.*;
import io.github.cardsandhuskers.tgttos.objects.Arena;
import io.github.cardsandhuskers.tgttos.objects.Countdown;
import io.github.cardsandhuskers.tgttos.objects.GameMessages;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.UUID;

import static io.github.cardsandhuskers.tgttos.TGTTOS.*;

public class GameStageHandler {
    private ArrayList<Arena> arenas;
    private TGTTOS plugin;
    private ArrayList<UUID> playersCompleted;
    private Countdown pregameTimer, preroundTimer, roundTimer, postroundTimer, gameEndTimer;
    private Arena currentArena;
    private Stats stats;

    public GameStageHandler(ArrayList<Arena> arenas, TGTTOS plugin, Stats stats) {
        this.arenas = arenas;
        this.plugin = plugin;
        this.stats = stats;
    }

    /**
     * Initializes listeners and starts timer
     */
    public void startGame() {
        plugin.getServer().getPluginManager().registerEvents(new BlockBreakListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new BlockPlaceListener(this), plugin);

        playersCompleted = new ArrayList<>();
        plugin.getServer().getPluginManager().registerEvents(new ButtonPressListener(this, playersCompleted,stats), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerDamageListener(plugin, this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerTrampleListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerJoinListener(plugin, this, playersCompleted), plugin);
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
                        Bukkit.broadcast(GameMessages.gameDescription());
                        for(Player p:Bukkit.getOnlinePlayers()) p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
                    }
                    if(t.getSecondsLeft() == t.getTotalSeconds() - 12) {
                        Bukkit.broadcast(GameMessages.pointsDescription(plugin));
                        for(Player p:Bukkit.getOnlinePlayers()) p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
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
        preroundTimer = new Countdown((JavaPlugin)plugin,
                //should be 20
                time,
                //Timer Start
                () -> {
                    //increment current round first, it starts as 0, so this brings it to 1
                    currentRound++;
                    gameState = TGTTOS.State.BETWEEN_ROUND;
                    currentArena = arenas.get(currentRound - 1);
                    numPlayersCompleted = 0;
                    totalPlayers = 0;
                    playersCompleted.clear();

                    currentArena.buildWall(Material.BARRIER);

                    if(currentArena.hasElytra()) {
                        currentArena.getSpawn().getWorld().setTime(18000);
                    } else {
                        currentArena.getSpawn().getWorld().setTime(1000);
                    }

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
                        p.sendTitle(ChatColor.AQUA + "Round Over!", "", 5,50,5);
                    }
                },

                //Each Second
                (t) -> {
                    timeVar = t.getSecondsLeft();

                    if(t.getSecondsLeft() % 2 == 0) {
                        for(Team team: handler.getTeams()) {
                            for(Player p:team.getOnlinePlayers()) {
                                if (p.getGameMode() == GameMode.SURVIVAL) {
                                    if (currentArena.hasTrident()) {
                                        if (!p.getInventory().contains(Material.TRIDENT)) giveItems(p);
                                    }
                                    if (currentArena.hasElytra()) {
                                        if (p.getInventory().getChestplate() == null) giveItems(p);
                                    }
                                    if (currentArena.hasBlocks()) {
                                        //TODO give blocks if they lose them somehow
                                    }
                                }
                            }
                        }
                    }

                }
        );

        // Start scheduling, don't use the "run" method unless you want to skip a second
        roundTimer.scheduleTimer();
    }

    /**
     * Post round timer, just a time buffer
     */
    public void postRoundTimer() {
        postroundTimer = new Countdown(plugin,
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
     * Writes collected stats to csv file.
     */
    public void gameEndTimer() {
        gameEndTimer = new Countdown((JavaPlugin)plugin,
                //should be 30
                plugin.getConfig().getInt("GameEndTime"),
                //Timer Start
                () -> {
                    gameState = TGTTOS.State.GAME_OVER;

                    int eventNum;
                    try {eventNum = Bukkit.getPluginManager().getPlugin("LobbyPlugin").getConfig().getInt("eventNum");} catch (Exception e) {eventNum = 1;}
                    stats.writeToFile(plugin.getDataFolder().toPath().toString(), "tgttosStats" + eventNum);
                },

                //Timer End
                () -> {

                    try {
                        plugin.statCalculator.calculateStats();
                    } catch (Exception e) {
                        StackTraceElement[] trace = e.getStackTrace();
                        String str = "";
                        for(StackTraceElement element:trace) str += element.toString() + "\n";
                        plugin.getLogger().severe("ERROR Calculating Stats!\n" + str);
                    }

                    for(Player p:Bukkit.getOnlinePlayers()) {
                        p.teleport(plugin.getConfig().getLocation("Lobby"));
                    }
                    HandlerList.unregisterAll(plugin);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "startRound");

                },

                //Each Second
                (t) -> {
                    timeVar = t.getSecondsLeft();
                    if(t.getSecondsLeft() == t.getTotalSeconds() - 1) GameMessages.announceTopPlayers();
                    if(t.getSecondsLeft() == t.getTotalSeconds() - 6) GameMessages.announceTeamPlayers();
                    if(t.getSecondsLeft() == t.getTotalSeconds() - 11) GameMessages.announceTeamLeaderboard();
                }
        );

        // Start scheduling, don't use the "run" method unless you want to skip a second
        gameEndTimer.scheduleTimer();
    }

    public boolean cancelTimers() {
        boolean cancel = false;
        if(pregameTimer != null) {
            pregameTimer.cancelTimer();
            cancel = true;
        }
        if(preroundTimer != null) {
            preroundTimer.cancelTimer();
            cancel = true;
        }
        if(roundTimer != null) {
            roundTimer.cancelTimer();
            cancel = true;
        }
        if(postroundTimer != null) {
            postroundTimer.cancelTimer();
            cancel = true;
        }
        if(gameEndTimer != null) {
            gameEndTimer.cancelTimer();
            cancel = true;
        }
        return cancel;
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
        if(numPlayersCompleted == totalPlayers) {
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
