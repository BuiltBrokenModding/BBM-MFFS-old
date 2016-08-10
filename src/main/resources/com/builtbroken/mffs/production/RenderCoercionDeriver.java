package com.builtbroken.mffs.production;

import com.builtbroken.mc.lib.render.RenderUtility;
import com.builtbroken.mc.lib.render.model.loader.EngineModelLoader;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import com.builtbroken.mffs.Reference;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelCustom;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
final class RenderCoercionDeriver
{
    static ResourceLocation textureOn = new ResourceLocation(Reference.domain, Reference.modelPath + "coercionDeriver_on.png");
    static ResourceLocation textureOff = new ResourceLocation(Reference.domain, Reference.modelPath + "coercionDeriver_off.png");
    static IModelCustom model = EngineModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "coercionDeriver.tcn"));

    public static void render(TileCoercionDeriver tileEntity, double x, double y, double z, float frame, boolean isActive, boolean isItem)
    {
        FMLClientHandler.instance().getClient().renderEngine.bindTexture(isActive ? textureOn : textureOff);

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);
        model.renderAllExcept("crystal");

        GL11.glPushMatrix();
        GL11.glTranslated(0, (0.3 + Math.sin(Math.toRadians(tileEntity.animation)) * 0.08) * tileEntity.animationTween - 0.1, 0);
        GL11.glRotated(tileEntity.animation, 0, 1, 0);
        RenderUtility.enableBlending();
        model.renderOnly("crystal");
        RenderUtility.disableBlending();
        GL11.glPopMatrix();

        GL11.glPopMatrix();
    }
}