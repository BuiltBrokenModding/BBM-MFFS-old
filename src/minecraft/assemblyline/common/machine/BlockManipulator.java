package assemblyline.common.machine;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import universalelectricity.core.UniversalElectricity;
import assemblyline.client.render.BlockRenderingHandler;
import assemblyline.common.TabAssemblyLine;
import assemblyline.common.imprinter.prefab.BlockImprintable;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * A block that manipulates item movement between inventories.
 * 
 * @author Calclavia
 * 
 */
public class BlockManipulator extends BlockImprintable
{
	public BlockManipulator(int id)
	{
		super("manipulator", id, UniversalElectricity.machine, TabAssemblyLine.INSTANCE);
		this.setBlockBounds(0, 0, 0, 1, 0.29f, 1);
	}

	@Override
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World par1World, int par2, int par3, int par4)
	{
		return AxisAlignedBB.getAABBPool().getAABB((double) par2, (double) par3, (double) par4, (double) par2 + 1, (double) par3 + 1, (double) par4 + 1);
	}

	@Override
	public boolean onSneakMachineActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileEntityManipulator)
		{
			((TileEntityManipulator) tileEntity).setSelfPulse(!((TileEntityManipulator) tileEntity).isSelfPulse());
		}

		return true;
	}

	@Override
	public boolean onSneakUseWrench(World world, int x, int y, int z, EntityPlayer par5EntityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileEntityManipulator)
		{
			TileEntityManipulator manip = (TileEntityManipulator) tileEntity;
			boolean manipMode = manip.isOutput();
			boolean inverted = manip.isInverted();
			if (manipMode && !inverted)
			{
				manip.toggleInversion();
			}
			else if (manipMode && inverted)
			{
				manip.toggleOutput();
				manip.toggleInversion();
			}
			else if (!manipMode && !inverted)
			{
				manip.toggleInversion();
			}
			else
			{
				manip.toggleOutput();
				manip.toggleInversion();
			}
		}

		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World var1)
	{
		return new TileEntityManipulator();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getRenderType()
	{
		return BlockRenderingHandler.BLOCK_RENDER_ID;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public int damageDropped(int par1)
	{
		return 0;
	}
}
