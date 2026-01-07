Feature: Comprehensive State Verification
  
  Background:
    Given Open the browser and go to "https://demoqa.com/radio-button"
    And wait for page load

  Scenario: Comprehensive Enable Variations
    # 1. Standard Enabled phrasings
    Then Verify Yes radio is enabled
    And Verify Yes isEnabled
    And Verify Yes is active
    And Verify Yes is clickable
    And Verify Yes is interactive
    And Verify Yes should be enabled
    
    # 2. Negative phrasing for Enabled
    And Verify Yes is not disabled

  Scenario: Comprehensive Disable Variations
    # No radio button is permanently disabled
    Then Verify No radio is disabled
    And Verify No isDisabled
    And Verify No is greyed out
    And Verify No is grayed out
    And Verify No is inactive
    And Verify No is read-only
    And Verify No is readonly
    And Verify No is restricted
    And Verify No button should be disabled
    
    # Negative phrasing for Disabled
    And Verify No button is not enabled

  Scenario: Comprehensive Selection and Checked Variations
    # 1. Initial unchecked state
    Then Verify Yes radio is not selected
    And Verify Yes is not checked
    And Verify Yes is unchecked
    And Verify Yes is off
    And Verify Yes is not chosen
    And Verify Yes should not be selected
    
    # 2. Checked state after click
    When click on Yes radio button
    Then Verify Yes radio button is selected
    And Verify Yes radio button is checked
    And Verify Yes radio button is on
    And Verify Yes radio button is chosen
    And Verify Yes radio button should be selected