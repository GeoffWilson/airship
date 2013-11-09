package co.piglet.airship;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ConcurrentHashMap;

public class AirshipPlugin extends JavaPlugin implements Listener {
    private ConcurrentHashMap<String, Airship> airships;

    @Override
    public void onEnable() {
        airships = new ConcurrentHashMap<>();
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("PigletAirship v1.0 Loaded");
    }

    @Override
    public void onDisable() {
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block eventBlock = event.getBlock();
        if (eventBlock.hasMetadata("airship-data")) {
            eventBlock.getDrops().clear();
        }
    }

    public String[] getAirships() {
        String airship[] = new String[airships.keySet().size()];
        return airships.keySet().toArray(airship);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        switch (cmd.getName()) {
            case "airship":

                if (sender instanceof Player) {
                    // Get the player
                    Player player = (Player) sender;

                    // Check we have the necessary number of parameters
                    if (args.length == 1) {
                        if (args[0].equals("list")) {
                            for (String airship : airships.keySet()) {
                                player.sendMessage(airship);
                            }
                        }
                        return true;
                    } else if (args.length != 2) {
                        sender.sendMessage("Command is /airship ACTION AIRSHIP");
                        return false;
                    }

                    String action = args[0];
                    String airshipName = args[1];

                    if (!airships.containsKey(airshipName) && !action.equals("create")) {
                        player.sendMessage("No airship by this name found");
                        return false;
                    }

                    Airship targetShip = airships.get(airshipName);

                    if (action.equals("stop")) {
                        targetShip.stopAirship();
                    }

                    if (action.equals("left")) {
                        targetShip.rotateAirship(Airship.TurnDirection.LEFT);
                    }

                    if (action.equals("right")) {
                        targetShip.rotateAirship(Airship.TurnDirection.RIGHT);
                    }

                    if (action.equals("up")) {
                        if (targetShip.currentDirection != BlockFace.UP && targetShip.currentDirection != BlockFace.DOWN) {
                            targetShip.lastDirection = targetShip.currentDirection;
                        }
                        targetShip.currentDirection = BlockFace.UP;
                        targetShip.isReversing = false;
                        targetShip.startAirship(this);
                    }

                    if (action.equals("down")) {
                        if (targetShip.currentDirection != BlockFace.UP && targetShip.currentDirection != BlockFace.DOWN) {
                            targetShip.lastDirection = targetShip.currentDirection;
                        }
                        targetShip.currentDirection = BlockFace.DOWN;
                        targetShip.isReversing = false;
                        targetShip.startAirship(this);
                    }

                    if (action.equals("forward")) {
                        targetShip.isReversing = false;
                        if (targetShip.currentDirection == BlockFace.UP || targetShip.currentDirection == BlockFace.DOWN) {
                            targetShip.currentDirection = targetShip.lastDirection;
                        }
                        targetShip.startAirship(this);
                    }

                    if (action.equals("reverse")) {
                        if (targetShip.isReversing) {
                            return false;
                        }
                        if (targetShip.currentDirection == BlockFace.UP || targetShip.currentDirection == BlockFace.DOWN) {
                            targetShip.currentDirection = targetShip.lastDirection;
                        }
                        targetShip.isReversing = true;
                        targetShip.startAirship(this);

                    }

                    if (action.equals("create")) {

                        if (player.isFlying()) {
                            player.sendMessage("Can't create an airship while flying, please stand on the flight deck");
                            return false;
                        }

                        if (airships.containsKey(airshipName)) {
                            sender.sendMessage("An airship with this name already exists");
                            return false;
                        }

                        Location initialLocation = player.getLocation();
                        initialLocation.setY(initialLocation.getBlockY() - 1);

                        World world = player.getWorld();

                        Airship newAirship = null;
                        try {
                            newAirship = new Airship(world, world.getBlockAt(initialLocation), player);
                            airships.put(airshipName, newAirship);

                            player.sendMessage("Airship created!");
                        } catch (IllegalAirshipException e) {
                            player.sendMessage("Airship creation failed: " + e.getMessage());
                        }

                    }

                    if (action.equals("delete")) {
                        if (airships.containsKey(airshipName)) {
                            airships.remove(airshipName);
                            player.sendMessage(airshipName + " deleted!");
                        } else {
                            player.sendMessage("That airship does not exist!");
                        }
                    }


                }

                break;
        }

        return true;
    }
}
