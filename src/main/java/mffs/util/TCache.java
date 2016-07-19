package mffs.util;


/**
 * For objects that uses caching method to reduce CPU work.
 *
 * @author Calclavia
 */
public interface TCache {

    Object getCache(String paramString);

    boolean putCache(String param, Object object);

    void clearCache(String paramString);

    void clearCache();
}