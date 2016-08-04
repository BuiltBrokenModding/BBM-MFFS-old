package mffs.field.module;

import com.builtbroken.mc.lib.transform.vector.Pos;
import mffs.api.machine.IFieldMatrix;
import mffs.base.ItemModule;
import net.minecraft.tileentity.TileEntity;

import java.util.List;
import java.util.stream.Collectors;


public class ItemModuleDome extends ItemModule
{
    public ItemModuleDome()
    {
        setMaxStackSize(1);
    }

    @Override
    public void onPostCalculate(IFieldMatrix projector, List<Pos> fieldBlocks)
    {
        Pos absoluteTranslation = new Pos((TileEntity) projector).add(projector.getTranslation());
        List<Pos> newField = fieldBlocks.stream().filter(v -> v.y() > absoluteTranslation.y()).collect(Collectors.toList());
        fieldBlocks.clear();
        fieldBlocks.addAll(newField);
    }
}