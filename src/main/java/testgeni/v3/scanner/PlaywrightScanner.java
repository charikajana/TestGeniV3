package testgeni.v3.scanner;

import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import testgeni.v3.core.domain.ScannedElement;
import testgeni.v3.core.domain.ElementType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Playwright-based implementation of PageScanner.
 * 
 * Uses an optimized JavaScript injection to scan the DOM in a single pass.
 */
public class PlaywrightScanner implements PageScanner {
    
    private final Map<String, List<ScannedElement>> cache = new HashMap<>();

    public PlaywrightScanner() {
        // Stateless, or could take config
    }

    @Override
    public List<ScannedElement> scan(Page page) {
        return scan(page.mainFrame(), "body");
    }

    @Override
    public List<ScannedElement> scan(Frame frame) {
        return scan(frame, "body");
    }

    @Override
    public List<ScannedElement> scan(Page page, String selector) {
        return scan(page.mainFrame(), selector);
    }

    private List<ScannedElement> scan(Frame frame, String selector) {
        try {
            // Wait for network idle to ensure DOM is stable (only for main frame usually)
            // frame.page().waitForLoadState(LoadState.NETWORKIDLE); 
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rawElements = (List<Map<String, Object>>) frame.evaluate(getScanScript(), selector);
            
            return rawElements.stream()
                .map(this::mapToScannedElement)
                .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("[Scanner] Error scanning frame: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public void refresh() {
        cache.clear();
    }

    private ScannedElement mapToScannedElement(Map<String, Object> map) {
        ScannedElement.Builder builder = new ScannedElement.Builder()
            .id((String) map.get("id"))
            .tagName((String) map.get("tagName"));
        
        builder.text((String) map.get("text"))
               .ariaLabel((String) map.get("ariaLabel"))
               .role((String) map.get("role"))
               .isVisible(map.get("isVisible") != null ? (Boolean) map.get("isVisible") : true)
               .isEnabled((Boolean) map.get("isEnabled"));
        
        // Extract important attributes into dedicated fields
        @SuppressWarnings("unchecked")
        Map<String, String> attrs = (Map<String, String>) map.get("attributes");
        if (attrs != null) {
            // Extract commonly used attributes
            if (attrs.containsKey("placeholder")) {
                builder.placeholder(attrs.get("placeholder"));
            }
            if (attrs.containsKey("title")) {
                builder.title(attrs.get("title"));
            }
            if (attrs.containsKey("value")) {
                builder.value(attrs.get("value"));
            }
            if (attrs.containsKey("type")) {
                builder.type(attrs.get("type"));
            }
            // Store all attributes  
            attrs.forEach(builder::attribute);
        }
        
        if (map.containsKey("x")) {
            builder.bounds(
                ((Number) map.get("x")).intValue(),
                ((Number) map.get("y")).intValue(),
                ((Number) map.get("width")).intValue(),
                ((Number) map.get("height")).intValue()
            );
        }
        
        @SuppressWarnings("unchecked")
        List<String> ancestorIds = (List<String>) map.get("ancestorIds");
        if (ancestorIds != null) {
            ancestorIds.forEach(builder::addAncestorId);
        }
        
        String typeStr = (String) map.get("detectedType");
        if (typeStr != null) {
            try {
                builder.detectedType(ElementType.valueOf(typeStr));
            } catch (Exception e) {
                builder.detectedType(ElementType.ANY);
            }
        }
        
        return builder.build();
    }

    private String getScanScript() {
        return """
            (selector) => {
                const root = document.querySelector(selector) || document.body;
                const elements = [];
                
                function isVisible(el) {
                    const tagName = el.tagName.toLowerCase();
                    if (['input', 'select', 'textarea', 'button', 'a'].includes(tagName)) {
                        return true; 
                    }
                    
                    const style = window.getComputedStyle(el);
                    if (style.display === 'none') return false;
                    if (style.visibility === 'hidden') return false;
                    if (style.opacity === '0') return false;
                    
                    return el.offsetWidth > 0 && el.offsetHeight > 0;
                }

                function getAttributes(el) {
                    const attrs = {};
                    for (const attr of el.attributes) {
                        attrs[attr.name] = attr.value;
                    }
                    return attrs;
                }

                function getAncestors(el) {
                    const ancestors = [];
                    let p = el.parentElement;
                    while (p) {
                        if (p.id) ancestors.push(p.id);
                        p = p.parentElement;
                    }
                    return ancestors;
                }

                function getAssociatedLabel(el) {
                    // 1. Check for <label for="id">
                    if (el.id) {
                        const label = document.querySelector(`label[for="${el.id}"]`);
                        if (label) return label.innerText?.trim();
                    }
                    
                    // 2. Check parent <label>
                    let parent = el.parentElement;
                    while (parent) {
                        if (parent.tagName === 'LABEL') return parent.innerText?.trim();
                        parent = parent.parentElement;
                    }
                    
                    // 3. Check for nearby text (preceding sibling)
                    let prev = el.previousElementSibling;
                    if (prev && prev.innerText?.trim()) return prev.innerText.trim();
                    
                    return null;
                }

                const walker = document.createTreeWalker(root, NodeFilter.SHOW_ELEMENT);
                let node;
                while (node = walker.nextNode()) {
                    if (!isVisible(node)) continue;
                    
                    const tagName = node.tagName.toLowerCase();
                    const role = node.getAttribute('role');
                    const text = node.innerText?.trim();
                    
                    // Expanded whitelist: Include buttons, inputs, links, and text containers with reasonable length
                    const isStandardControl = ['button', 'a', 'input', 'select', 'textarea', 'label'].includes(tagName);
                    const isInteractiveRole = role && !['presentation', 'none'].includes(role);
                    const isMeaningfulText = ['div', 'span', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'p'].includes(tagName) && 
                                           text && text.length > 0 && text.length < 100;

                    if (isStandardControl || isInteractiveRole || isMeaningfulText) {
                        const rect = node.getBoundingClientRect();
                        
                        // Smart label association for inputs
                        let finalHoverText = text;
                        if (['input', 'select', 'textarea'].includes(tagName) && !finalHoverText) {
                            finalHoverText = getAssociatedLabel(node);
                        }

                        elements.push({
                            id: node.id || 'tg-' + Math.random().toString(36).substr(2, 9),
                            tagName: tagName,
                            text: finalHoverText,
                            ariaLabel: node.getAttribute('aria-label'),
                            role: role,
                            isVisible: true,
                            isEnabled: !node.disabled,
                            x: Math.round(rect.x),
                            y: Math.round(rect.y),
                            width: Math.round(rect.width),
                            height: Math.round(rect.height),
                            attributes: getAttributes(node),
                            ancestorIds: getAncestors(node)
                        });
                    }
                }
                return elements;
            }
            """;
    }
}
