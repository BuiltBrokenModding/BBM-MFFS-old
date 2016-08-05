package mffs;

import com.builtbroken.mc.lib.mod.AbstractProxy;
import com.builtbroken.mc.lib.transform.vector.Pos;
import com.mojang.authlib.GameProfile;
import cpw.mods.fml.common.FMLCommonHandler;
import mffs.render.FieldColor;
import mffs.render.fx.IEffectController;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

import java.awt.*;

public class CommonProxy extends AbstractProxy
{
    //case 1 => return new ContainerFrequency(player, player.getCurrentEquippedItem)
    //case 2 => return new ContainerItem(player, player.getCurrentEquippedItem)


    public World getClientWorld()
    {
        return null;
    }

    /**
     * Checks if the player is an operator.
     */
    public boolean isOp(GameProfile profile)
    {
        MinecraftServer theServer = FMLCommonHandler.instance().getMinecraftServerInstance();

        if (theServer != null)
        {
            return theServer.getConfigurationManager().func_152596_g(profile);
        }
        return false;
    }

    public void renderBeam(World world, Pos position, Pos target, FieldColor color, int age)
    {
        renderBeam(world, position, target, new float[]{color.r / 255f, color.g / 255f, color.b / 255f}, age);
    }

    public void renderBeam(World world, Pos position, Pos target, Color color, int age)
    {
        renderBeam(world, position, target, new float[]{color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f}, age);
    }

    public void renderBeam(World world, Pos position, Pos target, float[] color, int age)
    {
    }

    public void renderHologram(World world, Pos position, float[] color, int age, Pos targetPosition)
    {
    }

    public void renderHologramOrbit(World world, Pos orbitCenter, float[] color, int age, float maxSpeed)
    {
    }

    public void renderHologramOrbit(IEffectController controller, World world, Pos orbitCenter, Pos position, float[] color, int age, float maxSpeed)
    {
    }

    public void renderHologramMoving(World world, Pos position, FieldColor color, int age)
    {
        renderHologramMoving(world, position, new float[]{color.r, color.g, color.b}, age);
    }

    public void renderHologramMoving(World world, Pos position, float[] color, int age)
    {
    }
}