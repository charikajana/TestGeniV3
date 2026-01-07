package testgeni.v3.orchestration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import testgeni.v3.util.V3Logger;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles persistence of successful element locators to a JSON file.
 * Enables "Self-Healing" by caching and reusing stable selectors.
 */
public class LocatorCacheRegistry {

    private static final String CACHE_DIR = "CacheLocatorRepo";
    private static final String CACHE_FILENAME = "locator_cache.json";
    private final Map<String, String> cache;
    private final ObjectMapper mapper;
    private final File file;

    public LocatorCacheRegistry() {
        this.mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        
        File dir = new File(CACHE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        this.file = new File(dir, CACHE_FILENAME);
        this.cache = loadCache();
    }

    /**
     * Get a cached selector for a given step text and context.
     * Context helps differentiate identical steps on different pages.
     */
    public String getSelector(String context, String stepText) {
        if (stepText == null) return null;
        String key = generateKey(context, stepText);
        return cache.get(key);
    }

    /**
     * Store a successful selector for a step with context.
     */
    public void putSelector(String context, String stepText, String selector) {
        if (stepText == null || selector == null) return;
        String key = generateKey(context, stepText);
        
        // Only update and save if the selector changed
        if (!selector.equals(cache.get(key))) {
            cache.put(key, selector);
            saveCache();
            V3Logger.trace("Cache Updated", "Key: " + key + " -> Selector: " + selector);
        }
    }

    private String generateKey(String context, String step) {
        // Sanitize the context to create a stable Page Identity
        // Example: https://demoqa.com/radio-button?id=123 -> demoqa_com_radio_button
        String pageIdentity = "global";
        if (context != null && !context.isEmpty()) {
            pageIdentity = context.trim().toLowerCase()
                .split("\\?")[0]              // Ignore query parameters
                .split("#")[0]               // Ignore fragments
                .replaceAll("https?://(www\\.)?", "") // Remove protocol
                .replaceAll("[^a-z0-9]", "_")         // Sanitize
                .replaceAll("_+", "_")               // Clean underscores
                .replaceAll("^_|_$", "");            // Trim underscores
            
            // Limit length for readable JSON keys
            if (pageIdentity.length() > 60) {
                pageIdentity = pageIdentity.substring(0, 60);
            }
        }
        
        return pageIdentity + "::" + normalizeKey(step);
    }

    private String normalizeKey(String step) {
        // Remove Gherkin keywords and normalize whitespace for consistent keys
        return step.toLowerCase()
                .replaceAll("^(given|when|then|and|but)\\s+", "")
                .trim()
                .replaceAll("\\s+", " ");
    }

    private Map<String, String> loadCache() {
        if (!file.exists()) {
            return new ConcurrentHashMap<>();
        }
        try {
            return mapper.readValue(file, new TypeReference<ConcurrentHashMap<String, String>>() {});
        } catch (IOException e) {
            V3Logger.warn("Cache Load Failed: Could not read locator_cache.json. Starting fresh. Error: " + e.getMessage());
            return new ConcurrentHashMap<>();
        }
    }

    private synchronized void saveCache() {
        try {
            mapper.writeValue(file, cache);
        } catch (IOException e) {
            V3Logger.error("Cache Save Failed: Could not write to locator_cache.json: " + e.getMessage());
        }
    }
    
    public void clear() {
        cache.clear();
        saveCache();
    }
}
