package mffs.field.mobilize;

import com.builtbroken.mc.lib.render.RenderUtility;
import com.builtbroken.mc.lib.render.model.loader.EngineModelLoader;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mffs.Reference;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelCustom;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderForceMobilizer
{
    public static ResourceLocation textureOn = new ResourceLocation(Reference.domain, Reference.modelPath + "forceMobilizer_on.png");
    public static ResourceLocation textureOff = new ResourceLocation(Reference.domain, Reference.modelPath + "forceMobilizer_off.png");
    public static IModelCustom model = EngineModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "forceMobilizer.tcn"));

    public static void render(TileForceMobilizer tileEntity, double x, double y, double z, float frame, boolean isActive, boolean isItem)
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
            RenderUtility.rotateBlockBasedOnDirection(tileEntity.getDirection());
        }

        model.renderAll();
        GL11.glPopMatrix();
    }
}