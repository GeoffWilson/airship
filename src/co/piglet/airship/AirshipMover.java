package co.piglet.airship;

import org.bukkit.block.BlockFace;

/**
 * Created with IntelliJ IDEA.
 * User: Geoff
 * Date: 25/10/13
 * Time: 17:01
 * To change this template use File | Settings | File Templates.
 */
public class AirshipMover implements Runnable
{
    private Airship airship;

    public AirshipMover(Airship airship) {
        this.airship = airship;
    }

    @Override
    public void run() {
        airship.moveAirship(airship.currentDirection);
    }
}
