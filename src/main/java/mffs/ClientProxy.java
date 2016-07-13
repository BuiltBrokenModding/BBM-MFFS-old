package mffs;

import com.builtbroken.mc.lib.transform.vector.Pos;
import com.mojang.authlib.GameProfile;
import cpw.mods.fml.client.FMLClientHandler;
import mffs.render.fx.IEffectController;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;

class ClientProxy extends CommonProxy
{
    public void init()
    {
        super.init();
        MinecraftForgeClient.registerItemRenderer(ModularForceFieldSystem.cardID, new RenderIDCard());
    }

    public World getClientWorld()
    {
        return Minecraft.getMinecraft().theWorld;
    }


    //TODO move to IGuiTile system
    //case tile: TileFortronCapacitor => return new GuiFortronCapacitor(player, tile)
    //case tile: TileElectromagneticProjector => return new GuiElectromagneticProjector(player, tile)
    //case tile: TileCoercionDeriver => return new GuiCoercionDeriver(player, tile)
    //case tile: TileBiometricIdentifier => return new GuiBiometricIdentifier(player, tile)
    //case tile: TileForceMobilizer => return new GuiForceMobilizer(player, tile)

    //case 1 => return new GuiFrequency(player, player.getCurrentEquippedItem)

    @Override
    public boolean isOp(GameProfile profile)
    {
        return false;
    }

    @Override
    public void renderBeam(World world, Pos position, Pos target, float[] color, int age)
    {
        FMLClientHandler.instance().getClient().effectRenderer.addEffect(new FXFortronBeam(world, position, target, color[0], color[1], color[2], age))
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