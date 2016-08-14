package com.builtbroken.mffs;

import com.builtbroken.mc.lib.transform.vector.Pos;
import com.mojang.authlib.GameProfile;
import cpw.mods.fml.client.FMLClientHandler;
import com.builtbroken.mffs.render.fx.*;
import com.builtbroken.mffs.security.card.RenderIDCard;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;

public class ClientProxy extends CommonProxy
{
    @Override
    public void init()
    {
        super.init();
        MinecraftForgeClient.registerItemRenderer(ModularForceFieldSystem.cardID, new RenderIDCard());
    }

    @Override
    public World getClientWorld()
    {
        return Minecraft.getMinecraft().theWorld;
    }

    //case 1 => return new GuiFrequency(player, player.getCurrentEquippedItem)

    @Override
    public boolean isOp(GameProfile profile)
    {
        return false;
    }

    @Override
    public void renderBeam(World world, Pos position, Pos target, float[] color, int age)
    {
        FMLClientHandler.instance().getClient().effectRenderer.addEffect(new FXFortronBeam(world, position, target, color[0], color[1], color[2], age));
    }

    @Override
    public void renderHologram(World world, Pos position, float[] color, int age, Pos targetPosition)
    {
        if (targetPosition != null)
        {
            FMLClientHandler.instance().getClient().effectRenderer.addEffect(new FXHologram(world, position, color[0], color[1], color[2], age).setTarget(targetPosition));
        }
        else
        {
            FMLClientHandler.instance().getClient().effectRenderer.addEffect(new FXHologram(world, position, color[0], color[1], color[2], age));
        }
    }

    @Override
    public void renderHologramOrbit(World world, Pos orbitCenter, float[] color, int age, float maxSpeed)
    {
        FMLClientHandler.instance().getClient().effectRenderer.addEffect(new FXHologramOrbit(world, orbitCenter, orbitCenter, color[0], color[1], color[2], age, maxSpeed));
    }

    @Override
    public void renderHologramOrbit(IEffectController controller, World world, Pos orbitCenter, Pos position, float[] color, int age, float maxSpeed)
    {
        FXHologramOrbit fx = new FXHologramOrbit(world, orbitCenter, position, color[0], color[1], color[2], age, maxSpeed);
        fx.setController(controller);
        FMLClientHandler.instance().getClient().effectRenderer.addEffect(fx);
    }

    @Override
    public void renderHologramMoving(World world, Pos position, float[] color, int age)
    {
        FMLClientHandler.instance().getClient().effectRenderer.addEffect(new FXHologramMoving(world, position, color[0], color[1], color[2], age));
    }
}