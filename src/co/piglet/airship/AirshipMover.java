package co.piglet.airship;

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
