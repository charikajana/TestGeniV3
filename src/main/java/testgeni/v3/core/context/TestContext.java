package testgeni.v3.core.context;

import testgeni.v3.executor.ActionExecutorRegistry;
import testgeni.v3.finder.ElementFinder;
import testgeni.v3.orchestration.LocatorCacheRegistry;
import testgeni.v3.orchestration.TestConfig;
import testgeni.v3.parser.StepParser;
import testgeni.v3.scanner.PageScanner;
import testgeni.v3.executor.interceptor.ExecutionInterceptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service Registry / Container for TestGeni v3.
 * Decouples components and enables parallel execution by providing isolated contexts.
 */
public class TestContext {
    private final Map<Class<?>, Object> services = new HashMap<>();
    private final List<ExecutionInterceptor> interceptors = new ArrayList<>();
    private final TestConfig config;
    
    // Dialog State Management
    private String lastDialogMessage;
    private String nextDialogAction = "ACCEPT"; // Default behavior
    private String nextPromptText;

    public TestContext(TestConfig config) {
        this.config = config != null ? config : TestConfig.defaultConfig();
    }

    public <T> void register(Class<T> serviceClass, T instance) {
        services.put(serviceClass, instance);
    }

    public void addInterceptor(ExecutionInterceptor interceptor) {
        interceptors.add(interceptor);
    }

    public List<ExecutionInterceptor> getInterceptors() {
        return interceptors;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> serviceClass) {
        Object service = services.get(serviceClass);
        if (service == null) {
            throw new IllegalStateException("Service not registered: " + serviceClass.getName());
        }
        return (T) service;
    }

    public TestConfig getConfig() {
        return config;
    }

    // Convenience accessors for core components
    public StepParser getParser() { return get(StepParser.class); }
    public PageScanner getScanner() { return get(PageScanner.class); }
    public ElementFinder getFinder() { return get(ElementFinder.class); }
    public ActionExecutorRegistry getExecutorRegistry() { return get(ActionExecutorRegistry.class); }
    public LocatorCacheRegistry getCacheRegistry() { return get(LocatorCacheRegistry.class); }

    // Dialog State Accessors
    public String getLastDialogMessage() { return lastDialogMessage; }
    public void setLastDialogMessage(String message) { this.lastDialogMessage = message; }
    
    public String getNextDialogAction() { return nextDialogAction; }
    public void setNextDialogAction(String action) { this.nextDialogAction = action; }
    
    public String getNextPromptText() { return nextPromptText; }
    public void setNextPromptText(String text) { this.nextPromptText = text; }
}
