package mffs.base;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/19/2016.
 */
public abstract class TileMFFSBlock extends TileMFFS
{
    @SideOnly(Side.CLIENT)
    protected static IIcon blockIconTop = null;
    @SideOnly(Side.CLIENT)
    protected static IIcon blockIconOn = null;
    @SideOnly(Side.CLIENT)
    protected static IIcon blockIconTopOn = null;

    public TileMFFSBlock(String name)
    {
        super(name);
        isOpaque = true;
        renderNormalBlock = true;
    }

    @Override
    public IIcon getIcon(int side)
    {
        if (isActive())
        {
            if (side == 0 || side == 1)
            {
                return blockIconTopOn;
            }
            return blockIconOn;
        }

        if (side == 0 || side == 1)
        {
            return blockIconTop;
        }

        return super.getIcon(side);
    }

    @Override
    public void registerIcons(IIconRegister iconRegister)
    {
        super.registerIcons(iconRegister);
        blockIconTop = iconRegister.registerIcon(this.getTextureName() + "_top");
        blockIconOn = iconRegister.registerIcon(this.getTextureName() + "_on");
        blockIconTopOn = iconRegister.registerIcon(this.getTextureName() + "_top_on");
    }
}
