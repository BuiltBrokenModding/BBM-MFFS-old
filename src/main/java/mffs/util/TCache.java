package mffs.util;


import java.util.Map;

/**
 * For objects that uses caching method to reduce CPU work.
 *
 * @author Calclavia
 * @deprecated Use an internal Map<String, Object> instead
 */
@Deprecated
public interface TCache
{

    default Object getCache(String paramString)
    {
        return cache().get(paramString);
    }

    default void putCache(String param, Object object)
    {
        cache().put(param, object);
    }

    default boolean cacheExists(String param)
    {
        return cache().containsKey(param);
    }

    default void clearCache(String paramString)
    {
        cache().remove(paramString);
    }

    default void clearCache()
    {
        cache().clear();
    }

    Map<String, Object> cache();
}