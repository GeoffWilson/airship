package co.piglet.airship;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

/**
 * Represents a block in the airship, uses code from WorldEdit licenced under GNU LGPLv3
 *
 * @author Geoff Wilson
 * @author Ben Carvell
 */
public class AirshipBlock implements Comparable<AirshipBlock> {

    public static void hello()
    {
        System.out.println("hello");
        }

    /**
     * This is the quick constructor for loading the Airship from disk
     * @param x The blocks X location in the world
     * @param y The blocks Y location in the world
     * @param z The blocks Z location in the world
     * @param t The blocks Material type
     * @param d The blocks data value (color etc..)
     */
    public AirshipBlock(int x, int y, int z, Material t, int d) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.t = t;
        this.d = (byte) d;
    }

    /**
     * Set the correct new data value to rotate a block, this code is adapted from WorldEdit to use standard
     * Material types instead of WorldEdit ones.
     *
     * @param type The type of block to rotate
     * @param data The current data value
     * @return The new data value for the block
     */
    public int rotate90Reverse(Material type, int data) {

        switch (type) {
            case TORCH:
            case REDSTONE_TORCH_OFF:
            case REDSTONE_TORCH_ON:
                switch (data) {
                    case 3:
                        return 1;
                    case 4:
                        return 2;
                    case 2:
                        return 3;
                    case 1:
                        return 4;
                }
                break;

            case RAILS:
                switch (data) {
                    case 7:
                        return 6;
                    case 8:
                        return 7;
                    case 9:
                        return 8;
                    case 6:
                        return 9;
                }

            case POWERED_RAIL:
            case DETECTOR_RAIL:
            case ACTIVATOR_RAIL:
                int power = data & ~0x7;
                switch (data & 0x7) {
                    case 1:
                        return power;
                    case 0:
                        return 1 | power;
                    case 5:
                        return 2 | power;
                    case 4:
                        return 3 | power;
                    case 2:
                        return 4 | power;
                    case 3:
                        return 5 | power;
                }
                break;

            case WOOD_STAIRS:
            case COBBLESTONE_STAIRS:
            case BRICK_STAIRS:
            case SMOOTH_STAIRS:
            case NETHER_BRICK_STAIRS:
            case SANDSTONE_STAIRS:
            case SPRUCE_WOOD_STAIRS:
            case BIRCH_WOOD_STAIRS:
            case JUNGLE_WOOD_STAIRS:
            case QUARTZ_STAIRS:
                switch (data) {
                    case 2:
                        return 0;
                    case 3:
                        return 1;
                    case 1:
                        return 2;
                    case 0:
                        return 3;
                    case 6:
                        return 4;
                    case 7:
                        return 5;
                    case 5:
                        return 6;
                    case 4:
                        return 7;
                }
                break;

            case LEVER:
            case STONE_BUTTON:
            case WOOD_BUTTON:
                int thrown = data & 0x8;
                int withoutThrown = data & ~0x8;
                switch (withoutThrown) {
                    case 3:
                        return 1 | thrown;
                    case 4:
                        return 2 | thrown;
                    case 2:
                        return 3 | thrown;
                    case 1:
                        return 4 | thrown;
                    case 6:
                        return 5 | thrown;
                    case 5:
                        return 6 | thrown;
                    case 0:
                        return 7 | thrown;
                    case 7:
                        return thrown;
                }
                break;

            case WOODEN_DOOR:
            case IRON_DOOR:
                if ((data & 0x8) != 0) {
                    // door top halves contain no orientation information
                    break;
                }

            case COCOA:
            case TRIPWIRE_HOOK:
                int extra = data & ~0x3;
                int withoutFlags = data & 0x3;
                switch (withoutFlags) {
                    case 1:
                        return extra;
                    case 2:
                        return 1 | extra;
                    case 3:
                        return 2 | extra;
                    case 0:
                        return 3 | extra;
                }
                break;

            case SIGN_POST:
                return (data + 12) % 16;

            case LADDER:
            case WALL_SIGN:
            case CHEST:
            case FURNACE:
            case BURNING_FURNACE:
            case ENDER_CHEST:
            case TRAPPED_CHEST:
            case HOPPER:
                switch (data) {
                    case 5:
                        return 2;
                    case 4:
                        return 3;
                    case 2:
                        return 4;
                    case 3:
                        return 5;
                }
                break;

            case DISPENSER:
            case DROPPER:
                int dispPower = data & 0x8;
                switch (data & ~0x8) {
                    case 5:
                        return 2 | dispPower;
                    case 4:
                        return 3 | dispPower;
                    case 2:
                        return 4 | dispPower;
                    case 3:
                        return 5 | dispPower;
                }
                break;
            case PUMPKIN:
            case JACK_O_LANTERN:
                switch (data) {
                    case 1:
                        return 0;
                    case 2:
                        return 1;
                    case 3:
                        return 2;
                    case 0:
                        return 3;
                }
                break;

            case HAY_BLOCK:
            case LOG:
                if (data >= 4 && data <= 11) {
                    data ^= 0xc;
                }
                break;

            case REDSTONE_COMPARATOR_OFF:
            case REDSTONE_COMPARATOR_ON:
            case DIODE_BLOCK_OFF:
            case DIODE_BLOCK_ON:
                int dir = data & 0x03;
                int delay = data - dir;
                switch (dir) {
                    case 1:
                        return delay;
                    case 2:
                        return 1 | delay;
                    case 3:
                        return 2 | delay;
                    case 0:
                        return 3 | delay;
                }
                break;

            case TRAP_DOOR:
                int withoutOrientation = data & ~0x3;
                int orientation = data & 0x3;
                switch (orientation) {
                    case 3:
                        return withoutOrientation;
                    case 2:
                        return 1 | withoutOrientation;
                    case 0:
                        return 2 | withoutOrientation;
                    case 1:
                        return 3 | withoutOrientation;
                }

            case PISTON_BASE:
            case PISTON_STICKY_BASE:
            case PISTON_EXTENSION:
                final int rest = data & ~0x7;
                switch (data & 0x7) {
                    case 5:
                        return 2 | rest;
                    case 4:
                        return 3 | rest;
                    case 2:
                        return 4 | rest;
                    case 3:
                        return 5 | rest;
                }
                break;

            case BROWN_MUSHROOM:
            case RED_MUSHROOM:
                if (data >= 10) {
                    return data;
                }
                return (data * 7) % 10;

            case VINE:
                return ((data >> 1) | (data << 3)) & 0xf;

            case FENCE_GATE:
                return ((data + 3) & 0x3) | (data & ~0x3);

            case ANVIL:
                return data ^ 0x1;

            case BED:
                return data & ~0x3 | (data - 1) & 0x3;

            case SKULL:
                switch (data) {
                    case 2:
                        return 4;
                    case 3:
                        return 5;
                    case 4:
                        return 3;
                    case 5:
                        return 2;
                }
        }

        return data;
    }

    /**
     * Set the correct new data value to rotate a block, this code is adapted from WorldEdit to use standard
     * Material types instead of WorldEdit ones.
     *
     * @param type The type of block to rotate
     * @param data The current data value
     * @return The new data value for the block
     */
    public int rotate90(Material type, int data) {
        switch (type) {
            case TORCH:
            case REDSTONE_TORCH_OFF:
            case REDSTONE_TORCH_ON:
                switch (data) {
                    case 1:
                        return 3;
                    case 2:
                        return 4;
                    case 3:
                        return 2;
                    case 4:
                        return 1;
                }
                break;

            case RAILS:
                switch (data) {
                    case 6:
                        return 7;
                    case 7:
                        return 8;
                    case 8:
                        return 9;
                    case 9:
                        return 6;
                }

            case POWERED_RAIL:
            case DETECTOR_RAIL:
            case ACTIVATOR_RAIL:
                switch (data & 0x7) {
                    case 0:
                        return 1 | (data & ~0x7);
                    case 1:
                        return (data & ~0x7);
                    case 2:
                        return 5 | (data & ~0x7);
                    case 3:
                        return 4 | (data & ~0x7);
                    case 4:
                        return 2 | (data & ~0x7);
                    case 5:
                        return 3 | (data & ~0x7);
                }
                break;

            case WOOD_STAIRS:
            case COBBLESTONE_STAIRS:
            case BRICK_STAIRS:
            case SMOOTH_STAIRS:
            case NETHER_BRICK_STAIRS:
            case SANDSTONE_STAIRS:
            case SPRUCE_WOOD_STAIRS:
            case BIRCH_WOOD_STAIRS:
            case JUNGLE_WOOD_STAIRS:
            case QUARTZ_STAIRS:
                switch (data) {
                    case 0:
                        return 2;
                    case 1:
                        return 3;
                    case 2:
                        return 1;
                    case 3:
                        return 0;
                    case 4:
                        return 6;
                    case 5:
                        return 7;
                    case 6:
                        return 5;
                    case 7:
                        return 4;
                }
                break;

            case LEVER:
            case STONE_BUTTON:
            case WOOD_BUTTON:
                int thrown = data & 0x8;
                int withoutThrown = data & ~0x8;
                switch (withoutThrown) {
                    case 1:
                        return 3 | thrown;
                    case 2:
                        return 4 | thrown;
                    case 3:
                        return 2 | thrown;
                    case 4:
                        return 1 | thrown;
                    case 5:
                        return 6 | thrown;
                    case 6:
                        return 5 | thrown;
                    case 7:
                        return thrown;
                    case 0:
                        return 7 | thrown;
                }
                break;

            case WOODEN_DOOR:
            case IRON_DOOR:
                if ((data & 0x8) != 0) {
                    // door top halves contain no orientation information
                    break;
                }

            case COCOA:
            case TRIPWIRE_HOOK:
                int extra = data & ~0x3;
                int withoutFlags = data & 0x3;
                switch (withoutFlags) {
                    case 0:
                        return 1 | extra;
                    case 1:
                        return 2 | extra;
                    case 2:
                        return 3 | extra;
                    case 3:
                        return extra;
                }
                break;

            case SIGN_POST:
                return (data + 4) % 16;

            case LADDER:
            case WALL_SIGN:
            case CHEST:
            case FURNACE:
            case BURNING_FURNACE:
            case ENDER_CHEST:
            case TRAPPED_CHEST:
            case HOPPER:
                switch (data) {
                    case 2:
                        return 5;
                    case 3:
                        return 4;
                    case 4:
                        return 2;
                    case 5:
                        return 3;
                }
                break;

            case DISPENSER:
            case DROPPER:
                int dispPower = data & 0x8;
                switch (data & ~0x8) {
                    case 2:
                        return 5 | dispPower;
                    case 3:
                        return 4 | dispPower;
                    case 4:
                        return 2 | dispPower;
                    case 5:
                        return 3 | dispPower;
                }
                break;

            case PUMPKIN:
            case JACK_O_LANTERN:
                switch (data) {
                    case 0:
                        return 1;
                    case 1:
                        return 2;
                    case 2:
                        return 3;
                    case 3:
                        return 0;
                }
                break;

            case HAY_BLOCK:
            case LOG:
                if (data >= 4 && data <= 11) {
                    data ^= 0xc;
                }
                break;

            case REDSTONE_COMPARATOR_ON:
            case REDSTONE_COMPARATOR_OFF:
            case DIODE_BLOCK_OFF:
            case DIODE_BLOCK_ON:
                int dir = data & 0x03;
                int delay = data - dir;
                switch (dir) {
                    case 0:
                        return 1 | delay;
                    case 1:
                        return 2 | delay;
                    case 2:
                        return 3 | delay;
                    case 3:
                        return delay;
                }
                break;

            case TRAP_DOOR:
                int withoutOrientation = data & ~0x3;
                int orientation = data & 0x3;
                switch (orientation) {
                    case 0:
                        return 3 | withoutOrientation;
                    case 1:
                        return 2 | withoutOrientation;
                    case 2:
                        return withoutOrientation;
                    case 3:
                        return 1 | withoutOrientation;
                }
                break;

            case PISTON_BASE:
            case PISTON_STICKY_BASE:
            case PISTON_EXTENSION:
                final int rest = data & ~0x7;
                switch (data & 0x7) {
                    case 2:
                        return 5 | rest;
                    case 3:
                        return 4 | rest;
                    case 4:
                        return 2 | rest;
                    case 5:
                        return 3 | rest;
                }
                break;

            case BROWN_MUSHROOM:
            case RED_MUSHROOM:
                if (data >= 10) {
                    return data;
                }
                return (data * 3) % 10;

            case VINE:
                return ((data << 1) | (data >> 3)) & 0xf;

            case FENCE_GATE:
                return ((data + 1) & 0x3) | (data & ~0x3);

            case ANVIL:
                return data ^ 0x1;

            case BED:
                return data & ~0x3 | (data + 1) & 0x3;

            case SKULL:
                switch (data) {
                    case 2:
                        return 5;
                    case 3:
                        return 4;
                    case 4:
                        return 2;
                    case 5:
                        return 3;
                }
        }

        return data;
    }

    // The location of the AirshipBlock
    public int x;
    public int y;
    public int z;

    // The data value of the AirshipBlock
    public byte d;

    // The type of the AirshipBlock
    public Material t;

    /**
     * Create a new AirshipBlock based on the Block from the world
     *
     * @param b The Block in the world to base this AirshipBlock on
     */
    @SuppressWarnings("deprecation")
    public AirshipBlock(Block b) {

        // Set the location values for the airship block
        x = b.getX();
        y = b.getY();
        z = b.getZ();

        // Set the data value for this airship block (color etc..)
        d = b.getData();

        // Set the type of the airship block
        t = b.getType();
    }

    /**
     * Checks if this AirshipBlock is equal to another airship block (this only takes location into account
     * not material)
     *
     * @param other The other AirshipBlock to compare this to
     * @return True if these blocks occupy the same location, False if not
     */
    @Override
    public boolean equals(Object other) {

        // Check if the object to compare is an AirshipBlock
        if (other instanceof AirshipBlock) {



            // Cast the other object to an AirshipBlock
            AirshipBlock otherBlock = (AirshipBlock) other;

            // Return true if the x,y and z values are equal between this block and the other block
            return otherBlock.x == x && otherBlock.y == y && otherBlock.z == z;

        }   else if (other instanceof Block){

            Block otherBlock = (Block) other;

            return otherBlock.getLocation().getBlockX() == x && otherBlock.getLocation().getBlockY() == y && otherBlock.getLocation().getBlockZ() == z;

        }

        // Return false as this isn't an airship block we are comparing to
        return false;
    }

    /**
     * Shifts the block in the specified direction
     *
     * @param direction   The direction to shift the block in
     * @param isReversing Indicates if the airship is reversing (the shift is opposite in this case)
     */
    public void shiftBlock(BlockFace direction, boolean isReversing) {

        // Set the default shift to nothing
        int xDelta = 0;
        int yDelta = 0;
        int zDelta = 0;

        // Which direction are we trying to shift the block?
        switch (direction) {
            case EAST:
                xDelta = isReversing ? -1 : 1;
                break;
            case WEST:
                xDelta = isReversing ? 1 : -1;
                break;
            case NORTH:
                zDelta = isReversing ? 1 : -1;
                break;
            case SOUTH:
                zDelta = isReversing ? -1 : 1;
                break;
            case UP:
                yDelta = 1; // Does not support reversing
                break;
            case DOWN:
                yDelta = -1; // Does not support reversing
        }

        // Adjust the blocks location based on the calculations above
        x += xDelta;
        y += yDelta;
        z += zDelta;
    }

    /**
     * Rotates the block in the specified direction about the origin points supplied
     *
     * @param turnDirection The direction we are trying to rotate the block in
     * @param originX       The x origin we are rotating about
     * @param originZ       The z origin we are rotating about
     */
    public void rotateBlock(TurnDirection turnDirection, int originX, int originZ) {

        // Work out the delta between the blocks location and the origin
        int x = this.x - originX;
        int z = this.z - originZ;

        // Calculation variables
        int x2;
        int z2;

        // Work out the correct x and z values for the rotation based on the deltas above
        if (turnDirection == TurnDirection.RIGHT) {
            x2 = -z;
            z2 = x;
        } else {
            x2 = z;
            z2 = -x;
        }

        // Set the new location of the block
        this.x = x2 + originX;
        this.z = z2 + originZ;

        // Get the correct data value for the block using the WorldEdit functions
        this.d = (byte) (turnDirection == TurnDirection.RIGHT ? rotate90(this.t, this.d) : rotate90Reverse(this.t, this.d));
    }

    /**
     * Compares this AirshipBlock to another (not used anymore). This was used for sorting the array but is no
     * longer required.
     *
     * @param o The other AirshipBlock we are comparing this one to
     * @return -1 if this block is less than the other, 0 if they are the same, and 1 if this block is greater
     */
    @Override
    public int compareTo(AirshipBlock o) {

        // Compare the X location of this AirshipBlock to the other one
        if (o.x < this.x) {
            return -1;
        } else if (o.x == this.x) {
            return 0;
        } else {
            return 1;
        }
    }
}