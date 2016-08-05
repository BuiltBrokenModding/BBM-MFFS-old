package mffs.security;

import com.builtbroken.mc.lib.access.Permission;
import com.builtbroken.mc.lib.access.Permissions;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/19/2016.
 */
public final class MFFSPermissions
{
    public static final Permission mffs = Permissions.root.addChild("mffs");
    /** Force Field Warp - Allows a player to go through force fields. */
    public static final Permission forceFieldWarp = mffs.addChild("warp");

    /** Defense - Allows the bypassing of interdiction matrix defenses. */
    public static final Permission defense = mffs.addChild("defense");
    /** Place Access - Allows to open GUIs and activate blocks. */
    public static final Permission blockAlter = defense.addChild("blockPlaceAccess");
    /** Block Access - Allows block access and opening GUIs. */
    public static final Permission blockAccess = defense.addChild("blockAccess");
    /** Bypass Confiscation - Allows the bypassing of interdiction matrix confiscation. */
    public static final Permission bypassConfiscation = defense.addChild("bypassConfiscation");

    /** Configure - Allows to configure biometric identifiers. */
    public static final Permission configure = mffs.addChild("configure");
    /** Remote Control - Allows player to remotely control blocks with the remote. */
    public static final Permission remoteControl = mffs.addChild("remoteControl");
}
