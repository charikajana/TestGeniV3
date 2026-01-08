Feature: RadioButton Automation
  Scenario: Interact with RadioButton
    Given Open the browser and go to "https://demoqa.com/radio-button"
    And wait for 2 seconds
    Then Validate #yesRadio is enabled
    And wait for 1 seconds
    Then Validate #impressiveRadio is enabled
    And wait for 1 seconds
    Then Validate #noRadio is disabled
    And Click Yes Radio Button
    Then Validate "You have selected Yes" message/text should be visible
    And take the screenshot
    And Click Impressive Radio Button
    Then Validate "You have selected Impressive" message/text should be visible
    And take the screenshot
