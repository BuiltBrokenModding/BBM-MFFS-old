package mffs.field.mode;

import com.builtbroken.jlib.type.Pair;
import com.builtbroken.mc.lib.helper.LanguageUtility;
import com.builtbroken.mc.lib.helper.NBTUtility;
import com.builtbroken.mc.lib.transform.vector.Pos;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mffs.ModularForceFieldSystem;
import mffs.Settings;
import mffs.api.machine.IFieldMatrix;
import mffs.api.machine.IProjector;
import mffs.api.modules.IProjectorMode;
import mffs.util.TCache;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemModeCustom extends Item implements TCache, IProjectorMode
{
    private final String NBT_ID = "id";
    private final String NBT_MODE = "mode";
    private final String NBT_POINT_1 = "point1";
    private final String NBT_POINT_2 = "point2";
    private final String NBT_FIELD_BLOCK_LIST = "fieldPoints";
    private final String NBT_FIELD_BLOCK_NAME = "blockID";
    private final String NBT_FIELD_BLOCK_METADATA = "blockMetadata";
    private final String NBT_FIELD_SIZE = "fieldSize";
    private final String NBT_FILE_SAVE_PREFIX = "custom_mode_";

    private Map<String, Object> cache = new HashMap();

    public ItemModeCustom()
    {
        this.setMaxStackSize(1);
    }

    @Override
    public Map<String, Object> cache()
    {
        return cache;
    }

    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer par2EntityPlayer, List list, boolean par4)
    {
        NBTTagCompound nbt = NBTUtility.getNBTTagCompound(itemStack);
        list.add(LanguageUtility.getLocal("info.modeCustom.mode") + " " + (nbt.getBoolean(NBT_MODE) ? LanguageUtility.getLocal("info.modeCustom.substraction") : LanguageUtility.getLocal("info.modeCustom.additive")));
        Pos point1 = new Pos(nbt.getCompoundTag(NBT_POINT_1));
        list.add(LanguageUtility.getLocal("info.modeCustom.point1") + " " + point1.xi() + ", " + point1.yi() + ", " + point1.zi());
        Pos point2 = new Pos(nbt.getCompoundTag(NBT_POINT_2));
        list.add(LanguageUtility.getLocal("info.modeCustom.point2") + " " + point2.xi() + ", " + point2.yi() + ", " + point2.zi());
        int modeID = nbt.getInteger(NBT_ID);
        if (modeID > 0)
        {
            list.add(LanguageUtility.getLocal("info.modeCustom.modeID") + " " + modeID);
            int fieldSize = nbt.getInteger(NBT_FIELD_SIZE);
            if (fieldSize > 0)
            {
                list.add(LanguageUtility.getLocal("info.modeCustom.fieldSize") + " " + fieldSize);
            }
            else
            {
                list.add(LanguageUtility.getLocal("info.modeCustom.notSaved"));
            }
        }
        if (GuiScreen.isShiftKeyDown())
        {
            super.addInformation(itemStack, par2EntityPlayer, list, par4);
        }
        else
        {
            list.add(LanguageUtility.getLocal("info.modeCustom.shift"));
        }
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer entityPlayer)
    {
        if (!world.isRemote)
        {
            if (entityPlayer.isSneaking())
            {
                NBTTagCompound nbt = NBTUtility.getNBTTagCompound(itemStack);
                if (nbt != null)
                {
                    Pos point1 = new Pos(nbt.getCompoundTag(NBT_POINT_1));
                    Pos point2 = new Pos(nbt.getCompoundTag(NBT_POINT_2));

                    if (nbt.hasKey(NBT_POINT_1) && nbt.hasKey(NBT_POINT_2) && !(point1 == point2))
                    {
                        if (point1.distance(point2) < Settings.maxForceFieldScale)
                        {
                            nbt.removeTag(NBT_POINT_1);
                            nbt.removeTag(NBT_POINT_2);
                            Pos midPoint = point1.midPoint(point2).floor();
                            point1 = point1.sub(midPoint);
                            point2 = point2.sub(midPoint);
                            Pos minPoint = point1.min(point2);
                            Pos maxPoint = point1.max(point2);

                            NBTTagCompound saveNBT = NBTUtility.loadData(getSaveDirectory(), NBT_FILE_SAVE_PREFIX + getModeID(itemStack));

                            if (saveNBT == null)
                            {
                                saveNBT = new NBTTagCompound();
                            }

                            NBTTagList list = null;
                            if (saveNBT.hasKey(NBT_FIELD_BLOCK_LIST))
                            {
                                list = saveNBT.getTagList(NBT_FIELD_BLOCK_LIST, 10);
                            }
                            else
                            {
                                list = new NBTTagList();
                            }

                            for (int x = minPoint.xi(); x <= maxPoint.xi(); x++)
                            {
                                for (int y = minPoint.yi(); y <= maxPoint.yi(); y++)
                                {
                                    for (int z = minPoint.zi(); z <= maxPoint.zi(); z++)
                                    {
                                        Pos position = new Pos(x, y, z);
                                        Pos targetCheck = midPoint.add(position);
                                        Block block = targetCheck.getBlock(world);

                                        if (!block.isAir(world, targetCheck.xi(), targetCheck.yi(), targetCheck.zi()))
                                        {
                                            /**
                                             * Additive and Subtractive modes
                                             */
                                            if (!nbt.getBoolean(NBT_MODE))
                                            {
                                                NBTTagCompound vectorTag = position.toNBT();
                                                vectorTag.setString(NBT_FIELD_BLOCK_NAME, Block.blockRegistry.getNameForObject(block));
                                                vectorTag.setInteger(NBT_FIELD_BLOCK_METADATA, targetCheck.getBlockMetadata(world));
                                                list.appendTag(vectorTag);
                                            }
                                            else
                                            {
                                                for (int i = 0; i < list.tagCount(); i++)
                                                {
                                                    if (new Pos(list.getCompoundTagAt(i)).equals(position))
                                                    {
                                                        list.removeTag(i);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            saveNBT.setTag(NBT_FIELD_BLOCK_LIST, list);
                            nbt.setInteger(NBT_FIELD_SIZE, list.tagCount());
                            NBTUtility.saveData(getSaveDirectory(), NBT_FILE_SAVE_PREFIX + getModeID(itemStack), saveNBT);
                            clearCache();
                            entityPlayer.addChatMessage(new ChatComponentText(LanguageUtility.getLocal("message.modeCustom.saved")));
                        }
                    }
                }
            }
            else
            {
                NBTTagCompound nbt = NBTUtility.getNBTTagCompound(itemStack);

                if (nbt != null)
                {
                    nbt.setBoolean(NBT_MODE, !nbt.getBoolean(NBT_MODE));
                    entityPlayer.addChatMessage(new ChatComponentText(LanguageUtility.getLocal("message.modeCustom.modeChange").replaceAll("#p", (nbt.getBoolean(NBT_MODE) ? LanguageUtility.getLocal("info.modeCustom.substraction") : LanguageUtility.getLocal("info.modeCustom.additive")))));
                }
            }
        }
        return itemStack;
    }

    @Override
    public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int par7, float par8, float par9, float par10)
    {
        if (!world.isRemote)
        {
            NBTTagCompound nbt = NBTUtility.getNBTTagCompound(itemStack);
            if (nbt != null)
            {
                Pos point1 = new Pos(nbt.getCompoundTag(NBT_POINT_1));
                if (!nbt.hasKey(NBT_POINT_1) || (point1 == new Pos(0, 0, 0)))
                {
                    nbt.setTag(NBT_POINT_1, new Pos(x, y, z).toNBT());
                    player.addChatMessage(new ChatComponentText("Set point 1: " + x + ", " + y + ", " + z + "."));
                }
                else
                {
                    nbt.setTag(NBT_POINT_2, new Pos(x, y, z).toNBT());
                    player.addChatMessage(new ChatComponentText("Set point 2: " + x + ", " + y + ", " + z + "."));
                }
            }
        }
        return true;
    }

    public Map<Pos, Pair<Block, Integer>> getFieldBlockMap(IFieldMatrix projector, ItemStack itemStack)
    {
        final String cacheID = "itemStack_" + itemStack.hashCode();
        //TODO use better key as this will almost never be the same key out side of one call set

        if (cacheExists(cacheID))
        {
            return (Map<Pos, Pair<Block, Integer>>) getCache(cacheID);
        }

        Map<Pos, Pair<Block, Integer>> fieldMap = getFieldBlockMapClean(projector, itemStack);

        putCache(cacheID, fieldMap);

        return fieldMap;
    }

    @Override
    public List<Pos> getInteriorPoints(ItemStack stack, IFieldMatrix projector)
    {
        return this.getExteriorPoints(stack, projector);
    }

    @Override
    public List<Pos> getExteriorPoints(ItemStack stack, IFieldMatrix projector)
    {
        return this.getFieldBlocks(projector, projector.getModeStack());
    }

    public List<Pos> getFieldBlocks(IFieldMatrix projector, ItemStack itemStack)
    {
        return getFieldBlockMapClean(projector, itemStack).keySet().stream().collect(Collectors.toList());
    }

    public Map<Pos, Pair<Block, Integer>> getFieldBlockMapClean(IFieldMatrix projector, ItemStack itemStack)
    {
        //TODO replace map with data object that uses an int array and get methods (north(), south(), etc)
        float scale = (projector.getModuleCount(ModularForceFieldSystem.moduleScale) / 3f + 1);
        Map<Pos, Pair<Block, Integer>> fieldBlocks = new HashMap();

        if (getSaveDirectory() != null)
        {
            NBTTagCompound nbt = NBTUtility.loadData(this.getSaveDirectory(), NBT_FILE_SAVE_PREFIX + getModeID(itemStack));

            if (nbt != null)
            {
                NBTTagList nbtTagList = nbt.getTagList(NBT_FIELD_BLOCK_LIST, 10);

                for (int i = 0; i < nbtTagList.tagCount(); i++)
                {
                    NBTTagCompound vectorTag = nbtTagList.getCompoundTagAt(i);
                    fieldBlocks.put(new Pos(vectorTag).multiply(scale), new Pair((Block.blockRegistry.getObject(vectorTag.getString(NBT_FIELD_BLOCK_NAME))), vectorTag.getInteger(NBT_FIELD_BLOCK_METADATA)));
                }
            }
        }
        return fieldBlocks;
    }

    public int getModeID(ItemStack itemStack)
    {
        NBTTagCompound nbt = NBTUtility.getNBTTagCompound(itemStack);
        int id = nbt.getInteger(NBT_ID);
        if (id <= 0)
        {
            nbt.setInteger(NBT_ID, getNextAvaliableID());
            id = nbt.getInteger(NBT_ID);
        }
        return id;
    }

    public int getNextAvaliableID()
    {
        //TODO find a better way as this is prone to issues
        //TODO use time stamp
        return 1 + this.getSaveDirectory().listFiles().length;
    }

    public File getSaveDirectory()
    {
        File saveDirectory = NBTUtility.getSaveDirectory(MinecraftServer.getServer().getFolderName());
        if (!saveDirectory.exists())
        {
            saveDirectory.mkdir();
        }
        File file = new File(saveDirectory, "mffs");
        if (!file.exists())
        {
            file.mkdir();
        }
        return file;
    }

    @Override
    public boolean isInField(ItemStack stack, IFieldMatrix projector, Pos position)
    {
        return false; //TODO implement?
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render(ItemStack stack, IProjector projector, double x, double y, double z, float f, long ticks)
    {
        //modes[(((TileEntity) projector).getWorldObj().rand.nextInt(modes.length - 1))].render(projector, x, y, z, f, ticks);
    }

    @Override
    public float getFortronCost(ItemStack stack, float amplifier)
    {
        return 8 * amplifier;
    }
}