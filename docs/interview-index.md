# Interview Question Index

Every Selenium, TestNG and framework question this lab answers with **working code**. For each one you get the short answer to give in an interview, the exact class and method that implements it, and the question number in [`interview-questions.pdf`](interview-questions.pdf), which has the long explanation and an example.

**Levels:** 🟢 Beginner · 🟡 Intermediate · 🔴 Advanced · ⭐ Frequently asked

**How to use it:** find your question, say the short answer out loud, then open the implementation and read the comments. Every line of code is commented, and the class and method Javadoc explains why the approach is used.

Paths are shortened below: `pages/` = [`src/main/java/com/automation/selenium/pages/`](../src/main/java/com/automation/selenium/pages), `tests/` = [`src/test/java/com/automation/selenium/tests/`](../src/test/java/com/automation/selenium/tests).

## Contents

1. [WebDriver basics](#1-webdriver-basics)
2. [Locators](#2-locators)
3. [Waits and synchronization](#3-waits-and-synchronization)
4. [Selenium exceptions](#4-selenium-exceptions)
5. [Browser and element interactions](#5-browser-and-element-interactions)
6. [Page Object Model and framework design](#6-page-object-model-and-framework-design)
7. [TestNG core](#7-testng-core)
8. [TestNG data, parallel runs, listeners and retry](#8-testng-data-parallel-runs-listeners-and-retry)
9. [Reporting, build and run](#9-reporting-build-and-run)
10. [Not covered by code yet](#10-not-covered-by-code-yet)

---

## 1. WebDriver basics

| # | Question | Level | Interview answer | Implementation | PDF |
|---|---|---|---|---|---|
| 1 | What is the difference between `close()` and `quit()`? | 🟢 ⭐ | `close()` closes the current window only; `quit()` closes every window and ends the driver session. Use `quit()` in teardown, or browsers and driver processes leak. | [`DriverManager.quit()`](../src/main/java/com/automation/selenium/driver/DriverManager.java) (quit) · `SeleniumScenariosTest.dashboardOpensInASecondTab()` (close one tab) | Q6 |
| 2 | What is the difference between `findElement` and `findElements`? | 🟢 ⭐ | `findElement` returns one element or throws `NoSuchElementException`; `findElements` returns a list that is empty when nothing matches. Use the list form to check that something is *absent*. | `AppPage.menuItems()` (a `List<WebElement>` field) · `LoginPage.errorAlerts`: empty list = no banner · `SeleniumScenariosTest.sideMenuListsTheCoreModules()` | Q7, Q91 |
| 3 | What is the difference between `get()` and `navigate()`? | 🟢 | `get(url)` loads a page and waits for its load event; `navigate()` also gives `back()`, `forward()` and `refresh()`. | `BasePage.openPath()` (get) · `LoginTest.backAfterLogoutStaysLoggedOut()` (back + refresh) | Q8 |
| 4 | How do you run tests headless, and what are browser options for? | 🟢 | Options configure the browser before it starts: headless mode, window size, flags. Headless needs no screen, so it suits CI; set the window size explicitly so the layout matches a normal run. | [`DriverManager.chromeOptions()`](../src/main/java/com/automation/selenium/driver/DriverManager.java), `firefoxOptions()`, `edgeOptions()` | Q9, Q10 |
| 5 | What is Selenium Manager? | 🟢 | Since Selenium 4.6, Selenium Manager finds or downloads the right driver binary for the installed browser, so no `chromedriver.exe` or WebDriverManager is needed. | `DriverManager.start()`: just `new ChromeDriver(options)`; there are no driver binaries in the repo | Q4 |

## 2. Locators

| # | Question | Level | Interview answer | Implementation | PDF |
|---|---|---|---|---|---|
| 6 | Which locator strategies are there, and which do you prefer? | 🟢 ⭐ | id, name, className, tagName, linkText, partialLinkText, CSS and XPath. Prefer stable, meaningful attributes (id, name, `data-*`), then short CSS, and XPath when you need text or to move up the tree. | [`LoginPage`](../src/main/java/com/automation/selenium/pages/LoginPage.java) fields: `@FindBy(name = …)`, `css`, `tagName`, `how = How.CSS` | Q12, Q13 |
| 7 | CSS selector or XPath? | 🟢 ⭐ | CSS is shorter and usually faster but cannot match text or go to a parent; XPath can do both (`normalize-space()`, `ancestor::`). | `BasePage`: CSS for class-based fields, XPath for `recordsFoundLabel`, `button(text)`, `inField(label, …)` | Q14 |
| 8 | How do you locate dynamic elements (changing ids, values known only at run time)? | 🟡 ⭐ | Anchor on something stable, such as visible text, a label or a stable class, and build the locator from the value at run time instead of hard-coding generated ids. | `BasePage.button(text)`, `BasePage.input(label)` · `AppPage.openMenu(item)` | Q17 |
| 9 | How do you find an input whose label is not linked to it? | 🟡 | Find the label by its text, go up to the container that holds both, then down to the input: `//label[.='X']/ancestor::div[…]//input`. | [`BasePage.inField()`](../src/main/java/com/automation/selenium/pages/BasePage.java), `input()` | Q18 |
| 10 | How do you find a table row by its content and click something in it? | 🟡 ⭐ | XPath with a predicate on the row: *the row that contains a cell with this text*, then the button inside that row. | [`EmployeeListPage.delete(employeeId)`](../src/main/java/com/automation/selenium/pages/EmployeeListPage.java) | Q19 |

## 3. Waits and synchronization

| # | Question | Level | Interview answer | Implementation | PDF |
|---|---|---|---|---|---|
| 11 | Why avoid `Thread.sleep()`? | 🟢 ⭐ | It always waits the full time: too long when the app is fast, too short when it is slow. An explicit wait polls a condition and continues as soon as it is true. | Nowhere in the code. Every action in [`BasePage`](../src/main/java/com/automation/selenium/pages/BasePage.java) (`click`, `type`, `textOf`, …) waits explicitly | Q21 |
| 12 | Implicit or explicit wait? Can you mix them? | 🟢 ⭐ | Implicit wait applies to every element lookup; explicit wait waits for one condition on one element. Don't mix them: the timeouts add up, and "is it gone?" checks become slow. Use explicit waits only. | `BasePage` constructor: one `WebDriverWait`, no `implicitlyWait` anywhere | Q22 |
| 13 | Which `ExpectedConditions` do you use? Presence or visibility? | 🟡 | `elementToBeClickable`, `visibilityOf…`, `invisibilityOfAllElements`, `urlContains`. Presence = in the DOM; visibility = in the DOM *and* displayed. Interact only after visibility or clickability. | `BasePage.click()`, `waitVisible()`, `textsOf()`, `waitForUrl()`, `waitForLoader()` | Q23, Q24 |
| 14 | How do you write a custom wait condition? | 🟡 | `wait.until(driver -> …)` with a lambda: return `null` or `false` to keep waiting, any other value to stop and use it. | [`EmployeeProfilePage.fullName()`](../src/main/java/com/automation/selenium/pages/EmployeeProfilePage.java) (wait for non-empty text) · `BasePage.tableRows()` | Q27 |
| 15 | How do you wait for a loading spinner (AJAX)? | 🟡 ⭐ | Give the spinner a short moment to appear, then wait until it is invisible. Waiting only for "invisible" can pass before loading has even started. | [`BasePage.waitForLoader()`](../src/main/java/com/automation/selenium/pages/BasePage.java) | Q26 |
| 16 | What happens when an explicit wait times out? | 🟡 | It throws `TimeoutException` with the last condition and its cause. Catch it only where "not there" is a valid answer, and turn it into `false`. | `BasePage.isVisible()`, `waitForUrl()` · `LoginPage.loginAs()` turns the timeout into a clear login error | Q28 |

## 4. Selenium exceptions

| # | Question | Level | Interview answer | Implementation | PDF |
|---|---|---|---|---|---|
| 17 | What is `StaleElementReferenceException`, and how do you handle it? | 🟡 ⭐ | The element you hold was removed from the DOM (reload, re-render, AJAX). Find it again instead of reusing the old reference; tell the wait to ignore staleness so it re-finds on the next poll. | `BasePage` constructor: `wait.ignoring(StaleElementReferenceException.class)` · `SeleniumScenariosTest.findByFieldSurvivesAReloadButCacheLookupGoesStale()` reproduces it | Q25 |
| 18 | Why does a click fail with `ElementClickInterceptedException`? | 🟡 | Another element (overlay, spinner, sticky header) covers the target. Wait for the overlay to go away; don't "fix" it with a JavaScript click, which hides the real problem. | [`AddEmployeePage.waitUntilLoaded()`](../src/main/java/com/automation/selenium/pages/AddEmployeePage.java) waits for the form overlay | Q86 |
| 19 | What does `TimeoutException` tell you? | 🟡 | The condition was not met in time. Read the message: it names the condition, often with a `NoSuchElementException` cause (wrong locator) or a page that never loaded. | `BasePage.isVisible()` · `LoginPage.loginAs()` | Q28, Q85 |

## 5. Browser and element interactions

| # | Question | Level | Interview answer | Implementation | PDF |
|---|---|---|---|---|---|
| 20 | How do you handle multiple windows or tabs? | 🟢 ⭐ | Remember the current handle, open or switch with `switchTo().newWindow(TAB)` or `switchTo().window(handle)`, then `close()` the tab and switch back. After `close()` the driver has no window until you switch. | [`SeleniumScenariosTest.dashboardOpensInASecondTab()`](../src/test/java/com/automation/selenium/tests/SeleniumScenariosTest.java) | Q33, Q95 |
| 21 | How do you handle a drop-down that is not a `<select>`? | 🟡 ⭐ | `Select` only works on `<select>`. For a custom drop-down, click the box to open the list, then click the option by its text. | `BasePage.choose()`, `options()` · `SeleniumScenariosTest.userRoleDropdownOffersAdminAndEss()` | Q30, Q93 |
| 22 | When do you use the `Actions` class? | 🟡 | For low-level input: keyboard keys, hover, drag and drop, right-click, double-click. Build the chain, then `perform()`. | [`LoginPage.loginWithKeyboard()`](../src/main/java/com/automation/selenium/pages/LoginPage.java) · `SeleniumScenariosTest.adminCanLogInWithTheKeyboard()` | Q34, Q94 |
| 23 | When do you use `JavascriptExecutor`, and what is the risk? | 🟡 ⭐ | To read or do what the WebDriver API cannot (`document.readyState`, scrolling). Risk: a JavaScript click skips the visibility and overlay checks a real user would hit, so it can hide real bugs. | `BasePage.runScript()` · `AppPage.readyState()` · `SeleniumScenariosTest.javascriptReadsThePageState()` | Q35, Q96 |
| 24 | `clear()` does not empty a field. Why, and what do you do? | 🟡 | Frameworks such as Vue or React keep their own model and may not see `clear()`. Send real keys instead: Ctrl+A, then Delete. | [`BasePage.type()`](../src/main/java/com/automation/selenium/pages/BasePage.java) | Q36 |
| 25 | How do you take a screenshot of the page and of one element? | 🟢 ⭐ | Page: cast the driver to `TakesScreenshot`. Element (Selenium 4): `element.getScreenshotAs(…)`. Take it on failure *before* cleanup, so it shows the failing moment. | `BaseTest.attachScreenshot()` (page, on failure) · `LoginPage.formScreenshot()` · `SeleniumScenariosTest.loginFormScreenshotIsAPng()` | Q37, Q97 |
| 26 | What is the difference between text, attribute and property? | 🟡 | `getText()` = visible text; `getDomAttribute()` = the value as written in the HTML; `getDomProperty()` = the live value (what is typed in an input, the absolute `href`). | `BasePage.valueOf()` (property) · `LoginPage.placeholder()` (attribute) · `AppPage.menuLinks()` (href property) | Q39 |
| 27 | How do you find broken links? | 🟡 ⭐ | Collect the `href` of every link with Selenium, then request each URL with an HTTP client; Selenium cannot read status codes. A status of 400 or more is broken. | [`Links`](../src/main/java/com/automation/selenium/utils/Links.java) · `SeleniumScenariosTest.sideMenuHasNoBrokenLinks()` | Q92 |
| 28 | How do you read a web table? | 🟡 ⭐ | Read the header titles, then each row's cells, and map them by position into *column → value*; read the whole table inside one wait, so a re-render retries. | `BasePage.tableRows()`, `column()` · `EmployeeListPage.employeeNames()` | Q19 |

## 6. Page Object Model and framework design

| # | Question | Level | Interview answer | Implementation | PDF |
|---|---|---|---|---|---|
| 29 | What is the Page Object Model, and why use it? | 🟢 ⭐ | One class per page holds its locators and actions; tests only call methods and assert. When the UI changes, you fix one page class instead of many tests. | [`pages/`](../src/main/java/com/automation/selenium/pages): the tests contain no locators | Q40 |
| 30 | Should page objects contain assertions? | 🟡 | Generally no: pages return values and states, tests decide what is correct. Pages may fail fast when they cannot do their job (e.g. login rejected). | `LoginPage.isDisplayed()` returns a boolean · `LoginPage.loginAs()` throws on a rejected login | Q41 |
| 31 | What is PageFactory? Is it the same as POM? | 🟡 ⭐ | POM is a design pattern; PageFactory is one way to create the elements (`@FindBy` + `initElements`). Each field is a lazy proxy that finds the element on every use. `@CacheLookup` keeps the first element and goes stale after a re-render. | `BasePage` constructor (`PageFactory.initElements`) · `LoginPage.cachedUsernameField` · `SeleniumScenariosTest.findByFieldSurvivesAReloadButCacheLookupGoesStale()` | Q42 |
| 32 | What is method chaining (a fluent page API)? | 🟢 | Methods return a page object (`this` or the next page), so steps read like a sentence: `openAdmin().filterByRole("Admin").search()`. | [`SystemUsersPage`](../src/main/java/com/automation/selenium/pages/SystemUsersPage.java) · `SystemUsersTest.filterByRoleAndStatus()` | Q43 |
| 33 | What goes into a `BasePage`? | 🟡 | What every page needs: the driver, the wait, PageFactory set-up, safe actions (click, type, read) and the app's shared widgets (spinner, toast, tables). | [`BasePage`](../src/main/java/com/automation/selenium/pages/BasePage.java), [`AppPage`](../src/main/java/com/automation/selenium/pages/AppPage.java) | Q44 |
| 34 | How do you run Selenium tests in parallel safely? How do you make WebDriver "thread-safe"? | 🔴 ⭐ | Give every thread its own browser: keep the driver in a `ThreadLocal<WebDriver>`, never in a static field. Strictly, WebDriver itself is still not thread-safe; ThreadLocal makes sure no two threads share one. | [`DriverManager`](../src/main/java/com/automation/selenium/driver/DriverManager.java) (`ThreadLocal`, `remove()` after quit) | Q45, Q87 |
| 35 | How do you manage the driver's life cycle? | 🔴 | Start a fresh browser before each test, quit it after each test (in `finally`), and quit any leftover before starting a new one. | `BaseTest.startBrowser()` / `stopBrowser()` · `DriverManager.start()` | Q45 |
| 36 | How do you manage configuration (URL, browser, timeouts)? | 🟡 | One properties file on the classpath, overridable by `-Dkey=value`, read through one class, so CI can change settings without code changes. | [`Config`](../src/main/java/com/automation/selenium/config/Config.java) · [`config.properties`](../src/test/resources/config.properties) | Q46, Q77 |
| 37 | Where do you keep test data? | 🟡 | Out of the test logic: data providers for tables of cases, generators for unique data, config for credentials. | [`TestData`](../src/test/java/com/automation/selenium/data/TestData.java) (`invalidCredentials`, `newEmployee()`) | Q47 |
| 38 | How do you keep tests independent on a shared environment? | 🔴 ⭐ | Each test creates the data it needs with unique values, never relies on existing records or counts, and deletes what it made, even when it fails. | `EmployeeTest.employeeCanBeAddedFoundAndDeleted()` + `cleanUp()` · `BaseTest.runCleanUp()` | Q48, Q88 |
| 39 | How do you log test steps? | 🟡 | Page methods log each user action; the steps are attached to the running test and shown in the report. Mask secrets. | [`Steps.log()`](../src/main/java/com/automation/selenium/utils/Steps.java) · `LoginPage.mask()` | Q50 |

## 7. TestNG core

| # | Question | Level | Interview answer | Implementation | PDF |
|---|---|---|---|---|---|
| 40 | In what order do TestNG annotations run? `@BeforeTest` or `@BeforeMethod`? | 🟢 ⭐ | Suite → Test → Class → Method → @Test → and back out. `@BeforeTest` runs once per `<test>` tag in testng.xml; `@BeforeMethod` runs before every test method. | [`BaseTest`](../src/test/java/com/automation/selenium/base/BaseTest.java) (`@BeforeMethod` / `@AfterMethod`) | Q52, Q53 |
| 41 | What does `alwaysRun = true` do? | 🟡 | A set-up or teardown method then runs even when a group filter would leave it out, or after a failure. Without it, `-Dgroups=smoke` would skip the browser start. | `BaseTest.startBrowser()`, `stopBrowser()` | Q54 |
| 42 | What are groups, and how do you run them? | 🟢 ⭐ | Labels on tests (`groups = "smoke"`), selected with `-Dgroups=…` or `<include>` and `<exclude>` in testng.xml. A class-level group applies to every method. | Class-level `@Test(groups = "login")` in `LoginTest` · `excludedGroups` in [`pom.xml`](../pom.xml) | Q55, Q75 |
| 43 | What do `priority` and `dependsOnMethods` do? What if the first test fails? | 🟢 ⭐ | `priority` orders tests (lower first). `dependsOnMethods` runs a test only if the other one passed; otherwise it is **skipped**, not failed. | `TestNgFeaturesTest.loginPageIsShown()` → `adminLogsInAfterThePageCheck()` · `ReportDemoTest.skippedBecauseDependencyFailed()` | Q56–Q58, Q100 |
| 44 | What does `invocationCount` do? | 🟢 | Runs the same test N times and reports each run, e.g. to catch a check that only sometimes passes. | `TestNgFeaturesTest.wrongPasswordIsRejectedEveryTime()` | Q56, Q101 |
| 45 | What does `timeOut` do? | 🟡 | Fails the test if it runs longer than the limit: a safety net against hangs, not a replacement for waits. | `TestNgFeaturesTest.loginFinishesWithinOneMinute()` | Q56, Q104 |
| 46 | Hard vs soft assertions? | 🟢 ⭐ | A hard assert stops at the first failure; `SoftAssert` collects every failure and reports them together at `assertAll()`. Forget `assertAll()` and the failures are lost. | `TestNgFeaturesTest.softAssertChecksTheLoginPage()` | Q59, Q98 |
| 47 | How do you skip a test at run time? | 🟢 | Throw `SkipException` with the reason; the test is reported as skipped, with that message. | `ReportDemoTest.skippedWhenPreconditionIsMissing()` | Q60, Q73 |
| 48 | How do you test that an exception is thrown? | 🟡 | `@Test(expectedExceptions = X.class, expectedExceptionsMessageRegExp = "…")`: the test passes only if that exception, with a matching message, is thrown. | `TestNgFeaturesTest.wrongPasswordThrowsTheLoginError()` | Q61, Q99 |

## 8. TestNG data, parallel runs, listeners and retry

| # | Question | Level | Interview answer | Implementation | PDF |
|---|---|---|---|---|---|
| 49 | How does a `@DataProvider` work? How do you share one? | 🟡 ⭐ | It returns `Object[][]`; TestNG runs the test once per row with the row as parameters. Share it by putting it in another class and naming it with `dataProviderClass`. | [`TestData.invalidCredentials()`](../src/test/java/com/automation/selenium/data/TestData.java) · `LoginTest.loginIsRejectedForInvalidCredentials()` | Q62, Q63 |
| 50 | `@DataProvider` or `@Parameters`? | 🟡 | `@Parameters` passes single values from testng.xml (environment-like settings); `@DataProvider` feeds many rows of test data from code. `@Optional` gives a default when the XML has no value. | `TestNgFeaturesTest.titleMatchesTheSuiteParameter()` · [`testng.xml`](../testng.xml) | Q64, Q103 |
| 51 | What goes into testng.xml? | 🟢 | Suites, tests, classes or packages, group include/exclude, parameters, listeners and parallel settings. | [`testng.xml`](../testng.xml) | Q65 |
| 52 | How do you run TestNG tests in parallel? Why `parallel=classes`? | 🔴 ⭐ | Set `parallel` (methods, classes, tests) and `thread-count`. `classes` keeps each class's methods on one thread, so a class's own fields and `dependsOnMethods` stay safe; each test still gets its own browser. | Surefire `parallel` / `threadCount` in [`pom.xml`](../pom.xml) · `parallel="classes"` in `testng.xml` · `DriverManager` | Q66, Q67 |
| 53 | Which listeners do you know? How do you register one? | 🔴 | `ITestListener` (per-test events), `ISuiteListener`, `IReporter` (report after the run), `IRetryAnalyzer`, `IAnnotationTransformer`. Register with `@Listeners`, in testng.xml, or through ServiceLoader (`META-INF/services`). | [`HtmlReportListener`](../src/main/java/com/automation/selenium/report/HtmlReportListener.java) (`IReporter`) · [`META-INF/services/org.testng.ITestNGListener`](../src/main/resources/META-INF/services/org.testng.ITestNGListener) | Q68, Q69 |
| 54 | How do you retry a failed test? Should you retry every failure? | 🔴 ⭐ | Implement `IRetryAnalyzer` and attach it with `retryAnalyzer = …`. Retry only for known environment hiccups; retrying everything hides real defects and flaky tests. | [`RetryOnce`](../src/test/java/com/automation/selenium/base/RetryOnce.java) · `TestNgFeaturesTest.adminLoginIsRetriedOnceOnFailure()` | Q71, Q102 |
| 55 | Why does a test show as skipped? | 🟡 | A failed dependency, a `SkipException`, a failed `@Before…` configuration method, or a retried attempt (the earlier try is reported as skipped). | `ReportDemoTest` (both skip types) | Q73 |

## 9. Reporting, build and run

| # | Question | Level | Interview answer | Implementation | PDF |
|---|---|---|---|---|---|
| 56 | How do you capture a screenshot on failure and show it in the report? | 🟡 ⭐ | In `@AfterMethod`, check that the result failed, take the screenshot, store it on the `ITestResult` as an attribute, and let the report read it from there. | `BaseTest.stopBrowser()` / `attachScreenshot()` · `HtmlReportListener.renderTest()` | Q37, Q82 |
| 57 | How do you build a custom HTML report? Why would you? | 🔴 | Implement `IReporter`; after the run, TestNG hands you every result. Write one self-contained file with what *your* team needs: steps, errors, screenshots, filters. | [`HtmlReportListener`](../src/main/java/com/automation/selenium/report/HtmlReportListener.java) | Q70, Q81 |
| 58 | How does Maven run TestNG? How do you run one class, method or group? | 🟢 | Surefire finds the TestNG tests and runs them. Use `-Dtest=Class#method` and `-Dgroups=…`. With `suiteXmlFiles` set, these switches are ignored, which is why this project runs without a suite file. | [`pom.xml`](../pom.xml) (Surefire) | Q74–Q76 |
| 59 | What is a Maven profile, and when do you use one? | 🟢 | A named set of settings switched on with `-P<id>`, e.g. a different group filter or environment. | `demo` profile in [`pom.xml`](../pom.xml): `mvn clean test -Pdemo` | Q78 |

## 10. Not covered by code yet

These are common interview topics this lab does **not** show with code yet. The OrangeHRM demo has no alerts, iframes, native `<select>` elements or checkboxes, so they will be added against a practice site. Until then, the PDF answers them in theory.

| Topic | PDF |
|---|---|
| JavaScript alerts (`switchTo().alert()`) | Q31 |
| iframes (`switchTo().frame()`, `defaultContent()`) | Q32 |
| Native `<select>` drop-downs (`Select` class) | Q29 |
| File upload (`sendKeys` on `input[type=file]`) | Q38 |
| Relative locators (`above`, `below`, `near`) | Q20 |
| Checkboxes and radio buttons, cookies | — |
| Implicit wait and FluentWait as runnable examples | Q22 |
| `NoSuchElementException`, `ElementNotInteractableException`, `NoSuchWindowException`, `NoSuchFrameException`, `InvalidSelectorException` reproduced on purpose | — |
| Full TestNG annotation order (`@BeforeSuite` … `@AfterSuite`) printed by a test | Q52 |
| An `ITestListener` example, and `parallel=methods` | Q66, Q68 |
