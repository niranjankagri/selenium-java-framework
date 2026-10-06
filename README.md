# Selenium Java Interview Automation Lab

> A hands-on collection of commonly asked Selenium WebDriver, TestNG and QA automation interview questions with executable Java implementations.

This repository is an **interview preparation and learning reference**, not a production enterprise automation framework. Every concept that comes up in Selenium, Java, TestNG and SDET interviews is shown as working, commented code against a real web application (the [OrangeHRM open source demo](https://opensource-demo.orangehrmlive.com/web/index.php/auth/login)), with the short answer to give and the reason the approach is preferred.

**Have an interview tomorrow?** Open the [Interview Question Index](docs/interview-index.md), find your question, read the short answer, then open the linked code.

![HTML test report](docs/images/html-report.png)

*The lab's own HTML report after a full run (`mvn clean test -Pdemo`, 6 Oct 2026): 112 tests, 107 passed. The demo tests fail twice and skip twice on purpose; the third failure is the practice site sending an error page into one frame (see [Reporting](#13-reporting)). Classes with a failure or skip come first and open.*

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
| Applications under test | [OrangeHRM open source demo](https://opensource-demo.orangehrmlive.com) (application tests, page objects) and [the-internet.herokuapp.com](https://the-internet.herokuapp.com) (one practice page per Selenium topic: alerts, frames, `<select>`, tables, uploads …) |
| Report | Custom TestNG `IReporter`: one HTML file, no extra dependencies |

**Repository philosophy.** Each topic tries to give you:

1. the interview question,
2. a short, interview-ready answer,
3. the concept and *why* the approach is used,
4. a working Java implementation you can run,
5. the common mistake and the better approach.

Every interview class starts with the **interview question, the interview-ready answer, the concept and the recommended approach** in its Javadoc, and every line of code is commented. Next to the code there are three pages: [common interview traps](docs/common-interview-traps.md), [avoid vs prefer](docs/avoid-vs-prefer.md) (bad code next to the better version) and 104 questions with long answers in [`docs/interview-questions.pdf`](docs/interview-questions.pdf).

## 2. Topics covered

Levels: 🟢 Beginner · 🟡 Intermediate · 🔴 Advanced · ⭐ Frequently asked

| Area | Covered with working code |
|---|---|
| **WebDriver basics** 🟢 | `close()` vs `quit()` · `findElement` vs `findElements` · navigation (back, refresh) · headless and browser options · Selenium Manager |
| **Locators** 🟢🟡 | All eight `By` strategies · CSS vs XPath and XPath axes · relative locators · dynamic locators built from a value · labels not linked to inputs · `@FindBy` forms |
| **Synchronization** 🟢🟡 | Why not `Thread.sleep` · implicit wait (and why not) · explicit waits · presence vs visibility · `ExpectedConditions` · FluentWait · custom conditions · spinners and AJAX |
| **Exceptions** 🟢🟡 | Reproduced and handled: `NoSuchElement`, `StaleElementReference`, `ElementNotInteractable`, `ElementClickIntercepted`, `Timeout`, `NoSuchWindow`, `NoSuchFrame`, `InvalidSelector`, `NoAlertPresent`, `UnhandledAlert` |
| **Alerts, frames, windows** 🟢 | Alert, confirm and prompt · nested frames, `parentFrame`, `defaultContent`, iframe by element · new window by handle set difference or by title · new tab |
| **Elements** 🟢🟡 | Native `<select>` · custom drop-downs · checkboxes · web tables (rows as maps, totals, `.//` inside a row) · text vs attribute vs property · `clear()` on Vue/React fields |
| **Advanced interactions** 🟡 | `Actions` (hover, right-click, key combinations, slider) · `JavascriptExecutor` (arguments, return types, scrolling, the JS-click risk) · page and element screenshots · cookies (skip the login) · file upload · broken links |
| **Framework design** 🟡🔴 | Page Object Model · PageFactory and `@CacheLookup` · `BasePage` · fluent page API · `ThreadLocal` driver (two threads, two sessions) · driver life cycle · configuration with `-D` overrides · test data · test isolation and cleanup · step logging |
| **TestNG** 🟢🟡🔴 | Annotation order · `alwaysRun` · groups · `priority` · `dependsOnMethods` / `dependsOnGroups` · `enabled` · `invocationCount` · `threadPoolSize` · `timeOut` · `SoftAssert` · `SkipException` · `expectedExceptions` · `@DataProvider` (shared, Iterator, Method-aware, parallel) · `@Parameters` + `@Optional` · parallel classes · `ITestListener` · `IReporter` · `IRetryAnalyzer` |
| **Build and reporting** 🟢🔴 | Maven Surefire with groups and profiles · screenshot on failure · custom HTML report |

Explained but not run as code: radio buttons and HTML5 drag and drop (see the end of the [index](docs/interview-index.md#12-not-covered-by-code)).

## 3. Interview question index

[**docs/interview-index.md**](docs/interview-index.md) maps **76 interview questions** to the exact class and method that answers them, with a short interview answer, a difficulty level and the matching question in the PDF. A sample:

| Question | Level | Implementation |
|---|---|---|
| How do you make WebDriver safe for parallel runs? | 🔴 ⭐ | [`DriverManager`](src/main/java/com/automation/selenium/driver/DriverManager.java) |
| Why avoid `Thread.sleep()`? Implicit or explicit wait? | 🟢 ⭐ | [`BasePage`](src/main/java/com/automation/selenium/pages/BasePage.java) |
| How do you handle `StaleElementReferenceException`? | 🟡 ⭐ | [`SeleniumExceptionsTest`](src/test/java/com/automation/selenium/interview/exceptions/SeleniumExceptionsTest.java) |
| How do you handle alerts and iframes? | 🟢 ⭐ | [`AlertHandlingTest`](src/test/java/com/automation/selenium/interview/alerts/AlertHandlingTest.java), [`IframeHandlingTest`](src/test/java/com/automation/selenium/interview/frames/IframeHandlingTest.java) |
| What is the difference between implicit, explicit and fluent waits? | 🟡 ⭐ | [`interview/synchronization/`](src/test/java/com/automation/selenium/interview/synchronization) |
| What is PageFactory? Is it the same as POM? | 🟡 ⭐ | [`BasePage`](src/main/java/com/automation/selenium/pages/BasePage.java), [`LoginPage`](src/main/java/com/automation/selenium/pages/LoginPage.java) |
| How do you handle multiple windows? | 🟢 ⭐ | [`MultipleWindowTest`](src/test/java/com/automation/selenium/interview/windows/MultipleWindowTest.java) |
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

[`ThreadLocalDriverTest`](src/test/java/com/automation/selenium/interview/framework/ThreadLocalDriverTest.java) proves it with two threads and two browser sessions; [`ParallelExecutionTest`](src/test/java/com/automation/selenium/interview/testng/ParallelExecutionTest.java) shows the same idea with TestNG's `threadPoolSize`.

## 5. Project structure

```
selenium-java-framework
├── docs/interview-index.md            76 questions → short answer → implementation, with difficulty levels
├── docs/common-interview-traps.md     Trick questions: the short answer and where the lab shows it
├── docs/avoid-vs-prefer.md            Common mistakes next to the better code, with the reason
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
    │   ├── interview   One package per interview topic (the lab):
    │   │   ├── PracticeSiteTest      base for the practice-site examples (open a page, explicit waits)
    │   │   ├── locators              LocatorStrategiesTest
    │   │   ├── synchronization       ImplicitWaitTest, ExplicitWaitTest, FluentWaitTest
    │   │   ├── exceptions            SeleniumExceptionsTest
    │   │   ├── alerts                AlertHandlingTest
    │   │   ├── frames                IframeHandlingTest
    │   │   ├── windows               MultipleWindowTest
    │   │   ├── dropdowns             SelectDropdownTest
    │   │   ├── elements              CheckboxTest, WebTableTest
    │   │   ├── actions               ActionsClassTest
    │   │   ├── javascript            JavaScriptExecutorTest
    │   │   ├── cookies               CookieTest
    │   │   ├── upload                FileUploadTest
    │   │   ├── testng                AnnotationOrderTest, ListenerTest (+ TestEventsListener),
    │   │   │                         DataProviderVariantsTest, ParallelExecutionTest, DependencyTest
    │   │   └── framework             ThreadLocalDriverTest, ConfigOverrideTest
    │   └── tests       LoginTest, SystemUsersTest, EmployeeTest        (application tests on OrangeHRM)
    │                   SeleniumScenariosTest, TestNgFeaturesTest       (interview scenarios on OrangeHRM)
    │                   ReportDemoTest                                  (fails and skips on purpose)
    └── test/resources/config.properties
```

**Two styles on purpose.** The `tests/` classes are built like a real suite: page objects hold every locator, and tests only call page methods and assert. The `interview/` examples call the Selenium API directly, with their locators as constants at the top, because there the API call *is* the interview topic (how do you switch to a frame?). Each `interview/` class can be read on its own.

**Practice site.** OrangeHRM has no alerts, frames, `<select>` elements or checkboxes, so those examples run on [the-internet.herokuapp.com](https://the-internet.herokuapp.com). It is a free Heroku app that sometimes sends an error page, or a page whose scripts did not load; `PracticeSiteTest.open()` detects that and loads the page again (up to three times), writing each reload into the report.

## 6. Selenium interview examples

**One class per topic** in [`interview/`](src/test/java/com/automation/selenium/interview). Each class Javadoc starts with the interview question and the answer to give, then the concept and the recommended approach; each test shows one technique. Run a topic with its group, e.g. `-Dgroups=frames`.

| Topic (group) | Level | Class | What it shows |
|---|---|---|---|
| Locators (`locators`) | 🟢🟡 ⭐ | `LocatorStrategiesTest` | All eight `By` strategies · XPath axes · relative locators · a dynamic locator built from a value |
| Waits (`synchronization`) | 🟢🟡 ⭐ | `ImplicitWaitTest`, `ExplicitWaitTest`, `FluentWaitTest` | No wait vs implicit wait · presence vs visibility · loading indicator · enabled input · FluentWait · custom condition |
| Exceptions (`exceptions`) | 🟢🟡 ⭐ | `SeleniumExceptionsTest` | Ten exceptions reproduced on purpose, each with its cause, fix and the workaround to avoid |
| Alerts (`alerts`) | 🟢 ⭐ | `AlertHandlingTest` | Alert · confirm (OK and Cancel) · prompt |
| Frames (`frames`) | 🟢 ⭐ | `IframeHandlingTest` | Nested frames by name · `parentFrame` · `defaultContent` · by index · iframe by `WebElement` |
| Windows (`windows`) | 🟢 ⭐ | `MultipleWindowTest` | New window by handle set difference · by title · close and switch back |
| Drop-downs (`dropdowns`) | 🟢 ⭐ | `SelectDropdownTest` | `Select` by text, value and index · options · a disabled option refused |
| Elements (`elements`) | 🟢🟡 ⭐ | `CheckboxTest`, `WebTableTest` | Set a checkbox state safely · table rows as maps · totals · columns by class · `.//` inside a row |
| Actions (`actions`) | 🟡 | `ActionsClassTest` | Hover · right-click · Shift + key · slider with arrow keys |
| JavaScript (`javascript`) | 🟡 ⭐ | `JavaScriptExecutorTest` | Arguments and return types · scrolling · why a JavaScript click hides bugs |
| Cookies (`cookies`) | 🟡 | `CookieTest` | Add, read, delete · log out by deleting the session cookie · skip the login with a saved cookie |
| Upload (`upload`) | 🟢 ⭐ | `FileUploadTest` | `sendKeys` the absolute path to the file input |

**Coding scenarios on OrangeHRM.** [`SeleniumScenariosTest`](src/test/java/com/automation/selenium/tests/SeleniumScenariosTest.java) solves one classic "can you code this?" task per test, through the page objects:

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

The page objects add more: labels not linked to inputs (`BasePage.inField`), spinner waits (`BasePage.waitForLoader`), tables as *column → value* (`BasePage.tableRows`), and `clear()` on a Vue field (`BasePage.type`).

## 7. TestNG interview examples

[`TestNgFeaturesTest`](src/test/java/com/automation/selenium/tests/TestNgFeaturesTest.java) uses one TestNG feature per test on a real OrangeHRM check. The classes in [`interview/testng/`](src/test/java/com/automation/selenium/interview/testng) need no browser and run in about two seconds (`-Dgroups=testng`).

| Feature | Level | Where |
|---|---|---|
| Annotation order: `@BeforeSuite` … `@AfterSuite` (recorded and checked) | 🟢 ⭐ | `AnnotationOrderTest` |
| `@BeforeMethod` / `@AfterMethod` with `alwaysRun` | 🟢 ⭐ | `BaseTest`, `AnnotationOrderTest` |
| Groups (class level + `smoke`), `excludedGroups` | 🟢 ⭐ | `LoginTest`, `SystemUsersTest`, `EmployeeTest`, `pom.xml` |
| `SoftAssert` | 🟢 ⭐ | `softAssertChecksTheLoginPage` |
| `expectedExceptions` + message regex | 🟡 | `wrongPasswordThrowsTheLoginError` |
| `priority` + `dependsOnMethods` | 🟢 ⭐ | `loginPageIsShown` → `adminLogsInAfterThePageCheck` |
| `dependsOnGroups`, `enabled = false` | 🟡 | `DependencyTest` |
| `invocationCount` | 🟢 | `wrongPasswordIsRejectedEveryTime` |
| `timeOut` | 🟡 | `loginFinishesWithinOneMinute` |
| `IRetryAnalyzer` | 🔴 ⭐ | `adminLoginIsRetriedOnceOnFailure` · `RetryOnce` |
| `@Parameters` + `@Optional` | 🟡 | `titleMatchesTheSuiteParameter` · `testng.xml` |
| `@DataProvider` shared through `dataProviderClass` | 🟡 ⭐ | `LoginTest.loginIsRejectedForInvalidCredentials` · `TestData` |
| `@DataProvider` as a lazy `Iterator`, `Method`-aware, `parallel = true` | 🔴 | `DataProviderVariantsTest` |
| `SkipException`; skip caused by a failed dependency | 🟢 | `ReportDemoTest` |
| Parallel classes; `invocationCount` + `threadPoolSize` with a `ThreadLocal` | 🔴 ⭐ | `pom.xml` (Surefire), `testng.xml` · `ParallelExecutionTest` |
| `ITestListener` registered with `@Listeners` | 🔴 | `ListenerTest` + `TestEventsListener` |
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
- **`ThreadLocal` driver and life cycle.** `DriverManager` keeps one `WebDriver` per thread; `BaseTest` starts a fresh browser before each test and always quits it afterwards. [`ThreadLocalDriverTest`](src/test/java/com/automation/selenium/interview/framework/ThreadLocalDriverTest.java) starts browsers on two threads and checks they get two sessions, and that `quit()` empties the slot.
- **Configuration.** `config.properties`, overridable with `-Dkey=value` (see [How to run](#11-how-to-run)); [`ConfigOverrideTest`](src/test/java/com/automation/selenium/interview/framework/ConfigOverrideTest.java) shows the override and the fail-fast message for a missing setting.
- **Test isolation on a shared site.** Checks never depend on how many records exist. The PIM test creates its own employee with a random ID and deletes it again; if the test fails halfway, `BaseTest` takes the screenshot, then calls the `cleanUp()` hook, which `EmployeeTest` overrides to delete the leftover.
- **Step logging.** Page methods record each action with `Steps.log` (passwords masked), and the report shows the steps under each test.

## 9. Common interview traps

The full page, with about 30 traps grouped by topic and a link to the code for each, is [**docs/common-interview-traps.md**](docs/common-interview-traps.md). A few of them:

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
| After a link opens a new window, does Selenium switch to it? | No. The driver stays on the old window until `switchTo().window(handle)`. |
| The click is intercepted: use a JavaScript click? | No. Something covers the element; a JavaScript click hides the real problem. Wait for the overlay to go. |
| Inside `row.findElement(By.xpath("//td"))`, which cell do you get? | The first cell of the whole page. Use `.//td` to stay in the row. |

## 10. Interview-ready answers

**Q: What is the difference between implicit and explicit wait?**
Implicit wait applies to every element lookup of the driver; explicit wait waits for one condition on one element. Explicit waits give precise synchronization and clear errors, and the two should not be mixed. → [`interview/synchronization/`](src/test/java/com/automation/selenium/interview/synchronization), [`BasePage`](src/main/java/com/automation/selenium/pages/BasePage.java)

**Q: How do you run Selenium tests in parallel safely?**
Give every test thread its own browser by keeping the driver in a `ThreadLocal<WebDriver>`, start and quit it per test, and keep test data independent. → [`DriverManager`](src/main/java/com/automation/selenium/driver/DriverManager.java), [`BaseTest`](src/test/java/com/automation/selenium/base/BaseTest.java)

**Q: How do you handle a stale element?**
Find the element again instead of reusing the old reference. In this lab, PageFactory proxies re-find on every use and the wait ignores `StaleElementReferenceException`, so a re-render is simply retried. → [`SeleniumExceptionsTest`](src/test/java/com/automation/selenium/interview/exceptions/SeleniumExceptionsTest.java)`.staleElementReferenceException`, `SeleniumScenariosTest.findByFieldSurvivesAReloadButCacheLookupGoesStale`

**Q: How do you handle a JavaScript alert or an iframe?**
An alert is a browser dialog: wait with `alertIsPresent()`, then `accept()`, `dismiss()` or `sendKeys()`. A frame is a separate document: switch into it (by name, index or element), and back with `defaultContent()`. → [`AlertHandlingTest`](src/test/java/com/automation/selenium/interview/alerts/AlertHandlingTest.java), [`IframeHandlingTest`](src/test/java/com/automation/selenium/interview/frames/IframeHandlingTest.java)

**Q: What is the Page Object Model, and why use it?**
One class per page holds its locators and actions; tests call methods and assert. A UI change is fixed in one place, and tests read like the business flow. → [`pages/`](src/main/java/com/automation/selenium/pages)

**Q: How does a DataProvider work?**
A method annotated `@DataProvider` returns `Object[][]`; TestNG runs the test once per row and passes the row as parameters. → [`TestData`](src/test/java/com/automation/selenium/data/TestData.java), `LoginTest.loginIsRejectedForInvalidCredentials`

All 76 answers are in the [index](docs/interview-index.md); the trick questions are on the [common interview traps](docs/common-interview-traps.md) page, and common mistakes with the better code are on [avoid vs prefer](docs/avoid-vs-prefer.md).

## 11. How to run

Requirements: JDK 17 or newer, Maven, and Chrome (or Firefox/Edge).

```bash
mvn clean test                          # all tests, visible browser
mvn clean test -Dheadless=true          # without a window
mvn clean test -Dgroups=interview       # one group: smoke, login, admin, pim or interview
mvn clean test -Dgroups=frames          # one interview topic: locators, synchronization, exceptions, alerts, frames,
                                        #   windows, dropdowns, elements, actions, javascript, cookies, upload,
                                        #   testng, framework
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
| `practiceUrl` | `https://the-internet.herokuapp.com` | Practice site for the `interview/` examples |
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
| `interview/…` Selenium topics (15 classes) | `interview` + topic | 46 examples on the practice site (see [section 6](#6-selenium-interview-examples)) |
| `interview/testng/…` (5 classes + a listener) | `interview`, `testng` | 24 runs, no browser, about 2 seconds |
| `interview/framework/…` (2 classes) | `interview`, `framework` | 4 tests: two threads with two browsers, configuration overrides |

A normal run has **108 tests**: 17 application tests, 17 interview scenarios on OrangeHRM, and 74 runs of the `interview/` examples, on 3 threads. Most of the run time is spent waiting for the two public sites; `-Dgroups=testng` runs in seconds.

| Command | Tests | Passed | Failed | Skipped | Time |
|---|---|---|---|---|---|
| `mvn clean test -Dheadless=true` | 108 | 107 | 1 (site data: OrangeHRM's menu lost "Leave"; test fixed since) | 0 | 12 min 24 s |
| `mvn clean test -Pdemo -Dheadless=true` | 112 | 107 | 2 on purpose + 1 (practice-site frame error) | 2 on purpose | 12 min 50 s |
| `mvn clean test -Dgroups=testng` | 24 | 24 | 0 | 0 | about 2 s |

Latest runs: 6 Oct 2026, headless Chrome, JDK 27, 3 threads. Both web sites are public and shared, so an occasional failure comes from the site (an error page, a module switched off by another user); the report's screenshot shows which. `SeleniumScenariosTest.adminCanLogInWithTheKeyboard` failed in some full parallel runs (the password landed in the user name field) but never in 27 isolated attempts; `LoginPage.loginWithKeyboard` now checks both fields before pressing Enter and names what each one holds if this happens again.

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

With the **Failed** filter and the failed cards opened. The first card is a real-world example: the practice site sent Heroku's "Application error" page into one frame, the test reloaded the page twice (both reloads are listed as steps), then failed with a screenshot that shows the broken frame:

![Failed tests in the HTML report](docs/images/html-report-failures.png)

## 14. Learning path

Work through the [index](docs/interview-index.md) in this order; every topic below has runnable code (the group name runs just that topic). Finish each level with the matching parts of the [traps page](docs/common-interview-traps.md) and [avoid vs prefer](docs/avoid-vs-prefer.md).

```
Level 1: Selenium basics
    close vs quit, findElement(s), navigation          tests/, interview/exceptions
    locators: all eight By, XPath axes, relative       interview/locators
    alerts, frames, windows and tabs                   interview/alerts, frames, windows
    <select>, custom drop-downs, checkboxes            interview/dropdowns, elements, BasePage
        ↓
Level 2: Synchronization
    why not Thread.sleep, implicit vs explicit         interview/synchronization
    presence vs visibility, ExpectedConditions,
    FluentWait, custom conditions, spinners
    stale elements                                     interview/exceptions
        ↓
Level 3: Advanced Selenium
    Actions, JavascriptExecutor                        interview/actions, javascript
    web tables, screenshots, broken links              interview/elements, tests/SeleniumScenariosTest
    cookies, file upload                               interview/cookies, upload
    the ten common exceptions                          interview/exceptions
        ↓
Level 4: TestNG
    annotation order, alwaysRun                        interview/testng/AnnotationOrderTest
    assertions and SoftAssert, groups, priority,       tests/TestNgFeaturesTest
    dependencies, invocationCount, timeOut, retry      interview/testng/DependencyTest
    DataProvider (all forms), Parameters               interview/testng/DataProviderVariantsTest
    listeners, parallel execution                      interview/testng/ListenerTest, ParallelExecutionTest
        ↓
Level 5: Framework design
    POM, PageFactory, BasePage, BaseTest               pages/, base/
    DriverManager + ThreadLocal, configuration         interview/framework
    test data, isolation and cleanup, reporting        tests/EmployeeTest, report/
```

## 15. Future topics

Next steps for the lab:

- **The PDF** updated to link the new `interview/` examples.

Deliberately **out of scope** here (they belong in a separate production-style portfolio project): Docker, Selenium Grid, CI/CD, API testing and cloud execution.

Known limitations: only Chrome has been run (Firefox and Edge are supported by `DriverManager`). Both sites are public and shared: OrangeHRM is sometimes slow or down, and the free Heroku practice site sometimes sends broken pages (handled by `PracticeSiteTest.open()`, which reloads and logs it). Application tests have no retry on purpose.

## 16. Author

**Niranjan Kumar Agri** · [GitHub](https://github.com/niranjankagri) · [LinkedIn](https://www.linkedin.com/in/niranjan-kumar-agri/)

Feedback and questions are welcome through GitHub issues.
