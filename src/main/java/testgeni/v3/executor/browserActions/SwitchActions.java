package testgeni.v3.executor.browserActions;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import testgeni.v3.core.domain.ActionResult;
import testgeni.v3.core.domain.ElementMatch;
import testgeni.v3.core.domain.StepIntent;
import testgeni.v3.orchestration.PageTracker;
import testgeni.v3.util.V3Logger;

import java.util.List;

/**
 * Handles all window/tab/frame switching actions.
 * Centralizes browser context switching logic for better maintainability.
 */
public class SwitchActions {

    /**
     * Handle window/tab switch operations.
     * Supports:
     * - Click and switch to new window
     * - Switch by index
     * - Switch to new/next/latest/last/recent/child/popup/tab window
     * - Switch to parent/main/first/home/original window
     * - Switch by title or URL match
     */
    public static ActionResult handleWindowSwitch(Page page, ElementMatch match, StepIntent intent) {
        BrowserContext context = page.context();
        List<Page> pages = context.pages();
        String target = intent.target != null ? intent.target.toLowerCase() : "";
        
        // STEP 1: If performClick modifier is present, use waitForPopup to click and capture new window
        boolean shouldPerformClick = "true".equals(intent.getModifier("performClick"));
        if (shouldPerformClick && match != null && match.locator != null) {
            return handleClickAndSwitchToNewWindow(page, match, intent);
        }

        // STEP 2: Regular window switching (without click)
        // Refresh pages list
        pages = context.pages();
        V3Logger.trace("Window Count", String.valueOf(pages.size()));
        
        // 1. Switch by Index (e.g. "2nd window")
        if (intent.elementIndex != null && intent.elementIndex > 0 && intent.elementIndex <= pages.size()) {
            return switchToWindowByIndex(pages, intent.elementIndex);
        }

        // 2. Switch to newest/new/next/latest/last/recent/child/popup/tab window
        if (target.contains("new") || target.contains("next") || target.contains("latest") || 
            target.contains("last") || target.contains("recent") || target.contains("child") ||
            target.contains("popup") || target.contains("tab")) {
            return switchToNewestWindow(pages);
        }

        // 3. Switch to "Parent", "Main", "First", "Home", or "Original" window
        if (target.contains("parent") || target.contains("main") || target.contains("first") || 
            target.contains("home") || target.contains("original")) {
            return switchToMainWindow(pages);
        }

        // 4. Switch by Title or URL from the VALUE (quoted string)
        String searchValue = intent.value != null ? intent.value.toLowerCase() : target;
        return switchToWindowByTitleOrUrl(pages, searchValue);
    }

    /**
     * Click an element that opens a new window and switch to it.
     */
    private static ActionResult handleClickAndSwitchToNewWindow(Page page, ElementMatch match, StepIntent intent) {
        try {
            V3Logger.debug("Clicking element and waiting for new popup window...");
            
            // Use Playwright's waitForPopup - the PROPER way to handle click-and-switch
            Page popupPage = page.waitForPopup(() -> {
                match.locator.click();
                V3Logger.success("Clicked element: " + intent.target);
            });
            
            // Wait for the popup to fully load
            popupPage.waitForLoadState();
            V3Logger.success("New window opened: " + popupPage.url());
            V3Logger.trace("New Window Title", popupPage.title());
            
            // Bring the popup to front (switch focus to it)
            popupPage.bringToFront();
            
            // Track this page as the active page
            PageTracker.setLastSwitchedPage(popupPage);
            
            // Give the browser a moment to update focus state
            try { Thread.sleep(100); } catch (InterruptedException e) {}
            
            V3Logger.success("Switched to new window");
            
            return ActionResult.success("Clicked and switched to new window: " + popupPage.title());
            
        } catch (Exception e) {
            V3Logger.error("Failed to click element or switch to new window", e);
            return ActionResult.failure("Failed to click and switch to new window: " + e.getMessage());
        }
    }

    /**
     * Switch to window by index (1-based).
     */
    private static ActionResult switchToWindowByIndex(List<Page> pages, int index) {
        Page targetPage = pages.get(index - 1);
        targetPage.bringToFront();
        PageTracker.setLastSwitchedPage(targetPage);
        try { Thread.sleep(100); } catch (InterruptedException e) {}
        return ActionResult.success("Switched to window at index " + index);
    }

    /**
     * Switch to the newest (last created) window.
     */
    private static ActionResult switchToNewestWindow(List<Page> pages) {
        if (pages.size() > 1) {
            Page targetPage = pages.get(pages.size() - 1);
            targetPage.bringToFront();
            PageTracker.setLastSwitchedPage(targetPage);
            try { Thread.sleep(100); } catch (InterruptedException e) {}
            V3Logger.success("Switched to the newest window");
            return ActionResult.success("Switched to the newest window");
        } else {
            return ActionResult.failure("No new window found. Only 1 window open.");
        }
    }

    /**
     * Switch to the main (first/parent) window.
     */
    private static ActionResult switchToMainWindow(List<Page> pages) {
        Page targetPage = pages.get(0);
        targetPage.bringToFront();
        PageTracker.setLastSwitchedPage(targetPage);
        try { Thread.sleep(100); } catch (InterruptedException e) {}
        V3Logger.success("Switched back to the main window");
        return ActionResult.success("Switched back to the main window");
    }

    /**
     * Switch to window by matching title or URL.
     */
    private static ActionResult switchToWindowByTitleOrUrl(List<Page> pages, String searchValue) {
        for (Page p : pages) {
            String title = "";
            String url = "";
            try {
                title = p.title().toLowerCase();
                url = p.url().toLowerCase();
            } catch (Exception e) {
                // Ignore pages that are not accessible
                V3Logger.trace("Page Check", "Skipping inaccessible page");
                continue;
            }
            if (title.contains(searchValue) || url.contains(searchValue)) {
                p.bringToFront();
                PageTracker.setLastSwitchedPage(p);
                try { Thread.sleep(100); } catch (InterruptedException e) {}
                V3Logger.success("Switched to window: " + p.title());
                return ActionResult.success("Switched to window matching: " + searchValue);
            }
        }

        return ActionResult.failure("Could not find a window matching: " + searchValue);
    }

    /**
     * Handle frame switch operations.
     */
    public static ActionResult handleFrameSwitch(Page page, ElementMatch match, StepIntent intent) {
        // In Playwright, we don't necessarily "switch" global state like Selenium,
        // but the TestRunner uses the frame from the ScannedElement.
        // However, if the user explicitly says "Switch to frame", we validate it exists.
        
        if (match != null && match.locator != null) {
            // Note: In V3, frame-level execution is handled by passing the frame to the executor.
            // This action confirms the frame is interactable.
            return ActionResult.success("Context switched to frame: " + intent.target);
        }
        
        return ActionResult.failure("Could not find frame: " + intent.target);
    }
}
