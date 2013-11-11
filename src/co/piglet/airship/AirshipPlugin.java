package co.piglet.airship;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
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
    private ConcurrentHashMap<String, Airship> airships;

    private static final String PLUGIN_VERSION = "1.0-alpha";

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
        // TODO: We should extract the events into a separate class if they get too long
        getServer().getPluginManager().registerEvents(this, this);

        // Write to the log so we know we have started up
        getLogger().info(String.format("PigletAirship v%s Loaded", PLUGIN_VERSION));
    }

    /**
     * Called when the plugin is disabled by CraftBukkit (on shutdown normally)
     * TODO: This should save the airships to disk/database
     */
    @Override
    public void onDisable() {
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

                // Substitute shorthand commands

                if (args[0].equals("f")) {
                    args[0] = "forward";
                } else if (args[0].equals("l")) {
                    args[0] = "left";
                } else if (args[0].equals("r")) {
                    args[0] = "right";
                } else if (args[0].equals("c")) {
                    args[0] = "create";
                } else if (args[0].equals("u")) {
                    args[0] = "up";
                } else if (args[0].equals("d")) {
                    args[0] = "down";
                } else if (args[0].equals("s")) {
                    args[0] = "stop";
                } else if (args[0].equals("ls")) {
                    args[0] = "list";
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

                        // DERPY MESS DOESNT WORK AFTER RELOAD, this check doesnt catch dead meta

                        List<MetadataValue> meta = player.getMetadata("activeAirship");

                        if (meta == null) {
                            player.sendMessage("You don't have an active airship!");
                            return true;
                        }

                        //then this murders the plugin

                        airshipName = player.getMetadata("activeAirship").get(0).asString();
                        player.sendMessage(airshipName);

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
                                Airship newAirship = new Airship(world, world.getBlockAt(initialLocation), player);

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
                            player.setMetadata("activeAirship", new FixedMetadataValue(this, airshipName));
                            player.sendMessage(airshipName + " activated!");
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
}
