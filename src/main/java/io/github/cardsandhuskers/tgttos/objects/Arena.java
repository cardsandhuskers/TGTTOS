package io.github.cardsandhuskers.tgttos.objects;

import io.github.cardsandhuskers.tgttos.TGTTOS;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;

public class Arena {
    private Location pos1, pos2;
    private Location spawn;
    private boolean elytra, trident, blocks;
    private int arenaNumber;
    private TGTTOS plugin;
    private String higherx, lowerx, highery, lowery, higherz, lowerz;
    private String name;

    public Arena(int num, TGTTOS plugin) {
        arenaNumber = num;
        this.plugin = plugin;
        pos1 = plugin.getConfig().getLocation("Arenas." + arenaNumber + ".pos1");
        pos2 = plugin.getConfig().getLocation("Arenas." + arenaNumber + ".pos2");
        spawn = plugin.getConfig().getLocation("Arenas." + arenaNumber + ".Spawn");
        elytra = plugin.getConfig().getBoolean("Arenas." + arenaNumber + ".Elytra");
        trident = plugin.getConfig().getBoolean("Arenas." + arenaNumber + ".Trident");
        blocks = plugin.getConfig().getBoolean("Arenas." + arenaNumber + ".Blocks");
        name = plugin.getConfig().getString("Arenas." + arenaNumber + ".Name");

        if(pos1 == null) System.out.println("POS1 IS NULL");
        if(pos2 == null) System.out.println("POS2 IS NULL");

        calcLocations();
        clearArena();
    }

    /**
     *
     * @return spawn location
     */
    public Location getSpawn() {
        return spawn.clone();
    }

    /**
     * Checks if the location is inside the arena
     * @param l
     * @return
     */
    public boolean isInArena(Location l) {

        if(l.getX() >= getCoordinate(lowerx, 'x') && l.getX() <= getCoordinate(higherx, 'x')) {
            if(l.getY() >= getCoordinate(lowery, 'y') && l.getY() <= getCoordinate(highery, 'y')) {
                if(l.getZ() >= getCoordinate(lowerz, 'z') && l.getZ() <= getCoordinate(higherz, 'z')) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Sets the wall blocking people from entering the arena to the specified material,
     * barrier to put it up, air to remove it
     * @param mat
     */
    public void buildWall(Material mat) {

        //hardcoding for elytra map because no time to make something good
        if (elytra) {
            //corner
            int x = spawn.getBlockX() - 18;
            int z = spawn.getBlockZ() + 18;

            for(int ix = x; ix < x + 11; ix++) {
                for (int iy = spawn.getBlockY(); iy <= spawn.getBlockY() + 3; iy++) {
                    Location loc = new Location(spawn.getWorld(), ix, iy, z);
                    loc.getBlock().setType(mat);
                }
            }

            for(int iz = z; iz > z - 11; iz--) {
                for (int iy = spawn.getBlockY(); iy <= spawn.getBlockY() + 3; iy++) {
                    Location loc = new Location(spawn.getWorld(), x, iy, iz);
                    loc.getBlock().setType(mat);
                }
            }

        } else {
            int z;
            if(Math.abs(getCoordinate(lowerz, 'z') - spawn.getBlockZ()) < Math.abs(getCoordinate(higherz, 'z') - spawn.getBlockZ())) {
                z = getCoordinate(lowerz, 'z');
            } else {
                z = getCoordinate(higherz, 'z');
            }

            for(int x = getCoordinate(lowerx, 'x'); x<=getCoordinate(higherx, 'x'); x++) {
                for(int y = spawn.getBlockY(); y<= spawn.getBlockY() + 6; y++) {
                    //System.out.println("X: " + x + " Y: " + y + "Z: " + z);
                    Location loc = new Location(spawn.getWorld(), x,y,z);
                    loc.getBlock().setType(mat);
                }
            }
        }



    }

    /**
     * Determines if the player is underneath the arena
     * @param l
     * @return whether player y is beneath arena
     */
    public boolean hasFallen(Location l) {
        if(l.getY() < getCoordinate(lowery, 'y')) {
            return true;
        }
        return false;
    }

    public String getMapName() {
        return name;
    }

    public boolean hasElytra() {
        return elytra;
    }

    public boolean hasTrident() {
        return trident;
    }
    public boolean hasBlocks() {
        return blocks;
    }

    /**
     * Removes all wool blocks from the arena (do not build arena using wool)
     */
    private void clearArena() {
        for(int x = getCoordinate(lowerx, 'x'); x <= getCoordinate(higherx, 'x'); x++) {
            int finalX = x;
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()-> {
                for (int y = getCoordinate(lowery, 'y'); y <= getCoordinate(highery, 'y'); y++) {
                    for (int z = getCoordinate(lowerz, 'z'); z <= getCoordinate(higherz, 'z'); z++) {
                        Location l = new Location(pos1.getWorld(), finalX, y, z);
                        if(isWool(l.getBlock())) {
                            l.getBlock().setType(Material.AIR);
                        }
                    }
                }
            },2);
        }
        //saveWater();
    }

    /**
     * Gets the specified coordinate from the position
     * @param pos
     * @param axis
     * @return int coordinate
     */
    private int getCoordinate(String pos, char axis) {
        Location l = null;
        if(pos == "pos1") {
            l = pos1;
        } else if(pos == "pos2") {
            l = pos2;
        } else {
            System.out.println("PROBLEM");
        }
        switch(axis) {
            case 'x': return l.getBlockX();
            case 'y': return l.getBlockY();
            case 'z': return l.getBlockZ();
            default: return 0;
        }
    }

    private void saveWater() {
        for(int x = getCoordinate(lowerx, 'x'); x <= getCoordinate(higherx, 'x'); x++) {
            for (int y = getCoordinate(lowery, 'y'); y <= getCoordinate(highery, 'y'); y++) {
                for (int z = getCoordinate(lowerz, 'z'); z <= getCoordinate(higherz, 'z'); z++) {
                    Location l = new Location(pos1.getWorld(), x, y, z);

                    if(l.getBlock().getType() == Material.WATER) {
                        Levelled level = (Levelled) l.getBlock().getBlockData();
                        System.out.println(level.getLevel()); //0 should be full block I think https://minecraft.fandom.com/wiki/Water#Block_states
                    }
                }
            }
        }
    }

    /**
     * Determines whether the specified block is wool
     * @param b
     * @return boolean if block is wool
     */
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
            case PINK_WOOL:
            case YELLOW_WOOL:
            case WHITE_WOOL: return true;
            default: return false;
        }
    }

    /**
     * Gets the higher and lower x,y,z values
     */
    private void calcLocations() {

        if (getCoordinate("pos1", 'x') > getCoordinate("pos2", 'x')) {
            higherx = "pos1";
            lowerx = "pos2";
        } else {
            higherx = "pos2";
            lowerx = "pos1";
        }
        if (getCoordinate("pos1", 'y') > getCoordinate("pos2", 'y')) {
            highery = "pos1";
            lowery = "pos2";
        } else {
            highery = "pos2";
            lowery = "pos1";
        }
        if (getCoordinate("pos1", 'z') > getCoordinate("pos2", 'z')) {
            higherz = "pos1";
            lowerz = "pos2";
        } else {
            higherz = "pos2";
            lowerz = "pos1";
        }
    }
}
