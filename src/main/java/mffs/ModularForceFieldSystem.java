package mffs;


import com.builtbroken.mc.core.registry.ModManager;
import com.builtbroken.mc.lib.helper.recipe.UniversalRecipe;
import com.builtbroken.mc.lib.mod.AbstractMod;
import com.builtbroken.mc.lib.mod.AbstractProxy;
import com.builtbroken.mc.lib.mod.ModCreativeTab;
import com.builtbroken.mc.lib.mod.loadable.LoadableHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import mffs.api.Blacklist;
import mffs.base.ItemMFFS;
import mffs.base.ItemModule;
import mffs.field.TileElectromagneticProjector;
import mffs.field.TileForceField;
import mffs.field.mobilize.TileForceMobilizer;
import mffs.field.mode.ItemMode;
import mffs.field.mode.ItemModeCustom;
import mffs.field.module.*;
import mffs.item.ItemRemoteController;
import mffs.item.card.ItemCard;
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
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.awt.*;

@Mod(modid = Reference.id, name = Reference.name, version = Reference.version, dependencies = "required-after:VoltzEngine", guiFactory = "mffs.MFFSGuiFactory")
public class ModularForceFieldSystem extends AbstractMod
{
    /** Damage type used by forcefields */
    public static final DamageSource damageFieldShock = new DamageSource("fieldShock").setDamageBypassesArmor();

    @SidedProxy(clientSide = "mffs.ClientProxy", serverSide = "mffs.CommonProxy")
    public static CommonProxy proxy;

    @Mod.Instance(Reference.id)
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
    public static ItemCardAccess cardID;
    public static Item cardLink;

