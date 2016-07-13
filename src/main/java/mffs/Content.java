package mffs;

import com.builtbroken.mc.lib.helper.recipe.UniversalRecipe;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.ArrayList;
import java.util.List;

/**
 * The main content of MFFS
 *
 * @author Calclavia
 */
public class Content
{
    public void postInit()
    {
        List<IRecipe> recipes = new ArrayList();
        /**
         * Add recipe.
         */
        recipes.add(new ShapedOreRecipe(new ItemStack(focusMatrix, 8), "RMR", "MDM", "RMR", 'M', UniversalRecipe.PRIMARY_METAL.get(), 'D', Items.diamond, 'R', Items.redstone));
        recipes.add(new ShapedOreRecipe(new ItemStack(remoteController), "WWW", "MCM", "MCM", 'W', UniversalRecipe.WIRE.get(), 'C', UniversalRecipe.BATTERY.get(), 'M', UniversalRecipe.PRIMARY_METAL.get()));
        recipes.add(new ShapedOreRecipe(new ItemStack(coercionDeriver), "FMF", "FCF", "FMF", 'C', UniversalRecipe.BATTERY.get(), 'M', UniversalRecipe.PRIMARY_METAL.get(), 'F', focusMatrix));
        recipes.add(new ShapedOreRecipe(new ItemStack(fortronCapacitor), "MFM", "FCF", "MFM", 'D', Items.diamond, 'C', UniversalRecipe.BATTERY.get(), 'F', focusMatrix, 'M', UniversalRecipe.PRIMARY_METAL.get()));
        recipes.add(new ShapedOreRecipe(new ItemStack(electromagneticProjector), " D ", "FFF", "MCM", 'D', Items.diamond, 'C', UniversalRecipe.BATTERY.get(), 'F', focusMatrix, 'M', UniversalRecipe.PRIMARY_METAL.get()));
        recipes.add(new ShapedOreRecipe(new ItemStack(biometricIdentifier), "FMF", "MCM", "FMF", 'C', cardBlank, 'M', UniversalRecipe.PRIMARY_METAL.get(), 'F', focusMatrix));
        recipes.add(new ShapedOreRecipe(new ItemStack(forceMobilizer), "FCF", "TMT", "FCF", 'F', focusMatrix, 'C', UniversalRecipe.MOTOR.get(), 'T', moduleTranslate, 'M', UniversalRecipe.MOTOR.get()));
        recipes.add(new ShapedOreRecipe(new ItemStack(cardBlank), "PPP", "PMP", "PPP", 'P', Items.paper, 'M', UniversalRecipe.PRIMARY_METAL.get()));
        recipes.add(new ShapedOreRecipe(new ItemStack(cardLink), "BWB", 'B', cardBlank, 'W', UniversalRecipe.WIRE.get()));
        recipes.add(new ShapedOreRecipe(new ItemStack(cardFrequency), "WBW", 'B', cardBlank, 'W', UniversalRecipe.WIRE.get()));
        recipes.add(new ShapedOreRecipe(new ItemStack(cardID), "R R", " B ", "R R", 'B', cardBlank, 'R', Items.redstone));
        recipes.add(new ShapedOreRecipe(new ItemStack(modeSphere), " F ", "FFF", " F ", 'F', focusMatrix));
        recipes.add(new ShapedOreRecipe(new ItemStack(modeCube), "FFF", "FFF", "FFF", 'F', focusMatrix));
        recipes.add(new ShapedOreRecipe(new ItemStack(modeTube), "FFF", "   ", "FFF", 'F', focusMatrix));
        recipes.add(new ShapedOreRecipe(new ItemStack(modePyramid), "F  ", "FF ", "FFF", 'F', focusMatrix));
        recipes.add(new ShapedOreRecipe(new ItemStack(modeCylinder), "S", "S", "S", 'S', modeSphere));
        recipes.add(new ShapedOreRecipe(new ItemStack(modeCustom), " C ", "TFP", " S ", 'S', modeSphere, 'C', modeCube, 'T', modeTube, 'P', modePyramid, 'F', focusMatrix));
        recipes.add(new ShapelessOreRecipe(new ItemStack(modeCustom), new ItemStack(modeCustom)));
        recipes.add(new ShapedOreRecipe(new ItemStack(moduleSpeed, 1), "FFF", "RRR", "FFF", 'F', focusMatrix, 'R', Items.redstone));
        recipes.add(new ShapedOreRecipe(new ItemStack(moduleCapacity, 2), "FCF", 'F', focusMatrix, 'C', UniversalRecipe.BATTERY.get()));
        recipes.add(new ShapedOreRecipe(new ItemStack(moduleShock), "FWF", 'F', focusMatrix, 'W', UniversalRecipe.WIRE.get()));
        recipes.add(new ShapedOreRecipe(new ItemStack(moduleSponge), "BBB", "BFB", "BBB", 'F', focusMatrix, 'B', Items.water_bucket));
        recipes.add(new ShapedOreRecipe(new ItemStack(moduleDisintegration), " W ", "FBF", " W ", 'F', focusMatrix, 'W', UniversalRecipe.WIRE.get(), 'B', UniversalRecipe.BATTERY.get()).config(Settings.config));
        recipes.add(new ShapedOreRecipe(new ItemStack(moduleDome), "F", " ", "F", 'F', focusMatrix));
        recipes.add(new ShapedOreRecipe(new ItemStack(moduleCamouflage), "WFW", "FWF", "WFW", 'F', focusMatrix, 'W', new ItemStack(Blocks.wool, 1, OreDictionary.WILDCARD_VALUE)));
        recipes.add(new ShapedOreRecipe(new ItemStack(moduleFusion), "FJF", 'F', focusMatrix, 'J', moduleShock));
        recipes.add(new ShapedOreRecipe(new ItemStack(moduleScale, 2), "FRF", 'F', focusMatrix));
        recipes.add(new ShapedOreRecipe(new ItemStack(moduleTranslate, 2), "FSF", 'F', focusMatrix, 'S', moduleScale).config(Settings.config));
        recipes.add(new ShapedOreRecipe(new ItemStack(moduleRotate, 4), "F  ", " F ", "  F", 'F', focusMatrix));
        recipes.add(new ShapedOreRecipe(new ItemStack(moduleGlow, 4), "GGG", "GFG", "GGG", 'F', focusMatrix, 'G', Blocks.glowstone));
        recipes.add(new ShapedOreRecipe(new ItemStack(moduleStabilize), "FDF", "PSA", "FDF", 'F', focusMatrix, 'P', Items.diamond_pickaxe, 'S', Items.diamond_shovel, 'A', Items.diamond_axe, 'D', Items.diamond));
        recipes.add(new ShapedOreRecipe(new ItemStack(moduleCollection), "F F", " H ", "F F", 'F', focusMatrix, 'H', Blocks.hopper));
        recipes.add(new ShapedOreRecipe(new ItemStack(moduleInvert), "L", "F", "L", 'F', focusMatrix, 'L', Blocks.lapis_block));
        recipes.add(new ShapedOreRecipe(new ItemStack(moduleSilence), " N ", "NFN", " N ", 'F', focusMatrix, 'N', Blocks.noteblock));
        recipes.add(new ShapedOreRecipe(new ItemStack(moduleApproximation), " N ", "NFN", " N ", 'F', focusMatrix, 'N', Items.golden_pickaxe));
        recipes.add(new ShapedOreRecipe(new ItemStack(moduleArray), " F ", "DFD", " F ", 'F', focusMatrix, 'D', Items.diamond));
        recipes.add(new ShapedOreRecipe(new ItemStack(moduleRepulsion), "FFF", "DFD", "SFS", 'F', focusMatrix, 'D', Items.diamond, 'S', Items.slime_ball).config(Settings.config));
        recipes.add(new ShapedOreRecipe(new ItemStack(moduleAntiHostile), " R ", "GFB", " S ", 'F', focusMatrix, 'G', Items.gunpowder, 'R', Items.rotten_flesh, 'B', Items.bone, 'S', Items.ghast_tear));
        recipes.add(new ShapedOreRecipe(new ItemStack(moduleAntiFriendly), " R ", "GFB", " S ", 'F', focusMatrix, 'G', Items.cooked_porkchop, 'R', new ItemStack(Blocks.wool, 1, OreDictionary.WILDCARD_VALUE), 'B', Items.leather, 'S', Items.slime_ball));
        recipes.add(new ShapedOreRecipe(new ItemStack(moduleAntiPersonnel), "BFG", 'F', focusMatrix, 'B', moduleAntiHostile, 'G', moduleAntiFriendly));
        recipes.add(new ShapedOreRecipe(new ItemStack(moduleConfiscate), "PEP", "EFE", "PEP", 'F', focusMatrix, 'E', Items.ender_eye, 'P', Items.ender_pearl).config(Settings.config));
        recipes.add(new ShapedOreRecipe(new ItemStack(moduleWarn), "NFN", 'F', focusMatrix, 'N', Blocks.noteblock));
        recipes.add(new ShapedOreRecipe(new ItemStack(moduleBlockAccess), " C ", "BFB", " C ", 'F', focusMatrix, 'B', Blocks.iron_block, 'C', Blocks.chest));
        recipes.add(new ShapedOreRecipe(new ItemStack(moduleBlockAlter), " G ", "GFG", " G ", 'F', moduleBlockAccess, 'G', Blocks.gold_block));
        recipes.add(new ShapedOreRecipe(new ItemStack(moduleAntiSpawn), " H ", "G G", " H ", 'H', moduleAntiHostile, 'G', moduleAntiFriendly));
    }
}
