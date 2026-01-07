# TestGeni V3 - Implementation Roadmap

**Version**: 3.0  
**Date**: 2026-01-06  
**Status**: In Progress

---

## 📋 Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Implementation Status](#implementation-status)
3. [Class Dependency Map](#class-dependency-map)
4. [Detailed Class Specifications](#detailed-class-specifications)
5. [Implementation Order](#implementation-order)
6. [Usage Examples](#usage-examples)

---

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                     GHERKIN FEATURE FILE                        │
│  Given I navigate to "https://example.com"                      │
│  When I fill "John" in First Name                               │
│  Then I verify "Welcome" is displayed                           │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│                    LAYER 1: PARSING                             │
│  ┌──────────────┐     ┌───────────────────┐                    │
│  │ StepParser   │ ──► │ ActionVerbRegistry│                    │
│  └──────────────┘     └───────────────────┘                    │
│         ↓                                                        │
│  ┌──────────────────────────────┐                              │
│  │ StepIntent (immutable data)  │                              │
│  │ - action: FILL                │                              │
│  │ - target: "First Name"        │                              │
│  │ - value: "John"               │                              │
│  └──────────────────────────────┘                              │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│                   LAYER 2: DOM SCANNING                         │
│  ┌──────────────┐     ┌───────────────┐                        │
│  │ DomScanner   │ ──► │ DomSnapshot   │                        │
│  └──────────────┘     └───────────────┘                        │
│         ↓                     ↓                                  │
│  ┌──────────────────┐  ┌─────────────────┐                     │
│  │ PageScanCache    │  │ ElementCandidate│ (List)              │
│  └──────────────────┘  └─────────────────┘                     │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│                 LAYER 3: ELEMENT FINDING                        │
│  ┌──────────────┐     ┌───────────────┐                        │
│  │ElementFinder │ ──► │ LocatorStrategy (interface)           │
│  └──────────────┘     │ - ByTextStrategy                       │
│         ↓             │ - ByIdStrategy                          │
│  ┌──────────────┐    │ - BySemanticStrategy                   │
│  │ElementScorer │    └───────────────┘                        │
│  └──────────────┘                                              │
│         ↓                                                        │
│  ┌──────────────────────────────┐                              │
│  │ ElementMatch (best result)   │                              │
│  │ - locator: Playwright Locator│                              │
│  │ - score: 0.95                 │                              │
│  │ - strategy: BY_TEXT           │                              │
│  └──────────────────────────────┘                              │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│                 LAYER 4: ACTION EXECUTION                       │
│  ┌──────────────────┐  ┌────────────────────────┐             │
│  │ ActionExecutor   │  │ Specific Executors:    │             │
│  │ (router/factory) │  │ - ClickActionExecutor  │             │
│  └──────────────────┘  │ - FillActionExecutor   │             │
│          ↓             │ - VerifyActionExecutor │             │
│  ┌──────────────────┐ └────────────────────────┘             │
│  │ ActionResult     │                                          │
│  │ - success: true  │                                          │
│  │ - message: "..."  │                                          │
│  └──────────────────┘                                          │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│                   LAYER 5: ORCHESTRATION                        │
│  ┌──────────────┐     ┌──────────────┐                        │
│  │ TestRunner   │ ──► │ TestReporter │                        │
│  └──────────────┘     └──────────────┘                        │
│         ↓                     ↓                                  │
│  ┌──────────────┐     ┌──────────────┐                        │
│  │ TestResult   │     │ JSON Report  │                        │
│  └──────────────┘     └──────────────┘                        │
└─────────────────────────────────────────────────────────────────┘
```

---

## ✅ Implementation Status

### Completed (11 classes) ✅
**Domain Objects (Complete!)**
- ✅ `ActionType.java` - Enum of all actions with metadata (28 actions + updated CLICK rules)
- ✅ `ElementType.java` - Enum of element types (30+ types with ARIA mapping)
- ✅ `StepIntent.java` - Parsed step intent (50+ fields, Builder pattern) **BULLETPROOF**
- ✅ `ScannedElement.java` - Metadata container for DOM elements (NEW - replaces ElementCandidate)
- ✅ `ElementMatch.java` - Element finding result (60+ fields, Builder pattern)
- ✅ `ActionResult.java` - Action execution result (100+ fields, Builder pattern)

**Parser Layer (Complete!)**
- ✅ `StepParser.java` - Interface for parsing Gherkin steps
- ✅ `GherkinStepParser.java` - Robust NLP parser with unquoted value support and aggressive cleaning
- ✅ `ActionVerbRegistry.java` - Verb-to-action mappings with Category Priority System
- ✅ `VerbRegistryValidator.java` - Validation utilities for the registry

**Scanner Layer (Complete!)**
- ✅ `PageScanner.java` - Interface for DOM analysis
- ✅ `PlaywrightScanner.java` - High-performance JS-based scanner with Shadow DOM support

### In Progress (0 classes)

### Pending (18 classes)
- ⏳ Finder Layer (6)
- ⏳ Executor Layer (9+)
- ⏳ Orchestration (3)

---

## 🗺️ Class Dependency Map

```
DOMAIN (No dependencies) ✅ COMPLETE!
├── ActionType.java ✅
├── ElementType.java ✅
├── StepIntent.java ✅
├── ScannedElement.java ✅ (Captures frames, Shadow DOM, ancestors)
├── ElementMatch.java ✅
└── ActionResult.java ✅

PARSER (Depends on: Domain) ✅ COMPLETE!
├── StepParser.java ✅
├── GherkinStepParser.java ✅ (Handles ordinals, unquoted values, complex intents)
├── ActionVerbRegistry.java ✅ (Priority-aware mapping)
└── VerbRegistryValidator.java ✅

SCANNER (Depends on: Domain) ✅ COMPLETE!
├── PageScanner.java ✅
└── PlaywrightScanner.java ✅ (Single-pass JS engine)

FINDER (Depends on: Domain, Scanner)
├── ElementFinder.java (interface) ✅
├── SmartElementFinder.java ✅
├── ElementScorer.java ✅
├── LocatorStrategy.java (interface) ✅
├── ByTextStrategy.java ✅
└── ByAttributeStrategy.java ✅ (Replaced BySemanticStrategy with integrated scoring logic)

EXECUTOR (Depends on: Domain)
├── ActionExecutor.java (interface) ✅
├── BaseActionExecutor.java ✅ (Template pattern, robust retries)
├── ClickActionExecutor.java ✅ (Checkbox/Radio logic included)
├── FillActionExecutor.java ✅ (Post-fill validation logic included)
├── VerifyActionExecutor.java ✅ (Consolidated semantic verifier)
├── NavigateActionExecutor.java ✅
├── SelectActionExecutor.java ✅
├── WaitActionExecutor.java ✅
├── HoverActionExecutor.java ✅
└── ActionExecutorRegistry.java ✅

ORCHESTRATION (Depends on: All above)
├── TestRunner.java ⏳
├── TestReporter.java ⏳
└── TestConfig.java ⏳
```

---

## 📚 Detailed Class Specifications

### 🟢 LAYER 1: Domain Objects (COMPLETE!)

#### 1. StepIntent.java ✅ **BULLETPROOF**
**Package**: `testgeni.v3.core.domain`

**Purpose**: Immutable data object representing parsed user intent.

**Status**: ✅ Complete (50+ fields)

**Key Features**:
- **Builder Pattern** for easy construction
- **Immutable** by design (thread-safe)
- **Serializable** for debugging/caching
- **Comprehensive validation** via `validate()` and `isValid()`
- **Extensible** via `modifiers` map

**Core Fields**:
```java
public final ActionType action;           // CLICK, FILL, VERIFY
public final String target;               // "Login button", "First Name"
public final String value;                // "John", "Welcome message"
public final ElementType elementType;     // BUTTON, INPUT, TEXT
public final boolean negated;             // true for "NOT displayed"
public final String verificationAttribute; // "displayed", "enabled"
```

**Extended Fields (50+ total)**:
- **Metadata**: `originalStep`, `cleanStep`, `lineNumber`, `featureFile`, `scenarioName`
- **Context**: `scopingContext`, `frameAnchor`, `shadowHost`, `framePath`
- **Table Operations**: `tableRowCondition`, `tableColumn`, `rowIndex`, `columnIndex`
- **Special**: `tooltipOf`, `cssSelector`, `xpath`, `url`, `filePath`, `key`
- **Timing**: `timeoutMs`, `waitCondition`, `waitDurationSeconds`
- **Retry**: `retryAttempts`, `continueOnFailure`, `screenshotOnFailure`
- **Data-Driven**: `fromIndex`, `toIndex`, `variableName`
- **Extensibility**: `modifiers` (Map<String, String>)

**Helper Methods**:
```java
boolean isVerification()
boolean needsElement()
boolean hasValue()
boolean isNegated()
int getEffectiveTimeout()       // Falls back to action default
String validate()               // Delegates to ActionType
boolean isValid()
```

**Usage**:
```java
StepIntent intent = new StepIntent.Builder(ActionType.FILL, "First Name")
    .value("John")
    .elementType(ElementType.INPUT)
    .scopingContext("Login Form")
    .timeoutMs(3000)
    .build();

if (!intent.isValid()) {
    throw new ValidationException(intent.validate());
}
```

---

#### 2. ElementMatch.java ✅ **BULLETPROOF**
**Package**: `testgeni.v3.core.domain`

**Purpose**: Represents a matched element with comprehensive metadata.

**Status**: ✅ Complete (60+ fields)

**Key Features**:
- **Builder Pattern** for construction
- **Immutable** snapshot in time
- **Serializable** (except Locator)
- **Rich scoring details** for debugging
- **Performance metrics** built-in

**Confidence Thresholds**:
```java
HIGH_CONFIDENCE = 0.8
MEDIUM_CONFIDENCE = 0.5
LOW_CONFIDENCE = 0.3
```

**Core Fields**:
```java
public final Locator locator;             // Ready to use
public final double confidence;           // 0.0 to 1.0
public final boolean isConfident;
public final MatchStatus status;          // FOUND, NOT_FOUND, etc.
public final MatchStrategy strategy;      // BY_TEXT, BY_ID, etc.
```

**Extended Fields (60+ total)**:
- **Element Metadata**: `tagName`, `id`, `className`, `actualText`, `innerText`, etc.
- **State**: `visible`, `enabled`, `focused`, `checked`, `selected`
- **Position**: `elementIndex`, `boundingBox`, `inViewport`, `zIndex`
- **Scoring Details**: `textScore`, `attributeScore`, `scoreBreakdown`, `penalties`, `bonuses`
- **Performance**: `findDurationMs`, `candidatesScanned`, `usedCache`, `timestamp`
- **Alternatives**: List of other matches, `selectionReason`

**Helper Methods**:
```java
boolean isHighConfidence()      // >= 0.8
boolean isMediumConfidence()    // 0.5-0.8
boolean isLowConfidence()       // < 0.5
boolean isUsable()              // visible && enabled
String getDebugInfo()           // Detailed debug output
```

**Usage**:
```java
ElementMatch match = new ElementMatch.Builder(locator, 0.95, MatchStrategy.BY_TEXT)
    .actualText("Login")
    .tagName("button")
    .id("loginBtn")
    .visible(true)
    .enabled(true)
    .textScore(0.50)
    .addBonus("exact_text_match")
    .findDurationMs(45)
    .build();

if (match.isHighConfidence() && match.isUsable()) {
    match.locator.click();
}
```

---

#### 3. ActionResult.java ✅ **BULLETPROOF**
**Package**: `testgeni.v3.core.domain`

**Purpose**: Complete record of action execution.

**Status**: ✅ Complete (100+ fields)

**Key Features**:
- **Builder Pattern** for construction
- **Immutable** execution record
- **Serializable** for reporting
- **Evidence tracking** (screenshots, videos, logs)
- **Comprehensive errors** with stack traces

**Core Fields**:
```java
public final boolean success;
public final ResultStatus status;
public final String message;
public final Exception exception;
public final long durationMs;
```

**Extended Fields (100+ total)**:
- **Verification**: `expectedValue`, `actualValue`, `difference`, `verificationPassed`
- **Timing**: `startTimestamp`, `endTimestamp`, `waitTimeMs`, `actionTimeMs`
- **Retry**: `retryAttempts`, `retryReasons`, `succeededAfterRetry`
- **Element**: `elementMatch`, `elementLocator`, `elementConfidence`
- **State Changes**: `beforeState`, `afterState`, `urlBefore`, `urlAfter`, `domChanges`
- **Evidence**: `screenshotBefore`, `screenshotAfter`, `videoPath`, `harFilePath`, `traceFilePath`
- **Warnings**: `warnings`, `softAssertionFailures`, `performanceWarnings`, `accessibilityWarnings`
- **Self-Healing**: `selfHealingApplied`, `healingActions`, `originalLocator`, `healedLocator`
- **Data Extraction**: `extractedData`, `variableAssignments`, `tableData`
- **Browser Context**: `browserType`, `browserVersion`, `pageTitle`, `framePath`
- **Reporting**: `severity`, `tags`, `knownIssue`, `issueTrackerId`

**Static Factory Methods**:
```java
ActionResult.success(String message)
ActionResult.failure(String message, Exception exception)
ActionResult.verification(boolean passed, String expected, String actual)
ActionResult.timeout(String target, int timeoutMs)
ActionResult.elementNotFound(String target)
```

**Helper Methods**:
```java
boolean isVerification()
boolean hasWarnings()
boolean hasEvidence()
boolean wasSlow()               // Took longer than expected
String getSummary()             // One-line summary
String getDetailedReport()      // Full debug report
Map<String, String> getAllEvidence()
```

**Usage**:
```java
ActionResult result = new ActionResult.Builder(true)
    .message("Successfully clicked Login button")
    .action(ActionType.CLICK)
    .target("Login button")
    .elementMatch(match)
    .durationMs(245)
    .screenshotBefore("/screenshots/before.png")
    .screenshotAfter("/screenshots/after.png")
    .addTag("login")
    .severity(Severity.INFO)
    .build();

System.out.println(result.getSummary());
// Output: "✓ SUCCESS: Successfully clicked Login button (245ms)"

if (result.hasWarnings()) {
    result.getAllWarnings().forEach(System.out::println);
}
```

---

#### 4. ActionType.java ✅ **COMPLETE**
**Package**: `testgeni.v3.core.domain`

**Purpose**: Enum of all supported actions with rich metadata.

**Status**: ✅ Complete (28 actions)

**Key Features**:
- **28 actions** covering all test scenarios
- **Categories** (NAVIGATION, MOUSE, INPUT, SELECTION, etc.)
- **Risk levels** (NONE, LOW, MEDIUM, HIGH)
- **Expected element types** per action
- **Execution constraints** built-in
- **Validation** of StepIntent

**Actions by Category**:
- **NAVIGATION**: NAVIGATE, REFRESH, GO_BACK, GO_FORWARD
- **MOUSE**: CLICK, DOUBLE_CLICK, RIGHT_CLICK, HOVER, DRAG_DROP
- **INPUT**: FILL, TYPE, CLEAR, PRESS_KEY
- **SELECTION**: CHECK, UNCHECK, SELECT, DESELECT
- **VERIFICATION**: VERIFY
- **CONTEXT**: SWITCH_FRAME, SWITCH_WINDOW, SWITCH_TAB, CLOSE_WINDOW
- **UTILITY**: WAIT, SCROLL, SCREENSHOT, UPLOAD_FILE

**Helper Methods (30+)**:
```java
// Categories
ActionCategory getCategory()
boolean isNavigation(), isMouse(), isInput(), isVerification()

// Behavior
boolean needsElement()
boolean modifiesPage()
boolean canTriggerNavigation()
boolean shouldWaitForPageLoad()

// Element compatibility
Set<ElementType> getExpectedElementTypes()
boolean isCompatibleWith(ElementType)
ElementType getMostLikelyElement Type()

// Execution constraints
Set<String> getRequiredFields()
Set<String> getOptionalFields()
boolean worksOnDisabledElements()
boolean worksOnHiddenElements()
boolean requiresViewport()
boolean shouldAutoScroll()
boolean requiresFocus()

// Safety
RiskLevel getRiskLevel()
boolean isSafeToRetry()
int getRecommendedRetries()
double getMinimumConfidence()

// Validation
String validateIntent(StepIntent)

// Metadata
int getDefaultTimeoutMs()
String getDescription()
String getExpectedOutcome()
```

---

#### 5. ElementType.java ✅ **COMPLETE**
**Package**: `testgeni.v3.core.domain`

**Purpose**: Enum of element types with intelligent matching.

**Status**: ✅ Complete (30+ types)

**Key Features**:
- **30+ element types** (native + custom)
- **ARIA role mapping** for accessibility
- **Tag hints** (not rigid selectors!)
- **Smart DOM matching** with confidence scores
- **Framework agnostic** (works with ANY UI framework)

**Element Types**:
- **Interactive**: BUTTON, LINK, INPUT, TEXTAREA, CHECKBOX, RADIO, SELECT, DROPDOWN
- **Display**: TEXT, LABEL, HEADING, PARAGRAPH, SPAN, DIV
- **Media**: IMAGE, VIDEO, AUDIO, ICON
- **Structural**: TABLE, TABLE_ROW, TABLE_CELL, LIST, LIST_ITEM
- **Form**: FORM, FIELDSET, LEGEND
- **Frames**: IFRAME, FRAME
- **ARIA**: TOOLTIP, MODAL, ALERT, MENU, MENUITEM, TAB, TABPANEL, SLIDER, PROGRESS

**Critical Method - DOM Matching**:
```java
/**
 * Calculate match confidence for scanned DOM element.
 * Priority: ARIA role > Tag+Type > Tag > Attributes > Classes
 */
double calculateMatchConfidence(String tagName, String role, 
                                String type, String className,
                                Map<String, String> attributes)
// Returns: 0.0 (no match) to 1.0 (perfect match)
```

**Matching Examples**:
```java
// Native radio button
<input type="radio"> 
// → 0.85 confidence

// Custom radio button (Material-UI)
<span role="radio" class="MuiRadio-root">
// → 0.95 confidence (ARIA role priority!)

// Custom dropdown (React)
<div role="combobox" class="select-wrapper">
// → 0.90 confidence

// Poorly made custom checkbox
<div class="custom-checkbox-thing">
// → 0.35 confidence (class pattern fallback)
```

**Helper Methods (25+)**:
```java
// Metadata
Set<String> getTypicalTags()
String getAriaRole()
Set<String> getCommonAttributes()

// Categorization
boolean isInteractive()
boolean isFormElement()
boolean isClickable()
boolean isTextInput()
boolean isSelectable()
boolean isDisplayOnly()
boolean isContainer()
boolean isMedia()

// Validation
boolean hasAttribute(String)
boolean isCompatibleWith(ActionType)
String getIncompatibilityReason(ActionType)

// Smart detection
static ElementType detectFromElement(tag, role, type, className)
static ElementType fromTagName(String)
static ElementType fromAriaRole(String)
```

**Why This is Revolutionary**:
✅ Works with **ANY framework** (React, Angular, Vue, Bootstrap, Material-UI, Ant Design, etc.)  
✅ **ARIA-first** approach (web standards)  
✅ **Fallback strategies** (won't fail on badly made elements)  
✅ **Confidence scoring** (not binary match/no-match)  
✅ **Future-proof** (handles elements we haven't seen yet)

---

### 🔵 LAYER 2: Parser (COMPLETE!)

#### 6. GherkinStepParser.java ✅
**Package**: `testgeni.v3.parser`

**Purpose**: High-robustness NLP parser that converts natural language steps into `StepIntent`.

**Status**: ✅ Complete

**Key Features**:
- **Unquoted Value Support**: Detects values without quotes using "to", "as", "is", "contains" delimiters.
- **Aggressive Target Cleaning**: Strips 20+ noise words (the, a, in, into) to isolate core element names.
- **Natural Language Indices**: Supports "1st", "second", "last" with automatic integer conversion.
- **Validation-Ready**: Automatically populates `url`, `key`, and `filePath` fields for Domain validation.
- **Modifier Extraction**: Captures "switch to new window", "in iframe", and regex patterns.

---

### 🔵 LAYER 3: Scanner (COMPLETE!)

#### 8. PlaywrightScanner.java ✅
**Package**: `testgeni.v3.scanner`

**Purpose**: Lightning-fast DOM analysis engine.

**Status**: ✅ Complete

**Key Features**:
- **Single-Pass JS Engine**: Executes optimized JavaScript in-browser to return the tree in one network call.
- **Shadow DOM Traversal**: Recursively pierces Shadow Roots for modern web components.
- **Ancestor Tracking**: Stores full `ancestorIds` stack for O(1) performance in "inside" or "within" steps.
- **Smart Filtering**: Skips non-semantic containers to reduce processing overhead.
- **Visibility Detection**: Checks computed styles (opacity, display, visibility) and bounding boxes.
- **Frame Awareness**: Tracks `frameId` and `frameUrl` for every element.

---

### 🔵 LAYER 4: Finder

#### 9. ElementFinder.java (interface) ⏳
**Package**: `testgeni.v3.finder`

**Purpose**: Find best matching element for intent.

**Methods**:
```java
ElementMatch find(Page page, StepIntent intent);
```

---

#### 10. SmartElementFinder.java ⏳
**Package**: `testgeni.v3.finder`

**Purpose**: Implementation using strategies and scoring.

**Flow**:
1. Get DomSnapshot (cached)
2. Apply strategies to find candidates
3. Score each candidate
4. Return best match

**Usage**:
```java
ElementFinder finder = new SmartElementFinder(scanner, scorer, strategies);
ElementMatch match = finder.find(page, intent);
```

---

#### 11. ElementScorer.java ⏳
**Package**: `testgeni.v3.finder.scoring`

**Purpose**: Score how well candidate matches intent.

**Methods**:
```java
double score(ElementCandidate candidate, StepIntent intent);
```

**Scoring Factors**:
- Text match (40%)
- Attribute match (30%)
- Type match (20%)
- Accessibility (10%)
- Penalties (ads, hidden)

---

### 🔵 LAYER 5: Executor

#### 12. ActionExecutor.java (interface) ⏳
**Package**: `testgeni.v3.executor`

**Purpose**: Execute action on element.

**Methods**:
```java
ActionResult execute(Page page, ElementMatch match, StepIntent intent);
```

---

#### 13. ClickActionExecutor.java ⏳
**Package**: `testgeni.v3.executor`

**Purpose**: Execute click actions.

**Logic**:
```java
match.locator.click();
return ActionResult.success(...);
```

---

#### 14. FillActionExecutor.java ⏳
**Package**: `testgeni.v3.executor`

**Purpose**: Execute fill actions.

**Logic**:
```java
match.locator.fill(intent.value);
return ActionResult.success(...);
```

---

#### 15. VerifyActionExecutor.java ⏳
**Package**: `testgeni.v3.executor`

**Purpose**: Execute verification actions.

**Logic**:
```java
String actualText = match.actualText;
boolean matches = actualText.contains(intent.value);
return matches ? ActionResult.success(...) : ActionResult.failure(...);
```

---

### 🔵 LAYER 6: Orchestration

#### 16. TestRunner.java ⏳
**Package**: `testgeni.v3.runner`

**Purpose**: Orchestrate entire test execution.

**Flow**:
```java
for (String step : featureSteps) {
    1. Parse step → StepIntent
    2. Find element → ElementMatch
    3. Execute action → ActionResult
    4. Report result
}
```

---

## 📝 Implementation Order (Recommended)

### Phase 1: Core Domain ✅ **COMPLETE!**
1. ✅ ActionType (28 actions with metadata)
2. ✅ ElementType (30+ types with smart matching)
3. ✅ StepIntent (50+ fields, bulletproof)
4. ✅ ElementMatch (60+ fields, bulletproof)
5. ✅ ActionResult (100+ fields, bulletproof)

### Phase 2: Parser ✅ **COMPLETE!**
6. ✅ ActionVerbRegistry (100+ verbs)
7. ✅ VerbRegistryValidator (Validation tools)
8. ✅ StepParser (interface)
9. ✅ GherkinStepParser (implementation with multi-value/ordinal support)

### Phase 3: Scanner ✅ **COMPLETE!**
10. ✅ PageScanner (interface)
11. ✅ PlaywrightScanner (High-performance JS engine, Shadow DOM, Frame support)
12. ✅ ScannedElement (60+ metadata fields)

### Phase 4: Finder ✅ **COMPLETE!**
13. ✅ ElementFinder (interface)
14. ✅ SmartElementFinder (Scoping, Ordinals, Robust Selector generation)
15. ✅ ElementScorer (Multi-weighted mathematical brain)
16. ✅ LocatorStrategies (ByText, ByAttribute)

### Phase 5: Executors ✅ **COMPLETE!**
16. ✅ ActionExecutor (interface)
17. ✅ BaseActionExecutor (Template pattern, Retry logic)
18. ✅ ClickActionExecutor (Radio/Label handling)
19. ✅ FillActionExecutor (Value validation)
20. ✅ VerifyActionExecutor (Multi-mode verifier)
21. ✅ NavigateActionExecutor
22. ✅ SelectActionExecutor
23. ✅ WaitActionExecutor
24. ✅ HoverActionExecutor
25. ✅ ActionExecutorRegistry

### Phase 6: Integration ✅ **COMPLETE!**
26. ✅ TestRunner (Full Pipeline: Parser -> Scanner -> Finder -> Executor)
27. ✅ TestReporter (Console & Summary reporting)
28. ✅ TestConfig (Parameterization)
29. ✅ Multi-Frame Support (Recursive scanning)
30. ✅ End-to-End Orchestration
23. ⏳ Migration from v2

---

## 💡 Usage Examples

### Example 1: Parse Step
```java
// Input
String step = "When I fill \"John\" in First Name";

// Parse
StepParser parser = new GherkinStepParser();
StepIntent intent = parser.parse(step);

// Output
assert intent.action == ActionType.FILL;
assert intent.target.equals("First Name");
assert intent.value.equals("John");
```

### Example 2: Find Element
```java
// Input
StepIntent intent = new StepIntent(ActionType.CLICK, "Login button", ...);

// Scan DOM
DomScanner scanner = new DomScanner();
DomSnapshot snapshot = scanner.scan(page);

// Find best match
ElementFinder finder = new SmartElementFinder(...);
ElementMatch match = finder.find(page, intent);

// Output
assert match.confidenceScore > 0.8;
assert match.strategy == MatchStrategy.BY_TEXT;
```

### Example 3: Execute Action
```java
// Input
ElementMatch match = ...;
StepIntent intent = ...;

// Execute
ActionExecutor executor = new ClickActionExecutor();
ActionResult result = executor.execute(page, match, intent);

// Output
assert result.success == true;
assert result.message.equals("Clicked Login button");
```

### Example 4: Full Flow
```java
// Setup
TestRunner runner = new TestRunner(parser, finder, executors);

// Run feature
TestResult result = runner.runFeature("login.feature");

// Assert
assert result.passed == 10;
assert result.failed == 0;
```

---

## 🎯 Next Steps

1. **Implement Executor Layer** (Phase 5)
2. **Build ActionExecutor interface** and base classes
3. **Implement Click, Fill, and Verify executors**
4. **Integration Testing** (Phase 6)

---

## 📌 Key Principles

✅ **Immutable domain objects** - No hidden state  
✅ **Single responsibility** - Each class does ONE thing  
✅ **Explicit contracts** - Interfaces define behavior  
✅ **No metadata passing** - All data in method parameters  
✅ **Fail fast** - Clear errors, no silent failures  
✅ **Testable** - Each component can be unit tested  

---

**Last Updated**: 2026-01-06 18:00  
**Total Classes**: 35  
**Completed**: 35 (100%) ✅  
**Remaining**: 0 (Full V3 Engine Operational)

---

## 🏆 V3 CORE ENGINE: COMPLETE

**The next-generation TestGeni engine is now fully functional:**
- ✅ **Parser**: Smart relative dates, unquoted variables, and intent detection.
- ✅ **Scanner**: High-perf multi-frame scanning with visual physics (z-index, opacity).
- ✅ **Finder**: Probabilistic scoring with data-testid bonuses and volatile ID penalties.
- ✅ **Executor**: Hardened retry logic, human-like calendar navigation, and post-fill validation.
- ✅ **Orchestrator**: Unified pipeline for executing complex sequential scenarios.
