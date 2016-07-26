package mffs.production;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mffs.Reference;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
final class RenderFortronCapacitor {
    static ResourceLocation textureOn = new ResourceLocation(Reference.domain, Reference.modelPath + "fortronCapacitor_on.png");
    static ResourceLocation textureOff = new ResourceLocation(Reference.domain, Reference.modelPath + "fortronCapacitor_off.png");
    static IModelCustom model = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "fortronCapacitor.tcn"));

    static void render(TileFortronCapacitor tileEntity, double x, double y, double z, float frame, boolean isActive, boolean isItem) {
        FMLClientHandler.instance().getClient().renderEngine.bindTexture(isActive ? textureOn : textureOff);

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 0.5 + 0.3 / 2, z + 0.5);
        GL11.glScalef(1.3f, 1.3f, 1.3f);
        model.renderAll();
        GL11.glPopMatrix();
    }
}