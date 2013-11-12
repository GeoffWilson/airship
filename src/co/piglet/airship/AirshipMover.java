package co.piglet.airship;

/**
 * This class is called by the repeating Bukkit task to move the airship
 *
 * @author Geoff Wilson
 */
public class AirshipMover implements Runnable {

    // This is the airship we are going to move
    private Airship airship;

    /**
     * Creates an instance of the AirshipMover class for the specified Airship
     *
     * @param airship The Airship we are trying to move.
     */
    public AirshipMover(Airship airship) {

            this.airship = airship;
        }

        /**
         * run() function called by the Bukkit scheduler to move the airship
         */
        @Override
        public void run() {

            // Move the airship in the airships current direction
            airship.moveAirship();
        }
}
