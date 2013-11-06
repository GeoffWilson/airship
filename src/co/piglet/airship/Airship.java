package co.piglet.airship;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Airship
{
    public BukkitTask task;
    private ConcurrentLinkedQueue<AirshipBlock> blocks;
    private World world;
    public Player owner;
    public BlockFace currentDirection;

    public boolean isMoving;

    public enum TurnDirection
    {
        LEFT, RIGHT
    }

    public Airship(World world, Block initialBlock)
    {
        this.currentDirection = BlockFace.EAST;
        this.world = world;
        blocks = new ConcurrentLinkedQueue<>();

        // We need to scan the airship
        scanAirship(initialBlock);
    }

    public void rescanAirship()
    {
        AirshipBlock b = blocks.peek();
        blocks.clear();
        scanAirship(world.getBlockAt(b.x, b.y, b.z));
    }

    private void scanAirship(Block block)
    {
        // Create array to hold the 6 block neighbours
        Block neighbours[] = new Block[18];

        // Get the blocks neighbours
        neighbours[0] = block.getRelative(BlockFace.NORTH);
        neighbours[1] = block.getRelative(BlockFace.EAST);
        neighbours[2] = block.getRelative(BlockFace.SOUTH);
        neighbours[3] = block.getRelative(BlockFace.WEST);
        neighbours[4] = block.getRelative(BlockFace.UP);
        neighbours[5] = block.getRelative(BlockFace.DOWN);

        neighbours[6] = block.getRelative(BlockFace.EAST_NORTH_EAST);
        neighbours[7] = block.getRelative(BlockFace.EAST_SOUTH_EAST);
        neighbours[8] = block.getRelative(BlockFace.NORTH_NORTH_EAST);
        neighbours[9] = block.getRelative(BlockFace.NORTH_NORTH_WEST);
        neighbours[10] = block.getRelative(BlockFace.SOUTH_SOUTH_EAST);
        neighbours[11] = block.getRelative(BlockFace.SOUTH_SOUTH_WEST);
        neighbours[12] = block.getRelative(BlockFace.WEST_NORTH_WEST);
        neighbours[13] = block.getRelative(BlockFace.WEST_SOUTH_WEST);

        neighbours[14] = block.getRelative(BlockFace.NORTH_EAST);
        neighbours[15] = block.getRelative(BlockFace.NORTH_WEST);
        neighbours[16] = block.getRelative(BlockFace.SOUTH_EAST);
        neighbours[17] = block.getRelative(BlockFace.SOUTH_WEST);

        for (Block neighbour : neighbours) {
            AirshipBlock b = new AirshipBlock(neighbour);
            if (b.t != Material.AIR && !blocks.contains(b)) {
                blocks.add(b);
                scanAirship(neighbour);
            }
        }
    }

    public void moveAirship(BlockFace direction)
    {
        for (AirshipBlock block : blocks)
        {
            world.getBlockAt(block.x, block.y, block.z).setType(Material.AIR);
            block.shiftBlock(direction);
        }

        for (AirshipBlock block : blocks)
        {
            Block newBlock = world.getBlockAt(block.x, block.y, block.z);
            newBlock.setType(block.t);
            newBlock.setData(block.d);
        }
    }

    public void rotateAirship(TurnDirection turnDirection)
    {
        int maxX = blocks.peek().x;
        int maxZ = blocks.peek().z;
        int minX = maxX;
        int minZ = maxZ;

        for (AirshipBlock block : blocks)
        {
            if (block.x > maxX)
                maxX = block.x;
            if (block.x < minX)
                minX = block.x;
            if (block.z > maxZ)
                maxZ = block.z;
            if (block.z < minZ)
                minZ = block.z;
        }

        int originX = (maxX + minX) / 2;
        int originZ = (maxZ + minZ) / 2;

        for (AirshipBlock block : blocks)
        {
            world.getBlockAt(block.x, block.y, block.z).setType(Material.AIR);
            block.rotateBlock(turnDirection, originX, originZ);
        }

        for (AirshipBlock block : blocks)
        {
            Block newBlock = world.getBlockAt(block.x, block.y, block.z);
            newBlock.setType(block.t);
            newBlock.setData(block.d);
        }
        switch (currentDirection)
        {
            case NORTH:
                currentDirection = turnDirection == TurnDirection.LEFT ? BlockFace.WEST : BlockFace.EAST;
                break;
            case EAST:
                currentDirection = turnDirection == TurnDirection.LEFT ? BlockFace.NORTH : BlockFace.SOUTH;
                break;
            case SOUTH:
                currentDirection = turnDirection == TurnDirection.LEFT ? BlockFace.EAST : BlockFace.WEST;
                break;
            case WEST:
                currentDirection = turnDirection == TurnDirection.LEFT ? BlockFace.SOUTH : BlockFace.NORTH;
                break;
        }
    }

    private class AirshipBlock implements Comparable<AirshipBlock>
    {
        public int rotate90Reverse(Material type, int data)
        {

            switch (type)
            {
                case TORCH:
                case REDSTONE_TORCH_OFF:
                case REDSTONE_TORCH_ON:
                    switch (data)
                    {
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
                    switch (data)
                    {
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
                    switch (data & 0x7)
                    {
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
                    switch (data)
                    {
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
                    switch (withoutThrown)
                    {
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
                    if ((data & 0x8) != 0)
                    {
                        // door top halves contain no orientation information
                        break;
                    }

                case COCOA:
                case TRIPWIRE_HOOK:
                    int extra = data & ~0x3;
                    int withoutFlags = data & 0x3;
                    switch (withoutFlags)
                    {
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
                    switch (data)
                    {
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
                    switch (data & ~0x8)
                    {
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
                    switch (data)
                    {
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
                    if (data >= 4 && data <= 11)
                        data ^= 0xc;
                    break;

                case REDSTONE_COMPARATOR_OFF:
                case REDSTONE_COMPARATOR_ON:
                case DIODE_BLOCK_OFF:
                case DIODE_BLOCK_ON:
                    int dir = data & 0x03;
                    int delay = data - dir;
                    switch (dir)
                    {
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
                    switch (orientation)
                    {
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
                    switch (data & 0x7)
                    {
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
                    if (data >= 10)
                        return data;
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
                    switch (data)
                    {
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

        public int rotate90(Material type, int data)
        {
            switch (type)
            {
                case TORCH:
                case REDSTONE_TORCH_OFF:
                case REDSTONE_TORCH_ON:
                    switch (data)
                    {
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
                    switch (data)
                    {
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
                    switch (data & 0x7)
                    {
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
                    switch (data)
                    {
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
                    switch (withoutThrown)
                    {
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
                    if ((data & 0x8) != 0)
                    {
                        // door top halves contain no orientation information
                        break;
                    }

                case COCOA:
                case TRIPWIRE_HOOK:
                    int extra = data & ~0x3;
                    int withoutFlags = data & 0x3;
                    switch (withoutFlags)
                    {
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
                    switch (data)
                    {
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
                    switch (data & ~0x8)
                    {
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
                    switch (data)
                    {
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
                    if (data >= 4 && data <= 11)
                        data ^= 0xc;
                    break;

                case REDSTONE_COMPARATOR_ON:
                case REDSTONE_COMPARATOR_OFF:
                case DIODE_BLOCK_OFF:
                case DIODE_BLOCK_ON:
                    int dir = data & 0x03;
                    int delay = data - dir;
                    switch (dir)
                    {
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
                    switch (orientation)
                    {
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
                    switch (data & 0x7)
                    {
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
                    if (data >= 10)
                        return data;
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
                    switch (data)
                    {
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

        public int x;
        public int y;
        public int z;
        public byte d;
        public Material t;
        public List metadataValue;

        public AirshipBlock(Block b)
        {
            x = b.getX();
            y = b.getY();
            z = b.getZ();
            d = b.getData();
            t = b.getType();
            metadataValue = b.getMetadata("AIRSHIP_CONTROL");
        }

        @Override
        public boolean equals(Object other)
        {
            if (other instanceof AirshipBlock)
            {
                AirshipBlock otherBlock = (AirshipBlock) other;
                return otherBlock.x == x && otherBlock.y == y && otherBlock.z == z;
            }
            return false;
        }

        public void shiftBlock(BlockFace direction)
        {
            int xDelta = 0;
            int zDelta = 0;

            switch (direction)
            {
                case EAST:
                    xDelta = 1;
                    break;
                case WEST:
                    xDelta = -1;
                    break;
                case NORTH:
                    zDelta = -1;
                    break;
                case SOUTH:
                    zDelta = 1;
                    break;
            }

            x += xDelta;
            z += zDelta;
        }

        public void rotateBlock(TurnDirection turnDirection, int originX, int originZ)
        {

            int x = this.x - originX;
            int z = this.z - originZ;
            int x2;
            int z2;

            if (turnDirection == TurnDirection.RIGHT)
            {
                x2 = -z;
                z2 = x;
            }
            else
            {
                x2 = z;
                z2 = -x;
            }

            this.x = x2 + originX;
            this.z = z2 + originZ;
            this.d = (byte) (turnDirection == TurnDirection.RIGHT ? rotate90(this.t, this.d) : rotate90Reverse(this.t, this.d));
        }

        @Override
        public int compareTo(AirshipBlock o)
        {
            if (o.x < this.x)
                return -1;
            else if (o.x == this.x)
                return 0;
            else
                return 1;
        }
    }
}