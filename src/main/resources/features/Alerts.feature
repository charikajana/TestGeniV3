Feature: JavaScript Alert Handling
  Testing alert, confirm, and prompt dialog handling with message verification

  Scenario: Handle simple JavaScript alert and verify message
    Given Navigate to "https://demoqa.com/alerts"
    When user Click on alertButton
    Then Verify alert says "You clicked a button"
    
  Scenario: Accept alert with specific message verification
  Given Navigate to "https://demoqa.com/alerts"
    When user Click on alertButton
    Then Accept alert with message "You clicked a button"
    
  Scenario: Handle alert that appears after delay and verify message
  Given Navigate to "https://demoqa.com/alerts"
    When user Click on timerAlertButton
    Then Wait 10 seconds
    And Verify alert says "This alert appeared after 5 seconds"
    
  Scenario: Handle confirm dialog - Accept with verification
  Given Navigate to "https://demoqa.com/alerts"
    When user Click on confirmButton
    Then Verify and accept alert with "Do you confirm action?"
    
  Scenario: Handle confirm dialog - Accept without verification
  Given Navigate to "https://demoqa.com/alerts"
    When we Click on confirmButton
    Then Accept confirm
    
  Scenario: Handle confirm dialog - Dismiss
  Given Navigate to "https://demoqa.com/alerts"
    When we Click on confirmButton
    Then Dismiss confirm
    
  Scenario: Handle prompt dialog with text input
  Given Navigate to "https://demoqa.com/alerts"
    When Click on promtButton
    Then Enter "Automation Test User" in prompt

  Scenario: Handle prompt dialog - Dismiss
  Given Navigate to "https://demoqa.com/alerts"
    When Click on promtButton
    Then Dismiss prompt

  Scenario: Handle simple JavaScript alert and verify message
    Given Navigate to "https://www.selenium.dev/selenium/web/alerts.html#"
    When user Click on click me link
    Then Verify alert says "cheese"
    And user accept alert
    And wait for 10 seconds
