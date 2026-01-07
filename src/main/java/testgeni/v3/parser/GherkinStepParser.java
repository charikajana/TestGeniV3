package testgeni.v3.parser;

import testgeni.v3.core.domain.ActionType;
import testgeni.v3.core.domain.ElementType;
import testgeni.v3.core.domain.StepIntent;
import testgeni.v3.util.V3Logger;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses Gherkin steps into StepIntent objects using a decoupled pipeline.
 */
public class GherkinStepParser implements StepParser {
    
    private final ActionVerbRegistry verbRegistry;
    private final StepCleaner cleaner;
    private final ContextExtractor contextExtractor;
    private final IntentSpecializer specializer;
    private final TargetExtractor targetExtractor;
    
    public GherkinStepParser() {
        this(new ActionVerbRegistry());
    }
    
    public GherkinStepParser(ActionVerbRegistry verbRegistry) {
        this.verbRegistry = verbRegistry;
        this.cleaner = new StepCleaner();
        this.contextExtractor = new ContextExtractor();
        this.specializer = new IntentSpecializer();
        this.targetExtractor = new TargetExtractor(verbRegistry);
    }
    
    @Override
    public StepIntent parse(String stepText) throws ParseException {
        return parse(stepText, -1, null, null);
    }
    
    @Override
    public StepIntent parse(String stepText, int lineNumber, String featureFile, String scenarioName) 
            throws ParseException {
        
        if (stepText == null || stepText.trim().isEmpty()) {
            throw new ParseException(stepText, lineNumber, "Step text is empty");
        }
        
        V3Logger.trace("Parser Input", stepText);
        
        try {
            // Stage 1: Cleaning
            String cleanStep = cleaner.clean(stepText);
            V3Logger.trace("Clean Step", cleanStep);
            
            // Stage 2: Action Discovery
            boolean isClickAndSwitch = hasSwitchWindow(cleanStep);
            ActionType action = verbRegistry.findActionInText(cleanStep);
            ActionType originalAction = action;
            
            if (isClickAndSwitch && action == ActionType.CLICK) {
                action = ActionType.SWITCH_WINDOW;
            }
            
            if (action == null) {
                throw new ParseException(stepText, lineNumber, "Could not identify action type in step: " + cleanStep);
            }
            
            // Special check for Dialogs (Prompts/Alerts)
            String alertMessage = contextExtractor.extractAlertMessage(cleanStep);
            if (alertMessage != null || cleanStep.toLowerCase().contains("confirm") || cleanStep.toLowerCase().contains("prompt")) {
                if (action != ActionType.CLICK) { // Don't override Click which triggers the alert
                    action = ActionType.HANDLE_ALERT;
                }
            }
            
            // Stage 3: Value Extraction
            List<String> quotedValues = extractQuotedStrings(cleanStep);
            String primaryValue = quotedValues.isEmpty() ? null : quotedValues.get(0);
            
            if (primaryValue == null) {
                primaryValue = extractUnquotedValue(cleanStep, action);
            }
            
            // Special handling for REMOVE unquoted values
            if (action == ActionType.REMOVE && primaryValue == null) {
                primaryValue = contextExtractor.extractRemoveValue(cleanStep);
            }
            
            // Stage 4: Target Extraction
            String stepForTarget = cleanStep;
            ActionType actionForTarget = originalAction;
            if (isClickAndSwitch) {
                stepForTarget = cleanStep.replaceAll("(?i)\\s+and\\s+switch\\s+to\\s+(new|parent|main)\\s+window", "");
                actionForTarget = ActionType.CLICK;
            }
            
            String target = targetExtractor.extract(stepForTarget, actionForTarget, quotedValues, primaryValue);
            
            // Stage 5: Negation & Specialization
            boolean negated = isNegated(cleanStep);
            String verificationAttribute = null;
            if (action == ActionType.VERIFY) {
                verificationAttribute = specializer.extractVerificationAttribute(cleanStep);
                action = specializer.specialize(action, cleanStep, verificationAttribute);
                
                String urlModifier = specializer.extractUrlModifier(cleanStep);
                if (urlModifier != null) {
                    verificationAttribute = "url_" + urlModifier;
                }
            }
            
            // Stage 6: Context Extraction
            StepIntent.Builder builder = new StepIntent.Builder(action, target);
            populateContext(builder, cleanStep);
            
            // Stage 7: Metadata & Finalization
            builder.originalStep(stepText)
                   .cleanStep(cleanStep)
                   .negated(negated)
                   .elementType(detectElementType(cleanStep, target, action))
                   .lineNumber(lineNumber)
                   .featureFile(featureFile)
                   .scenarioName(scenarioName);
            
            processValues(builder, primaryValue, quotedValues, cleanStep, action);
            
            if (verificationAttribute != null) {
                builder.verificationAttribute(verificationAttribute);
            }
            
            if (isClickAndSwitch || hasSwitchWindow(cleanStep)) {
                builder.modifier("switchWindow", "true");
                if (action == ActionType.SWITCH_WINDOW) builder.modifier("performClick", "true");
            }
            
            handleWaitActions(builder, cleanStep, action, primaryValue);
            handleProgressVerifications(builder, cleanStep);
            
            return builder.build();
            
        } catch (ParseException e) {
            throw e;
        } catch (Exception e) {
            throw new ParseException(stepText, lineNumber, "Unexpected error: " + e.getMessage());
        }
    }

