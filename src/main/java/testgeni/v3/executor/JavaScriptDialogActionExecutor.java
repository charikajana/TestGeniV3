package testgeni.v3.executor;

import com.microsoft.playwright.Page;
import testgeni.v3.core.context.TestContext;
import testgeni.v3.core.domain.ActionResult;
import testgeni.v3.core.domain.ElementMatch;
import testgeni.v3.core.domain.StepIntent;
import testgeni.v3.util.V3Logger;

/**
 * Executor for JavaScript Dialogs (Alert, Confirm, Prompt).
 * Instead of interacting with the DOM, this interacts with the Browser's native dialog handler
 * via the TestContext's dialog state.
 */
public class JavaScriptDialogActionExecutor extends BaseActionExecutor {

    @Override
    protected ActionResult performAction(TestContext context, Page page, ElementMatch match, StepIntent intent) throws Exception {
        String stepText = intent.cleanStep.toLowerCase();
        
        // 1. Verification Logic
        if (stepText.contains("verify") || stepText.contains("says")) {
            String expectedMessage = intent.value;
            String actualMessage = context.getLastDialogMessage();
            
            if (actualMessage == null) {
                // Wait briefly in case the alert is still appearing (async)
                long start = System.currentTimeMillis();
                while (System.currentTimeMillis() - start < 3000 && context.getLastDialogMessage() == null) {
                    Thread.sleep(200);
                }
                actualMessage = context.getLastDialogMessage();
            }
            
            if (actualMessage == null) {
                return ActionResult.failure("No JavaScript alert/dialog was detected.");
            }
            
            if (expectedMessage != null && !actualMessage.contains(expectedMessage)) {
                return ActionResult.failure(String.format("Alert message mismatch. Expected: [%s], Actual: [%s]", 
                    expectedMessage, actualMessage));
            }
            
            return ActionResult.success("Verified alert message: " + actualMessage);
        }
        
        // 2. Interaction Logic (Accept/Dismiss)
        if (stepText.contains("dismiss")) {
            context.setNextDialogAction("DISMISS");
            return ActionResult.success("Configured next dialog to be DISMISSED");
        }
        
        if (stepText.contains("accept")) {
            context.setNextDialogAction("ACCEPT");
            return ActionResult.success("Configured next dialog to be ACCEPTED");
        }
        
        if (stepText.contains("enter") || stepText.contains("prompt")) {
            if (intent.value != null) {
                context.setNextPromptText(intent.value);
                context.setNextDialogAction("ACCEPT");
                return ActionResult.success("Configured prompt with input: " + intent.value);
            }
        }

        return ActionResult.success("Handled JavaScript dialog action");
    }
}
