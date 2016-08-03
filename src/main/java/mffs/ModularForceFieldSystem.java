package mffs;


import com.builtbroken.mc.core.registry.ModManager;
import com.builtbroken.mc.lib.mod.AbstractMod;
import com.builtbroken.mc.lib.mod.AbstractProxy;
import com.builtbroken.mc.lib.mod.ModCreativeTab;
import com.builtbroken.mc.lib.mod.loadable.LoadableHandler;
import com.mojang.authlib.GameProfile;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import mffs.api.Blacklist;
import mffs.base.ItemMFFS;
import mffs.base.ItemModule;
import mffs.field.TileElectromagneticProjector;
import mffs.field.TileForceField;
import mffs.field.mobilize.TileForceMobilizer;
import mffs.field.mode.*;
import mffs.field.module.*;
import mffs.item.ItemRemoteController;
import mffs.item.card.ItemCard;
import mffs.item.card.ItemCardFrequency;
import mffs.item.card.ItemCardLink;
import mffs.item.fortron.ItemCardInfinite;
import mffs.production.TileCoercionDeriver;
import mffs.production.TileFortronCapacitor;
import mffs.security.TileBiometricIdentifier;
import mffs.security.card.ItemCardAccess;
import mffs.security.card.ItemCardIdentification;
import mffs.security.module.*;
import mffs.util.FortronUtility;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidRegistry;

import java.awt.*;
import java.util.UUID;

@Mod(modid = Reference.id, name = Reference.name, version = Reference.version, dependencies = "required-after:ResonantEngine", modLanguage = "scala", guiFactory = "mffs.MFFSGuiFactory")
public class ModularForceFieldSystem extends AbstractMod
{
    /** Damage type used by forcefields */
    public static final DamageSource damageFieldShock = new DamageSource("fieldShock").setDamageBypassesArmor();
    /** Fake player data used by the mod */
    public final GameProfile fakeProfile = new GameProfile(UUID.randomUUID(), "mffs");

    @SidedProxy(clientSide = "mffs.ClientProxy", serverSide = "mffs.CommonProxy")
    public static CommonProxy proxy;

    @Mod.Instance(Reference.domain)
    public static ModularForceFieldSystem instance;

    //TODO once update is done prefix all item objects with item, ex itemItemName or itemSomeItem
    /**
     * Misc Items
     */
    public static Item remoteController;
    public static Item focusMatrix;

    /**
     * Cards
     */
    public static Item cardBlank;
    public static Item cardInfinite;
    public static Item cardFrequency;
    public static ItemCardAccess cardID;
    public static Item cardLink;

    /**
     * Modes
     */
    public static ItemMode modeCube;
    public static ItemMode modeSphere;
    public static ItemMode modeTube;
    public static ItemMode modeCylinder;
    public static ItemMode modePyramid;
    public static ItemMode modeCustom;
    public static ItemModule moduleTranslate;
    public static ItemModule moduleScale;
    public static ItemModule moduleRotate;
    public static ItemModule moduleSpeed;
    public static ItemModule moduleCapacity;
    public static ItemModule moduleCollection;
    public static ItemModule moduleInvert;
    public static ItemModule moduleSilence;
    public static ItemModule moduleFusion;
    public static ItemModule moduleDome;
    public static ItemModule moduleCamouflage;
    public static ItemModule moduleApproximation;
    public static ItemModuleArray moduleArray;
    public static ItemModule moduleDisintegration;
    public static ItemModule moduleShock;
    public static ItemModule moduleGlow;
    public static ItemModule moduleSponge;
    public static ItemModule moduleStabilize;
    public static ItemModule moduleRepulsion;
    public static ItemModule moduleAntiHostile;
    public static ItemModule moduleAntiFriendly;
    public static ItemModule moduleAntiPersonnel;
    public static ItemModule moduleConfiscate;
    public static ItemModule moduleWarn;
    public static ItemModule moduleBlockAccess;
    public static ItemModule moduleBlockAlter;
    public static ItemModule moduleAntiSpawn;

    public static Block coercionDeriver;
    public static Block fortronCapacitor;
    public static Block electromagneticProjector;
    public static Block biometricIdentifier;
    public static Block forceMobilizer;
    public static Block forceField;

    public static Color fieldColor = Color.BLUE; //TODO change to original

    public ModularForceFieldSystem()
    {
        super(Reference.domain);
        manager.defaultTab = new ModCreativeTab("mffs");
    }

    @Override
    public void loadHandlers(LoadableHandler loader)
    {
        MinecraftForge.EVENT_BUS.register(new SubscribeEventHandler());
        MinecraftForge.EVENT_BUS.register(remoteController);
    }

