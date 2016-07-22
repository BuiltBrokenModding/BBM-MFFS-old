package mffs.render.fx;

import com.builtbroken.mc.lib.render.RenderUtility;
import com.builtbroken.mc.lib.transform.rotation.EulerAngle;
import com.builtbroken.mc.lib.transform.vector.Pos;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mffs.ModularForceFieldSystem;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

/**
 * @author Calclavia
 */
@SideOnly(Side.CLIENT)
public class FXHologramOrbit extends FXHologram
{
    private double rotation = 0;
    private double maxSpeed = 0;
    private Pos orbitPosition;

    public FXHologramOrbit(World world, Pos orbitPosition, Pos position, float red, float green, float blue, int age, float maxSpeed)
    {
        super(world, position, red, green, blue, age);
        this.orbitPosition = orbitPosition;
        this.maxSpeed = maxSpeed;
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        double xDifference = this.posX - orbitPosition.x();
        double yDifference = this.posY - orbitPosition.y();
        double zDifference = this.posZ - orbitPosition.z();
        double speed = this.maxSpeed * ((float) this.particleAge / (float) this.particleMaxAge);
        Pos originalPosition = new Pos(this);
        Pos relativePosition = originalPosition.clone().subtract(this.orbitPosition);
        relativePosition.transform(new EulerAngle(speed, 0, 0));
        Pos newPosition = this.orbitPosition.clone().add(relativePosition);
        this.rotation += speed;
        this.moveEntity(newPosition.x() - originalPosition.x(), newPosition.y() - originalPosition.y(), newPosition.z() - originalPosition.z());
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
        GL11.glRotated(-this.rotation, 0, 1, 0);
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