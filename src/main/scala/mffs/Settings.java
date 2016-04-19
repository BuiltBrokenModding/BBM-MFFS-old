package mffs;

import net.minecraftforge.common.config.Configuration;

/**
 * MFFS Configuration Settings
 *
 * @author Calclavia
 */
public final class Settings
{
    public static Configuration configuration;

    public static int maxFrequencyDigits = 8;

    public static int maxForceFieldsPerTick = 5000;

    public static int maxForceFieldScale = 200;

    public static double fortronProductionMultiplier = 1;
    //@Config(comment = "Should the interdiction matrix interact with creative players?.")
    public static boolean interdictionInteractCreative = true;
    //@Config(comment = "Set this to false to turn off the MFFS Chunkloading capabilities.")
    public static boolean loadFieldChunks = true;
    //@Config(comment = "Allow the operator(s) to override security measures created by MFFS?")
    public static boolean allowOpOverride = true;
    //@Config(comment = "Cache allows temporary data saving to decrease calculations required.")
    public static boolean useCache = true;
    //@Config(comment = "Turning this to false will make MFFS run without electricity or energy systems required. Great for vanilla!")
    public static boolean enableElectricity = true;
    //@Config(comment = "Turning this to false will enable better client side packet and updates but in the cost of more packets sent.")
    public static boolean conservePackets = true;
    //@Config(comment = "Turning this to false will reduce rendering and client side packet graphical packets.")
    public static boolean highGraphics = true;
    //@Config(comment = "The energy required to perform a kill for the interdiction matrix.")
    public static int interdictionMatrixMurderEnergy = 0;
    //@Config(comment = "The maximum range for the interdiction matrix.")
    public static int interdictionMatrixMaxRange = Integer.MAX_VALUE;

    public static boolean enableForceManipulator = true;

    public static boolean allowForceManipulatorTeleport = true;

    public static boolean allowFortronTeleport = true;
    //@Config(comment = "A list of block names to not be moved by the force mobilizer.")
    public static String[] mobilizerBlacklist;
    //@Config(comment = "A list of block names to not be stabilized by the electromagnetic projector.")
    public static String[] stabilizationBlacklist;
    //@Config(comment = "A list of block names to not be disintegrated by the electromagnetic projector.")
    public static String[] disintegrationBlacklist;

    public void load()
    {

    }
}
