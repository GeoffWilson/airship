package co.piglet.airship;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Represents an airship on the server
 *
 * @author Geoff Wilson
 * @author Ben Carvell
 */
public class Airship {

    // Task object for moving the airship on a regular interval
    public BukkitTask task;

    // The world that this airship is being kept in
    private World world;

    // The player who owns this airship
    public String owner;

    // The current direction of travel for the airship
    public BlockFace currentDirection;

    // The plugin object that owns this airship
    public Plugin owningPlugin;

    // The list of blocks in the airship (in custom block format)
    private ArrayList<AirshipBlock> blocks;

    // Is the airship currently moving?
    public boolean isMoving;

    // Is the airship configured to reverse?
    public boolean isReversing;

    // The last direction the block was travelling in (when moving up/down)
    public BlockFace lastDirection;

    /**
     * Creates a new airship
     *
     * @param world        The world that this airship is being created in
     * @param initialBlock The block to start scanning this airship from (this is the block below the player)
     * @param player       The player who owns this airship
     * @throws IllegalAirshipException Thrown if the number of blocks in the airship exceeds the limit
     */
    public Airship(World world, Block initialBlock, String player) throws IllegalAirshipException {

        // Set the airship variables from the constructor parameters
        this.world = world;
        this.owner = player;

        // Create the collection to store the airship in
        blocks = new ArrayList<>();

        // Get the direction the player is facing
        currentDirection = playerDirection(Bukkit.getPlayer(player));

        // We need to scan the airship
        scanAirship(initialBlock);
    }

    /**
     * Loads an airship from disk
     * @param file The File of the airship to load
     * @throws IOException Thrown if the Airship file is corrupt and can't be read
     */
    public Airship(File file) throws IOException {

        // Create the collection to store the airship in
        blocks = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new FileReader(file.getAbsoluteFile()));

        this.owner = reader.readLine();

        String world = reader.readLine();
        this.world = Bukkit.getWorld(world);

        String direction = reader.readLine();
        this.currentDirection = BlockFace.valueOf(direction);

