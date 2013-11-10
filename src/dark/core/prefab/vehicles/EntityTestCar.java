package dark.core.prefab.vehicles;

import dark.core.common.CoreRecipeLoader;
import net.minecraft.world.World;

public class EntityTestCar extends EntityDrivable
{

    public EntityTestCar(World world)
    {
        super(world);       
    }

    public EntityTestCar(World world, float xx, float yy, float zz)
    {
        super(world, xx, yy, zz);
    }

    @Override
    public void updateClients()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void dropAsItem()
    {
        this.dropItemWithOffset(CoreRecipeLoader.itemVehicleTest.itemID, 1, 0.0F);
    }

}
