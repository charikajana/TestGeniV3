package testgeni.v3.executor;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import testgeni.v3.core.context.TestContext;
import testgeni.v3.core.domain.ActionResult;
import testgeni.v3.core.domain.ElementMatch;
import testgeni.v3.core.domain.StepIntent;

/**
 * Executor for SELECT/DESELECT actions (Dropdowns/Multi-selects).
 */
public class SelectActionExecutor extends BaseActionExecutor {

    @Override
    protected ActionResult performAction(TestContext context, Page page, ElementMatch match, StepIntent intent) throws Exception {
        if (match == null || match.locator == null) {
            return ActionResult.elementNotFound(intent.target);
        }

        Locator locator = match.locator;
        String val = intent.value;
        String tagName = (String) locator.evaluate("el => el.tagName.toLowerCase()");
        boolean isNativeSelect = "select".equals(tagName);

        if (intent.action == testgeni.v3.core.domain.ActionType.SELECT) {
            // 1. Handle Native Select (Standard way)
            if (isNativeSelect) {
                String[] values = val != null ? val.split("\\s*,\\s*") : new String[0];
                locator.selectOption(values);
                return ActionResult.success("Selected '" + val + "' in standard dropdown " + intent.target);
            }

            // 2. Handle Smart Calendar / Custom Dropdown
            try {
                // If it looks like a resolved date (yyyy-MM-dd), use smart navigation
                if (val.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    return navigateAndSelectDate(page, locator, val, intent);
                }

                locator.click();
                page.waitForTimeout(300); // Wait for animation
                
                // Look for the value as an element inside the picker/dropdown overlay
                Locator option = page.locator("text=\"" + val + "\"").first();
                if (option.isVisible()) {
                    option.click();
                    return ActionResult.success("Selected '" + val + "' from " + intent.target + " (Human-like interaction)");
                }
                
                // Fallback: If it's a date and we're looking for a number (e.g. day 15)
                if (val.matches("\\d+")) {
                     Locator day = page.locator(".calendar, .datepicker, .picker").locator("text=\"" + val + "\"").first();
                     if (day.isVisible()) {
                         day.click();
                         return ActionResult.success("Selected day " + val + " from calendar");
                     }
                }
            } catch (Exception e) {
                return ActionResult.failure("Could not perform selection for: " + intent.target + ". Error: " + e.getMessage());
            }
            
            return ActionResult.failure("Target " + intent.target + " is not a standard select and common selection patterns failed.");

        } else {
            // DESELECT Logic
            if (isNativeSelect) {
                if (val == null || val.isEmpty() || val.equalsIgnoreCase("all")) {
                    locator.evaluate("el => { for(let i=0; i<el.options.length; i++) el.options[i].selected = false; el.dispatchEvent(new Event('change')); }");
                    return ActionResult.success("Cleared all selections in " + intent.target);
                }
                locator.evaluate("el => { for(let i=0; i<el.options.length; i++) if(el.options[i].text === '" + val + "') el.options[i].selected = false; el.dispatchEvent(new Event('change')); }");
                return ActionResult.success("Deselected '" + val + "' in " + intent.target);
            }
            
            return ActionResult.failure("Deselection is only supported for native multi-select elements currently.");
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

            // Heuristic Month Navigation (Retry up to 12 times to avoid infinite loop)
            for (int i = 0; i < 24; i++) {
                String currentPickerText = page.locator(".calendar, .datepicker, .picker, .ui-datepicker").innerText().toLowerCase();
                
                if (currentPickerText.contains(monthName.toLowerCase()) && currentPickerText.contains(String.valueOf(targetDate.getYear()))) {
                    // We are in the right month! Find and click the day.
                    Locator dayElement = page.locator(".calendar, .datepicker, .picker, .ui-datepicker").locator("text=\"" + day + "\"").first();
                    dayElement.click();
                    return ActionResult.success("Selected date " + dateStr + " (Navigated to " + monthName + " " + targetDate.getYear() + ")");
                }

                // Need to move! 
                // Find "Next" or "Prev" based on date comparison
                java.time.LocalDate now = java.time.LocalDate.now();
                Locator navButton;
                if (targetDate.isAfter(now)) {
                    navButton = page.locator(".next, [aria-label*='Next'], [title*='Next'], .ui-datepicker-next, .fa-chevron-right").first();
                } else {
                    navButton = page.locator(".prev, [aria-label*='Prev'], [title*='Prev'], .ui-datepicker-prev, .fa-chevron-left").first();
                }

                if (navButton.isVisible()) {
                    navButton.click();
                    page.waitForTimeout(200);
                } else {
                    break; // Can't navigate further
                }
            }

            // Final fallback: try to just fill the input if navigation failed
            picker.fill(dateStr);
            return ActionResult.success("Calendar navigation failed but filled input with " + dateStr);

        } catch (Exception e) {
            return ActionResult.failure("Date navigation failed: " + e.getMessage());
        }
    }
}
