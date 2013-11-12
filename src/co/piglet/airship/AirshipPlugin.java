package co.piglet.airship;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Plugin code for managing and controlling Airships on a CraftBukkit server
 * This is built for the 1.6.4 Bukkit API
 *
 * @author Geoff Wilson
 * @author Ben Carvell
 * @version 1.0
 */
public class AirshipPlugin extends JavaPlugin implements Listener {

    // The collection that stores the airships present on the server
    private ConcurrentHashMap<String, Airship> airships;

    // The plugin version (always 1.0 :)
    private static final String PLUGIN_VERSION = "1.0-alpha";

    // Redis pool for storing metadata
    private JedisPool redisPool;

    /**
     * Called when then plugin is enabled by CraftBukkit. This is where we should
     * instance all of our class variables and do any setup
     * TODO: This should load the airships from disk/database
     */
    @Override
    public void onEnable() {

        // Create the collection to store the airships in
        airships = new ConcurrentHashMap<>();

        // Register this class as the event handler
        getServer().getPluginManager().registerEvents(this, this);

        // Create the redis connection pool
        redisPool = new JedisPool("127.0.0.1");

        // Load the Airships
        try {

            // Get the airships folder
            File folder = new File("./airships/");

            // List all of the files in the airships folder
            File[] airshipFiles = folder.listFiles();

            // Loop over each file in the airships folder (if the list of files is null then create a dummy 0 length array)
            for (File airship : airshipFiles != null ? airshipFiles : new File[0]) {

                // Check that the file ends with .airship
                if (airship.getName().endsWith(".airship")) {

                    // Call the File based Airship constructor to load the file
                    Airship loadedAirship = new Airship(airship.getAbsoluteFile());

                    // Set this as the owning plugin for the loaded airship
                    loadedAirship.owningPlugin = this;

                    // Add the airship to the collection of active airships
                    airships.put(airship.getName().split("\\.")[0], loadedAirship);
                }
            }
        } catch (IOException e) {

            // Log the load error so it can be investigated
            getLogger().severe("Failed to load airship 'pigship3' : " + e.getMessage());
        }

        // Write to the log so we know we have started up
        getLogger().info(String.format("PigletAirship v%s Loaded", PLUGIN_VERSION));
    }

    /**
     * Called when the plugin is disabled by CraftBukkit (on shutdown normally)
     * TODO: This should save the airships to disk/database
     */
    @Override
    public void onDisable() {

        for (String airshipName : airships.keySet()) {
            try {
                airships.get(airshipName).saveAirship(airshipName + ".airship");
            } catch (IOException e) {
                getLogger().severe("Failed to save airship " + airshipName + ": " + e.getMessage());
            }
        }

        // Save the airships to disk
        getLogger().info(String.format("PigletAirship v%s Shutdown", PLUGIN_VERSION));
    }

    /**
     * Gets the list of airships from the server, this is called by the Android plugin using reflection
     *
     * @return An array containing the list of airships
     */
    @SuppressWarnings("unused")
    public String[] getAirships() {
        String airship[] = new String[airships.keySet().size()];
        return airships.keySet().toArray(airship);
    }

