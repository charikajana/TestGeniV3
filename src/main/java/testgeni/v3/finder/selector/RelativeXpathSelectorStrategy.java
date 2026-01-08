package testgeni.v3.finder.selector;

import testgeni.v3.core.domain.ScannedElement;

/**
 * Strategy to generate a relative XPath (//tag[@attr='val']).
 * This is used as a powerful fallback when specific Playwright locators aren't enough.
 */
public class RelativeXpathSelectorStrategy implements SelectorStrategy {
    @Override
    public String generate(ScannedElement el) {
        String tag = el.tagName != null ? el.tagName : "*";
        String text = el.text != null ? el.text.trim() : "";
        
        // 1. Tag + Text contains (Using . instead of text() for nested matches)
        if (!text.isEmpty() && text.length() < 50) {
            return String.format("xpath=//%s[contains(., '%s')]", 
                tag, text.replace("'", ""));
        }
        
        // 2. Tag + Any unique attribute found
        for (String attr : el.attributes.keySet()) {
            if ("id".equals(attr) || "class".equals(attr) || "tg-id".equals(attr)) continue;
            
            String val = el.attributes.get(attr);
            if (val != null && !val.isEmpty()) {
                return String.format("xpath=//%s[@%s='%s']", tag, attr, val.replace("'", ""));
            }
        }
        
        return null;
    }

    @Override
    public int priority() { return 10; } // Low priority fallback
}
