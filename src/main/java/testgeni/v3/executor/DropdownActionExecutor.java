package testgeni.v3.executor;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import testgeni.v3.core.context.TestContext;
import testgeni.v3.core.domain.ActionResult;
import testgeni.v3.core.domain.ActionType;
import testgeni.v3.core.domain.ElementMatch;
import testgeni.v3.core.domain.StepIntent;
import testgeni.v3.util.V3Logger;

import java.util.List;

/**
 * Advanced Executor for Dropdowns, Auto-completes, and Selectable lists.
 * Handles both native HTML <select> and custom UI components (React-Select, etc.).
 */
public class DropdownActionExecutor extends BaseActionExecutor {

    @Override
    protected ActionResult performAction(TestContext context, Page page, ElementMatch match, StepIntent intent) throws Exception {
        if (match == null || match.locator == null) {
            return ActionResult.elementNotFound(intent.target);
        }

        Locator container = match.locator;
        String val = intent.value;
        String tagName = (String) container.evaluate("el => el.tagName.toLowerCase()");
        boolean isNativeSelect = "select".equals(tagName);

        if (intent.action == ActionType.SELECT) {
            return handleSelect(page, container, val, isNativeSelect, intent);
        } else if (intent.action == ActionType.DESELECT || intent.action == ActionType.REMOVE) {
            return handleRemove(page, container, val, isNativeSelect, intent);
        }

        return ActionResult.failure("Unsupported action for DropdownActionExecutor: " + intent.action);
    }

    private ActionResult handleSelect(Page page, Locator container, String value, boolean isNative, StepIntent intent) {
        if (value == null) return ActionResult.failure("No value specified for selection in " + intent.target);

        if (isNative) {
            String[] values = value.split("\\s*,\\s*");
            container.selectOption(values);
            return ActionResult.success("Selected '" + value + "' in native dropdown " + intent.target);
        }

        // Custom Dropdown / Auto-complete logic
        try {
            V3Logger.debug("Attempting custom selection for: " + value);
            
            // If it looks like a resolved date (yyyy-MM-dd), use smart navigation
            if (value.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return navigateAndSelectDate(page, container, value, intent);
            }

            // 1. If it's an input field (Auto-complete), type the value first if it's currently empty
            String currentVal = container.inputValue();
            if (currentVal == null || currentVal.isEmpty()) {
                container.click();
                container.fill(value);
                page.waitForTimeout(500); // Wait for results to filter
            } else {
                container.click();
                page.waitForTimeout(300);
            }

            // 2. Look for the option in the resulting listbox / dropdown overlay
            // We search for elements with roles 'option', 'listitem' or just text match
            Locator option = page.locator("role=option, .option, .item, [class*='option'], [class*='item']")
                .filter(new Locator.FilterOptions().setHasText(value)).first();
            
            if (option.isVisible()) {
                option.click();
                return ActionResult.success("Selected '" + value + "' from custom dropdown " + intent.target);
            }

            // 3. Last ditch effort: search global text
            Locator globalOption = page.locator("text=\"" + value + "\"").last();
            if (globalOption.isVisible()) {
                globalOption.click();
                return ActionResult.success("Selected '" + value + "' using global text match");
            }

            return ActionResult.failure("Found the dropdown but could not find option matching: " + value);
        } catch (Exception e) {
            return ActionResult.failure("Custom selection failed: " + e.getMessage());
        }
    }

