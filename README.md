# Selenium Java Interview Automation Lab

A hands-on **Selenium WebDriver + Java + TestNG interview preparation repository** containing commonly asked QA automation interview questions with executable, commented Java implementations.

This repository is intentionally designed as an **Interview Automation Lab**, not as a production enterprise automation framework.

The goal is simple:

> **Interview Question → Interview Answer → Concept → Working Code → Common Trap → Recommended Approach**

Each topic is implemented with working Java/Selenium/TestNG code so that the repository can be used both as an interview preparation reference and as a practical coding example.

**Have an interview tomorrow?** Open the [Interview Question Index](docs/interview-index.md), find your question, read the short answer, then open the linked code.

![HTML test report](docs/images/html-report.png)

---

## Project Overview

This repository demonstrates commonly asked concepts in:

* Selenium WebDriver
* Java automation
* TestNG
* UI test automation
* Page Object Model
* PageFactory
* WebDriver synchronization
* Selenium exceptions
* Browser windows and tabs
* Alerts and frames
* Dropdowns and web tables
* Actions API
* JavaScriptExecutor
* Cookies and file upload
* TestNG DataProviders
* TestNG listeners
* Retry mechanisms
* Parallel execution
* ThreadLocal WebDriver
* Test isolation and cleanup
* Custom HTML reporting
* Java coding questions (strings, arrays, collections, streams)

Every interview-focused example is designed to answer three questions:

1. **What is the interview question?**
2. **How would I explain it in an interview?**
3. **How would I implement it in Java?**

---

# Repository Philosophy

This project is deliberately different from a production automation framework.

The primary objective is **interview preparation and technical demonstration**.

Each interview topic should provide:

1. Interview question
2. Short interview-ready answer
3. Concept explanation
4. Working Java implementation
5. Common mistake or interview trap
6. Recommended approach

The code is intentionally commented and self-contained where appropriate so that the Selenium/TestNG API being discussed is easy to identify. Every interview class starts with the question, the interview answer, the concept and the recommended approach in its Javadoc, and every line of code is commented.

---

# Application Strategy

This repository intentionally uses two public demo applications because no single application provides every Selenium interview scenario.

## 1. OrangeHRM

