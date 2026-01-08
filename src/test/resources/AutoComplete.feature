Feature: Modern UI Components Testing
  Testing accordions, tabs, carousels, sliders, progress bars, and other modern UI elements

  Background:
    Given Navigate to "https://demoqa.com/auto-complete"

  Scenario: Accordion expand and collapse
    When User Enter "Re" in Type multiple color names
    And Click on Green
    And wait for 5 seconds
    When User Enter "Bl" in Type multiple color names
    And Click on Blue
    And wait for 5 seconds
    And Take the Screenshot
    And remove Green from Type multiple color names
    And wait for 5 seconds
    And Take the Screenshot
    And remove Blue from Type multiple color names
    And wait for 5 seconds
    And Take the Screenshot

    

