Feature: Comprehensive State Verification
  
  Background:
    Given Open the browser and go to "https://demoqa.com/radio-button"
    And wait for page load
    And wait for page load

  Scenario: Comprehensive Enable Variations
    # 1. Standard Enabled phrasings
    Then Verify #yesRadio is enabled
    And Verify #yesRadio isEnabled
    And Verify #yesRadio is active
    And Verify #yesRadio is clickable
    And Verify #yesRadio is interactive
    And Verify #yesRadio should be enabled
    
    # 2. Negative phrasing for Enabled
    And Verify #yesRadio is not disabled

  Scenario: Comprehensive Disable Variations
    # No radio button is permanently disabled
    And wait for 2 seconds
    Then Verify #noRadio is disabled
    And Verify #noRadio isDisabled
    And Verify #noRadio is greyed out
    And Verify #noRadio is grayed out
    And Verify #noRadio is inactive
    And Verify #noRadio is read-only
    And Verify #noRadio is readonly
    And Verify #noRadio is restricted
    And Verify #noRadio button should be disabled
    
    # Negative phrasing for Disabled
    And Verify #noRadio button is not enabled

  Scenario: Comprehensive Selection and Checked Variations
    # 1. Initial unchecked state
    Then Verify Yes radio is not selected
    And Verify Yes is not checked
    And Verify Yes is unchecked
    And Verify Yes is off
    And Verify Yes is not chosen
    And Verify Yes should not be selected
    
    # 2. Checked state after click
    When click Yes
    Then Verify Yes radio is selected
    And Verify Yes is checked
    And Verify Yes is on
    And Verify Yes is chosen
    And Verify Yes should be selected