        int airshipBlockCount = Integer.valueOf(reader.readLine());
        for (int i = 0; i < airshipBlockCount; i ++) {
            String[] blockData = reader.readLine().split(",");
            int x = Integer.valueOf(blockData[0]);
            int y = Integer.valueOf(blockData[1]);
            int z = Integer.valueOf(blockData[2]);
            Material m = Material.valueOf(blockData[3]);
            int d = Integer.valueOf(blockData[4]);

            AirshipBlock airshipBlock = new AirshipBlock(x, y, z, m, d);
            blocks.add(airshipBlock);
        }
    }

    /**
     * Works out the current direction the player is facing
     *
     * @param player The player who's direction we want to know
     * @return The current direction the player is facing as a BlockFace
     */
    private BlockFace playerDirection(Player player) {

        // Get the Yaw of the player
        float absoluteYaw = player.getLocation().getYaw();

        // Adjust the yaw if it is outside the expected range
        absoluteYaw = Math.abs(absoluteYaw) > 180.00 ? (((Math.abs(absoluteYaw) + 180) % 360) - 180) * (absoluteYaw / Math.abs(absoluteYaw)) : absoluteYaw;

        // Return the direction the player is facing based on the Yaw
        if (Math.abs(absoluteYaw) >= 135) {
            return BlockFace.NORTH; // North is -135 to 135
        } else if (Math.abs(absoluteYaw) <= 45) {
            return BlockFace.SOUTH; // South is -45 to 45
        } else if (absoluteYaw < 0) {
            return BlockFace.EAST; // East if the Yaw is negative (and not North or South)
        } else {
            return BlockFace.WEST; // West if the Yaw is positive (and not North or South)
        }
    }

    /**
     * Rescan the blocks in the airship based on the first block.
     */
    @SuppressWarnings("unused")
    public void rescanAirship() {

        // Get the first block in the airship (index 0)
        AirshipBlock b = blocks.get(0);

        // Clear the list of blocks from the airship
        blocks.clear();

        try {
            // Rescan the airship
            scanAirship(world.getBlockAt(b.x, b.y, b.z));
        } catch (IllegalAirshipException e) {
            e.printStackTrace();
        }
    }


    /**
     * Recursive function to scan all of the blocks in the airship, takes the initial block and scans all neighbours
     * until it finds air in all neighbouring locations. Will only scan up to a pre-defined limit of blocks before
     * throwing and exception
     *
     * @param block The initial block to begin this round of scanning from
     * @throws IllegalAirshipException Thrown if the number of blocks in the airship exceeds the pre-defined limit
     */
    private void scanAirship(Block block) throws IllegalAirshipException {

        // Create array to hold the 26 block neighbours
        Block neighbours[] = new Block[26];

        // Create array to hold the 6 nearest neighbours
        Integer[] nativeNearest = {4, 10, 12, 13, 15, 21};
        ArrayList<Integer> nearestNeighbours = new ArrayList<>(Arrays.asList(nativeNearest));

        // Create an array for each of the blocks that neighbours the initial block
        int x = 0; // Array index counter
        for (int i = -1; i < 2; i++) { // X
            for (int j = -1; j < 2; j++) { // Y
                for (int k = -1; k < 2; k++) { // Z
                    if (i == 0 && j == 0 && k == 0) // We do not want the initial block repeating
                        continue;
                    // Get the block and add to the neighbour array
                    neighbours[x++] = block.getRelative(i, j, k);
                }
            }
        }

        // Go through each neighbouring block and scan it.
        for (int i = 0; i < neighbours.length; i++) {

            // Create the airship block for this neighbour
            AirshipBlock b = new AirshipBlock(neighbours[i]);

            // If this block is already in the airship we can ignore it
            if (!blocks.contains(b)) {

                // If this block is made of air then we do not add it to the airship (unless it is a direct
                // neighbour, these are up,down,north,east,south and west.)
                if ((b.t == Material.AIR && nearestNeighbours.contains(i)) || b.t != Material.AIR) {
                    blocks.add(b);
                }

                // If the airship is already at the limit then throw an exception to prevent it being created
                if (blocks.size() > 5000) {
                    throw new IllegalAirshipException("Too many blocks son!");
                }

                // If the block is not air, then we need to scan its neighbours
                if (b.t != Material.AIR) {
                    scanAirship(neighbours[i]); // recursion : )
                }
            }
        }
    }

    /**
     * Moves the airship in the specified direction
     */
    @SuppressWarnings("deprecation")
    public void moveAirship() {

        // We need to loop over each block in the airship and move the block.
        for (AirshipBlock block : blocks) {

            // Call the method to shift the block in the specified direction
            block.shiftBlock(currentDirection, isReversing);

            // Get the current block at the new location
            Block newBlock = world.getBlockAt(block.x, block.y, block.z);

            // Set the necessary meta-data on the block
            newBlock.setMetadata("airship-data", new FixedMetadataValue(owningPlugin, "1"));

            // If the new location is the same block as the old location we can skip over changing it
            if (newBlock.getType() == block.t && newBlock.getData() == block.d) {
                continue;
            }

            // Set the type of the new location
            newBlock.setType(block.t);

            // Set the data of the new location (color etc..)
            newBlock.setData(block.d);
        }
    }

    /**
     * Starts the airship moving, if it is already moving then we simply do nothing.
     *
     * @param plugin The airship plugin that has called start on this airship
     */
    public void startAirship(AirshipPlugin plugin) {

        // Check if the airship already moving
        if (!isMoving) {

            // Play the engine start sound to the player (TODO: Change when custom sounds are supported)
            Bukkit.getPlayer(owner).playSound(Bukkit.getPlayer(owner).getLocation(), Sound.PORTAL, 1.0f, 1.0f);

            // Create a repeating task to move the airship on a regular interval (currently 2.5 seconds)
            task = Bukkit.getScheduler().runTaskTimer(plugin, new AirshipMover(this), 0, 50);

            // Set the is moving flag to true
            isMoving = true;
        }
    }

    /**
     * Stops the airship moving by cancelling the repeating task
     */
    public void stopAirship() {

        // If the airship is not moving then we don't do anything
        if (isMoving) {

            // Cancel the repeating task
            task.cancel();

            // Set the moving flag to false
            isMoving = false;
        }
    }

    /**
     * Rotates the airship in the specified direction
     *
     * @param turnDirection The direction to rotate the airship (left or right)
     */
    @SuppressWarnings("deprecation")
    public void rotateAirship(TurnDirection turnDirection) {

        // We need to get the center point of the airship
        int maxX = blocks.get(0).x;
        int maxZ = blocks.get(0).z;

        // Temporary variables for the rotation calculation
        int minX = maxX;
        int minZ = maxZ;

        // Go through each block and find the block at the minimum X and Z values
        for (AirshipBlock block : blocks) {
            if (block.x > maxX) {
                maxX = block.x;
            }
            if (block.x < minX) {
                minX = block.x;
            }
            if (block.z > maxZ) {
                maxZ = block.z;
            }
            if (block.z < minZ) {
                minZ = block.z;
            }
        }

        // Work out the center point (from the minimum X/Z and maximum X/Z values)
        int originX = (maxX + minX) / 2;
        int originZ = (maxZ + minZ) / 2;

        // Get the current location of the airships owner
        Location playerLocation =  Bukkit.getPlayer(owner).getLocation();

        // Get the location that is the block below the player
        playerLocation.setY(playerLocation.getY() - 1);

        // We do not want to move the airships owner by default
        boolean rotatePlayer = false;

        // Go through each block in the airship and rotate it
        for (AirshipBlock block : blocks) {

            // Get the location of the block in the airship
            Location blockLocation = new Location(world, block.x, block.y, block.z);

            // If this location is the same as the block below the owner (see above) then we need to move the owner
            if (blockLocation.getBlockX() == playerLocation.getBlockX() &&
                    blockLocation.getBlockY() == playerLocation.getBlockY() &&
                    blockLocation.getBlockZ() == playerLocation.getBlockZ()) {

                // Set the move owner flag to true
                rotatePlayer = true;
            }

            // Remove the current block from the server (set to air)
            world.getBlockAt(block.x, block.y, block.z).setType(Material.AIR);

            // Rotate the airship block in the necessary direction about the center point
            block.rotateBlock(turnDirection, originX, originZ);
        }

        // Go through each block in the airship again and place it on the server
        for (AirshipBlock block : blocks) {

            // Get the block at the new location
            Block newBlock = world.getBlockAt(block.x, block.y, block.z);

            // Set the type value for the block at the new location
            newBlock.setType(block.t);

            // Set the data value for the block at the new location
            newBlock.setData(block.d);
        }

        // We need to update the direction of travel based on the existing direction
        switch (currentDirection) {
            case NORTH:
                // North can become East or West
                currentDirection = turnDirection == TurnDirection.LEFT ? BlockFace.WEST : BlockFace.EAST;
                break;
            case EAST:
                // East can become North or South
                currentDirection = turnDirection == TurnDirection.LEFT ? BlockFace.NORTH : BlockFace.SOUTH;
                break;
            case SOUTH:
                // South can become East or West
                currentDirection = turnDirection == TurnDirection.LEFT ? BlockFace.EAST : BlockFace.WEST;
                break;
            case WEST:
                // West can become South or North
                currentDirection = turnDirection == TurnDirection.LEFT ? BlockFace.SOUTH : BlockFace.NORTH;
                break;
        }

        // If we have decided we need to move the owner then do it here
        if (rotatePlayer) {

            // Teleport the player to the new location
            rotatePlayer(turnDirection, originX, originZ, Bukkit.getPlayer(owner));
        }
    }

    /**
     * Rotates the player about the origin location in the direction specified
     *
     * @param turnDirection The direction to rotate the player in
     * @param originX       The center point X value
     * @param originZ       The center point Y value
     * @param player        The player we are going to rotate
     */
    public void rotatePlayer(TurnDirection turnDirection, int originX, int originZ, Player player) {

        // Work out the deltas for the rotation
        int x = (int) (player.getLocation().getX() - originX);
        int z = (int) (player.getLocation().getZ() - originZ);
        int x2;
        int z2;

        // Set the new x and z values based on the deltas
        if (turnDirection == TurnDirection.RIGHT) {
            x2 = -z;
            z2 = x;
        } else {
            x2 = z;
            z2 = -x;
        }

        // Create a new location for the player based on the calculations above
        Location newLocation = new Location(world, x2 + originX, player.getLocation().getY(), z2 + originZ);

        // Set the new Yaw of the player
        newLocation.setYaw(player.getLocation().getYaw() + (turnDirection == TurnDirection.LEFT ? -90 : 90));

        // Teleport the player to the new location
        player.teleport(newLocation);

    }

    public void saveAirship(String fileName) throws IOException {

        // Create a file for this airship
        File file = new File("./airships/" + fileName);

        // Check if the file exists, if not create a new one
        if (!file.exists()) file.createNewFile();

        // Get a BufferedWriter to save the airship
        BufferedWriter writer = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));

        writer.write(owner);
        writer.newLine();
        writer.write(world.getName());
        writer.newLine();
        writer.write(currentDirection.name());
        writer.newLine();
        writer.write(String.valueOf(blocks.size()));
        writer.newLine();

        for(AirshipBlock block : blocks){
            writer.write(String.format("%d,%d,%d,%s,%d", block.x, block.y, block.z, block.t, block.d));
            writer.newLine();
        }

        writer.flush();
        writer.close();
    }
}
