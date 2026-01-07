package testgeni.v3.executor;

import testgeni.v3.core.domain.ActionType;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry for mapping ActionTypes to their specific executors.
 */
public class ActionExecutorRegistry {

    private final Map<ActionType, ActionExecutor> executors = new HashMap<>();

    public ActionExecutorRegistry() {
        registerDefaults();
    }

    private void registerDefaults() {
        ActionExecutor click = new ClickActionExecutor();
        executors.put(ActionType.CLICK, click);
        executors.put(ActionType.DOUBLE_CLICK, click); // Same logic for now
        executors.put(ActionType.RIGHT_CLICK, click);

        executors.put(ActionType.FILL, new FillActionExecutor());
        executors.put(ActionType.TYPE, new FillActionExecutor());
        executors.put(ActionType.CLEAR, new FillActionExecutor());

        executors.put(ActionType.VERIFY, new VerifyActionExecutor());
        executors.put(ActionType.VERIFY_STATE, new VerifyActionExecutor());
        executors.put(ActionType.VERIFY_TEXT, new VerifyActionExecutor());
        
        ActionExecutor nav = new NavigateActionExecutor();
        executors.put(ActionType.NAVIGATE, nav);
        executors.put(ActionType.GO_BACK, nav);
        executors.put(ActionType.GO_FORWARD, nav);
        executors.put(ActionType.REFRESH, nav);

        ActionExecutor dropdown = new DropdownActionExecutor();
        executors.put(ActionType.SELECT, dropdown);
        executors.put(ActionType.DESELECT, dropdown);
        executors.put(ActionType.REMOVE, dropdown);

        executors.put(ActionType.WAIT, new WaitActionExecutor());
        executors.put(ActionType.HOVER, new HoverActionExecutor());
        executors.put(ActionType.SCROLL, new ScrollActionExecutor());
        
        ContextActionExecutor context = new ContextActionExecutor();
        executors.put(ActionType.SWITCH_WINDOW, context);
        executors.put(ActionType.SWITCH_TAB, context);
        executors.put(ActionType.SWITCH_FRAME, context);
        executors.put(ActionType.CLOSE_WINDOW, context);
        
        executors.put(ActionType.HANDLE_ALERT, new JavaScriptDialogActionExecutor());
        executors.put(ActionType.SCREENSHOT, new ScreenshotActionExecutor());
    }

    public ActionExecutor getExecutor(ActionType type) {
        return executors.get(type);
    }
}
