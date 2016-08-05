package mffs.field.module;

import com.builtbroken.mc.lib.transform.vector.Pos;
import mffs.api.machine.IFieldMatrix;
import mffs.base.ItemModule;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemModuleArray extends ItemModule
{
    public void onPreCalculate(IFieldMatrix projector, List<Pos> fieldBlocks)
    {
        onPreCalculateInterior(projector, fieldBlocks, fieldBlocks);
    }

    public void onPreCalculateInterior(IFieldMatrix projector, List<Pos> exterior, List<Pos> interior)
    {
        List<Pos> originalField = interior.stream().collect(Collectors.toList());
        Map<ForgeDirection, Integer> longestDirectional = getDirectionWidthMap(exterior);

        for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
        {
            int copyAmount = projector.getSidedModuleCount(this, direction);

            int directionalDisplacement = Math.abs(longestDirectional.get(direction)) + Math.abs(longestDirectional.get(direction.getOpposite())) + 1;

            for (int i = 0; i < copyAmount; i++)
            {
                int directionalDisplacementScale = directionalDisplacement * (i + 1);

                originalField.forEach(originalFieldBlock ->
                {
                    Pos newFieldBlock = originalFieldBlock.clone().add(new Pos(direction).multiply(directionalDisplacementScale));
                    interior.add(newFieldBlock);
                });
            }
        }
    }

    public Map<ForgeDirection, Integer> getDirectionWidthMap(Collection<Pos> field)
    {
        //TODO replace with custom data object that uses int array
        Map<ForgeDirection, Integer> longestDirectional = new HashMap();

        longestDirectional.put(ForgeDirection.DOWN, 0);
        longestDirectional.put(ForgeDirection.UP, 0);
        longestDirectional.put(ForgeDirection.NORTH, 0);
        longestDirectional.put(ForgeDirection.SOUTH, 0);
        longestDirectional.put(ForgeDirection.WEST, 0);
        longestDirectional.put(ForgeDirection.EAST, 0);

        for (Pos fieldPosition : field)
        {
            if (fieldPosition.xi() > 0 && fieldPosition.xi() > longestDirectional.get(ForgeDirection.EAST))
            {
                longestDirectional.put(ForgeDirection.EAST, fieldPosition.xi());
            }
            else if (fieldPosition.xi() < 0 && fieldPosition.xi() < longestDirectional.get(ForgeDirection.WEST))
            {
                longestDirectional.put(ForgeDirection.WEST, fieldPosition.xi());
            }
            if (fieldPosition.yi() > 0 && fieldPosition.yi() > longestDirectional.get(ForgeDirection.UP))
            {
                longestDirectional.put(ForgeDirection.UP, fieldPosition.yi());
            }
            else if (fieldPosition.yi() < 0 && fieldPosition.yi() < longestDirectional.get(ForgeDirection.DOWN))
            {
                longestDirectional.put(ForgeDirection.DOWN, fieldPosition.yi());
            }
            if (fieldPosition.zi() > 0 && fieldPosition.zi() > longestDirectional.get(ForgeDirection.SOUTH))
            {
                longestDirectional.put(ForgeDirection.SOUTH, fieldPosition.zi());
            }
            else if (fieldPosition.zi() < 0 && fieldPosition.zi() < longestDirectional.get(ForgeDirection.NORTH))
            {
                longestDirectional.put(ForgeDirection.NORTH, fieldPosition.zi());
            }
        }
        return longestDirectional;
    }

    public float getFortronCost(float amplifier)
    {
        return super.getFortronCost(amplifier) + (super.getFortronCost(amplifier) * amplifier) / 100f;
    }
}