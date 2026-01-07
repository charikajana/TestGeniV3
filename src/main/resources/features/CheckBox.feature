Feature: Checkbox Automation
  Scenario: Interact with Checkboxes
    Given Open the browser and go to "https://demoqa.com"
    When Click on Elements
    And Click Check Box
    And Click on Home checkbox
    Then Verify "You have selected :" is displayed
    Then Verify "home" is displayed
    Then take the screenshot
    And Click on Home checkbox
    Then Verify "You have selected :" not displayed
    Then take the screenshot