    @Override
    public void loadItems(ModManager manager)
    {
        remoteController = manager.newItem("MFFSxRemoteController", new ItemRemoteController());

        focusMatrix = manager.newItem("MFFSxCardFocusMatrix", new ItemMFFS());

        /**
         * Cards
         */
        cardBlank = manager.newItem("MFFSxCardBlack", new ItemCard());
        cardInfinite = manager.newItem("MFFSxCardInfinite", new ItemCardInfinite());
        cardFrequency = manager.newItem("MFFSxCardFrequency", new ItemCardFrequency());
        cardID = manager.newItem("MFFSxCardID", new ItemCardIdentification());
        cardLink = manager.newItem("MFFSxCardLink", new ItemCardLink());

        /**
         * Modes
         */
        modeCube = manager.newItem("MFFSxCardModeCube", new ItemModeCube());
        modeSphere = manager.newItem("MFFSxCardModeSphere", new ItemModeSphere());
        modeTube = manager.newItem("MFFSxCardNodeTube", new ItemModeTube());
        modeCylinder = manager.newItem("MFFSxCardModeCylinder", new ItemModeCylinder());
        modePyramid = manager.newItem("MFFSxCardModePyramid", new ItemModePyramid());
        modeCustom = manager.newItem("MFFSxCardModeCustom", new ItemModeCustom());
        /**
         * Modules
         */

        moduleTranslate = manager.newItem(new ItemModule()).setCost(3f);

        moduleScale = manager.newItem(new ItemModule()).setCost(2.5f);

        moduleRotate = manager.newItem(new ItemModule()).setCost(0.5f);

        moduleSpeed = manager.newItem(new ItemModule()).setCost(1.5f);

        moduleCapacity = manager.newItem(new ItemModule()).setCost(0.5f);

        moduleCollection = manager.newItem(new ItemModule()).setMaxStackSize(1).setCost(15);

        moduleInvert = manager.newItem(new ItemModule()).setMaxStackSize(1).setCost(15);

        moduleSilence = manager.newItem(new ItemModule()).setMaxStackSize(1).setCost(1);
        moduleFusion = manager.newItem(new ItemModuleFusion());
        moduleDome = manager.newItem(new ItemModuleDome());

        moduleCamouflage = manager.newItem(new ItemModule()).setCost(1.5f).setMaxStackSize(1);

        moduleApproximation = manager.newItem(new ItemModule()).setMaxStackSize(1).setCost(1f);
        moduleArray = manager.newItem(new ItemModuleArray()).setCost(3f);
        moduleDisintegration = manager.newItem(new ItemModuleDisintegration());
        moduleShock = manager.newItem(new ItemModuleShock());

        moduleGlow = manager.newItem(new ItemModule());
        moduleSponge = manager.newItem(new ItemModuleSponge());
        moduleStabilize = manager.newItem(new ItemModuleStabilize());
        moduleRepulsion = manager.newItem(new ItemModuleRepulsion());
        moduleAntiHostile = manager.newItem(new ItemModuleAntiHostile()).setCost(10);
        moduleAntiFriendly = manager.newItem(new ItemModuleAntiFriendly()).setCost(5);
        moduleAntiPersonnel = manager.newItem(new ItemModuleAntiPersonnel()).setCost(15);
        moduleConfiscate = manager.newItem(new ItemModuleConfiscate());
        moduleWarn = manager.newItem(new ItemModuleBroadcast());

        moduleBlockAccess = manager.newItem(new ItemModuleDefense()).setCost(10);

        moduleBlockAlter = manager.newItem(new ItemModuleDefense()).setCost(15);

        moduleAntiSpawn = manager.newItem(new ItemModuleDefense()).setCost(10);

    }

    @Override
    protected void loadBlocks(ModManager manager)
    {
        coercionDeriver = manager.newBlock(TileCoercionDeriver.class);
        fortronCapacitor = manager.newBlock(TileFortronCapacitor.class);
        electromagneticProjector = manager.newBlock(TileElectromagneticProjector.class);
        biometricIdentifier = manager.newBlock(TileBiometricIdentifier.class);
        forceMobilizer = manager.newBlock(TileForceMobilizer.class);
        forceField = manager.newBlock(TileForceField.class);

        FortronUtility.fluidFortron.setGaseous(true);
        FluidRegistry.registerFluid(FortronUtility.fluidFortron);

        ((ModCreativeTab) manager.defaultTab).itemStack = new ItemStack(fortronCapacitor);
    }


    @Override
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        super.preInit(event);

    }

    @Override
    @Mod.EventHandler
    public void init(FMLInitializationEvent evt)
    {
        super.init(evt);
    }

    @Override
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent evt)
    {

        /**
         * Add to black lists
         */
        Blacklist.stabilizationBlacklist.add(Blocks.water);
        Blacklist.stabilizationBlacklist.add(Blocks.flowing_water);
        Blacklist.stabilizationBlacklist.add(Blocks.lava);
        Blacklist.stabilizationBlacklist.add(Blocks.flowing_lava);

        Blacklist.disintegrationBlacklist.add(Blocks.water);
        Blacklist.disintegrationBlacklist.add(Blocks.flowing_water);
        Blacklist.disintegrationBlacklist.add(Blocks.lava);
        Blacklist.disintegrationBlacklist.add(Blocks.flowing_lava);

        Blacklist.mobilizerBlacklist.add(Blocks.bedrock);
        Blacklist.mobilizerBlacklist.add(forceField);
        try
        {
            //TODO replace with proxy call to avoid using reflection that will always break
            Class clazz = Class.forName("ic2.api.tile.ExplosionWhitelist");
            clazz.getMethod("addWhitelistedBlock", Block.class).invoke(null, forceField);
        }
        catch (Exception e)
        {
            Reference.logger.error("IC2 Explosion white list API not found. Ignoring...", e);
        }
        super.postInit(evt);
    }

    @Override
    public AbstractProxy getProxy()
    {
        return proxy;
    }
}