    private void populateContext(StepIntent.Builder builder, String step) {
        String scoping = contextExtractor.extractScoping(step);
        builder.scopingContext(scoping)
               .frameAnchor(contextExtractor.extractFrame(step))
               .tableRowCondition(contextExtractor.extractTableRow(step))
               .tableColumn(contextExtractor.extractTableColumn(step))
               .tooltipOf(contextExtractor.extractTooltip(step))
               .value(contextExtractor.extractAlertMessage(step));
        
        Integer windowCount = contextExtractor.extractWindowCount(step);
        if (windowCount != null) builder.value(windowCount.toString());
        
        Integer elementIndex = contextExtractor.extractElementIndex(step);
        if (scoping != null) {
            Integer contextIndex = contextExtractor.extractElementIndex(scoping);
            builder.contextIndex(contextIndex);
            if (elementIndex != null && elementIndex.equals(contextIndex)) {
                elementIndex = contextExtractor.extractElementIndex(step.replace(scoping, ""));
            }
        }
        builder.elementIndex(elementIndex);
    }

    private void processValues(StepIntent.Builder builder, String primary, List<String> quoted, String step, ActionType action) {
        if (primary != null) {
            if (primary.startsWith("/") && primary.endsWith("/") && primary.length() > 2) {
                String pattern = primary.substring(1, primary.length() - 1);
                builder.value(pattern).isRegex(true).regexPattern(pattern);
            } else {
                String resolved = DateResolver.resolveDate(primary);
                builder.value(resolved != null ? resolved : primary);
                if (resolved != null) builder.metadata("original_date_expr", primary);
            }
            
            if (action == ActionType.NAVIGATE) builder.url(primary);
            else if (action == ActionType.PRESS_KEY) builder.key(primary);
            else if (action == ActionType.UPLOAD_FILE) builder.filePath(primary);
        }
        
        List<String> multi = extractMultipleValues(step, quoted);
        if (!multi.isEmpty()) multi.forEach(builder::addValue);
        else if (!quoted.isEmpty()) quoted.forEach(builder::addValue);
    }

    private void handleWaitActions(StepIntent.Builder builder, String step, ActionType action, String primary) {
        if (action == ActionType.WAIT) {
            Integer waitTime = extractWaitDuration(step);
            if (waitTime != null) {
                builder.waitDurationSeconds(waitTime);
                if (primary == null) builder.value(waitTime.toString());
            }
        }
    }