**Purpose:** Realistic application automation and framework examples ([opensource-demo.orangehrmlive.com](https://opensource-demo.orangehrmlive.com)).

Used for:

* Login/logout
* Admin user management
* Employee lifecycle
* Search and filtering
* Form validation
* Custom dropdowns
* Toasts and messages
* Page Object Model
* PageFactory
* Test data
* Test isolation
* Cleanup
* Parallel execution

OrangeHRM is used where a realistic application flow makes the interview concept easier to demonstrate.

## 2. The Internet

**Purpose:** Focused Selenium WebDriver exercises ([the-internet.herokuapp.com](https://the-internet.herokuapp.com)).

Used for Selenium-specific scenarios that are not naturally available in OrangeHRM:

* JavaScript alerts
* Frames / iframes
* Browser windows and tabs
* Native `<select>` dropdowns
* Checkboxes
* Web tables
* File upload
* Cookies
* Dynamic loading (waits and Selenium exceptions)
* Actions API
* Other isolated WebDriver exercises

This separation keeps each example focused on the **interview concept being demonstrated**.

---

# Tech Stack

| Technology             | Version / Usage               |
| ---------------------- | ----------------------------- |
| Java                   | 17+                           |
| Selenium WebDriver     | 4.50                          |
| TestNG                 | 7.12                          |
| Maven                  | Build / dependency management |
| Maven Surefire         | Test execution                |
| Selenium Manager       | Browser driver management     |
| Page Object Model      | Application automation        |
| PageFactory            | Interview demonstration       |
| ThreadLocal            | Parallel WebDriver management |
| Custom TestNG Reporter | HTML reporting                |

---

# Topics Covered

## Selenium WebDriver

* WebDriver basics
* `close()` vs `quit()`
* `findElement()` vs `findElements()`
* Browser navigation
* Browser options
* Headless execution
* Selenium Manager

## Locators

* ID
* Name
* Class Name
* Tag Name
* Link Text
* Partial Link Text
* CSS Selector
* XPath
* XPath axes
* Relative locators
* Dynamic locators
* Label-based locators
* `@FindBy`

## Synchronization

* Why `Thread.sleep()` should normally be avoided
* Implicit wait
* Explicit wait
* FluentWait
* Presence vs visibility
* `ExpectedConditions`
* Custom wait conditions
* AJAX
* Loading spinners
* Dynamic DOM updates

## Selenium Exceptions

* `NoSuchElementException`
* `StaleElementReferenceException`
* `ElementNotInteractableException`
* `ElementClickInterceptedException`
* `TimeoutException`
* `NoSuchWindowException`
* `NoSuchFrameException`
* `InvalidSelectorException`
* `NoAlertPresentException`
* `UnhandledAlertException`

## Alerts

* Alert
* Confirm
* Prompt
* Accept
* Dismiss
* Read alert text
* Send text to prompt

## Frames / iFrames

* Switch to frame
* Switch by index
* Switch by name/id
* Switch by WebElement
* Nested frames
* `parentFrame()`
* `defaultContent()`

## Windows and Tabs

* Window handles
* Switching windows
* Switching by title
* Opening a new tab
* Returning to the original window

## Elements

* Native dropdowns
* Custom dropdowns
* Checkboxes
* Radio buttons (explained in `CheckboxTest`; the practice site has none to run against)
* Web tables
* Text vs attribute vs property
* Dynamic form fields

## Advanced Interactions

* Hover
* Right click
* Keyboard combinations
* Slider
* Drag and drop (explained only; see the [index](docs/interview-index.md#13-not-covered-by-code))
* `Actions` API

## JavaScriptExecutor

* JavaScript click (and why it can hide real problems)
* Scroll
* Set value
* Read page information
* Execute JavaScript against elements

## Browser Features

* Cookies
* File upload
* Screenshots
* Element screenshots
* Broken links

## Framework Design

* Page Object Model
* PageFactory
* BasePage
* Explicit waits
* Fluent page methods
* ThreadLocal WebDriver
* Driver lifecycle
* Configuration overrides
* Test data
* Test isolation
* Cleanup
* Step logging
* Custom HTML reporting

## TestNG

* Annotation lifecycle
* `@BeforeMethod`
* `@AfterMethod`
* `alwaysRun`
* Groups
* Priority
* Dependencies
* `dependsOnMethods`
* `dependsOnGroups`
* `enabled`
* `invocationCount`
* `threadPoolSize`
* Timeout
* SoftAssert
* SkipException
* expectedExceptions
* DataProviders
* Iterator DataProvider
* Method-aware DataProvider
* Parallel DataProvider
* Parameters
* Optional parameters
* Parallel classes
* ITestListener
* IReporter
* IRetryAnalyzer

## Java Coding

* Strings: reverse, palindrome, anagram, character frequency, duplicates, first non-repeating character, words
* Arrays: duplicates, second-highest, missing number, max/min, sorting, intersection
* Numbers: Fibonacci, prime check, swap without a third variable
* Collections and streams: removing duplicates in order, word frequency with `groupingBy`

---

# Interview Question Index

The repository contains an interview question index mapping **96 interview questions** to the relevant implementation, each with a difficulty level, a short interview-ready answer and the matching question in the PDF.

See: [**docs/interview-index.md**](docs/interview-index.md)

The repository also contains [**docs/interview-questions.pdf**](docs/interview-questions.pdf): 104 Selenium and TestNG interview questions with explanations, examples and links to the corresponding implementations.

---

# Architecture

The framework examples follow this general structure:

```text
Test Classes
     │
     ▼
 BaseTest
     │
     ▼
 Page Objects
     │
     ▼
 BasePage / AppPage
     │
     ├── DriverManager
     ├── Config
     ├── Steps
     └── Reporting
```

For parallel execution:

```text
TestNG
  │
  ├── Thread 1 → WebDriver 1 → Test 1
  │
  ├── Thread 2 → WebDriver 2 → Test 2
  │
  └── Thread 3 → WebDriver 3 → Test 3
```

Each thread owns its own WebDriver instance through `ThreadLocal`. [`ThreadLocalDriverTest`](src/test/java/com/automation/selenium/interview/framework/ThreadLocalDriverTest.java) shows it with two threads and two browser sessions.

---

# Project Structure

```text
selenium-java-framework
│
├── docs/
│   ├── images/
│   ├── interview-index.md
│   ├── common-interview-traps.md
│   ├── avoid-vs-prefer.md
│   └── interview-questions.pdf
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/automation/selenium/
│   │   │       ├── config/
│   │   │       ├── driver/
│   │   │       ├── pages/
│   │   │       ├── report/
│   │   │       └── utils/
│   │   │
│   │   └── resources/
│   │       └── META-INF/services/
│   │
│   └── test/
│       ├── java/
│       │   └── com/automation/selenium/
│       │       ├── base/
│       │       ├── data/
│       │       ├── interview/      one package per topic: locators, synchronization, exceptions,
│       │       │                   alerts, frames, windows, dropdowns, elements, actions,
│       │       │                   javascript, cookies, upload, testng, framework, java
│       │       └── tests/
│       │
│       └── resources/
│           └── config.properties
│
├── pom.xml
├── testng.xml
└── README.md
```

---

# Two Styles Used in This Repository

The repository intentionally contains two different styles.

## `tests/`

These represent realistic automation-suite design.

They use:

* Page Object Model
* BaseTest
* Page methods
* Reusable utilities
* Test data
* Assertions

Tests should primarily describe **business behavior**, not low-level Selenium implementation.

Example:

```java
loginPage.loginAsAdmin()
         .openAdmin()
         .filterByUsername("Admin")
         .search();
```

## `interview/`

These examples are intentionally more direct.

The Selenium API call being discussed is usually visible directly in the test class.

For example:

```java
driver.switchTo().alert().accept();
```

or:

```java
driver.switchTo().frame(frameElement);
```

This is intentional.

For interview preparation, the interviewer should be able to immediately see the Selenium API being demonstrated.

---

# PageFactory and @CacheLookup

PageFactory is demonstrated because it is a commonly discussed Selenium interview topic.

Example:

```java
@FindBy(name = "username")
private WebElement usernameField;
```

and:

```java
PageFactory.initElements(driver, this);
```

PageFactory creates Selenium element proxies for fields declared with `@FindBy`.

The underlying element lookup occurs when the field is used rather than storing the original `WebElement` reference immediately.

### Important interview point

PageFactory does **not** eliminate stale-element problems.

The DOM element can still become stale after:

* Page refresh
* Navigation
* DOM replacement
* AJAX re-rendering
* React/Vue/Angular updates

### @CacheLookup

`@CacheLookup` caches the located element reference.

It may improve performance for genuinely static elements, but it can cause stale-element problems when the DOM changes.

This repository intentionally demonstrates:

```text
@FindBy
vs
@FindBy + @CacheLookup
```

so that the difference can be explained during an interview ([`SeleniumScenariosTest.findByFieldSurvivesAReloadButCacheLookupGoesStale`](src/test/java/com/automation/selenium/tests/SeleniumScenariosTest.java)).

### Interview answer

> PageFactory provides element proxies and can defer element lookup, but it does not eliminate stale-element exceptions. I avoid `@CacheLookup` for dynamic applications unless I know the DOM reference remains stable.

---

# Common Interview Traps

See: [**docs/common-interview-traps.md**](docs/common-interview-traps.md) (about 30 traps) and [**docs/avoid-vs-prefer.md**](docs/avoid-vs-prefer.md) (26 mistakes next to the better code).

Important examples include:

### Thread.sleep

**Avoid:**

```java
Thread.sleep(5000);
```

**Prefer:**

```java
wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
```

### PageFactory

**Wrong assumption:**

> PageFactory means elements can never become stale.

**Correct:**

PageFactory does not eliminate stale elements. `@CacheLookup` can make stale-element problems more likely on dynamic pages.

### ThreadLocal

**Wrong assumption:**

> ThreadLocal makes Selenium WebDriver thread-safe.

**Correct:**

ThreadLocal provides a separate WebDriver reference for each thread. The important benefit is preventing multiple parallel tests from sharing the same driver instance.

### RetryAnalyzer

**Wrong assumption:**

> Every Selenium failure should automatically be retried.

**Correct:**

Retries should be controlled. Blind retries can hide real application defects.

### JavaScript click

**Wrong assumption:**

> JavaScript click is always better than Selenium click.

**Correct:**

Prefer normal Selenium interaction first. JavaScript can bypass browser-level interaction checks and may hide real problems such as overlays or incorrect element state.

### Window switching

Opening a new tab/window does not automatically switch WebDriver to it.

You must explicitly switch using the window handle.

### Table XPath

Inside a table row:

```xpath
.//td
```

is usually preferable to:

```xpath
//td
```

because `.//td` searches relative to the current row.

---

# Java Interview Coding

The repository also contains a focused set of Java coding problems commonly asked in SDET and automation interviews, as TestNG tests in [`src/test/java/com/automation/selenium/interview/java/`](src/test/java/com/automation/selenium/interview/java):

```text
ReverseStringTest.java
PalindromeTest.java
CharacterFrequencyTest.java
DuplicateCharactersTest.java
FirstNonRepeatingCharacterTest.java
AnagramTest.java
ReverseWordsTest.java
CountWordsTest.java
RemoveDuplicateCharactersTest.java
FindDuplicateElementsTest.java
SecondHighestNumberTest.java
FindMissingNumberTest.java
FindMaxMinTest.java
SortArrayTest.java
FibonacciTest.java
PrimeNumberTest.java
SwapNumbersTest.java
ArrayIntersectionTest.java
RemoveDuplicatesFromListTest.java
StreamFrequencyTest.java
```

Each Java coding example contains:

```java
/**
 * Interview question:
 * ...
 *
 * Interview answer:
 * ...
 *
 * Concept:
 * ...
 *
 * Recommended approach:
 * ...
 */
```

followed by the solution, tests for its edge cases (empty input, negatives, overflow, …) and, where one exists, a check against the library way of doing it.

The focus is on commonly expected SDET skills:

* Strings
* Arrays
* Lists
* Sets
* Maps
* Loops
* Collections
* Java Streams
* Basic algorithms

Run them on their own (no browser, about 2 seconds):

```bash
mvn clean test -Dgroups=java
```

---

# Selenium Interview Examples

Examples include:

* Locator strategies
* Wait strategies
* Selenium exceptions
* Alerts
* Frames
* Windows
* Dropdowns
* Web tables
* Actions
* JavaScriptExecutor
* Cookies
* File upload
* Screenshots
* Broken links

They live in [`interview/`](src/test/java/com/automation/selenium/interview) (one package per topic, run with `-Dgroups=<topic>`) and in [`SeleniumScenariosTest`](src/test/java/com/automation/selenium/tests/SeleniumScenariosTest.java) on OrangeHRM.

---

# TestNG Interview Examples

Examples include:

* Annotation lifecycle
* Groups
* Priorities
* Dependencies
* SoftAssert
* DataProviders
* Parameters
* Optional parameters
* Invocation count
* Timeout
* RetryAnalyzer
* SkipException
* Listeners
* Parallel execution
* Custom reporting

They live in [`interview/testng/`](src/test/java/com/automation/selenium/interview/testng) (no browser, `-Dgroups=testng`), [`TestNgFeaturesTest`](src/test/java/com/automation/selenium/tests/TestNgFeaturesTest.java) and [`ReportDemoTest`](src/test/java/com/automation/selenium/tests/ReportDemoTest.java).

---

# Framework Design Examples

The repository demonstrates:

* Page Object Model
* BasePage
* BaseTest
* PageFactory
* Explicit waits
* ThreadLocal WebDriver
* Driver lifecycle
* Configuration overrides
* Test data management
* Test isolation
* Cleanup
* Step logging
* Custom HTML reporting

---

# Test Execution Results

## Normal Interview Suite

The normal suite is designed to remain green.

| Run          | Tests | Passed | Failed | Skipped |
| ------------ | ----: | -----: | -----: | ------: |
| Normal suite |   148 |    144 |      4 |       0 |

**Latest normal run (6 Oct 2026, headless Chrome, 14 min 06 s): 144 of 148 passed.** The 4 failures were all on the practice site, which answered with Heroku's "Application error" page on all three attempts (each reload is listed in the report); all 40 Java coding tests, all TestNG examples and all OrangeHRM tests passed. Because the suite runs against two public sites, an occasional failure like this comes from the site, and the report's screenshot shows which.

The normal suite is made of 17 OrangeHRM application tests, 17 interview scenarios on OrangeHRM, 74 runs of the `interview/` Selenium, TestNG and framework examples, and 40 Java coding tests. Most of the run time is spent waiting for the two public sites; `-Dgroups=java` and `-Dgroups=testng` run in seconds.

Run:

```bash
mvn clean test -Dheadless=true
```

---

## Demo Outcome Suite

The repository also contains a dedicated `demo` group that intentionally demonstrates different TestNG outcomes.

| Run        | Tests | Passed |        Failed |       Skipped |
| ---------- | ----: | -----: | ------------: | ------------: |
| Demo suite |   152 | the 148 of the normal suite | 2 intentional | 2 intentional |

The failed and skipped tests are deliberate demonstrations of:

* Assertion failure
* Runtime failure
* `SkipException`
* Dependency-based skip

They are **not defects in the normal test suite**.

Run:

```bash
mvn clean test -Pdemo
```

---

# How to Run

## Normal test execution

```bash
mvn clean test
```

## Headless execution

```bash
mvn clean test -Dheadless=true
```

## Run a specific group

```bash
mvn clean test -Dgroups=smoke
```

Groups: `smoke`, `login`, `admin`, `pim`, `interview`, and one per interview topic: `locators`, `synchronization`, `exceptions`, `alerts`, `frames`, `windows`, `dropdowns`, `elements`, `actions`, `javascript`, `cookies`, `upload`, `testng`, `framework`, `java`.

## Run a specific test class

```bash
mvn clean test -Dtest=LoginTest
```

## Run with Edge

```bash
mvn clean test -Dbrowser=edge
```

## Run with Firefox

```bash
mvn clean test -Dbrowser=firefox
```

## Run sequentially

```bash
mvn clean test -Dthreads=1
```

## Run demo outcome tests

```bash
mvn clean test -Pdemo
```

## Run only demo tests

```bash
mvn clean test -Pdemo -Dgroups=demo
```

---

# Configuration

Configuration is available in:

```text
src/test/resources/config.properties
```

Example:

```properties
baseUrl=https://opensource-demo.orangehrmlive.com
practiceUrl=https://the-internet.herokuapp.com
browser=chrome
headless=false
timeoutSeconds=15
username=Admin
password=admin123
```

Values can be overridden from Maven:

```bash
mvn clean test -Dbrowser=edge -Dheadless=true
```

---

# Reporting

The framework generates a self-contained HTML report.

Location:

```text
target/surefire-reports/selenium-test-report.html
```

The report contains:

* Pass/fail/skip status
* Pass rate
* Environment information
* Browser
* Java version
* Operating system
* Test groups
* Execution time
* Test steps
* Failure messages
* Filtered stack traces
* Screenshots
* Skip reasons

Failure screenshots are stored under:

```text
target/screenshots/
```

The report is intentionally dependency-light and can be opened as a standalone HTML file. With the **Failed** filter and the failed cards opened, each failure shows its steps, the error and the browser at the moment of failure:

![Failed tests in the HTML report](docs/images/html-report-failures.png)

---

# Test Isolation and Cleanup

The public demo application is shared and its data changes over time.

Therefore, tests should avoid assumptions such as:

```text
There must be exactly 10 users.
```

Instead, tests should verify behavior:

```text
Every returned record matches the requested filter.
```

Tests that create data should clean it up.

For example:

```text
Create employee
      ↓
Verify employee
      ↓
Delete employee
      ↓
Cleanup
```

This makes the examples more representative of real automation engineering practices.

---

# Known Limitations

* The primary validation has been performed with Chrome.
* Firefox and Edge are supported but should be validated separately when required.
* Public demo applications can occasionally be slow, unavailable or return temporary server errors. The free Heroku practice site sometimes sends broken pages; `PracticeSiteTest` loads such a page again (up to three times) and writes every reload into the report.
* Most tests intentionally avoid blind retries because retrying every failure can hide genuine defects.
* A `RetryOnce` implementation is included where controlled retry behavior is useful.
* No CI pipeline is included because this repository is intentionally focused on Selenium/Java/TestNG interview preparation.

---

# What This Repository Intentionally Does NOT Cover

This repository focuses specifically on:

**Selenium + Java + TestNG + UI automation interview preparation.**

The following areas are intentionally reserved for a separate production-style QA portfolio project:

* REST API automation
* Playwright
* Cypress
* Mobile automation
* Performance testing
* Docker
* Selenium Grid
* Cloud test execution
* CI/CD pipelines
* GitHub Actions
* Jenkins
* AWS
* Kubernetes
* SonarQube
* Enterprise quality gates
* Advanced infrastructure
* Large-scale test orchestration

This separation keeps the current repository focused rather than turning it into an unnecessarily large collection of unrelated technologies.

---

# Future Portfolio Project

A separate repository will demonstrate production-style QA engineering.

Planned areas include:

```text
UI Automation
+
API Automation
+
CI/CD
+
Docker
+
Cloud Execution
+
Test Architecture
+
Quality Engineering
+
Reporting
+
Code Quality
```

This repository and the future portfolio repository therefore have different purposes.

### This repository

```text
selenium-java-framework

Interview Automation Lab
Selenium + Java + TestNG
Question → Answer → Code
```

### Future repository

```text
qa-automation-portfolio

Production-Style QA Engineering
UI + API + CI/CD + Docker + Cloud
```

---

# Learning Path

Recommended learning order:

```text
1. Selenium WebDriver Basics
        ↓
2. Locators
        ↓
3. Synchronization / Waits
        ↓
4. Selenium Exceptions
        ↓
5. Alerts / Frames / Windows
        ↓
6. Elements / Dropdowns / Tables
        ↓
7. Actions / JavaScript
        ↓
8. Page Object Model
        ↓
9. PageFactory
        ↓
10. TestNG
        ↓
11. DataProviders
        ↓
12. Listeners / Reporting
        ↓
13. ThreadLocal / Parallel Execution
        ↓
14. Java Coding Questions
        ↓
15. SDET Interview Preparation
```

---

# Interview Preparation Strategy

For each interview question, use the following answer structure:

### 1. Definition

Explain what the concept is.

### 2. Why

Explain why it is used.

### 3. Example

Give a practical automation example.

### 4. Implementation

Show the relevant Java/Selenium code.

### 5. Trade-off

Explain when the approach should and should not be used.

### Example

**Question: Why should you avoid `Thread.sleep()`?**

**Answer:**

`Thread.sleep()` introduces a fixed delay regardless of whether the application is ready. It can make tests unnecessarily slow and can still fail when the application takes longer than the hardcoded delay.

I prefer explicit waits because they wait for a specific application condition and continue as soon as that condition is satisfied.

---

# Documentation

Additional documentation:

* [docs/interview-index.md](docs/interview-index.md)
* [docs/common-interview-traps.md](docs/common-interview-traps.md)
* [docs/avoid-vs-prefer.md](docs/avoid-vs-prefer.md)
* [docs/interview-questions.pdf](docs/interview-questions.pdf)

These documents provide:

* Interview questions
* Interview-ready answers
* Code references
* Common mistakes
* Better approaches
* Topic mapping
* Learning guidance

---

# Contributing / Extending

When adding a new interview example:

1. Identify the interview question.
2. Add the interview-ready answer.
3. Explain the concept.
4. Add a working Java implementation.
5. Document the common mistake.
6. Explain the recommended approach.
7. Add the question to the interview index.
8. Keep the example focused on the concept being demonstrated.

Avoid adding technologies simply for the sake of increasing the technology list.

---

# Author

**Niranjan Kumar Agri**

Technical Lead QA | SDET | QA Automation | Selenium | Java | TestNG

Focused on:

* Test Automation
* QA Engineering
* Automation Architecture
* Selenium
* Java
* TestNG
* API Automation
* CI/CD
* Quality Engineering

---

## Repository Goal

The purpose of this repository is not to demonstrate the largest possible automation stack.

The purpose is to demonstrate that I can:

* Understand Selenium deeply
* Explain automation concepts clearly
* Write maintainable Java automation
* Design reusable test components
* Handle synchronization and dynamic applications
* Use TestNG effectively
* Design parallel execution
* Diagnose common Selenium problems
* Explain trade-offs during technical interviews
* Translate interview questions into working automation code

> **Learn the concept. Explain the concept. Implement the concept.**
