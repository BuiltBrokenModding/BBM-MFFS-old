package com.builtbroken.mffs.field;

import com.builtbroken.mc.lib.render.RenderUtility;
import com.builtbroken.mc.lib.render.model.loader.EngineModelLoader;
import cpw.mods.fml.client.FMLClientHandler;
import com.builtbroken.mffs.Reference;
import com.builtbroken.mffs.Settings;
import com.builtbroken.mffs.render.FieldColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelCustom;
import org.lwjgl.opengl.GL11;

public final class RenderElectromagneticProjector
{
    private static ResourceLocation textureOn = new ResourceLocation(Reference.domain,
            Reference.modelPath + "electromagneticProjector_on.png");
    private static ResourceLocation textureOff = new ResourceLocation(Reference.domain,
            Reference.modelPath + "electromagneticProjector_off.png");

    private static IModelCustom model = EngineModelLoader
            .loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "electromagneticProjector.tcn"));

    public static void render(TileElectromagneticProjector tileEntity, double x, double y, double z, float frame, boolean isActive, boolean isItem)
    {
        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);

        if (isActive)
        {
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(textureOn);
        }
        else
        {
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(textureOff);
        }

        if (!isItem)
        {
            GL11.glRotatef(-90, 0, 1, 0);
            RenderUtility.rotateBlockBasedOnDirection(tileEntity.getDirection());
        }

        model.renderAll();
        //.render(tileEntity.animation, 0.0625F)
        GL11.glPopMatrix();

        if (tileEntity.getMode() != null)
        {
            Tessellator tessellator = Tessellator.instance;
            RenderHelper.disableStandardItemLighting();
            GL11.glPushMatrix();
            GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);
            double xDifference = Minecraft.getMinecraft().thePlayer.posX - (tileEntity.xCoord + 0.5);
            double zDifference = Minecraft.getMinecraft().thePlayer.posZ - (tileEntity.zCoord + 0.5);
            float rotatation = (float) Math.toDegrees(Math.atan2(zDifference, xDifference));
            GL11.glRotatef(-rotatation + 27, 0.0F, 1.0F, 0.0F);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glDepthMask(false);
            GL11.glPushMatrix();
            tessellator.startDrawing(6);
            float height = 2, width = 2;
            tessellator.setColorRGBA(72, 198, 255, 255);
            tessellator.addVertex(0.0D, 0.0D, 0.0D);
            tessellator.setColorRGBA_I(0, 0);
            tessellator.addVertex(-0.866D * width, height, -0.5F * width);
            tessellator.addVertex(0.866D * width, height, -0.5F * width);
            tessellator.addVertex(0.0D, height, 1.0F * width);
            tessellator.addVertex(-0.866D * width, height, -0.5F * width);
            tessellator.draw();
            GL11.glPopMatrix();
            GL11.glDepthMask(true);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glShadeModel(GL11.GL_FLAT);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            RenderHelper.enableStandardItemLighting();
            GL11.glPopMatrix();

            /**
             * Render hologram
             */
            if (Settings.highGraphics)
            {
                GL11.glPushMatrix();
                GL11.glTranslated(x + 0.5, y + 1.35, z + 0.5);

                FieldColor color = isActive ? FieldColor.BLUE : FieldColor.RED;
                FMLClientHandler.instance().getClient().renderEngine.bindTexture(Reference.hologramTexture);

                RenderUtility.enableBlending();
                RenderUtility.disableLighting();
                GL11.glPushMatrix();
                GL11.glColor4d(color.r, color.g, color.b, Math.sin(tileEntity.getTicks() / 10.0) / 2 + 0.8);
                GL11.glTranslatef(0, (float) (Math.sin(Math.toRadians(tileEntity.getTicks() * 3)) / 7.0), 0);
                GL11.glRotatef(tileEntity.getTicks() * 4, 0, 1, 0);
                GL11.glRotatef(36f + tileEntity.getTicks() * 4, 0, 1, 1);
                tileEntity.getMode().render(tileEntity.getModeStack(), tileEntity, x, y, z, frame, tileEntity.getTicks());
                GL11.glPopMatrix();
                RenderUtility.enableLighting();
                RenderUtility.disableBlending();
                GL11.glPopMatrix();
            }
        }

    }
}