    /**
     * Called when a command is sent to the server by a client
     *
     * @param sender The entity who sent the command (Console or Player)
     * @param cmd    The command sent by the sender
     * @param label  The alias that was used to issue the command (not used in this plugin)
     * @param args   The additional arguments sent with the command
     * @return True if the command was executed, False if it was aborted
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        // What was the command issued?
        switch (cmd.getName()) {

            // We only handle airship commands
            case "airship":
            case "as":


                // Substitute shorthand commands and catch calls with no commands
                if (args.length != 0) {
                    switch (args[0]) {
                        case "f":
                            args[0] = "forward";
                            break;
                        case "l":
                            args[0] = "left";
                            break;
                        case "r":
                            args[0] = "right";
                            break;
                        case "c":
                            args[0] = "create";
                            break;
                        case "u":
                            args[0] = "up";
                            break;
                        case "d":
                            args[0] = "down";
                            break;
                        case "s":
                            args[0] = "stop";
                            break;
                        case "ls":
                            args[0] = "list";
                            break;
                    }
                }


                // Check if this command was sent by a player
                if (sender instanceof Player) {

                    // Get the player
                    Player player = (Player) sender;

                    String action;
                    String airshipName;

                    // Check for correct number of arguments and set action type and airship name
                    if (args.length == 1) {

                        action = args[0];

                        if (action.equals("list")) {

                            // Loop through each airship in the collection
                            for (String airship : airships.keySet()) {

                                // Send the name of the airship to the player
                                player.sendMessage(airship);
                            }
                            return true;
                        }

                        // Get metadata from the player object
                        airshipName = this.getMetadata(player.getName(), "activeAirship");

                        // If the metadata is null or the array is 0 then error
                        if (airshipName == null) {

                            // No metadata found, inform the player
                            player.sendMessage("You don't have an active airship!");
                            return true;
                        }

                        // Check that this airship actually still exists
                        if (!airships.containsKey(airshipName)) {

                            // Clear the metadata
                            this.clearMetadata(player.getName(), "activeAirship");

                            // Inform the player that this airship doesn't exist
                            player.sendMessage("No airship by this name found");
                            return true;
                        }

                        // Get the target airship from then collection
                        Airship targetShip = airships.get(airshipName);

                        // This handles the '/airship stop' command
                        if (action.equals("stop")) {

                            // Stop the target airship
                            targetShip.stopAirship();
                        }

                        // This handles the '/airship left' command
                        if (action.equals("left")) {

                            // Turn the target airship left
                            targetShip.rotateAirship(TurnDirection.LEFT);
                        }

                        // This handles the '/airship right' command
                        if (action.equals("right")) {

                            // Turn the airship right
                            targetShip.rotateAirship(TurnDirection.RIGHT);
                        }

                        // This handles the '/airship up' and '/airship down' command
                        if (action.equals("up") || action.equals("down")) {

                            // If the airship is not currently moving up or down we must store the current direction for later
                            if (targetShip.currentDirection != BlockFace.UP && targetShip.currentDirection != BlockFace.DOWN) {
                                targetShip.lastDirection = targetShip.currentDirection;
                            }

                            // Set the current direction of the airship to up or down
                            targetShip.currentDirection = action.equals("up") ? BlockFace.UP : BlockFace.DOWN;

                            // We are no longer reversing the airship
                            targetShip.isReversing = false;

                            // Start the airship
                            targetShip.startAirship(this);
                        }

                        // This handles the '/airship forward' and '/airship reverse' commands
                        if (action.equals("forward") || action.equals("reverse")) {

                            // Set the reversing flag as necessary
                            targetShip.isReversing = action.equals("reverse");

                            // If the current direction is up or down then set back to the direction before these
                            if (targetShip.currentDirection == BlockFace.UP || targetShip.currentDirection == BlockFace.DOWN) {
                                targetShip.currentDirection = targetShip.lastDirection;
                            }

                            // Start the airship
                            targetShip.startAirship(this);
                        }

                    } else if (args.length == 2) {

                        action = args[0];
                        airshipName = args[1];

                        // Check if the airship specified exists (or that this is a create command)
                        if (!airships.containsKey(airshipName) && !action.equals("create")) {

                            // Inform the player that this airship doesn't exist
                            player.sendMessage("No airship by this name found");
                            return true;
                        }

                        // This handles the '/airship create' command
                        if (action.equals("create")) {

                            // Check if the player is flying
                            if (player.isFlying()) {

                                // Send the player a message if they are flying as we can't create an airship in this case
                                player.sendMessage("Can't create an airship while flying, please stand on the flight deck");
                                return true;
                            }

                            // Check if the airship name already exists
                            if (airships.containsKey(airshipName)) {

                                // Inform the player an airship by this name already exists
                                sender.sendMessage("An airship with this name already exists");
                                return true;
                            }

                            // Get the location of the player who issued the create command
                            Location initialLocation = player.getLocation();

                            // Adjust the location to one block below (this is the block the player is stood on)
                            initialLocation.setY(initialLocation.getBlockY() - 1);

                            // Get the world the player is in
                            World world = player.getWorld();

                            try {
                                // Create a new Airship object for the world, player and specified initial block
                                Airship newAirship = new Airship(world, world.getBlockAt(initialLocation), player.getName());

                                // Set this plugin as the owner of the new airship
                                newAirship.owningPlugin = this;

                                // Add the airship to the collection
                                airships.put(airshipName, newAirship);

                                // Inform the player that this airship has been created successfully
                                player.sendMessage(String.format("Airship '%s' created!", airshipName));

                            } catch (IllegalAirshipException e) {

                                // The scan of the airship failed, we need to inform the player
                                player.sendMessage("Airship creation failed: " + e.getMessage());
                            }
                        }


                        // This handles the '/airship delete' command
                        if (action.equals("delete")) {

                            // Get the target airship from then collection
                            Airship targetShip = airships.get(airshipName);

                            // Stop the airship
                            targetShip.stopAirship();

                            // Remove the airship from the collection
                            airships.remove(airshipName);
                            player.sendMessage(String.format("Airship '%s' deleted!", airshipName));

                        }

                        // This handles the '/airship activate' command
                        if (action.equals("activate")) {

                            // Get the target airship from then collection
                            Airship targetShip = airships.get(airshipName);

                            // Check player owns the selected airship

                            if (player.getName().equals(targetShip.owner)) {
                                // Use redis to store persistent metadata
                                this.setMetadata(player.getName(), "activeAirship", airshipName);

                                // Inform the user that their selected airship is active
                                player.sendMessage(airshipName + " activated!");

                            } else {
                                player.sendMessage("You do not own " + airshipName + "!");
                            }


                        }

                    } else {

                        player.sendMessage("Type /airship help for a list of commands.");
                        return true;

                    }


                }

                break;
        }

        // We didn't do anything, return true
        return true;
    }

    /**
     * Checks if the metadata key exists for the specified player name
     *
     * @param playerName The player name to check on the server
     * @param key        The key we are checking for the existence of
     * @return True if the key is set for the specified player, false if not
     */
    private boolean hasMetadata(String playerName, String key) {

        // Get a connection to the Redis server
        Jedis redis = redisPool.getResource();

        // Check if the key exists for the specified player
        boolean exists = redis.hexists(playerName, key);

        // Return the connection to the pool
        redisPool.returnResource(redis);

        // Return the existence state for the metadata
        return exists;
    }

