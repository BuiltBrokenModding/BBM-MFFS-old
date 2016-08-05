package mffs;


import com.builtbroken.mc.core.registry.ModManager;
import com.builtbroken.mc.lib.helper.recipe.UniversalRecipe;
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
import cpw.mods.fml.common.registry.GameRegistry;
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
import java.util.UUID;

@Mod(modid = Reference.id, name = Reference.name, version = Reference.version, dependencies = "required-after:VoltzEngine", guiFactory = "mffs.MFFSGuiFactory")
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
    }

    @Override
    public void loadItems(ModManager manager)
    {
        remoteController = manager.newItem("MFFSxRemoteController", new ItemRemoteController());
        MinecraftForge.EVENT_BUS.register(remoteController);

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

        moduleTranslate = manager.newItem("MFFSxCardModuleTranslate", new ItemModule()).setCost(3f);

        moduleScale = manager.newItem("MFFSxCardModuleScale", new ItemModule()).setCost(2.5f);

        moduleRotate = manager.newItem("MFFSxCardModuleRotate", new ItemModule()).setCost(0.5f);

        moduleSpeed = manager.newItem("MFFSxCardModuleSpeed", new ItemModule()).setCost(1.5f);

        moduleCapacity = manager.newItem("MFFSxCardModuleCapacity", new ItemModule()).setCost(0.5f);

        moduleCollection = manager.newItem("MFFSxCardModuleCollection", new ItemModule()).setMaxStackSize(1).setCost(15);

        moduleInvert = manager.newItem("MFFSxCardModuleInvert", new ItemModule()).setMaxStackSize(1).setCost(15);

        moduleSilence = manager.newItem("MFFSxCardModuleSilence", new ItemModule()).setMaxStackSize(1).setCost(1);
        moduleFusion = manager.newItem("MFFSxCardModuleFusion", new ItemModuleFusion());
        moduleDome = manager.newItem("MFFSxCardModuleDome", new ItemModuleDome());

        moduleCamouflage = manager.newItem("MFFSxCardModuleCamouflage", new ItemModule()).setCost(1.5f).setMaxStackSize(1);

        moduleApproximation = manager.newItem("MFFSxCardModuleApproximation", new ItemModule()).setMaxStackSize(1).setCost(1f);
        moduleArray = (ItemModuleArray) manager.newItem("MFFSxCardModuleArray", new ItemModuleArray()).setCost(3f);
        moduleDisintegration = manager.newItem("MFFSxCardModuleDisintegration", new ItemModuleDisintegration());
        moduleShock = manager.newItem("MFFSxCardModuleShock", new ItemModuleShock());

        moduleGlow = manager.newItem("MFFSxCardModuleGlow", new ItemModule());
        moduleSponge = manager.newItem("MFFSxCardModuleSponge", new ItemModuleSponge());
        moduleStabilize = manager.newItem("MFFSxCardModuleStabilize", new ItemModuleStabilize());
        moduleRepulsion = manager.newItem("MFFSxCardModuleRepulsion", new ItemModuleRepulsion());
        moduleAntiHostile = manager.newItem("MFFSxCardModuleAntiHostile", new ItemModuleAntiHostile()).setCost(10);
        moduleAntiFriendly = manager.newItem("MFFSxCardModuleAntiFriendly", new ItemModuleAntiFriendly()).setCost(5);
        moduleAntiPersonnel = manager.newItem("MFFSxCardModuleAntiPersonnel", new ItemModuleAntiPersonnel()).setCost(15);
        moduleConfiscate = manager.newItem("MFFSxCardModuleConfiscate", new ItemModuleConfiscate());
        moduleWarn = manager.newItem("MFFSxCardModuleWarn", new ItemModuleBroadcast());

        moduleBlockAccess = manager.newItem("MFFSxCardModuleBlockAccess", new ItemModuleDefense()).setCost(10);

        moduleBlockAlter = manager.newItem("MFFSxCardModuleBlockAlter", new ItemModuleDefense()).setCost(15);

        moduleAntiSpawn = manager.newItem("MFFSxCardModuleAntiSpawn", new ItemModuleDefense()).setCost(10);

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
        /**
         * Add recipe.
         */
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(focusMatrix, 8), "RMR", "MDM", "RMR", 'M', UniversalRecipe.PRIMARY_METAL.get(), 'D', Items.diamond, 'R', Items.redstone));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(remoteController), "WWW", "MCM", "MCM", 'W', UniversalRecipe.WIRE.get(), 'C', UniversalRecipe.BATTERY.get(), 'M', UniversalRecipe.PRIMARY_METAL.get()));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(coercionDeriver), "FMF", "FCF", "FMF", 'C', UniversalRecipe.BATTERY.get(), 'M', UniversalRecipe.PRIMARY_METAL.get(), 'F', focusMatrix));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(fortronCapacitor), "MFM", "FCF", "MFM", 'D', Items.diamond, 'C', UniversalRecipe.BATTERY.get(), 'F', focusMatrix, 'M', UniversalRecipe.PRIMARY_METAL.get()));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(electromagneticProjector), " D ", "FFF", "MCM", 'D', Items.diamond, 'C', UniversalRecipe.BATTERY.get(), 'F', focusMatrix, 'M', UniversalRecipe.PRIMARY_METAL.get()));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(biometricIdentifier), "FMF", "MCM", "FMF", 'C', cardBlank, 'M', UniversalRecipe.PRIMARY_METAL.get(), 'F', focusMatrix));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(forceMobilizer), "FCF", "TMT", "FCF", 'F', focusMatrix, 'C', UniversalRecipe.MOTOR.get(), 'T', moduleTranslate, 'M', UniversalRecipe.MOTOR.get()));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(cardBlank), "PPP", "PMP", "PPP", 'P', Items.paper, 'M', UniversalRecipe.PRIMARY_METAL.get()));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(cardLink), "BWB", 'B', cardBlank, 'W', UniversalRecipe.WIRE.get()));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(cardFrequency), "WBW", 'B', cardBlank, 'W', UniversalRecipe.WIRE.get()));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(cardID), "R R", " B ", "R R", 'B', cardBlank, 'R', Items.redstone));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(modeSphere), " F ", "FFF", " F ", 'F', focusMatrix));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(modeCube), "FFF", "FFF", "FFF", 'F', focusMatrix));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(modeTube), "FFF", "   ", "FFF", 'F', focusMatrix));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(modePyramid), "F  ", "FF ", "FFF", 'F', focusMatrix));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(modeCylinder), "S", "S", "S", 'S', modeSphere));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(modeCustom), " C ", "TFP", " S ", 'S', modeSphere, 'C', modeCube, 'T', modeTube, 'P', modePyramid, 'F', focusMatrix));
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
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleRotate, 4), "F  ", " F ", "  F", 'F', focusMatrix));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleGlow, 4), "GGG", "GFG", "GGG", 'F', focusMatrix, 'G', Blocks.glowstone));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleStabilize), "FDF", "PSA", "FDF", 'F', focusMatrix, 'P', Items.diamond_pickaxe, 'S', Items.diamond_shovel, 'A', Items.diamond_axe, 'D', Items.diamond));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleCollection), "F F", " H ", "F F", 'F', focusMatrix, 'H', Blocks.hopper));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleInvert), "L", "F", "L", 'F', focusMatrix, 'L', Blocks.lapis_block));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleSilence), " N ", "NFN", " N ", 'F', focusMatrix, 'N', Blocks.noteblock));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleApproximation), " N ", "NFN", " N ", 'F', focusMatrix, 'N', Items.golden_pickaxe));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleArray), " F ", "DFD", " F ", 'F', focusMatrix, 'D', Items.diamond));
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
