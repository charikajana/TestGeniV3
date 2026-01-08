Feature: Buttons Automation
  Scenario: Interact with Buttons
    Given Open the browser and go to "https://demoqa.com/buttons"
    And wait for 2 seconds
    When double click on Double Click Me button
    Then Verify "You have done a double click" is displayed
    And take the ScreenShot
    And wait for 2 seconds
    And right click on Right Click Me button
    Then Verify "You have done a right click" is displayed
    And take the ScreenShot
    And wait for 2 seconds
    And Click on Click Me button
    Then Verify "You have done a dynamic click" is displayed
    And take the ScreenShot
    And wait for 2 seconds



    