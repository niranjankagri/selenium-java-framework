# Interview Question Index

Every Selenium, TestNG and framework question this lab answers with **working code**. For each one you get the short answer to give in an interview, the exact class and method that implements it, and the question number in [`interview-questions.pdf`](interview-questions.pdf), which has the long explanation and an example.

**Levels:** 🟢 Beginner · 🟡 Intermediate · 🔴 Advanced · ⭐ Frequently asked

**How to use it:** find your question, say the short answer out loud, then open the implementation and read the comments. Every class starts with the interview question, the concept and the recommended approach; every line is commented. Trick questions are collected in [**Common interview traps**](common-interview-traps.md).

**Where the code is:**

| Short name | Folder | What runs there |
|---|---|---|
| `interview/<topic>/` | [`src/test/java/com/automation/selenium/interview/`](../src/test/java/com/automation/selenium/interview) | One example class per topic. Selenium topics run on the practice site [the-internet.herokuapp.com](https://the-internet.herokuapp.com); TestNG and framework topics need no site |
| `tests/` | [`src/test/java/com/automation/selenium/tests/`](../src/test/java/com/automation/selenium/tests) | Application tests and scenarios on OrangeHRM, built with page objects |
| `pages/` | [`src/main/java/com/automation/selenium/pages/`](../src/main/java/com/automation/selenium/pages) | Page objects for OrangeHRM |

Run one topic with its group, e.g. `mvn clean test -Dgroups=exceptions`.

## Contents

1. [WebDriver basics](#1-webdriver-basics)
2. [Locators](#2-locators)
3. [Waits and synchronization](#3-waits-and-synchronization)
4. [Selenium exceptions](#4-selenium-exceptions)
5. [Alerts, frames and windows](#5-alerts-frames-and-windows)
6. [Drop-downs, checkboxes and web tables](#6-drop-downs-checkboxes-and-web-tables)
7. [Actions, JavaScript, screenshots, cookies and files](#7-actions-javascript-screenshots-cookies-and-files)
8. [Page Object Model and framework design](#8-page-object-model-and-framework-design)
9. [TestNG core](#9-testng-core)
10. [TestNG data, parallel runs, listeners and retry](#10-testng-data-parallel-runs-listeners-and-retry)
11. [Reporting, build and run](#11-reporting-build-and-run)
12. [Not covered by code](#12-not-covered-by-code)

---

## 1. WebDriver basics

| # | Question | Level | Interview answer | Implementation | PDF |
|---|---|---|---|---|---|
| 1 | What is the difference between `close()` and `quit()`? | 🟢 ⭐ | `close()` closes the current window only; `quit()` closes every window and ends the driver session. Use `quit()` in teardown, or browsers and driver processes leak. | [`DriverManager.quit()`](../src/main/java/com/automation/selenium/driver/DriverManager.java) · `interview/windows/MultipleWindowTest` (close one window) | Q6 |
| 2 | What is the difference between `findElement` and `findElements`? | 🟢 ⭐ | `findElement` returns one element or throws `NoSuchElementException`; `findElements` returns a list that is empty when nothing matches. Use the list form to check that something is *absent*. | `interview/exceptions/SeleniumExceptionsTest.noSuchElementException` · `AppPage.menuItems()` · `SeleniumScenariosTest.sideMenuListsTheCoreModules()` | Q7, Q91 |
| 3 | What is the difference between `get()` and `navigate()`? | 🟢 | `get(url)` loads a page and waits for its load event; `navigate()` also gives `back()`, `forward()` and `refresh()`. | `BasePage.openPath()` (get) · `LoginTest.backAfterLogoutStaysLoggedOut()` (back + refresh) | Q8 |
| 4 | How do you run tests headless, and what are browser options for? | 🟢 | Options configure the browser before it starts: headless mode, window size, flags. Headless needs no screen, so it suits CI; set the window size explicitly so the layout matches a normal run. | [`DriverManager.chromeOptions()`](../src/main/java/com/automation/selenium/driver/DriverManager.java), `firefoxOptions()`, `edgeOptions()` | Q9, Q10 |
| 5 | What is Selenium Manager? | 🟢 | Since Selenium 4.6, Selenium Manager finds or downloads the right driver binary for the installed browser, so no `chromedriver.exe` or WebDriverManager is needed. | `DriverManager.start()`: just `new ChromeDriver(options)`; there are no driver binaries in the repo | Q4 |

## 2. Locators

| # | Question | Level | Interview answer | Implementation | PDF |
|---|---|---|---|---|---|
| 6 | Which locator strategies are there, and which do you prefer? | 🟢 ⭐ | id, name, className, tagName, linkText, partialLinkText, CSS and XPath. Prefer stable, meaningful attributes (id, name, `data-*`), then short CSS, and XPath when you need text or to move up the tree. | [`interview/locators/LocatorStrategiesTest`](../src/test/java/com/automation/selenium/interview/locators/LocatorStrategiesTest.java)`.everyLocatorStrategyFindsAnElement` · `LoginPage` `@FindBy` fields | Q12, Q13 |
| 7 | CSS selector or XPath? What are XPath axes? | 🟢 ⭐ | CSS is shorter and usually faster but cannot match text or go to a parent; XPath can (`normalize-space()`, `ancestor::`, `following-sibling::`). | `LocatorStrategiesTest.xpathAxesNavigateFromALabel` · `BasePage.inField()` | Q14, Q16 |
| 8 | How do you locate dynamic elements (values known only at run time)? | 🟡 ⭐ | Anchor on something stable (visible text, a label, a stable class) and build the locator from the value with `String.format`, instead of hard-coding generated ids. | `LocatorStrategiesTest.dynamicLocatorIsBuiltFromAValue` · `BasePage.button(text)`, `input(label)` | Q17 |
| 9 | What are relative locators (Selenium 4)? | 🟡 | `RelativeLocator.with(By.tagName("input")).below(element)`: find an element by its position (`above`, `below`, `toLeftOf`, `toRightOf`, `near`). Layout-dependent, so a real attribute is still better when there is one. | `LocatorStrategiesTest.relativeLocatorsFindFieldsByPosition` | Q20 |
| 10 | How do you find an input whose label is not linked to it? | 🟡 | Find the label by its text, go up to the container that holds both, then down to the input: `//label[.='X']/ancestor::div[…]//input`. | [`BasePage.inField()`](../src/main/java/com/automation/selenium/pages/BasePage.java), `input()` | Q18 |

## 3. Waits and synchronization

| # | Question | Level | Interview answer | Implementation | PDF |
|---|---|---|---|---|---|
| 11 | Why avoid `Thread.sleep()`? | 🟢 ⭐ | It always waits the full time: too long when the app is fast, too short when it is slow. An explicit wait polls a condition and continues as soon as it is true. | Nowhere in the code: every action in [`BasePage`](../src/main/java/com/automation/selenium/pages/BasePage.java) waits explicitly · `interview/synchronization/ExplicitWaitTest` | Q21 |
| 12 | What is an implicit wait? Can you mix it with explicit waits? | 🟢 ⭐ | `implicitlyWait(t)` makes every `findElement` retry for up to `t`; it is global and only waits for *existence*. Don't mix it with explicit waits: the timeouts add up. Use explicit waits only. | [`interview/synchronization/ImplicitWaitTest`](../src/test/java/com/automation/selenium/interview/synchronization/ImplicitWaitTest.java) (without and with, reset afterwards) | Q22 |
| 13 | Presence or visibility? Which `ExpectedConditions` do you use? | 🟡 ⭐ | Presence = in the DOM; visibility = in the DOM *and* displayed. A hidden element is present at once. Common conditions: `visibilityOf…`, `elementToBeClickable`, `invisibilityOf…`, `textToBe`, `urlContains`, `alertIsPresent`, `frameToBeAvailableAndSwitchToIt`. | [`ExplicitWaitTest.presenceIsNotVisibility`](../src/test/java/com/automation/selenium/interview/synchronization/ExplicitWaitTest.java) · `BasePage.click()`, `waitVisible()` | Q23, Q24 |
| 14 | How do you wait until a disabled element becomes usable? | 🟢 | `elementToBeClickable` = visible *and* enabled; wait for it before typing or clicking. | `ExplicitWaitTest.waitUntilAnInputIsEnabled` | Q23 |
| 15 | How do you wait for a loading spinner (AJAX)? | 🟡 ⭐ | Give the spinner a moment to appear, then wait until it is invisible. Waiting only for "invisible" can pass before loading has even started. | `ExplicitWaitTest.waitUntilTheLoadingIndicatorDisappears` · [`BasePage.waitForLoader()`](../src/main/java/com/automation/selenium/pages/BasePage.java) | Q26 |
| 16 | What is a FluentWait? How is it different from `WebDriverWait`? | 🟡 ⭐ | `FluentWait` lets you set the timeout, polling interval, ignored exceptions and message. `WebDriverWait` *is* a `FluentWait<WebDriver>` with defaults (500 ms, ignores `NotFoundException`). | [`interview/synchronization/FluentWaitTest`](../src/test/java/com/automation/selenium/interview/synchronization/FluentWaitTest.java)`.fluentWaitPollsUntilTheElementExists` | Q22, Q27 |
| 17 | How do you write a custom wait condition? | 🟡 | `wait.until(driver -> …)`: return `null`/`false` to keep waiting, any other value to stop and use it. Add `withMessage(...)`: a lambda has no readable name in the timeout. | `FluentWaitTest.customConditionReturnsTheValueItWaitedFor` · `EmployeeProfilePage.fullName()` · `BasePage.tableRows()` | Q27 |

## 4. Selenium exceptions

All in [`interview/exceptions/SeleniumExceptionsTest`](../src/test/java/com/automation/selenium/interview/exceptions/SeleniumExceptionsTest.java): each test reproduces the exception on purpose, then shows the fix; the Javadoc lists cause, fix and the workaround to avoid.

| # | Question | Level | Interview answer | Implementation | PDF |
|---|---|---|---|---|---|
| 18 | `NoSuchElementException`: causes and handling? | 🟢 ⭐ | Wrong locator, element not there yet, or in another frame/window. Fix the locator, wait, or switch frame; check absence with `findElements(...).isEmpty()`. | `noSuchElementException` | Q7, Q85 |
| 19 | What is `StaleElementReferenceException`, and how do you handle it? | 🟡 ⭐ | The element you hold was removed from the DOM (reload, re-render, AJAX). Find it again after the change; wait with `stalenessOf(old)`; keep locators, not elements. | `staleElementReferenceException` · `BasePage` (`wait.ignoring(Stale…)`) · `SeleniumScenariosTest.findByFieldSurvivesAReloadButCacheLookupGoesStale` | Q25 |
| 20 | `ElementNotInteractableException`? | 🟡 | The element exists but is hidden or has no size. Wait for visibility; point the locator at the visible element. Not a JavaScript click: a user can't click a hidden element either. | `elementNotInteractableException` | Q24 |
| 21 | `ElementClickInterceptedException`? | 🟡 ⭐ | Another element (overlay, spinner, banner) is on top. Wait for it to disappear; don't switch to a JavaScript click. | `elementClickInterceptedException` · `AddEmployeePage.waitUntilLoaded()` | Q86 |
| 22 | What does `TimeoutException` tell you? | 🟡 | The condition was not met in time; the message names the condition and the time tried. Read it before raising timeouts. | `timeoutException` · `BasePage.isVisible()` | Q28, Q85 |
| 23 | `NoSuchWindowException` / `NoSuchFrameException`? | 🟢 | Window: you used the driver after closing its window; switch to an open one. Frame: wrong name, or a nested frame; switch level by level. | `noSuchWindowException`, `noSuchFrameException` | Q32, Q33 |
| 24 | `InvalidSelectorException`? | 🟢 | The CSS or XPath itself is malformed (e.g. an unclosed bracket). Try the expression in DevTools first (`$x("…")`, `$$("…")`). | `invalidSelectorException` | Q14 |
| 25 | `NoAlertPresentException` / `UnhandledAlertException`? | 🟡 | No alert to switch to (wait with `alertIsPresent`), or the page was used while an alert was open (answer every alert you open). | `alertExceptions` | Q31 |

## 5. Alerts, frames and windows

| # | Question | Level | Interview answer | Implementation | PDF |
|---|---|---|---|---|---|
| 26 | How do you handle JavaScript alerts, confirms and prompts? | 🟢 ⭐ | They are browser dialogs, not page elements: `wait.until(alertIsPresent())`, then `getText()`, `accept()`, `dismiss()`, or `sendKeys()` for a prompt. | [`interview/alerts/AlertHandlingTest`](../src/test/java/com/automation/selenium/interview/alerts/AlertHandlingTest.java) | Q31 |
| 27 | How do you work with frames and iframes? | 🟢 ⭐ | Switch in by name/id, index or `WebElement` (`frameToBeAvailableAndSwitchToIt` waits too); `parentFrame()` goes one level up, `defaultContent()` back to the page. | [`interview/frames/IframeHandlingTest`](../src/test/java/com/automation/selenium/interview/frames/IframeHandlingTest.java) | Q32 |
| 28 | How do you handle a link that opens a new window? | 🟢 ⭐ | Selenium doesn't follow it: remember the original handle, wait for the window count to grow, take the new handle by set difference (or by title), switch, work, `close()`, switch back. | [`interview/windows/MultipleWindowTest`](../src/test/java/com/automation/selenium/interview/windows/MultipleWindowTest.java) | Q33 |
| 29 | How do you open a new tab yourself? | 🟢 | Selenium 4: `driver.switchTo().newWindow(WindowType.TAB)` opens it and switches to it; tabs share the session cookie. | `SeleniumScenariosTest.dashboardOpensInASecondTab()` | Q33, Q95 |

## 6. Drop-downs, checkboxes and web tables

| # | Question | Level | Interview answer | Implementation | PDF |
|---|---|---|---|---|---|
| 30 | How do you handle a `<select>` drop-down? | 🟢 ⭐ | `new Select(element)`: `selectByVisibleText`, `selectByValue`, `selectByIndex`, `getOptions`, `getFirstSelectedOption`, `isMultiple`. A disabled option is refused. | [`interview/dropdowns/SelectDropdownTest`](../src/test/java/com/automation/selenium/interview/dropdowns/SelectDropdownTest.java) | Q29 |
| 31 | How do you handle a drop-down that is not a `<select>`? | 🟡 ⭐ | `Select` only works on `<select>`. Click the box to open the list, then click the option by its text. | `BasePage.choose()`, `options()` · `SeleniumScenariosTest.userRoleDropdownOffersAdminAndEss()` | Q30, Q93 |
| 32 | How do you handle checkboxes and radio buttons? | 🟢 | Read `isSelected()` first and click only if the state must change; a click toggles a checkbox. A selected radio button is never unselected by clicking it. | [`interview/elements/CheckboxTest`](../src/test/java/com/automation/selenium/interview/elements/CheckboxTest.java) | Q39 |
| 33 | How do you read a web table? | 🟡 ⭐ | Read the headers, then each row's cells, and map them by position into *column → value*; then any question is plain Java. Prefer stable class names over positions when the cells have them. | [`interview/elements/WebTableTest`](../src/test/java/com/automation/selenium/interview/elements/WebTableTest.java) · `BasePage.tableRows()` | Q19 |
| 34 | How do you find a table row by its content and click something in it? | 🟡 ⭐ | Find the row by a cell's text, then search *inside the row*: `row.findElement(By.xpath(".//a"))`. Without the dot, `//` searches the whole page. | `WebTableTest.xpathInsideARowMustStartWithADot` · `EmployeeListPage.delete()` | Q19 |

## 7. Actions, JavaScript, screenshots, cookies and files

| # | Question | Level | Interview answer | Implementation | PDF |
|---|---|---|---|---|---|
| 35 | When do you use the `Actions` class? | 🟡 ⭐ | For low-level input: hover (`moveToElement`), right-click (`contextClick`), key combinations (`keyDown`/`keyUp`), drag and drop. Nothing happens until `perform()`. | [`interview/actions/ActionsClassTest`](../src/test/java/com/automation/selenium/interview/actions/ActionsClassTest.java) · `LoginPage.loginWithKeyboard()` | Q34, Q94 |
| 36 | When do you use `JavascriptExecutor`? What does it return? | 🟡 ⭐ | For what WebDriver can't do: page state, scrolling. `arguments[0…]` go in; `return` values come back as `Long`, `Double`, `String`, `Boolean`, `WebElement`, `List` or `null`. | [`interview/javascript/JavaScriptExecutorTest`](../src/test/java/com/automation/selenium/interview/javascript/JavaScriptExecutorTest.java) · `BasePage.runScript()` | Q35, Q96 |
| 37 | What is the risk of a JavaScript click? | 🟡 ⭐ | It skips what a user goes through (visibility, overlays), so a test can pass on a page a user cannot use. | `JavaScriptExecutorTest.javascriptClickHidesAnOverlayProblem` | Q35 |
| 38 | `clear()` does not empty a field. Why, and what do you do? | 🟡 | Frameworks such as Vue or React keep their own model and may not see `clear()`. Send real keys instead: Ctrl+A, then Delete. | [`BasePage.type()`](../src/main/java/com/automation/selenium/pages/BasePage.java) | Q36 |
| 39 | How do you take a screenshot of the page and of one element? | 🟢 ⭐ | Page: cast the driver to `TakesScreenshot`. Element (Selenium 4): `element.getScreenshotAs(…)`. On failure, take it *before* cleanup. | `BaseTest.attachScreenshot()` · `LoginPage.formScreenshot()` · `SeleniumScenariosTest.loginFormScreenshotIsAPng()` | Q37, Q97 |
| 40 | What is the difference between text, attribute and property? | 🟡 | `getText()` = visible text; `getDomAttribute()` = as written in the HTML; `getDomProperty()` = the live value (typed text, absolute `href`). | `BasePage.valueOf()` · `LoginPage.placeholder()` · `AppPage.menuLinks()` | Q39 |
| 41 | How do you find broken links? | 🟡 ⭐ | Collect every `href` with Selenium, then request each URL with an HTTP client; Selenium cannot read status codes. 400 or more is broken. | [`Links`](../src/main/java/com/automation/selenium/utils/Links.java) · `SeleniumScenariosTest.sideMenuHasNoBrokenLinks()` | Q92 |
| 42 | How do you work with cookies? Can you skip the login? | 🟡 | `driver.manage()`: `getCookies`, `getCookieNamed`, `addCookie`, `deleteCookieNamed`, `deleteAllCookies` (open a page of the site first). Saving the session cookie and adding it to a fresh session skips the login form. | [`interview/cookies/CookieTest`](../src/test/java/com/automation/selenium/interview/cookies/CookieTest.java) | — |
| 43 | How do you upload a file? | 🟢 ⭐ | `sendKeys(absolutePath)` on the `<input type="file">`; never click it (OS dialog). On Grid add a `LocalFileDetector`. No AutoIt or Robot. | [`interview/upload/FileUploadTest`](../src/test/java/com/automation/selenium/interview/upload/FileUploadTest.java) | Q38 |

## 8. Page Object Model and framework design

| # | Question | Level | Interview answer | Implementation | PDF |
|---|---|---|---|---|---|
| 44 | What is the Page Object Model, and why use it? | 🟢 ⭐ | One class per page holds its locators and actions; tests only call methods and assert. When the UI changes, you fix one page class instead of many tests. | [`pages/`](../src/main/java/com/automation/selenium/pages): the tests in `tests/` contain no locators | Q40 |
| 45 | Should page objects contain assertions? | 🟡 | Generally no: pages return values and states, tests decide what is correct. Pages may fail fast when they cannot do their job (e.g. login rejected). | `LoginPage.isDisplayed()` returns a boolean · `LoginPage.loginAs()` throws on a rejected login | Q41 |
| 46 | What is PageFactory? Is it the same as POM? | 🟡 ⭐ | POM is a design pattern; PageFactory is one way to create the elements (`@FindBy` + `initElements`). Each field is a lazy proxy that finds the element on every use. `@CacheLookup` keeps the first element and goes stale after a re-render. | `BasePage` constructor · `LoginPage.cachedUsernameField` · `SeleniumScenariosTest.findByFieldSurvivesAReloadButCacheLookupGoesStale()` | Q42 |
| 47 | What is method chaining (a fluent page API)? | 🟢 | Methods return a page object (`this` or the next page), so steps read like a sentence: `openAdmin().filterByRole("Admin").search()`. | [`SystemUsersPage`](../src/main/java/com/automation/selenium/pages/SystemUsersPage.java) · `SystemUsersTest.filterByRoleAndStatus()` | Q43 |
| 48 | What goes into a `BasePage`? | 🟡 | What every page needs: the driver, the wait, PageFactory set-up, safe actions (click, type, read) and the app's shared widgets (spinner, toast, tables). | [`BasePage`](../src/main/java/com/automation/selenium/pages/BasePage.java), [`AppPage`](../src/main/java/com/automation/selenium/pages/AppPage.java) | Q44 |
| 49 | How do you run Selenium tests in parallel safely? Is WebDriver "thread-safe"? | 🔴 ⭐ | Give every thread its own browser: keep the driver in a `ThreadLocal<WebDriver>`, never in a static field. WebDriver itself is still not thread-safe; ThreadLocal makes sure no two threads share one. | [`DriverManager`](../src/main/java/com/automation/selenium/driver/DriverManager.java) · [`interview/framework/ThreadLocalDriverTest`](../src/test/java/com/automation/selenium/interview/framework/ThreadLocalDriverTest.java) (two threads, two sessions) | Q45, Q87 |
| 50 | How do you manage the driver's life cycle? | 🔴 | Start a fresh browser before each test, quit it after each test (in `finally`), `remove()` the ThreadLocal slot, and quit any leftover before starting a new one. | `BaseTest.startBrowser()` / `stopBrowser()` · `DriverManager.start()`, `quit()` · `ThreadLocalDriverTest.quitEmptiesTheThreadsSlot` | Q45 |
| 51 | How do you manage configuration (URL, browser, timeouts)? | 🟡 | One properties file on the classpath, overridable by `-Dkey=value`, read through one class; a missing key fails at once with its name. | [`Config`](../src/main/java/com/automation/selenium/config/Config.java) · [`interview/framework/ConfigOverrideTest`](../src/test/java/com/automation/selenium/interview/framework/ConfigOverrideTest.java) | Q46, Q77 |
| 52 | Where do you keep test data? | 🟡 | Out of the test logic: data providers for tables of cases, generators for unique data, config for credentials. | [`TestData`](../src/test/java/com/automation/selenium/data/TestData.java) (`invalidCredentials`, `newEmployee()`) | Q47 |
| 53 | How do you keep tests independent on a shared environment? | 🔴 ⭐ | Each test creates the data it needs with unique values, never relies on existing records or counts, and deletes what it made, even when it fails. | `EmployeeTest.employeeCanBeAddedFoundAndDeleted()` + `cleanUp()` · `BaseTest.runCleanUp()` | Q48, Q88 |
| 54 | How do you log test steps? | 🟡 | Page methods log each user action; the steps are attached to the running test and shown in the report. Mask secrets. | [`Steps.log()`](../src/main/java/com/automation/selenium/utils/Steps.java) · `LoginPage.mask()` | Q50 |

## 9. TestNG core

| # | Question | Level | Interview answer | Implementation | PDF |
|---|---|---|---|---|---|
| 55 | In what order do TestNG annotations run? `@BeforeTest` or `@BeforeMethod`? | 🟢 ⭐ | Suite → Test → Class → Method → @Test → and back out. `@BeforeTest` runs once per `<test>` tag in testng.xml; `@BeforeMethod` before every test method. | [`interview/testng/AnnotationOrderTest`](../src/test/java/com/automation/selenium/interview/testng/AnnotationOrderTest.java) (records and checks the order) · `BaseTest` | Q52, Q53 |
| 56 | What does `alwaysRun = true` do? | 🟡 ⭐ | Configuration methods don't inherit class groups; under `-Dgroups=…` a method without `alwaysRun` is left out (no browser!). `alwaysRun` also runs teardown after failures. | `BaseTest.startBrowser()`, `stopBrowser()` · `AnnotationOrderTest` | Q54 |
| 57 | What are groups, and how do you run them? | 🟢 ⭐ | Labels on tests (`groups = "smoke"`), selected with `-Dgroups=…` or `<include>`/`<exclude>` in testng.xml. A class-level group applies to every method. | Class-level groups on every test class (e.g. `{"interview", "exceptions"}`) · `excludedGroups` in [`pom.xml`](../pom.xml) | Q55, Q75 |
| 58 | What do `priority` and `dependsOnMethods` do? What if the first test fails? | 🟢 ⭐ | `priority` orders tests in a class (lower first). `dependsOnMethods` runs a test only if the other passed; otherwise it is **skipped**, not failed. | `TestNgFeaturesTest.loginPageIsShown()` → `adminLogsInAfterThePageCheck()` · `ReportDemoTest.skippedBecauseDependencyFailed()` | Q56–Q58, Q100 |
| 59 | What are `dependsOnGroups` and `enabled = false`? | 🟡 | `dependsOnGroups` waits for a whole group to pass; `alwaysRun = true` makes a dependency "soft". `enabled = false` switches a test off: not run, not reported. | [`interview/testng/DependencyTest`](../src/test/java/com/automation/selenium/interview/testng/DependencyTest.java) | Q58 |
| 60 | What does `invocationCount` do? | 🟢 | Runs the same test N times and reports each run, e.g. to catch a check that only sometimes passes. | `TestNgFeaturesTest.wrongPasswordIsRejectedEveryTime()` · `ParallelExecutionTest` | Q56, Q101 |
| 61 | What does `timeOut` do? | 🟡 | Fails the test if it runs longer than the limit: a safety net against hangs, not a replacement for waits. | `TestNgFeaturesTest.loginFinishesWithinOneMinute()` | Q56, Q104 |
| 62 | Hard vs soft assertions? | 🟢 ⭐ | A hard assert stops at the first failure; `SoftAssert` collects every failure and reports them together at `assertAll()`. Forget `assertAll()` and the failures are lost. | `TestNgFeaturesTest.softAssertChecksTheLoginPage()` | Q59, Q98 |
| 63 | How do you skip a test at run time? | 🟢 | Throw `SkipException` with the reason; the test is reported as skipped, with that message. | `ReportDemoTest.skippedWhenPreconditionIsMissing()` | Q60, Q73 |
| 64 | How do you test that an exception is thrown? | 🟡 | `@Test(expectedExceptions = X.class, expectedExceptionsMessageRegExp = "…")`, or inside a test `assertThrows` / `expectThrows` (which returns the exception to inspect). | `TestNgFeaturesTest.wrongPasswordThrowsTheLoginError()` · `SeleniumExceptionsTest` (`assertThrows`, `expectThrows`) | Q61, Q99 |

## 10. TestNG data, parallel runs, listeners and retry

| # | Question | Level | Interview answer | Implementation | PDF |
|---|---|---|---|---|---|
| 65 | How does a `@DataProvider` work? How do you share one? | 🟡 ⭐ | It returns `Object[][]`; TestNG runs the test once per row with the row as parameters. Share it by putting it in another class and naming it with `dataProviderClass`. | [`TestData.invalidCredentials()`](../src/test/java/com/automation/selenium/data/TestData.java) · `LoginTest.loginIsRejectedForInvalidCredentials()` | Q62, Q63 |
| 66 | What other forms can a data provider take? | 🔴 | A lazy `Iterator<Object[]>`; a provider with a `Method` parameter that serves several tests; `parallel = true` to run rows at the same time (the test must then be thread-safe). | [`interview/testng/DataProviderVariantsTest`](../src/test/java/com/automation/selenium/interview/testng/DataProviderVariantsTest.java) | Q62 |
| 67 | `@DataProvider` or `@Parameters`? | 🟡 | `@Parameters` passes single values from testng.xml (environment-like settings); `@DataProvider` feeds many rows of test data from code. `@Optional` gives a default when the XML has no value. | `TestNgFeaturesTest.titleMatchesTheSuiteParameter()` · [`testng.xml`](../testng.xml) | Q64, Q103 |
| 68 | What goes into testng.xml? | 🟢 | Suites, tests, classes or packages, group include/exclude, parameters, listeners and parallel settings. | [`testng.xml`](../testng.xml) | Q65 |
| 69 | How do you run tests in parallel? Why `parallel=classes`? | 🔴 ⭐ | Set `parallel` (methods, classes, tests, instances) and `thread-count`; on one method, `invocationCount` + `threadPoolSize`. `classes` keeps each class's methods on one thread, so its fields and `dependsOnMethods` stay safe. | Surefire `parallel`/`threadCount` in [`pom.xml`](../pom.xml) · [`interview/testng/ParallelExecutionTest`](../src/test/java/com/automation/selenium/interview/testng/ParallelExecutionTest.java) (threads forced to overlap; ThreadLocal keeps them apart) | Q66, Q67 |
| 70 | Which listeners do you know? How do you register one? | 🔴 | `ITestListener`, `ISuiteListener`, `IReporter`, `IRetryAnalyzer`, `IAnnotationTransformer`. Register with `@Listeners` (applies to the whole suite!), in testng.xml, or through ServiceLoader. | [`interview/testng/ListenerTest`](../src/test/java/com/automation/selenium/interview/testng/ListenerTest.java) + `TestEventsListener` · `HtmlReportListener` via [`META-INF/services`](../src/main/resources/META-INF/services/org.testng.ITestNGListener) | Q68, Q69 |
| 71 | How do you retry a failed test? Should you retry every failure? | 🔴 ⭐ | Implement `IRetryAnalyzer` and attach it with `retryAnalyzer = …`. Retry only known environment hiccups; retrying everything hides real defects. | [`RetryOnce`](../src/test/java/com/automation/selenium/base/RetryOnce.java) · `TestNgFeaturesTest.adminLoginIsRetriedOnceOnFailure()` | Q71, Q102 |
| 72 | Why does a test show as skipped? | 🟡 | A failed dependency, a `SkipException`, a failed `@Before…` configuration method, or a retried attempt (the earlier try is reported as skipped). | `ReportDemoTest` (both skip types) | Q73 |

## 11. Reporting, build and run

| # | Question | Level | Interview answer | Implementation | PDF |
|---|---|---|---|---|---|
| 73 | How do you capture a screenshot on failure and show it in the report? | 🟡 ⭐ | In `@AfterMethod`, check that the result failed, take the screenshot, store it on the `ITestResult` as an attribute, and let the report read it from there. | `BaseTest.stopBrowser()` / `attachScreenshot()` · `HtmlReportListener.renderTest()` | Q37, Q82 |
| 74 | How do you build a custom HTML report? Why would you? | 🔴 | Implement `IReporter`; after the run, TestNG hands you every result. Write one self-contained file with what *your* team needs: steps, errors, screenshots, filters. | [`HtmlReportListener`](../src/main/java/com/automation/selenium/report/HtmlReportListener.java) | Q70, Q81 |
| 75 | How does Maven run TestNG? How do you run one class, method or group? | 🟢 | Surefire finds and runs the tests: `-Dtest=Class#method`, `-Dgroups=…`. With `suiteXmlFiles` set, these switches are ignored, which is why this project runs without a suite file. | [`pom.xml`](../pom.xml) (Surefire) | Q74–Q76 |
| 76 | What is a Maven profile, and when do you use one? | 🟢 | A named set of settings switched on with `-P<id>`, e.g. a different group filter or environment. | `demo` profile in [`pom.xml`](../pom.xml): `mvn clean test -Pdemo` | Q78 |

## 12. Not covered by code

Topics this lab explains but does not run, and why:

| Topic | Why not | Where it is explained |
|---|---|---|
| Radio buttons | The practice site has none; they behave like checkboxes except that clicking a selected one does not unselect it | `CheckboxTest` Javadoc |
| HTML5 drag and drop | `Actions.dragAndDrop` has long been unreliable with HTML5 (`draggable`) drag events in Chrome, so it does not make a dependable example; it works for mouse-based drag (sliders, jQuery UI) | PDF Q34 |
| Table sorting by header click | The practice site's sorting plugin does not load in the browser | `WebTableTest` |
| Selenium Grid, Docker, CI | Out of scope for this interview lab; they belong in a production-style project | PDF Q79, Q80 |
