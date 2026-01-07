package testgeni.v3.executor;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.MouseButton;
import testgeni.v3.core.context.TestContext;
import testgeni.v3.core.domain.ActionResult;
import testgeni.v3.core.domain.ElementMatch;
import testgeni.v3.core.domain.StepIntent;

/**
 * Robust executor for CLICK actions.
 * Handles:
 * - Radio/Checkbox label clicks (Best practice)
 * - Wait for element to be stable
 * - Scrolling into view
 */
public class ClickActionExecutor extends BaseActionExecutor {

    @Override
    protected ActionResult performAction(TestContext context, Page page, ElementMatch match, StepIntent intent) throws Exception {
        if (match == null || match.locator == null) {
            return ActionResult.elementNotFound(intent.target);
        }

        Locator locator = match.locator;
        
        // ENHANCEMENT: Find clickable parent if current element is not interactive
        Locator clickableElement = findClickableParent(locator, page);
        final Locator finalLocator = (clickableElement != null) ? clickableElement : locator;
        
        // 1. Robust check for Radio/Checkbox
        String tagName = (String) finalLocator.evaluate("el => el.tagName.toLowerCase()");
        String type = (String) finalLocator.evaluate("el => el.type");
        
        if ("input".equals(tagName) && ("radio".equals(type) || "checkbox".equals(type))) {
            String id = (String) finalLocator.evaluate("el => el.id");
            if (id != null && !id.isEmpty()) {
                Locator label = page.locator("label[for='" + id + "']");
                if (label.count() > 0 && label.isVisible()) {
                    label.click();
                    return ActionResult.success("Clicked " + intent.target + " (via label)");
                }
            }
        }

        // 2. Determine Click Options
        Locator.ClickOptions options = new Locator.ClickOptions()
            .setButton(getMouseButton(intent))
            .setForce(intent.getModifier("force") != null)
            .setTimeout(intent.getEffectiveTimeout());

        // 3. Execution based on action type
        if (intent.action == testgeni.v3.core.domain.ActionType.DOUBLE_CLICK) {
            finalLocator.dblclick(new Locator.DblclickOptions()
                .setForce(options.force)
                .setTimeout((double)options.timeout));
        } else if ("true".equals(intent.getModifier("switchWindow"))) {
            // Atomic Click and Switch
            Page popup = page.waitForPopup(() -> {
                try {
                    finalLocator.click(options);
                } catch (Exception e) {
                    throw new RuntimeException("Click failed before popup: " + e.getMessage());
                }
            });
            
            if (popup != null) {
                popup.bringToFront();
                // Wait for page to be fully loaded and focus to register
                popup.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
                try { Thread.sleep(1000); } catch (InterruptedException e) {}
            }
            return ActionResult.success("Clicked " + intent.target + " and switched to new window/tab");
        } else {
            finalLocator.click(options);
        }

        return ActionResult.success(intent.action.name() + " performed on " + intent.target);
    }

    private MouseButton getMouseButton(StepIntent intent) {
        if (intent.action == testgeni.v3.core.domain.ActionType.RIGHT_CLICK) return MouseButton.RIGHT;
        String btn = intent.getModifier("button");
        if ("right".equalsIgnoreCase(btn)) return MouseButton.RIGHT;
        if ("middle".equalsIgnoreCase(btn)) return MouseButton.MIDDLE;
        return MouseButton.LEFT;
    }
    
    /**
     * Finds a clickable parent element if the matched element is not interactive.
     * Climbs up to 3 levels in the DOM tree looking for clickable elements.
     * @param locator The originally matched locator
     * @param page The page context
     * @return Clickable parent locator, or null if current element is already clickable
     */
    private Locator findClickableParent(Locator locator, Page page) {
        try {
            // Check if current element is already clickable
            Boolean isClickable = (Boolean) locator.evaluate(
                "el => { " +
                "const tag = el.tagName.toLowerCase(); " +
                "const role = el.getAttribute('role'); " +
                "const onclick = el.getAttribute('onclick') || el.onclick; " +
                "const classList = el.className || ''; " +
                "if (tag === 'button' || tag === 'a' || " +
                "    (tag === 'input' && ['button', 'submit', 'reset'].includes(el.type)) || " +
                "    role === 'button' || onclick || " +
                "    classList.includes('btn') || classList.includes('button') || classList.includes('clickable')) { " +
                "  return true; " +
                "} " +
                "return false; " +
                "}"
            );
            
            if (Boolean.TRUE.equals(isClickable)) {
                return null; // Already clickable, no need to climb
            }
            
            // Climb up to 3 levels to find clickable parent
            for (int level = 1; level <= 3; level++) {
                String parentSelector = "xpath=./ancestor::*[" + level + "]";
                Locator parent = locator.locator(parentSelector);
                
                if (parent.count() == 0) {
                    break; // No more parents
                }
                
                Boolean parentClickable = (Boolean) parent.evaluate(
                    "el => { " +
                    "const tag = el.tagName.toLowerCase(); " +
                    "const role = el.getAttribute('role'); " +
                    "const onclick = el.getAttribute('onclick') || el.onclick; " +
                    "const classList = el.className || ''; " +
                    "if (tag === 'button' || tag === 'a' || tag === 'li' || " +
                    "    (tag === 'input' && ['button', 'submit', 'reset'].includes(el.type)) || " +
                    "    role === 'button' || onclick || " +
                    "    classList.includes('btn') || classList.includes('button') || classList.includes('clickable')) { " +
                    "  return true; " +
                    "} " +
                    "return false; " +
                    "}"
                );
                
                if (Boolean.TRUE.equals(parentClickable)) {
                    String parentTag = (String) parent.evaluate("el => el.tagName.toLowerCase()");
                    String currentTag = (String) locator.evaluate("el => el.tagName.toLowerCase()");
                    testgeni.v3.util.V3Logger.debug(
                        "Found clickable parent: Upgraded <" + currentTag + "> to <" + parentTag + "> (level " + level + ")"
                    );
                    return parent;
                }
            }
            
        } catch (Exception e) {
            testgeni.v3.util.V3Logger.debug("Error finding clickable parent: " + e.getMessage());
        }
        
        return null; // No clickable parent found, use original element
    }
}
