# Selenium Java Interview Automation Lab

> A hands-on collection of commonly asked Selenium WebDriver, TestNG and QA automation interview questions with executable Java implementations.

This repository is an **interview preparation and learning reference**, not a production enterprise automation framework. Every concept that comes up in Selenium, Java, TestNG and SDET interviews is shown as working, commented code against a real web application (the [OrangeHRM open source demo](https://opensource-demo.orangehrmlive.com/web/index.php/auth/login)), with the short answer to give and the reason the approach is preferred.

**Have an interview tomorrow?** Open the [Interview Question Index](docs/interview-index.md), find your question, read the short answer, then open the linked code.

![HTML test report](docs/images/html-report.png)

*The lab's own HTML report after a full run (`mvn clean test -Pdemo`): 38 tests, 34 passed, plus 2 failed and 2 skipped on purpose by the demo tests.*

## Contents

1. [Project overview](#1-project-overview)
2. [Topics covered](#2-topics-covered)
3. [Interview question index](#3-interview-question-index)
4. [Architecture](#4-architecture)
5. [Project structure](#5-project-structure)
6. [Selenium interview examples](#6-selenium-interview-examples)
7. [TestNG interview examples](#7-testng-interview-examples)
8. [Framework design examples](#8-framework-design-examples)
9. [Common interview traps](#9-common-interview-traps)
10. [Interview-ready answers](#10-interview-ready-answers)
11. [How to run](#11-how-to-run)
12. [Test execution examples](#12-test-execution-examples)
13. [Reporting](#13-reporting)
14. [Learning path](#14-learning-path)
15. [Future topics](#15-future-topics)
16. [Author](#16-author)

## 1. Project overview

| | |
|---|---|
| Language | Java 17 |
| Browser automation | Selenium WebDriver 4.50 (Selenium Manager downloads the driver) |
| Test runner | TestNG 7.12 |
| Build | Maven (Surefire 3.5) |
| Application under test | OrangeHRM open source demo (public, shared) |
| Report | Custom TestNG `IReporter`: one HTML file, no extra dependencies |

**Repository philosophy.** Each topic tries to give you:

1. the interview question,
2. a short, interview-ready answer,
3. the concept and *why* the approach is used,
4. a working Java implementation you can run,
5. the common mistake and the better approach.

Every line of code is commented, and the class and method Javadoc explains the reasoning. There are also 104 questions with long answers in [`docs/interview-questions.pdf`](docs/interview-questions.pdf).

## 2. Topics covered

Levels: 🟢 Beginner · 🟡 Intermediate · 🔴 Advanced · ⭐ Frequently asked

| Area | Covered with working code |
|---|---|
| **WebDriver basics** 🟢 | `close()` vs `quit()` · `findElement` vs `findElements` · navigation (back, refresh) · headless and browser options · Selenium Manager |
| **Locators** 🟢🟡 | `@FindBy` by name, CSS, tag and `How` · CSS vs XPath · dynamic locators built from text · labels not linked to inputs · table row by content |
| **Synchronization** 🟡 | Explicit waits only (no `Thread.sleep`, no implicit wait) · `ExpectedConditions` · custom wait conditions · spinner (AJAX) waits · timeouts |
| **Exceptions** 🟡 | `StaleElementReferenceException` (reproduced and handled) · `ElementClickInterceptedException` · `TimeoutException` |
| **Interactions** 🟢🟡 | Windows and tabs · custom drop-downs · `Actions` (keyboard) · `JavascriptExecutor` · page and element screenshots · text vs attribute vs property · broken links · web tables |
| **Framework design** 🟡🔴 | Page Object Model · PageFactory and `@CacheLookup` · `BasePage` · fluent page API · `ThreadLocal` driver · driver life cycle · configuration · test data · test isolation and cleanup · step logging |
| **TestNG** 🟢🟡🔴 | `@BeforeMethod`/`@AfterMethod` with `alwaysRun` · groups · `priority` · `dependsOnMethods` · `invocationCount` · `timeOut` · `SoftAssert` · `SkipException` · `expectedExceptions` · `@DataProvider` · `@Parameters` + `@Optional` · parallel classes · `IReporter` · `IRetryAnalyzer` · ServiceLoader registration |
| **Build and reporting** 🟢🔴 | Maven Surefire with groups and profiles · screenshot on failure · custom HTML report |

Not yet shown with code: alerts, iframes, native `<select>`, checkboxes and radio buttons, cookies, file upload, FluentWait, and several exceptions. See [Future topics](#15-future-topics).

## 3. Interview question index

[**docs/interview-index.md**](docs/interview-index.md) maps **59 interview questions** to the exact class and method that answers them, with a short interview answer, a difficulty level and the matching question in the PDF. A sample:

| Question | Level | Implementation |
|---|---|---|
| How do you make WebDriver safe for parallel runs? | 🔴 ⭐ | [`DriverManager`](src/main/java/com/automation/selenium/driver/DriverManager.java) |
| Why avoid `Thread.sleep()`? Implicit or explicit wait? | 🟢 ⭐ | [`BasePage`](src/main/java/com/automation/selenium/pages/BasePage.java) |
| How do you handle `StaleElementReferenceException`? | 🟡 ⭐ | `SeleniumScenariosTest.findByFieldSurvivesAReloadButCacheLookupGoesStale` |
| What is PageFactory? Is it the same as POM? | 🟡 ⭐ | [`BasePage`](src/main/java/com/automation/selenium/pages/BasePage.java), [`LoginPage`](src/main/java/com/automation/selenium/pages/LoginPage.java) |
| How do you handle multiple windows? | 🟢 ⭐ | `SeleniumScenariosTest.dashboardOpensInASecondTab` |
| How do you handle a custom drop-down? | 🟡 ⭐ | `BasePage.choose` / `options` |
| How does a DataProvider work? | 🟡 ⭐ | [`TestData`](src/test/java/com/automation/selenium/data/TestData.java), `LoginTest` |
| How does a RetryAnalyzer work? Should it retry everything? | 🔴 ⭐ | [`RetryOnce`](src/test/java/com/automation/selenium/base/RetryOnce.java) |
| How do you take a screenshot on failure? | 🟡 ⭐ | [`BaseTest`](src/test/java/com/automation/selenium/base/BaseTest.java) |
| How do you find broken links? | 🟡 ⭐ | [`Links`](src/main/java/com/automation/selenium/utils/Links.java), `SeleniumScenariosTest` |

## 4. Architecture

```
  Test classes (tests/)            what to check: assertions only, no locators
        │ extend
  BaseTest (base/)                 browser per test · screenshot on failure · cleanUp() hook
        │ call
  Page objects (pages/)            LoginPage, DashboardPage, SystemUsersPage, … : locators + actions
        │ extend
  AppPage → BasePage               menu/user menu · PageFactory, explicit waits, shared widgets
        │ use
  DriverManager · Config · Steps   ThreadLocal browser · settings · report steps
        │
  HtmlReportListener               TestNG IReporter → one HTML report
```

**Parallel execution with `ThreadLocal`.** Surefire runs test classes on 3 threads. `DriverManager` keeps one browser per thread, so parallel tests never touch each other's browser:

```
Thread 1 ── LoginTest        ── ChromeDriver 1
Thread 2 ── SystemUsersTest  ── ChromeDriver 2
Thread 3 ── EmployeeTest     ── ChromeDriver 3
```

## 5. Project structure

New interview examples will go into topic packages (`interview/<topic>/`); the existing tests stay where they are and are mapped in the [index](docs/interview-index.md).

```
selenium-java-framework
├── docs/interview-index.md            Question → short answer → implementation, with difficulty levels
├── docs/interview-questions.pdf       104 Selenium + TestNG questions: explanation, example, link to the code
├── docs/images/                       Report screenshots used in this README
├── pom.xml                            Maven build: Surefire parallel classes, groups, demo profile
├── testng.xml                         Suite for running from the IDE
└── src
    ├── main/java/com/automation/selenium
    │   ├── config      Config: config.properties + -D overrides
    │   ├── driver      DriverManager: ThreadLocal browser per thread (chrome / firefox / edge, headless)
    │   ├── pages       BasePage (PageFactory, explicit waits, shared widgets), AppPage (menus),
    │   │               LoginPage, DashboardPage, SystemUsersPage,
    │   │               EmployeeListPage, AddEmployeePage, EmployeeProfilePage
    │   ├── report      HtmlReportListener: custom IReporter
    │   └── utils       Steps: report steps; Links: HTTP status of a link
    ├── main/resources/META-INF/services   Registers the report listener with TestNG (ServiceLoader)
    ├── test/java/com/automation/selenium
    │   ├── base        BaseTest: browser life cycle, screenshot, cleanup; RetryOnce: IRetryAnalyzer
    │   ├── data        TestData: data providers, generated employees
    │   └── tests       LoginTest, SystemUsersTest, EmployeeTest        (application tests)
    │                   SeleniumScenariosTest, TestNgFeaturesTest       (interview scenarios)
    │                   ReportDemoTest                                  (fails and skips on purpose)
    └── test/resources/config.properties
```

## 6. Selenium interview examples

[`SeleniumScenariosTest`](src/test/java/com/automation/selenium/tests/SeleniumScenariosTest.java) solves one classic "can you code this?" task per test. The page objects show the techniques used in every test.

| Interview task | Level | Where |
|---|---|---|
| Read a list of elements (`findElements`) | 🟢 ⭐ | `sideMenuListsTheCoreModules` · `AppPage.menuItems` |
| Find broken links | 🟡 ⭐ | `sideMenuHasNoBrokenLinks` · `Links` |
| Read the options of a custom (non-`<select>`) drop-down | 🟡 ⭐ | `userRoleDropdownOffersAdminAndEss` · `BasePage.options` |
| Keyboard actions with `Actions` (Tab, Enter) | 🟡 | `adminCanLogInWithTheKeyboard` · `LoginPage.loginWithKeyboard` |
| Open a new tab, switch, close, switch back | 🟢 ⭐ | `dashboardOpensInASecondTab` |
| `JavascriptExecutor` | 🟡 ⭐ | `javascriptReadsThePageState` · `BasePage.runScript` |
| Stale element: `@FindBy` vs `@CacheLookup` after a reload | 🟡 ⭐ | `findByFieldSurvivesAReloadButCacheLookupGoesStale` |
| Element screenshot (Selenium 4) | 🟢 | `loginFormScreenshotIsAPng` · `LoginPage.formScreenshot` |
| Dynamic locator from a value; table row by content | 🟡 ⭐ | `BasePage.button`, `inField` · `EmployeeListPage.delete` |
| Wait for a spinner; custom wait condition | 🟡 | `BasePage.waitForLoader` · `EmployeeProfilePage.fullName` |
| Read a web table as *column → value* | 🟡 ⭐ | `BasePage.tableRows` |
| `clear()` does not work on a Vue/React field | 🟡 | `BasePage.type` (Ctrl+A, Delete) |

## 7. TestNG interview examples

[`TestNgFeaturesTest`](src/test/java/com/automation/selenium/tests/TestNgFeaturesTest.java) uses one TestNG feature per test on a real check. The other classes show the rest.

| Feature | Level | Where |
|---|---|---|
| `@BeforeMethod` / `@AfterMethod` with `alwaysRun` | 🟢 ⭐ | `BaseTest` |
| Groups (class level + `smoke`), `excludedGroups` | 🟢 ⭐ | `LoginTest`, `SystemUsersTest`, `EmployeeTest`, `pom.xml` |
| `SoftAssert` | 🟢 ⭐ | `softAssertChecksTheLoginPage` |
| `expectedExceptions` + message regex | 🟡 | `wrongPasswordThrowsTheLoginError` |
| `priority` + `dependsOnMethods` | 🟢 ⭐ | `loginPageIsShown` → `adminLogsInAfterThePageCheck` |
| `invocationCount` | 🟢 | `wrongPasswordIsRejectedEveryTime` |
| `timeOut` | 🟡 | `loginFinishesWithinOneMinute` |
| `IRetryAnalyzer` | 🔴 ⭐ | `adminLoginIsRetriedOnceOnFailure` · `RetryOnce` |
| `@Parameters` + `@Optional` | 🟡 | `titleMatchesTheSuiteParameter` · `testng.xml` |
| `@DataProvider` shared through `dataProviderClass` | 🟡 ⭐ | `LoginTest.loginIsRejectedForInvalidCredentials` · `TestData` |
| `SkipException`; skip caused by a failed dependency | 🟢 | `ReportDemoTest` |
| Parallel classes | 🔴 ⭐ | `pom.xml` (Surefire), `testng.xml` |
| `IReporter` registered through ServiceLoader | 🔴 | `HtmlReportListener`, `META-INF/services` |

## 8. Framework design examples

- **Page Object Model.** Page objects hold every locator and action; tests only call methods such as `loginAsAdmin().openAdmin().filterByUsername("Admin").search()` and assert on the results.
- **PageFactory.** Fixed elements are `@FindBy` fields, filled by `PageFactory.initElements(driver, this)` in the `BasePage` constructor:

  ```java
  @FindBy(name = "username")
  private WebElement usernameField;
  @FindBy(css = ".oxd-alert-content-text")
  private List<WebElement> errorAlerts;   // empty list = no banner
  ```

  Each field is a proxy that finds the element again on every use, so it never goes stale when the page re-renders. `@CacheLookup` is left out on purpose; `SeleniumScenariosTest` shows why. Locators built from a value (a label, a button text, a row ID) cannot be annotations and stay methods that return a `By`.
- **Explicit waits only.** `BasePage` wraps each action in an explicit wait that ignores stale elements, and waits for the loading spinner after searches.
- **`ThreadLocal` driver and life cycle.** `DriverManager` keeps one `WebDriver` per thread; `BaseTest` starts a fresh browser before each test and always quits it afterwards.
- **Configuration.** `config.properties`, overridable with `-Dkey=value` (see [How to run](#11-how-to-run)).
- **Test isolation on a shared site.** Checks never depend on how many records exist. The PIM test creates its own employee with a random ID and deletes it again; if the test fails halfway, `BaseTest` takes the screenshot, then calls the `cleanUp()` hook, which `EmployeeTest` overrides to delete the leftover.
- **Step logging.** Page methods record each action with `Steps.log` (passwords masked), and the report shows the steps under each test.

## 9. Common interview traps

| Trap question | Short answer |
|---|---|
| Can we use `Thread.sleep()`? | Technically yes, but not for synchronization: it always waits the full time. Use an explicit wait for the condition you need. |
| Is PageFactory required for Page Object Model? | No. POM is a design pattern; PageFactory is one optional way to create a page's elements. |
| Does `ThreadLocal` make WebDriver thread-safe? | Not strictly. It gives each thread its *own* driver, so no two threads share one, which is what parallel tests need. |
| Should a RetryAnalyzer retry every failure? | No. Retry only known environment hiccups; retrying everything hides real defects. |
| Is `@CacheLookup` always bad? | No. It is fine for truly static elements, but goes stale when the element is re-created (reload, re-render). |
| Does `dependsOnMethods` fail the dependent test? | No. If the first test fails, the dependent test is **skipped**. |
| Does `-Dgroups=smoke` work when Surefire uses testng.xml? | No. Surefire ignores `-Dgroups` and `-Dtest` when `suiteXmlFiles` is set, which is why this project runs without a suite file. |
| Will `@BeforeMethod` run when I filter by group? | Only if it is in that group or has `alwaysRun = true`. |

## 10. Interview-ready answers

**Q: What is the difference between implicit and explicit wait?**
Implicit wait applies to every element lookup of the driver; explicit wait waits for one condition on one element. Explicit waits give precise synchronization and clear errors, and the two should not be mixed. → [`BasePage`](src/main/java/com/automation/selenium/pages/BasePage.java)

**Q: How do you run Selenium tests in parallel safely?**
Give every test thread its own browser by keeping the driver in a `ThreadLocal<WebDriver>`, start and quit it per test, and keep test data independent. → [`DriverManager`](src/main/java/com/automation/selenium/driver/DriverManager.java), [`BaseTest`](src/test/java/com/automation/selenium/base/BaseTest.java)

**Q: How do you handle a stale element?**
Find the element again instead of reusing the old reference. In this lab, PageFactory proxies re-find on every use and the wait ignores `StaleElementReferenceException`, so a re-render is simply retried. → `SeleniumScenariosTest.findByFieldSurvivesAReloadButCacheLookupGoesStale`

**Q: What is the Page Object Model, and why use it?**
One class per page holds its locators and actions; tests call methods and assert. A UI change is fixed in one place, and tests read like the business flow. → [`pages/`](src/main/java/com/automation/selenium/pages)

**Q: How does a DataProvider work?**
A method annotated `@DataProvider` returns `Object[][]`; TestNG runs the test once per row and passes the row as parameters. → [`TestData`](src/test/java/com/automation/selenium/data/TestData.java), `LoginTest.loginIsRejectedForInvalidCredentials`

All 59 answers are in the [index](docs/interview-index.md).

## 11. How to run

Requirements: JDK 17 or newer, Maven, and Chrome (or Firefox/Edge).

```bash
mvn clean test                          # all tests, visible browser
mvn clean test -Dheadless=true          # without a window
mvn clean test -Dgroups=interview       # one group: smoke, login, admin, pim or interview
mvn clean test -Dtest=LoginTest         # one class
mvn clean test -Dtest=SeleniumScenariosTest#dashboardOpensInASecondTab   # one method
mvn clean test -Dbrowser=edge           # chrome (default), firefox or edge
mvn clean test -Dthreads=1              # run the classes one after another
mvn clean test -Pdemo                   # also run the demo tests (2 fail, 2 skip on purpose)
```

In the IDE, run `testng.xml` or any test class or method directly.

**Configuration:** `src/test/resources/config.properties`; every key can be overridden with `-Dkey=value`.

| Key | Default | Meaning |
|---|---|---|
| `baseUrl` | `https://opensource-demo.orangehrmlive.com` | Application under test |
| `browser` | `chrome` | `chrome`, `firefox` or `edge` |
| `headless` | `false` | Run without a window |
| `timeoutSeconds` | `15` | Explicit wait timeout |
| `username` / `password` | `Admin` / `admin123` | Demo administrator |

## 12. Test execution examples

| Class | Group | Tests |
|---|---|---|
| `LoginTest` | `login` | Admin can log in (`smoke`) · invalid credentials rejected (data provider: wrong password, unknown user, wrong case) · username and password required · password required · log out (`smoke`) · Back and reload after logout end on the login page · dashboard URL redirects to login when logged out |
| `SystemUsersTest` | `admin` | System Users opens from the menu (`smoke`) · filter by username · filter by role and status · unknown username finds no records · Reset clears every filter |
| `EmployeeTest` | `pim` | An employee can be added, found by ID and deleted (`smoke`) · first and last name are required · unknown employee ID finds no records |
| `SeleniumScenariosTest` | `interview` | 8 Selenium coding scenarios (see [section 6](#6-selenium-interview-examples)) |
| `TestNgFeaturesTest` | `interview` | 9 runs of TestNG features (see [section 7](#7-testng-interview-examples)) |
| `ReportDemoTest` | `demo` | Fails and skips **on purpose** (see below) |

A normal run has 34 tests (17 application tests + 17 interview tests) on 3 threads.

| Command | Tests | Passed | Failed | Skipped | Time |
|---|---|---|---|---|---|
| `mvn clean test -Dheadless=true` | 34 | 34 | 0 | 0 | 2 min 22 s |
| `mvn clean test -Pdemo -Dheadless=true` | 38 | 34 | 2 (on purpose) | 2 (on purpose) | 2 min 38 s |

**Demo tests.** `ReportDemoTest` shows how the framework and the report handle every outcome, not just passes:

| Test | Result | What the report shows |
|---|---|---|
| Dashboard title checked against a wrong value | Failed (assertion) | `expected [Home] but found [Dashboard]`, stack trace, screenshot |
| Login with a wrong password | Failed (error) | `IllegalStateException: Login as "Admin" failed: Invalid credentials`, screenshot of the login page |
| Precondition missing | Skipped (`SkipException`) | The steps run so far and the skip reason |
| Depends on a failed test | Skipped (dependency) | The test it depends on; it never starts |

The `demo` group is left out of normal runs (`excludedGroups` in `pom.xml`, `<exclude>` in `testng.xml`), so the build stays green. With `-Pdemo` the build ends as failed, as it should.

## 13. Reporting

| Output | Location |
|---|---|
| Custom HTML report | `target/surefire-reports/selenium-test-report.html` (`test-output/` when run from the IDE) |
| Failure screenshots | `target/screenshots/<Class>_<method>_<time>.png` (also embedded in the report) |
| TestNG / Surefire reports | `target/surefire-reports/` |

The report is an interview example in itself: a custom TestNG `IReporter` (`HtmlReportListener`). It shows why you would write your own report and how a screenshot reaches it: `BaseTest` stores the screenshot on the `ITestResult`, and the reporter reads it back after the run.

- Verdict, pass rate, environment (application, browser, Java, OS).
- One collapsible row per test class with pass/fail/skip chips; classes with a failure or skip open first.
- Each test: groups, duration, recorded steps, failure message, filtered stack trace and screenshot; skip reasons.
- Search, status filter, expand/collapse all, dark mode, print styles, gentle animations (off when the system asks for reduced motion). One file, works offline.

With the **Failed** filter and the failed cards opened:

![Failed tests in the HTML report](docs/images/html-report-failures.png)

## 14. Learning path

Work through the [index](docs/interview-index.md) in this order. ✅ = runnable code in this lab, ○ = PDF answer only, code planned.

```
Level 1: Selenium basics
    WebDriver vs WebElement, close vs quit ✅, findElement(s) ✅, navigation ✅,
    locators ✅, custom drop-downs ✅, windows and tabs ✅, alerts ○, frames ○, <select> ○
        ↓
Level 2: Synchronization
    why not Thread.sleep ✅, implicit vs explicit ✅, ExpectedConditions ✅,
    custom waits ✅, spinners ✅, stale elements ✅, FluentWait ○
        ↓
Level 3: Advanced Selenium
    Actions ✅, JavascriptExecutor ✅, web tables ✅, screenshots ✅, broken links ✅,
    text vs attribute vs property ✅, exceptions (partly ✅)
        ↓
Level 4: TestNG
    annotations ✅, assertions and SoftAssert ✅, DataProvider ✅, groups ✅, parameters ✅,
    dependencies ✅, listeners ✅, retry ✅, parallel execution ✅
        ↓
Level 5: Framework design
    POM ✅, PageFactory ✅, BasePage ✅, BaseTest ✅, DriverManager + ThreadLocal ✅,
    configuration ✅, test data ✅, isolation and cleanup ✅, reporting ✅
```

## 15. Future topics

Planned additions in topic packages (`src/test/java/.../interview/<topic>/`), against a practice site for what OrangeHRM cannot show:

- **Selenium:** alerts, iframes, native `<select>`, checkboxes and radio buttons, cookies, file upload, relative locators.
- **Synchronization:** implicit wait and FluentWait as runnable examples, dynamic loading.
- **Exceptions:** `NoSuchElementException`, `ElementNotInteractableException`, `NoSuchWindowException`, `NoSuchFrameException`, `InvalidSelectorException`, each with cause, reproduction and handling.
- **TestNG:** the full annotation order printed by a test, an `ITestListener`, `parallel=methods`.
- **Docs:** a full common-traps page and "avoid vs prefer" code examples.

Deliberately **out of scope** here (they belong in a separate production-style portfolio project): Docker, Selenium Grid, CI/CD, API testing and cloud execution.

Known limitations: only Chrome has been run (Firefox and Edge are supported by `DriverManager`); the public demo site is shared and sometimes slow or down, and most tests have no retry on purpose.

## 16. Author

**Niranjan Kumar Agri** · [GitHub](https://github.com/niranjankagri) · [LinkedIn](https://www.linkedin.com/in/niranjan-kumar-agri/)

Feedback and questions are welcome through GitHub issues.
