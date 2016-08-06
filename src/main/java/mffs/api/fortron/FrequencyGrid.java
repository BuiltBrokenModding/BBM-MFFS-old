package mffs.api.fortron;

import com.builtbroken.mc.lib.transform.region.Cube;
import com.builtbroken.mc.lib.transform.vector.Pos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A grid MFFS uses to search for machines with frequencies that can be linked and spread Fortron
 * energy.
 *
 * @author Calclavia
 */
@Deprecated //TODO recode this mess, use RadioNetwork as a base
public class FrequencyGrid implements IFrequencyGrid
{
    //TODO make per world
    //TODO make per chunk for faster access
    private static IFrequencyGrid GRID = new FrequencyGrid();

    //TODO fix, we have two entries per tile (lock to server side only)
    public List<IBlockFrequency> tiles = new ArrayList(); //TODO time complexity is a bitch

    public static IFrequencyGrid instance()
    {
        return GRID;
    }

    @Override
    public void add(IBlockFrequency tileEntity)
    {
        if (tileEntity != null && !tileEntity.world().isRemote && !tiles.contains(tileEntity))
        {
            tiles.add(tileEntity);
        }
        //TODO return
    }

    @Override
    public void remove(IBlockFrequency tileEntity)
    {
        if (tiles.contains(tileEntity))
        {
            tiles.remove(tileEntity);
        }
        //TODO return
    }

    @Override
    public List<IBlockFrequency> getNodes()
    {
        return tiles;
    }

    @Override
    public <C extends IBlockFrequency> List<C> getNodes(Class<C> clazz)
    {
        //TODO unit test
        return tiles.stream().filter(t -> t.getClass().isAssignableFrom(clazz)).map(t -> (C) t).collect(Collectors.toList());
    }

    @Override
    public List<IBlockFrequency> getNodes(int frequency)
    {
        return tiles.stream().filter(t -> t.getFrequency() == frequency).collect(Collectors.toList());
    }

    @Override
    public <C extends IBlockFrequency> List<C> getNodes(Class<C> clazz, int frequency)
    {
        //TODO unit test
        return tiles.stream().filter(t -> t.getClass().isAssignableFrom(clazz) && t.getFrequency() == frequency).map(t -> (C) t).collect(Collectors.toList());
    }

    @Override
    public List<IBlockFrequency> getNodes(World world, Pos position, int radius, int frequency)
    {
        return tiles.stream().filter(block -> block.world() == world && position.distance(block.x(), block.y(), block.z()) <= radius && block.getFrequency() == frequency).collect(Collectors.toList());
    }

    @Override
    public <C extends IBlockFrequency> List<C> getNodes(Class<C> clazz, World world, Pos position, int radius, int frequency)
    {
        return tiles.stream().filter(block -> block.getClass().isAssignableFrom(clazz) && block.world() == world && position.distance(block.x(), block.y(), block.z()) <= radius && block.getFrequency() == frequency).map(block -> (C) block).collect(Collectors.toList());
    }

    @Override
    public List<IBlockFrequency> getNodes(World world, Cube region, int frequency)
    {
        return tiles.stream().filter(t -> t.world() == world && region.intersects(t.x(), t.y(), t.z()) && t.getFrequency() == frequency).collect(Collectors.toList());
    }

    @Override
    public <C extends IBlockFrequency> List<C> getNodes(Class<C> clazz, World world, Cube region, int frequency)
    {
        return tiles.stream().filter(t -> t.getClass().isAssignableFrom(clazz) && t.world() == world && region.intersects(t.x(), t.y(), t.z()) && t.getFrequency() == frequency).map(t -> (C) t).collect(Collectors.toList());
    }
}