    /**
     * Gets the value of the specified metadata value for the player given
     *
     * @param playerName The player we are tyring to query on the Redis server
     * @param key        The key we want to get the value of
     * @return A String value representing the key value, or null if the key is not set for the player
     */
    private String getMetadata(String playerName, String key) {
        // Get a connection to the Redis server
        Jedis redis = redisPool.getResource();

        // Check if the key exists for the specified player
        if (hasMetadata(playerName, key)) {

            // Get the value for the specified player and key
            String value = redis.hget(playerName, key);

            // Return the connection to the pool
            redisPool.returnResource(redis);

            // Return the value for the key/player
            return value;
        }

        // Return the connection to the pool
        redisPool.returnResource(redis);

        // Return null
        return null;
    }

    /**
     * Sets a metadata value for the specified player on the Redis server, this does not check if the value is
     * already set for a given key, and will always simply overwrite the old value
     *
     * @param playerName The player name we want to set the data for
     * @param key        The key to store the value under
     * @param value      The value we want to store for the player
     * @return True if the data was successfully set on the server, false if not.
     */
    private boolean setMetadata(String playerName, String key, String value) {
        // Get a connection to the Redis server
        Jedis redis = redisPool.getResource();

        // Set the value for the specified player/key
        long result = redis.hset(playerName, key, value);

        // Return the connection to the pool
        redisPool.returnResource(redis);

        // Return true if the Redis server set the value against the key
        return result == 1;
    }

    /**
     * Removes a key from the specified player on the Redis server
     *
     * @param playerName The player we want to delete a given key for
     * @param key        The key value to remove from the player
     * @return True if the key was deleted successfully, false if the key wasn't removed or didn't exists
     */
    private boolean clearMetadata(String playerName, String key) {

        // Get a connection to the Redis server
        Jedis redis = redisPool.getResource();

        // Delete the key from the player
        long result = redis.hdel(playerName, key);

        // Return the connection to the pool
        redisPool.returnResource(redis);

        // Return true if the Redis server deleted a row
        return result == 1;
    }
}
