package co.piglet.airship;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ConcurrentHashMap;

public class AirshipPlugin extends JavaPlugin implements Listener
{

    private ConcurrentHashMap<String, Airship> airships;

    @Override
    public void onEnable()
    {
        airships = new ConcurrentHashMap<>();
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("PigletAirship v1.0 Loaded");
    }

    @Override
    public void onDisable()
    {
    }

    public String[] getAirships()
    {
        String airship[] = new String[airships.keySet().size()];
        return airships.keySet().toArray(airship);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        switch (cmd.getName())
        {
            case "airship":

                if (sender instanceof Player)
                {

                    // Get the player
                    Player player = (Player) sender;

                    // Check we have the necessary number of parameters
                    if (args.length != 2)
                    {
                        sender.sendMessage("Command is /airship ACTION AIRSHIP");
                        return false;
                    }

                    String action = args[0];
                    String airshipName = args[1];

                    if (action.equals("start"))
                    {
                        if (!airships.containsKey(airshipName))
                        {
                            player.sendMessage("No airship by this name found");
                            return false;
                        }

                        Airship targetShip = airships.get(airshipName);

                        if (!targetShip.isMoving)
                        {
                            targetShip.rescanAirship();
                            player.playSound(player.getLocation(), Sound.PORTAL, 1.0f, 1.0f);
                            targetShip.task = Bukkit.getScheduler().runTaskTimer(this, new AirshipMover(airships.get(airshipName)), 0, 50);
                            targetShip.isMoving = true;
                        }
                        else
                        {
                            player.sendMessage("This airship is already started");
                            return false;
                        }
                    }

                    if (action.equals("stop"))
                    {
                        if (!airships.containsKey(airshipName))
                        {
                            player.sendMessage("No airship by this name found");
                            return false;
                        }

                        Airship targetShip = airships.get(airshipName);

                        if (targetShip.isMoving)
                        {
                            targetShip.task.cancel();
                            targetShip.isMoving = false;
                            player.sendMessage("Airship stopped");
                        }
                        else
                        {
                            player.sendMessage("This airship is not moving");
                            return false;
                        }
                    }

                    if (action.equals("left"))
                    {
                        if (!airships.containsKey(airshipName))
                        {
                            player.sendMessage("No airship by this name found");
                            return false;
                        }

                        Airship targetShip = airships.get(airshipName);
                        targetShip.rotateAirship(Airship.TurnDirection.LEFT);
                    }

                    if (action.equals("right"))
                    {
                        if (!airships.containsKey(airshipName))
                        {
                            player.sendMessage("No airship by this name found");
                            return false;
                        }

                        Airship targetShip = airships.get(airshipName);
                        targetShip.rotateAirship(Airship.TurnDirection.RIGHT);
                    }

                    if (action.equals("up"))
                    {
                        if (!airships.containsKey(airshipName))
                        {
                            player.sendMessage("No airship by this name found");
                            return false;
                        }
                        Airship targetShip = airships.get(airshipName);
                        if (targetShip.currentDirection != BlockFace.UP && targetShip.currentDirection != BlockFace.DOWN)
                        {
                            targetShip.lastDirection = targetShip.currentDirection;
                        }
                        targetShip.currentDirection = BlockFace.UP;
                        targetShip.isReversing = false;
                    }

                    if (action.equals("down"))
                    {
                        if (!airships.containsKey(airshipName))
                        {
                            player.sendMessage("No airship by this name found");
                            return false;
                        }
                        Airship targetShip = airships.get(airshipName);
                        if (targetShip.currentDirection != BlockFace.UP && targetShip.currentDirection != BlockFace.DOWN)
                        {
                            targetShip.lastDirection = targetShip.currentDirection;
                        }
                        targetShip.currentDirection = BlockFace.DOWN;
                        targetShip.isReversing = false;

                    }

                    if (action.equals("forward"))
                    {
                        if (!airships.containsKey(airshipName))
                        {
                            player.sendMessage("No airship by this name found");
                            return false;
                        }

                        Airship targetShip = airships.get(airshipName);
                        targetShip.isReversing = false;
                        if (targetShip.currentDirection == BlockFace.UP || targetShip.currentDirection == BlockFace.DOWN)
                        {
                            targetShip.currentDirection = targetShip.lastDirection;
                        }

                    }

                    if (action.equals("reverse"))
                    {
                        if (!airships.containsKey(airshipName))
                        {
                            player.sendMessage("No airship by this name found");
                            return false;
                        }
                        Airship targetShip = airships.get(airshipName);
                        if (targetShip.isReversing)
                        {
                            player.sendMessage("Airship is already reversing!");
                            return false;
                        }
                        if (targetShip.currentDirection == BlockFace.UP || targetShip.currentDirection == BlockFace.DOWN)
                        {
                            targetShip.currentDirection = targetShip.lastDirection;
                        }
                        targetShip.isReversing = true;


                    }

                    if (action.equals("create"))
                    {

                        if (player.isFlying())
                        {
                            player.sendMessage("Can't create an airship while flying, please stand on the flight deck");
                            return false;
                        }

                        if (airships.containsKey(airshipName))
                        {
                            sender.sendMessage("An airship with this name already exists");
                            return false;
                        }

                        Location initialLocation = player.getLocation();
                        initialLocation.setY(initialLocation.getBlockY() - 1);

                        World world = player.getWorld();

                        Airship newAirship = new Airship(world, world.getBlockAt(initialLocation), player);
                        airships.put(airshipName, newAirship);

                        player.sendMessage("Airship created!");
                    }
                }

                break;
        }

        return true;
    }
}
