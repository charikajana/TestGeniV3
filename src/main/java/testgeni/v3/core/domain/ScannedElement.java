package testgeni.v3.core.domain;

import java.util.*;

/**
 * Represents an element scanned from the DOM.
 * 
 * Contains all metadata necessary for the Intelligence/Finder layer to
 * make matching decisions without further browser interaction.
 */
public class ScannedElement {
    
    // Core Identity
    public final String id;      // Internal unique ID for this scan session
    public final String tagName;
    public final String type;    // From type attribute
    public final String role;    // ARIA role
    
    // Attributes
    public final Map<String, String> attributes;
    public final Set<String> classes;
    
    // Content
    public final String text;
    public final String ariaLabel;
    public final String placeholder;
    public final String title;
    public final String value;
    
    // State
    public final boolean isVisible;
    public final boolean isEnabled;
    public final boolean isChecked;
    public final boolean isSelected;
    
    // Location
    public final int x;
    public final int y;
    public final int width;
    public final int height;
    
    // Hierarchy
    public final String parentId;
    public final List<String> ancestorIds;
    public final boolean inShadowDom;
    public final String frameId;
    public final String frameUrl;
    
    // Meta (for Finder scoring)
    public final ElementType detectedType;
    public final Map<String, String> computedStyle;

    public ScannedElement(Builder builder) {
        this.id = builder.id;
        this.tagName = builder.tagName;
        this.type = builder.type;
        this.role = builder.role;
        this.attributes = Collections.unmodifiableMap(new HashMap<>(builder.attributes));
        this.classes = Collections.unmodifiableSet(new HashSet<>(builder.classes));
        this.text = builder.text;
        this.ariaLabel = builder.ariaLabel;
        this.placeholder = builder.placeholder;
        this.title = builder.title;
        this.value = builder.value;
        this.isVisible = builder.isVisible;
        this.isEnabled = builder.isEnabled;
        this.isChecked = builder.isChecked;
        this.isSelected = builder.isSelected;
        this.x = builder.x;
        this.y = builder.y;
        this.width = builder.width;
        this.height = builder.height;
        this.parentId = builder.parentId;
        this.ancestorIds = Collections.unmodifiableList(new ArrayList<>(builder.ancestorIds));
        this.inShadowDom = builder.inShadowDom;
        this.frameId = builder.frameId;
        this.frameUrl = builder.frameUrl;
        this.detectedType = builder.detectedType;
        this.computedStyle = Collections.unmodifiableMap(new HashMap<>(builder.computedStyle));
    }

    public static class Builder {
        private String id;
        private String tagName;
        private String type;
        private String role;
        private Map<String, String> attributes = new HashMap<>();
        private Set<String> classes = new HashSet<>();
        private String text;
        private String ariaLabel;
        private String placeholder;
        private String title;
        private String value;
        private boolean isVisible = true;
        private boolean isEnabled = true;
        private boolean isChecked = false;
        private boolean isSelected = false;
        private int x;
        private int y;
        private int width;
        private int height;
        private String parentId;
        private List<String> ancestorIds = new ArrayList<>();
        private boolean inShadowDom = false;
        private String frameId;
        private String frameUrl;
        private ElementType detectedType;
        private Map<String, String> computedStyle = new HashMap<>();

        public Builder id(String val) { this.id = val; return this; }
        public Builder tagName(String val) { this.tagName = val; return this; }
        public Builder type(String val) { this.type = val; return this; }
        public Builder role(String val) { this.role = val; return this; }
        public Builder attribute(String key, String value) { this.attributes.put(key, value); return this; }
        public Builder addClass(String val) { this.classes.add(val); return this; }
        public Builder text(String val) { this.text = val; return this; }
        public Builder ariaLabel(String val) { this.ariaLabel = val; return this; }
        public Builder placeholder(String val) { this.placeholder = val; return this; }
        public Builder title(String val) { this.title = val; return this; }
        public Builder value(String val) { this.value = val; return this; }
        public Builder isVisible(boolean val) { this.isVisible = val; return this; }
        public Builder isEnabled(boolean val) { this.isEnabled = val; return this; }
        public Builder isChecked(boolean val) { this.isChecked = val; return this; }
        public Builder isSelected(boolean val) { this.isSelected = val; return this; }
        public Builder bounds(int x, int y, int w, int h) {
            this.x = x; this.y = y; this.width = w; this.height = h;
            return this;
        }
        public Builder parentId(String val) { this.parentId = val; return this; }
        public Builder ancestorIds(List<String> val) { this.ancestorIds = val; return this; }
        public Builder addAncestorId(String val) { this.ancestorIds.add(val); return this; }
        public Builder inShadowDom(boolean val) { this.inShadowDom = val; return this; }
        public Builder frameId(String val) { this.frameId = val; return this; }
        public Builder frameUrl(String val) { this.frameUrl = val; return this; }
        public Builder detectedType(ElementType val) { this.detectedType = val; return this; }
        public Builder addComputedStyle(String key, String value) { this.computedStyle.put(key, value); return this; }
        public Builder computedStyle(Map<String, String> val) { this.computedStyle = val; return this; }

        public ScannedElement build() {
            if (detectedType == null) {
                detectedType = ElementType.detectFromElement(tagName, role, type, String.join(" ", classes), computedStyle);
            }
            return new ScannedElement(this);
        }
    }
}
