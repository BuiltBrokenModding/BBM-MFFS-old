package com.builtbroken.mffs;

import com.builtbroken.mc.core.asm.ChunkSetBlockEvent;
import com.builtbroken.mc.lib.transform.vector.Pos;
import com.mojang.authlib.GameProfile;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import com.builtbroken.mffs.api.event.EventForceMobilize;
import com.builtbroken.mffs.api.event.EventStabilize;
import com.builtbroken.mffs.api.fortron.FrequencyGrid;
import com.builtbroken.mffs.base.TileFortron;
import com.builtbroken.mffs.field.TileElectromagneticProjector;
import com.builtbroken.mffs.util.FortronUtility;
import com.builtbroken.mffs.util.MFFSUtility;
import net.minecraft.block.BlockSkull;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemSkull;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.List;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/19/2016.
 */
public class SubscribeEventHandler
{
    public static HashMap<String, IIcon> fluidIconMap = new HashMap();

    public void registerIcon(String name, TextureStitchEvent.Pre event)
    {
        fluidIconMap.put(name, event.map.registerIcon(name));
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void preTextureHook(TextureStitchEvent.Pre event)
    {
        if (event.map.getTextureType() == 0)
        {
            registerIcon(Reference.prefix + "fortron", event);
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void textureHook(TextureStitchEvent.Post event)
    {
        FortronUtility.fluidFortron.setIcons(fluidIconMap.get(Reference.prefix + "fortron"));
    }

    @SubscribeEvent
    public void eventPreForceManipulate(EventForceMobilize.EventPreForceManipulate evt)
    {
        //TODO not sure what this does?, might consider modifying as this is simply logic for an event system
        TileEntity tileEntity = evt.world.getTileEntity(evt.beforeX, evt.beforeY, evt.beforeZ);

        if (tileEntity instanceof TileFortron)
        {
            ((TileFortron) tileEntity).markSendFortron = false;
        }
    }

    /**
     * Special stabilization cases.
     *
     * @param evt
     */
    @SubscribeEvent
    public void eventStabilize(EventStabilize evt)
    {
        //TODO tbh this should be handled by a handler system to allow more blocks special cases, without using events
        if (evt.itemStack.getItem() instanceof ItemSkull)
        {
            evt.world.setBlock(evt.x, evt.y, evt.z, Blocks.skull, evt.itemStack.getItemDamage(), 2);
            TileEntity tile = evt.world.getTileEntity(evt.x, evt.y, evt.z);

            if (tile instanceof TileEntitySkull)
            {
                NBTTagCompound nbt = evt.itemStack.getTagCompound();

                if (nbt != null)
                {
                    GameProfile gameProfile = null;

                    if (nbt.hasKey("SkullOwner", 10))
                    {
                        gameProfile = NBTUtil.func_152459_a(nbt.getCompoundTag("SkullOwner"));
                    }
                    else if (nbt.hasKey("SkullOwner", 8) && nbt.getString("SkullOwner").length() > 0)
                    {
                        gameProfile = new GameProfile(null, nbt.getString("SkullOwner"));
                    }

                    if (gameProfile != null)
                    {
                        ((TileEntitySkull) tile).func_152106_a(gameProfile);
                    }
                    else
                    {
                        ((TileEntitySkull) tile).func_152107_a(evt.itemStack.getItemDamage());
                    }

                    ((BlockSkull) Blocks.skull).func_149965_a(evt.world, evt.x, evt.y, evt.z, (TileEntitySkull) tile);
                }
            }

            evt.itemStack.stackSize -= 1;
            evt.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void playerInteractEvent(PlayerInteractEvent evt)
    {
        //TODO ensure this runs on both sides to block client side data from ending up on the server
        // Cancel if we click on a force field.
        if (evt.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK && evt.entityPlayer.worldObj.getBlock(evt.x, evt.y, evt.z) == ModularForceFieldSystem.forceField)
        {
            evt.setCanceled(true);
            return;
        }

        // Only check non-creative players
        if (evt.entityPlayer.capabilities.isCreativeMode)
        {
            return;
        }

        Pos position = new Pos(evt.x, evt.y, evt.z);

        List<TileElectromagneticProjector> relevantProjectors = MFFSUtility.getRelevantProjectors(evt.entityPlayer.worldObj, position);

        //Check if we can sync this block (activate). If not, we cancel the event.
        for (TileElectromagneticProjector projector : relevantProjectors)
        {
            if (!projector.isAccessGranted(evt.entityPlayer.worldObj, new Pos(evt.x, evt.y, evt.z), evt.entityPlayer, evt.action))
            {
                //Check if player has permission
                evt.entityPlayer.addChatMessage(new ChatComponentText("[" + Reference.name + "] You have no permission to do that!")); //TODO add translation
                evt.setCanceled(true);
            }
        }
    }

    /**
     * When a block breaks, mark force field projectors for an update.
     */
    @SubscribeEvent
    public void chunkModifyEvent(ChunkSetBlockEvent event)
    {
        if (!event.world.isRemote && event.block == Blocks.air)
        {
            Pos vec = new Pos(event.x, event.y, event.z);

            List<TileElectromagneticProjector> projectorSet = FrequencyGrid.instance().getNodes(TileElectromagneticProjector.class);
            for (TileElectromagneticProjector projector : projectorSet)
            {
                if (projector.world() == event.world && projector.getCalculatedField() != null && projector.getCalculatedField().contains(vec))
                {
                    projector.markFieldUpdate = true;
                }
            }
        }
    }

    @SubscribeEvent
    public void livingSpawnEvent(LivingSpawnEvent evt)
    {
        if (!(evt.entity instanceof EntityPlayer))
        {
            List<TileElectromagneticProjector> set = MFFSUtility.getRelevantProjectors(evt.world, new Pos(evt.entityLiving));
            for (TileElectromagneticProjector projector : set)
            {
                if (projector.getModuleCount(ModularForceFieldSystem.moduleAntiSpawn) > 0)
                {
                    evt.setResult(Event.Result.DENY);
                }
            }
        }
    }
}
