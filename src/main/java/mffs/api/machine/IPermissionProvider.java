package mffs.api.machine;

import com.builtbroken.mc.lib.access.Permission;
import com.mojang.authlib.GameProfile;

/**
 * Used by tiles that provide permissions.
 *
 * @author Calclavia
 */
public interface IPermissionProvider
{
    /**
     * Does this field matrix provide a specific permission?
     */
    boolean hasPermission(GameProfile profile, Permission permission);
}
