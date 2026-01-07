Feature: Hover Fix Verification

  Scenario: Verify correctly clicking on elements with 'hover' in their name
    Given Navigate to "https://demoqa.com/tool-tips"
    # This was previously misclassified as HOVER 'element'
    When Click on Hover me to see button
    And wait for 1 seconds
    Then Verify "Hover me to see button" is displayed
    
    # Verify tooltip action explicitly
    And Verify tooltip of Hover me to see button contains "You hovered over the Button"

  Scenario: Verify correctly clicking on checkboxes without picking ads
    Given Navigate to "https://demoqa.com/checkbox"
    When Click on Home check Box
    Then Verify "You have selected" is displayed
