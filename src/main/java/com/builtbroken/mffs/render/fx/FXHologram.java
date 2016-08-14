package com.builtbroken.mffs.render.fx;

import com.builtbroken.mc.lib.render.RenderUtility;
import com.builtbroken.mc.lib.transform.vector.Pos;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import com.builtbroken.mffs.ModularForceFieldSystem;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class FXHologram extends FXMFFS
{
    private Pos targetPosition = null;

    public FXHologram(World world, Pos position, float red, float green, float blue, int age)
    {
        super(world, position.x(), position.y(), position.z(), 0, 0, 0);
        this.setRBGColorF(red, green, blue);
        this.particleMaxAge = age;
        this.noClip = true;
    }


    /**
     * The target the hologram is going to translate to.
     *
     * @param targetPosition
     * @return
     */
    public FXHologram setTarget(Pos targetPosition)
    {
        this.targetPosition = targetPosition;
        this.motionX = (this.targetPosition.x() - this.posX) / this.particleMaxAge;
        this.motionY = (this.targetPosition.y() - this.posY) / this.particleMaxAge;
        this.motionZ = (this.targetPosition.z() - this.posZ) / this.particleMaxAge;
        return this;
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        particleAge += 1;

        if (particleAge - 1 >= this.particleMaxAge)
        {
            this.setDead();
            return;
        }
        if (this.targetPosition != null)
        {
            this.moveEntity(this.motionX, this.motionY, this.motionZ);
        }
    }

    @Override
    public void renderParticle(Tessellator tessellator, float f, float f1, float f2, float f3, float f4, float f5)
    {
        tessellator.draw();
        GL11.glPushMatrix();
        float xx = (float) (this.prevPosX + (this.posX - this.prevPosX) * f - EntityFX.interpPosX);
        float yy = (float) (this.prevPosY + (this.posY - this.prevPosY) * f - EntityFX.interpPosY);
        float zz = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * f - EntityFX.interpPosZ);
        GL11.glTranslated(xx, yy, zz);
        GL11.glScalef(1.01f, 1.01f, 1.01f);
        float op = 0.5f;
        if ((this.particleMaxAge - this.particleAge <= 4))
        {
            op = 0.5f - (5 - (this.particleMaxAge - this.particleAge)) * 0.1F;
        }
        GL11.glColor4d(this.particleRed, this.particleGreen, this.particleBlue, op * 2);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        RenderUtility.enableBlending();
        RenderUtility.setTerrainTexture();
        RenderUtility.renderNormalBlockAsItem(ModularForceFieldSystem.forceField, 0, new RenderBlocks());
        RenderUtility.disableBlending();
        GL11.glPopMatrix();
        tessellator.startDrawingQuads();
        FMLClientHandler.instance().getClient().renderEngine.bindTexture(RenderUtility.PARTICLE_RESOURCE);
    }
}