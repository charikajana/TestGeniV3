Feature: Window Switching Reproduction
  
  Scenario: Switch back and forth
    When I navigate to "https://vinothqaacademy.com/multiple-windows/"
    Then Verify page title is "Demo Site – Multiple Windows – Vinoth Tech Solutions"
    
    # Open new tab
    When I click on New Browser Tab button and switch to new window
    And wait for 5 seconds
    Then Verify page title is "Demo Site – WebTable – Vinoth Tech Solutions"
    
    # Switch back to parent
    When I switch to parent window
    And wait for 2 seconds
    Then Verify page title is "Demo Site – Multiple Windows – Vinoth Tech Solutions"
    
    # Switch to new window again (by title)
    When I switch to window "WebTable"
    And wait for 2 seconds
    Then Verify page title is "Demo Site – WebTable – Vinoth Tech Solutions"
