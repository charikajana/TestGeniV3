Feature: JavaScript Alert Handling
  Testing alert, confirm, and prompt dialog handling with message verification

  Background:
    Given Navigate to "https://hotelbooker.cert.sabre.com"

  Scenario: Handle simple JavaScript alert and verify message
    When user Enter "QA_Sabre" in User Name
    And user Enter "Te5t@1234" in Password
    And user Click on Login button
    And wait for 10 seconds
    And click on "Test QA Client(Sabre)"
    And wait for 20 seconds
    And verify "Open Bookings" is displayed
    And wait for 20 seconds
    And Take screenshot
    And Enter "2" in Nights
    And select Arrival Date "90" days from today
    And wait for 2 seconds
    And Take screenshot 
    And Select "1" from Rooms
    And Select "1" from Guests
    And Select "USA" from Country
    And Enter "Dallas" in Location
    And Select "10 Miles" from Distance
    And wait for 20 seconds
    And Take screenshot
    And click on Search

