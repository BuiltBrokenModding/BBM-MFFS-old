package mffs.security;

import com.builtbroken.mc.lib.render.RenderUtility;
import com.builtbroken.mc.lib.transform.vector.Pos;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mffs.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderBiometricIdentifier
{
    public static ResourceLocation textureOn = new ResourceLocation(Reference.domain, Reference.modelPath + "biometricIdentifier_on.png");
    public static ResourceLocation textureOff = new ResourceLocation(Reference.domain, Reference.modelPath + "biometricIdentifier_off.png");
    public static IModelCustom model = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "biometricIdentifier.tcn"));

    public static void render(TileBiometricIdentifier tile, double x, double y, double z, float frame, boolean isActive, boolean isItem)
    {
        if (isActive)
        {
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(textureOn);
        }
        else
        {
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(textureOff);
        }

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);

        if (!isItem)
        {
            GL11.glRotatef(-90, 0, 1, 0);
            RenderUtility.rotateBlockBasedOnDirection(tile.getDirection());
        }

        model.renderAllExcept("holoScreen");

        if (!isItem)
        {
            /**
             * Simulate flicker and, hovering
             */
            long t = System.currentTimeMillis();

            MovingObjectPosition look = Minecraft.getMinecraft().thePlayer.rayTrace(8, 1);

            if (look != null && tile.toPos().equals(new Pos(look).floor()))
            {
                if (Math.random() > 0.05 || (tile.lastFlicker - t) > 200)
                {
                    GL11.glPushMatrix();
                    GL11.glTranslated(0, Math.sin(Math.toRadians(tile.animation)) * 0.05, 0);
                    RenderUtility.enableBlending();
                    model.renderOnly("holoScreen");
                    RenderUtility.disableBlending();
                    GL11.glPopMatrix();
                    tile.lastFlicker = t;
                }
            }
        }

        GL11.glPopMatrix();
    }
}