    private void handleProgressVerifications(StepIntent.Builder builder, String step) {
        Matcher matcher = RegexPatterns.WAIT_FOR_PROGRESS.matcher(step);
        if (matcher.find()) {
            builder.value(matcher.group(2)).waitCondition("progress_reach");
        }
    }

    // Existing helper methods that are too simple/integrated to move, or used internally
    private List<String> extractQuotedStrings(String step) {
        List<String> values = new ArrayList<>();
        Matcher matcher = RegexPatterns.QUOTED_STRING.matcher(step);
        while (matcher.find()) values.add(matcher.group(1));
        return values;
    }

    private boolean isNegated(String step) {
        return RegexPatterns.NEGATION.matcher(step).find();
    }

    private boolean hasSwitchWindow(String step) {
        return RegexPatterns.CLICK_AND_SWITCH.matcher(step).find();
    }

    private String extractUnquotedValue(String step, ActionType action) {
        if (!EnumSet.of(ActionType.FILL, ActionType.TYPE, ActionType.VERIFY, ActionType.NAVIGATE, 
                        ActionType.PRESS_KEY, ActionType.UPLOAD_FILE, ActionType.SELECT).contains(action)) {
            return null;
        }
        String lower = step.toLowerCase();
        for (String delim : new String[]{" to ", " as ", " is ", " contains ", " should be "}) {
            if (lower.contains(delim)) {
                String val = step.substring(lower.indexOf(delim) + delim.length()).trim();
                return val.replaceAll("(?i)\\s+(inside|within|in iframe|in the row|in .+ column).*$", "");
            }
        }
        return null;
    }

    private List<String> extractMultipleValues(String step, List<String> quoted) {
        return step.matches(".*['\"][^'\"]+['\"]\\s+and\\s+['\"].*") ? new ArrayList<>(quoted) : Collections.emptyList();
    }

    private Integer extractWaitDuration(String step) {
        Matcher m = RegexPatterns.WAIT_DURATION.matcher(step);
        if (m.find()) {
            int v = Integer.parseInt(m.group(1));
            return m.group(2).toLowerCase().startsWith("m") ? v / 1000 : v;
        }
        return null;
    }

    private ElementType detectElementType(String step, String target, ActionType action) {
        String lowerStep = step.toLowerCase();
        String lowerTarget = target != null ? target.toLowerCase() : "";
        if (lowerStep.contains("button") || lowerTarget.contains("button")) return ElementType.BUTTON;
        if (lowerStep.contains("link") || lowerTarget.contains("link")) return ElementType.LINK;
        if (lowerStep.contains("checkbox") || lowerTarget.contains("checkbox")) return ElementType.CHECKBOX;
        if (lowerStep.contains("radio") || lowerTarget.contains("radio")) return ElementType.RADIO;
        if (lowerStep.contains("dropdown") || lowerTarget.contains("dropdown")) return ElementType.DROPDOWN;
        if (lowerStep.contains("input") || lowerTarget.contains("input")) return ElementType.INPUT;
        if (lowerStep.contains("textarea") || lowerTarget.contains("textarea")) return ElementType.TEXTAREA;
        if (lowerStep.contains("table") || lowerTarget.contains("table")) return ElementType.TABLE;
        if (lowerStep.contains("menu") || lowerTarget.contains("menu")) return ElementType.MENU;
        if (lowerStep.contains("icon") || lowerTarget.contains("icon")) return ElementType.ICON;
        return null;
    }

    @Override
    public List<StepIntent> parseSteps(List<String> steps) throws ParseException {
        List<StepIntent> intents = new ArrayList<>();
        for (int i = 0; i < steps.size(); i++) intents.add(parse(steps.get(i), i + 1, null, null));
        return intents;
    }

    @Override
    public boolean canParse(String stepText) { return getParseError(stepText) == null; }

    @Override
    public String getParseError(String stepText) {
        try { parse(stepText); return null; } catch (ParseException e) { return e.getReason(); }
    }
}
