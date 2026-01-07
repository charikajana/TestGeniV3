Feature: Book Store Search and Navigation
  Test search functionality and clicking on filtered results

  Scenario: Search for books and click on a result
    Given Navigate to "https://demoqa.com/books"
    When Enter given value 'JavaScript' in search box
    And Click on Learning JavaScript Design Patterns
    And wait for 5 seconds
    And Take the screenshot

  Scenario: Search and navigate to different book
    Given Navigate to "https://demoqa.com/books"
    When Enter the text 'Git' in search box
    And Click on Git Pocket Guide
    And wait for 5 seconds
    And Take the screenshot

  Scenario: Search filters book list correctly
    Given Navigate to "https://demoqa.com/books"
    When Enter the value 'JavaScript' in search box
