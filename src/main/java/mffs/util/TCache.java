package mffs.util;


/**
 * For objects that uses caching method to reduce CPU work.
 *
 * @author Calclavia
 */
public interface TCache {

    Object getCache(String paramString);

    void putCache(String param, Object object);

    boolean cacheExists(String param);

    void clearCache(String paramString);

    void clearCache();
}