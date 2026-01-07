package testgeni.v3.executor;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.ScreenshotType;
import testgeni.v3.core.context.TestContext;
import testgeni.v3.core.domain.ActionResult;
import testgeni.v3.core.domain.ElementMatch;
import testgeni.v3.core.domain.StepIntent;
import testgeni.v3.util.V3Logger;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.File;

/**
 * Executor for take screenshot actions.
 */
public class ScreenshotActionExecutor extends BaseActionExecutor {

    private static final String SCREENSHOT_DIR = "Screenshots";

    public ScreenshotActionExecutor() {
        File dir = new File(SCREENSHOT_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @Override
    protected ActionResult performAction(TestContext context, Page page, ElementMatch match, StepIntent intent) throws Exception {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
        String filename = "screenshot_" + timestamp + ".png";
        String path = Paths.get(SCREENSHOT_DIR, filename).toString();

        Page.ScreenshotOptions options = new Page.ScreenshotOptions()
                .setPath(Paths.get(path))
                .setFullPage(true);

        page.screenshot(options);

        V3Logger.info("Screenshot saved: " + path);

        return new ActionResult.Builder(true)
                .message("Screenshot saved: " + path)
                .status(ActionResult.ResultStatus.SUCCESS)
                .build();
    }
}
