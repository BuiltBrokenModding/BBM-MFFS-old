package com.builtbroken.mffs.api.fortron;

import com.builtbroken.mc.lib.transform.region.Cube;
import com.builtbroken.mc.lib.transform.vector.Pos;
import net.minecraft.world.World;

import java.util.List;

public interface IFrequencyGrid
{
    void add(IBlockFrequency tileEntity);

    void remove(IBlockFrequency tileEntity);

    List<IBlockFrequency> getNodes();

    <C extends IBlockFrequency> List<C> getNodes(Class<C> clazz);

    /**
     * Gets a list of TileEntities that has a specific frequency.
     */
    List<IBlockFrequency> getNodes(int frequency);

    <C extends IBlockFrequency> List<C> getNodes(Class<C> clazz, int frequency);

    /**
     * Gets a list of TileEntities that has a specific frequency, within a radius around a position.
     */
    List<IBlockFrequency> getNodes(World world, Pos position, int radius, int frequency);

    <C extends IBlockFrequency> List<C> getNodes(Class<C> clazz, World world, Pos position, int radius, int frequency);

    List<IBlockFrequency> getNodes(World world, Cube region, int frequency);

    <C extends IBlockFrequency> List<C> getNodes(Class<C> clazz, World world, Cube region, int frequency);
}