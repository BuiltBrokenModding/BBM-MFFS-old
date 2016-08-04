package mffs.security.card;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mffs.ModularForceFieldSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import javax.vecmath.Vector2d;

/**
 * All thanks to Briman for the id card face rendering! Check out the mod MineForver!
 *
 * @author Briman, Calclavia
 */
@SideOnly(Side.CLIENT)
public class RenderIDCard implements IItemRenderer {

    /**
     * Checks if this renderer should handle a specific item's render type
     *
     * @param item The item we are trying to render
     * @param type A render type to check if this renderer handles
     * @return true if this renderer should handle the given render type,
     * otherwise false
     */
    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return true;
    }

    /**
     * Checks if certain helper functionality should be executed for this renderer.
     * See ItemRendererHelper for more info
     *
     * @param type   The render type
     * @param item   The ItemStack being rendered
     * @param helper The type of helper functionality to be ran
     * @return True to run the helper functionality, false to not.
     */
    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return false;
    }

    /**
     * Called to do the actual rendering, see ItemRenderType for details on when specific
     * types are run, and what extra data is passed into the data parameter.
     *
     * @param renderType The render type
     * @param itemStack  The ItemStack being rendered
     * @param data       Extra Type specific data
     */
    @Override
    public void renderItem(ItemRenderType renderType, ItemStack itemStack, Object... data) {
        if (itemStack.getItem() instanceof ItemCardIdentification) {
            ItemCardIdentification card = (ItemCardIdentification) itemStack.getItem();
            GL11.glPushMatrix();
            GL11.glDisable(GL11.GL_CULL_FACE);
            transform(renderType);
            renderItemIcon(ModularForceFieldSystem.cardID.getIcon(itemStack, 0));

            if (renderType != ItemRenderType.INVENTORY) {
                GL11.glTranslatef(0f, 0f, -0.0005f);
            }
            renderPlayerFace(getSkinFace(card.getAccess(itemStack).getName()));
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glPopMatrix();
        }
    }

    private void transform(ItemRenderType renderType) {
        float scale = 0.0625f;
        if (renderType != ItemRenderType.INVENTORY) {
            GL11.glScalef(scale, -scale, -scale);
            GL11.glTranslatef(20f, -16f, 0f);
            GL11.glRotatef(180f, 1f, 1f, 0f);
            GL11.glRotatef(-90f, 0f, 0f, 1f);
        }
        if (renderType == ItemRenderType.ENTITY) {
            GL11.glTranslatef(20f, 0f, 0f);
            GL11.glRotatef(Minecraft.getSystemTime() / 12f % 360f, 0f, 1f, 0f);
            GL11.glTranslatef(-8f, 0f, 0f);
            GL11.glTranslated(0.0, 2.0 * Math.sin(Minecraft.getSystemTime() / 512.0 % 360.0), 0.0);
        }
    }

    private ResourceLocation getSkinFace(String username) {
        try {
            //ResourceLocation resourceLocation = Minecraft.getMinecraft().thePlayer.getLocationSkin();

            if (username != null && !username.isEmpty()) {
                ResourceLocation resourceLocation = AbstractClientPlayer.getLocationSkin(username);
                AbstractClientPlayer.getDownloadImageSkin(resourceLocation, username);
                return resourceLocation;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void renderPlayerFace(ResourceLocation resourcelocation) {
        if (resourcelocation != null) {
            Vector2d translation = new Vector2d(9, 5);
            int xSize = 4;
            int ySize = 4;
            int topLX = (int) translation.x;
            int topRX = (int) translation.x + xSize;
            int botLX = (int) translation.x;
            int botRX = (int) translation.x + xSize;
            int topLY = (int) translation.y;
            int topRY = (int) translation.y;
            int botLY = (int) translation.y + ySize;
            int botRY = (int) translation.y + ySize;
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(resourcelocation);
            GL11.glColor4f(1, 1, 1, 1);
            GL11.glBegin(GL11.GL_QUADS);

            GL11.glTexCoord2f(1f / 8f, 1f / 4f);
            GL11.glVertex2f(topLX, topLY);
            GL11.glTexCoord2f(1f / 8f, 2f / 4f);
            GL11.glVertex2f(botLX, botLY);
            GL11.glTexCoord2f(2f / 8f, 2f / 4f);
            GL11.glVertex2f(botRX, botRY);
            GL11.glTexCoord2f(2f / 8f, 1f / 4f);
            GL11.glVertex2f(topRX, topRY);

            GL11.glEnd();
            GL11.glBegin(GL11.GL_QUADS);

            GL11.glTexCoord2f(5f / 8f, 1f / 4f);
            GL11.glVertex2f(topLX, topLY);
            GL11.glTexCoord2f(5f / 8f, 2f / 4f);
            GL11.glVertex2f(botLX, botLY);
            GL11.glTexCoord2f(6f / 8f, 2f / 4f);
            GL11.glVertex2f(botRX, botRY);
            GL11.glTexCoord2f(6f / 8f, 1f / 4f);
            GL11.glVertex2f(topRX, topRY);

            GL11.glEnd();
        }
    }

    private void renderItemIcon(IIcon icon) {
        GL11.glBegin(GL11.GL_QUADS);

        GL11.glTexCoord2f(icon.getMinU(), icon.getMinV());
        GL11.glVertex2f(0, 0);
        GL11.glTexCoord2f(icon.getMinU(), icon.getMaxV());
        GL11.glVertex2f(0, 16);
        GL11.glTexCoord2f(icon.getMaxU(), icon.getMaxV());
        GL11.glVertex2f(16, 16);
        GL11.glTexCoord2f(icon.getMaxU(), icon.getMinV());
        GL11.glVertex2f(16, 0);

        GL11.glEnd();
    }

    private void renderItem3D(EntityLiving par1EntityLiving, ItemStack par2ItemStack, int par3) {
        IIcon icon = par1EntityLiving.getItemIcon(par2ItemStack, par3);
        if (icon == null) {
            GL11.glPopMatrix();
            return;
        }

        Tessellator tessellator = Tessellator.instance;
        float f = icon.getMinU();
        float f1 = icon.getMaxU();
        float f2 = icon.getMinV();
        float f3 = icon.getMaxV();
        float f4 = 0.0F;
        float f5 = 0.3F;
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glTranslatef(-f4, -f5, 0.0F);
        float f6 = 1.5F;
        GL11.glScalef(f6, f6, f6);
        GL11.glRotatef(50.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(335.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef(-0.9375F, -0.0625F, 0.0F);
        ItemRenderer.renderItemIn2D(tessellator, f1, f2, f, f3, icon.getIconWidth(), icon.getIconHeight(), 0.0625F);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
    }
}