    private ActionResult handleRemove(Page page, Locator container, String value, boolean isNative, StepIntent intent) {
        if (isNative) {
            if (value == null || value.equalsIgnoreCase("all")) {
                container.evaluate("el => { for(let i=0; i<el.options.length; i++) el.options[i].selected = false; el.dispatchEvent(new Event('change')); }");
                return ActionResult.success("Cleared all selections in " + intent.target);
            }
            container.evaluate("el => { for(let i=0; i<el.options.length; i++) if(el.options[i].text === '" + value + "') el.options[i].selected = false; el.dispatchEvent(new Event('change')); }");
            return ActionResult.success("Deselected '" + value + "' in native dropdown " + intent.target);
        }

        // Custom Removal logic (Tags/Pills)
        try {
            if (value == null) return ActionResult.failure("No item value specified to remove from " + intent.target);

            V3Logger.debug("Attempting to find and remove tag: " + value);

            // Strategy: Find a container (tag/pill) that has the text 'value' and click its remove icon
            // Common classes: .multi-value, .tag, .pill, [class*='multi-value']
            Locator tag = page.locator(".tag, .pill, [class*='multiValue'], [class*='MultiValue'], [class*='tag'], [class*='pill']")
                .filter(new Locator.FilterOptions().setHasText(value)).first();

            if (tag.isVisible()) {
                // Find the close icon inside this tag
                // Usually an SVG, a button with 'x', or a div with 'remove' in class
                Locator removeIcon = tag.locator("svg, button, [class*='remove'], [class*='Remove'], [class*='close'], [aria-label*='Remove']").first();
                if (removeIcon.isVisible()) {
                    removeIcon.click();
                    return ActionResult.success("Removed '" + value + "' tag from " + intent.target);
                } else {
                    // If no specific icon, try clicking the tag itself if it's small
                    tag.click();
                    return ActionResult.success("Clicked tag '" + value + "' to attempt removal (no icon found)");
                }
            }

            return ActionResult.failure("Could not find a selected item/tag matching '" + value + "' inside " + intent.target);
        } catch (Exception e) {
            return ActionResult.failure("Custom item removal failed: " + e.getMessage());
        }
    }

    /**
     * Smart navigation for Date Pickers. 
     * Handles month/year switching by finding "Next" buttons.
     */
    private ActionResult navigateAndSelectDate(Page page, Locator picker, String dateStr, StepIntent intent) {
        try {
            java.time.LocalDate targetDate = java.time.LocalDate.parse(dateStr);
            String day = String.valueOf(targetDate.getDayOfMonth());
            String monthName = targetDate.getMonth().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH);
            
            picker.click();
            page.waitForTimeout(500);

            // Heuristic Month Navigation (Retry up to 24 times)
            for (int i = 0; i < 24; i++) {
                String currentPickerText = page.locator(".calendar, .datepicker, .picker, .ui-datepicker, .react-datepicker").innerText().toLowerCase();
                
                if (currentPickerText.contains(monthName.toLowerCase()) && currentPickerText.contains(String.valueOf(targetDate.getYear()))) {
                    // We are in the right month! Find and click the day.
                    Locator dayElement = page.locator(".calendar, .datepicker, .picker, .ui-datepicker, .react-datepicker").locator("text=\"" + day + "\"").first();
                    dayElement.click();
                    return ActionResult.success("Selected date " + dateStr + " (Navigated to " + monthName + " " + targetDate.getYear() + ")");
                }

                // Need to move! 
                java.time.LocalDate now = java.time.LocalDate.now();
                Locator navButton;
                if (targetDate.isAfter(now)) {
                    navButton = page.locator(".next, [aria-label*='Next'], [title*='Next'], .ui-datepicker-next, .fa-chevron-right, .react-datepicker__navigation--next").first();
                } else {
                    navButton = page.locator(".prev, [aria-label*='Prev'], [title*='Prev'], .ui-datepicker-prev, .fa-chevron-left, .react-datepicker__navigation--previous").first();
                }

                if (navButton.isVisible()) {
                    navButton.click();
                    page.waitForTimeout(200);
                } else {
                    break; 
                }
            }

            picker.fill(dateStr);
            return ActionResult.success("Calendar navigation failed but filled input with " + dateStr);

        } catch (Exception e) {
            return ActionResult.failure("Date navigation failed: " + e.getMessage());
        }
    }
}