    /**
     * Modes
     */
    public static ItemMode modeCard;
    public static ItemMode modeCustom;
    public static ItemModule moduleTranslate;
    public static ItemModule moduleScale;
    public static ItemModule moduleSpeed;
    public static ItemModule moduleCapacity;
    public static ItemModule moduleCollection;
    public static ItemModule moduleInvert;
    public static ItemModule moduleSilence;
    public static ItemModule moduleFusion;
    public static ItemModule moduleDome;
    public static ItemModule moduleCamouflage;
    public static ItemModule moduleApproximation;
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
    }

    @Override
    public void loadItems(ModManager manager)
    {
        remoteController = manager.newItem("MFFSxRemoteController", new ItemRemoteController()).setTextureName(Reference.prefix + "remoteController");
        MinecraftForge.EVENT_BUS.register(remoteController);

        focusMatrix = manager.newItem("cardFocusMatrix", new ItemMFFS()).setTextureName(Reference.prefix + "focusMatrix");

        /**
         * Cards
         */
        cardBlank = manager.newItem("cardBlank", new ItemCard()).setTextureName(Reference.prefix + "cardBlank");
        cardInfinite = manager.newItem("cardInfinite", new ItemCardInfinite()).setTextureName(Reference.prefix + "cardInfinite");
        cardID = (ItemCardIdentification) manager.newItem("cardID", new ItemCardIdentification()).setTextureName(Reference.prefix + "cardIdentification");
        cardLink = manager.newItem("cardLink", new ItemCardLink()).setTextureName(Reference.prefix + "cardLink");

        /**
         * Modes
         */
        modeCard = manager.newItem("modeCard", new ItemMode());
        modeCustom = (ItemModeCustom) manager.newItem("modeCustom", new ItemModeCustom()).setTextureName(Reference.prefix + "modeCustom");
        /**
         * Modules
         */

        moduleTranslate = (ItemModule) manager.newItem("moduleTranslate", new ItemModule()).setCost(3f).setTextureName(Reference.prefix + "moduleTranslate");

        moduleScale = (ItemModule) manager.newItem("moduleScale", new ItemModule()).setCost(2.5f).setTextureName(Reference.prefix + "moduleScale");

        moduleSpeed = (ItemModule) manager.newItem("moduleSpeed", new ItemModule()).setCost(1.5f).setTextureName(Reference.prefix + "moduleSpeed");

        moduleCapacity = (ItemModule) manager.newItem("moduleCapacity", new ItemModule()).setCost(0.5f).setTextureName(Reference.prefix + "moduleCapacity");

        moduleCollection = (ItemModule) manager.newItem("moduleCollection", new ItemModule()).setMaxStackSize(1).setCost(15).setTextureName(Reference.prefix + "moduleCollection");

        moduleInvert = (ItemModule) manager.newItem("moduleInvert", new ItemModule()).setMaxStackSize(1).setCost(15).setTextureName(Reference.prefix + "moduleInvert");

        moduleSilence = (ItemModule) manager.newItem("moduleSilence", new ItemModule()).setMaxStackSize(1).setCost(1).setTextureName(Reference.prefix + "moduleSilence");
        moduleFusion = (ItemModuleFusion) manager.newItem("moduleFusion", new ItemModuleFusion()).setTextureName(Reference.prefix + "moduleFusion");
        moduleDome = (ItemModuleDome) manager.newItem("moduleDome", new ItemModuleDome()).setTextureName(Reference.prefix + "moduleDome");

        moduleCamouflage = (ItemModule) manager.newItem("moduleCamouflage", new ItemModule()).setCost(1.5f).setMaxStackSize(1).setTextureName(Reference.prefix + "moduleCamouflage");

        moduleApproximation = (ItemModule) manager.newItem("moduleApproximation", new ItemModule()).setMaxStackSize(1).setCost(1f).setTextureName(Reference.prefix + "moduleApproximation");
        moduleDisintegration = (ItemModuleDisintegration) manager.newItem("moduleDisintegration", new ItemModuleDisintegration()).setTextureName(Reference.prefix + "moduleDisintegration");
        moduleShock = (ItemModuleShock) manager.newItem("moduleShock", new ItemModuleShock()).setTextureName(Reference.prefix + "moduleShock");

        moduleGlow = (ItemModule) manager.newItem("moduleGlow", new ItemModule()).setTextureName(Reference.prefix + "moduleGlow");
        moduleSponge = (ItemModuleSponge) manager.newItem("moduleSponge", new ItemModuleSponge()).setTextureName(Reference.prefix + "moduleSponge");
        moduleStabilize = (ItemModuleStabilize) manager.newItem("moduleStabilize", new ItemModuleStabilize()).setTextureName(Reference.prefix + "moduleStabilize");
        moduleRepulsion = (ItemModuleRepulsion) manager.newItem("moduleRepulsion", new ItemModuleRepulsion()).setTextureName(Reference.prefix + "moduleRepulsion");
        moduleAntiHostile = (ItemModuleAntiHostile) manager.newItem("moduleAntiHostile", new ItemModuleAntiHostile()).setCost(10).setTextureName(Reference.prefix + "moduleAntiHostile");
        moduleAntiFriendly = (ItemModuleAntiFriendly) manager.newItem("moduleAntiFriendly", new ItemModuleAntiFriendly()).setCost(5).setTextureName(Reference.prefix + "moduleAntiFriendly");
        moduleAntiPersonnel = (ItemModuleAntiPersonnel) manager.newItem("moduleAntiPersonnel", new ItemModuleAntiPersonnel()).setCost(15).setTextureName(Reference.prefix + "moduleAntiPersonnel");
        moduleConfiscate = (ItemModuleConfiscate) manager.newItem("moduleConfiscate", new ItemModuleConfiscate()).setTextureName(Reference.prefix + "moduleConfiscate");
        moduleWarn = (ItemModuleBroadcast) manager.newItem("moduleWarn", new ItemModuleBroadcast()).setTextureName(Reference.prefix + "moduleWarn");

        moduleBlockAccess = (ItemModuleDefense) manager.newItem("moduleBlockAccess", new ItemModuleDefense()).setCost(10).setTextureName(Reference.prefix + "moduleBlockAccess");

        moduleBlockAlter = (ItemModuleDefense) manager.newItem("moduleBlockAlter", new ItemModuleDefense()).setCost(15).setTextureName(Reference.prefix + "moduleBlockAlter");

        moduleAntiSpawn = (ItemModuleDefense) manager.newItem("moduleAntiSpawn", new ItemModuleDefense()).setCost(10).setTextureName(Reference.prefix + "moduleAntiSpawn");

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

        FluidRegistry.registerFluid(new Fluid("fortron").setGaseous(true));
        FortronUtility.fluidFortron = FluidRegistry.getFluid("fortron");
        FluidRegistry.registerFluid(FortronUtility.fluidFortron);

        ((ModCreativeTab) manager.defaultTab).itemStack = new ItemStack(fortronCapacitor);
    }


    @Override
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        super.preInit(event);
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

        super.postInit(evt);

        //TODO move recipes to each block/items class
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(focusMatrix, 8), "RMR", "MDM", "RMR", 'M', UniversalRecipe.PRIMARY_METAL.get(), 'D', Items.diamond, 'R', Items.redstone));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(coercionDeriver), "FMF", "FCF", "FMF", 'C', UniversalRecipe.BATTERY.get(), 'M', UniversalRecipe.PRIMARY_METAL.get(), 'F', focusMatrix));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(fortronCapacitor), "MFM", "FCF", "MFM", 'D', Items.diamond, 'C', UniversalRecipe.BATTERY.get(), 'F', focusMatrix, 'M', UniversalRecipe.PRIMARY_METAL.get()));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(electromagneticProjector), " D ", "FFF", "MCM", 'D', Items.diamond, 'C', UniversalRecipe.BATTERY.get(), 'F', focusMatrix, 'M', UniversalRecipe.PRIMARY_METAL.get()));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(biometricIdentifier), "FMF", "MCM", "FMF", 'C', cardBlank, 'M', UniversalRecipe.PRIMARY_METAL.get(), 'F', focusMatrix));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(forceMobilizer), "FCF", "TMT", "FCF", 'F', focusMatrix, 'C', UniversalRecipe.MOTOR.get(), 'T', moduleTranslate, 'M', UniversalRecipe.MOTOR.get()));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(cardBlank), "PPP", "PMP", "PPP", 'P', Items.paper, 'M', UniversalRecipe.PRIMARY_METAL.get()));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(cardLink), "BWB", 'B', cardBlank, 'W', UniversalRecipe.WIRE.get()));
        //GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(cardFrequency), "WBW", 'B', cardBlank, 'W', UniversalRecipe.WIRE.get()));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(cardID), "R R", " B ", "R R", 'B', cardBlank, 'R', Items.redstone));

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(modeCustom), " C ", "TFP", " S ", 'S', ItemMode.Modes.SPHERE.toStack(), 'C', ItemMode.Modes.CUBE.toStack(), 'T', ItemMode.Modes.TUBE.toStack(), 'P', ItemMode.Modes.PYRAMID.toStack(), 'F', focusMatrix));
        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(modeCustom), new ItemStack(modeCustom)));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleSpeed, 1), "FFF", "RRR", "FFF", 'F', focusMatrix, 'R', Items.redstone));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleCapacity, 2), "FCF", 'F', focusMatrix, 'C', UniversalRecipe.BATTERY.get()));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleShock), "FWF", 'F', focusMatrix, 'W', UniversalRecipe.WIRE.get()));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleSponge), "BBB", "BFB", "BBB", 'F', focusMatrix, 'B', Items.water_bucket));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleDisintegration), " W ", "FBF", " W ", 'F', focusMatrix, 'W', UniversalRecipe.WIRE.get(), 'B', UniversalRecipe.BATTERY.get()));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleDome), "F", " ", "F", 'F', focusMatrix));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleCamouflage), "WFW", "FWF", "WFW", 'F', focusMatrix, 'W', new ItemStack(Blocks.wool, 1, OreDictionary.WILDCARD_VALUE)));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleFusion), "FJF", 'F', focusMatrix, 'J', moduleShock));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleScale, 2), "FRF", 'F', focusMatrix));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleTranslate, 2), "FSF", 'F', focusMatrix, 'S', moduleScale));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleGlow, 4), "GGG", "GFG", "GGG", 'F', focusMatrix, 'G', Blocks.glowstone));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleStabilize), "FDF", "PSA", "FDF", 'F', focusMatrix, 'P', Items.diamond_pickaxe, 'S', Items.diamond_shovel, 'A', Items.diamond_axe, 'D', Items.diamond));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleCollection), "F F", " H ", "F F", 'F', focusMatrix, 'H', Blocks.hopper));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleInvert), "L", "F", "L", 'F', focusMatrix, 'L', Blocks.lapis_block));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleSilence), " N ", "NFN", " N ", 'F', focusMatrix, 'N', Blocks.noteblock));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleApproximation), " N ", "NFN", " N ", 'F', focusMatrix, 'N', Items.golden_pickaxe));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleRepulsion), "FFF", "DFD", "SFS", 'F', focusMatrix, 'D', Items.diamond, 'S', Items.slime_ball));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleAntiHostile), " R ", "GFB", " S ", 'F', focusMatrix, 'G', Items.gunpowder, 'R', Items.rotten_flesh, 'B', Items.bone, 'S', Items.ghast_tear));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleAntiFriendly), " R ", "GFB", " S ", 'F', focusMatrix, 'G', Items.cooked_porkchop, 'R', new ItemStack(Blocks.wool, 1, OreDictionary.WILDCARD_VALUE), 'B', Items.leather, 'S', Items.slime_ball));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleAntiPersonnel), "BFG", 'F', focusMatrix, 'B', moduleAntiHostile, 'G', moduleAntiFriendly));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleConfiscate), "PEP", "EFE", "PEP", 'F', focusMatrix, 'E', Items.ender_eye, 'P', Items.ender_pearl));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleWarn), "NFN", 'F', focusMatrix, 'N', Blocks.noteblock));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleBlockAccess), " C ", "BFB", " C ", 'F', focusMatrix, 'B', Blocks.iron_block, 'C', Blocks.chest));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleBlockAlter), " G ", "GFG", " G ", 'F', moduleBlockAccess, 'G', Blocks.gold_block));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleAntiSpawn), " H ", "G G", " H ", 'H', moduleAntiHostile, 'G', moduleAntiFriendly));
    }

    @Override
    public AbstractProxy getProxy()
    {
        return proxy;
    }
}
