package com.builtbroken.mffs;

import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A static variable reference file.
 *
 * @author Calclavia
 */
public final class Reference
{
    /**
     * General Variable Definition
     */
    public static final String id = "mffs-bbm";
    public static final String name = "Modular Force Field System";
    public static final Logger logger = LogManager.getLogger(name);
    public static final String domain = "mffs";
    public static final String prefix = domain + ":";
    public static final String majorVersion = "@MAJOR@";
    public static final String minorVersion = "@MINOR@";
    public static final String revisionVersion = "@REVIS@";
    public static final String version = majorVersion + "." + minorVersion + "." + revisionVersion;
    public static final String buildVersion = "@BUILD@";

    /**
     * Directories Definition
     */
    public static final String resourceDirectory = "/assets/mffs/";
    public static final String textureDirectory = "textures/";
    public static final String blockDirectory = textureDirectory + "blocks/";
    public static final String itemDirectory = textureDirectory + "items/";
    public static final String modelPath = "models/";
    public static final String modelDirectory = resourceDirectory + "models/";
    public static final String guiDirectory = textureDirectory + "gui/";

    public static final ResourceLocation hologramTexture = new ResourceLocation(domain, modelPath + "hologram.png");
    public static final ResourceLocation guiButtonTexture = new ResourceLocation(domain, guiDirectory + "gui_button.png");
}
