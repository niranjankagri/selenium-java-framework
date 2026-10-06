# Common Interview Traps

Questions where the obvious answer is wrong or only half right. Each one has the short answer to give, why it matters, and where this lab shows it in working code. For the same mistakes as code, see [Avoid vs prefer](avoid-vs-prefer.md).

**Levels:** 🟢 Beginner · 🟡 Intermediate · 🔴 Advanced · ⭐ Frequently asked

Paths: `interview/` = [`src/test/java/com/automation/selenium/interview/`](../src/test/java/com/automation/selenium/interview), `pages/` = [`src/main/java/com/automation/selenium/pages/`](../src/main/java/com/automation/selenium/pages), `tests/` = [`src/test/java/com/automation/selenium/tests/`](../src/test/java/com/automation/selenium/tests).

## Contents

1. [Waits and synchronization](#1-waits-and-synchronization)
2. [Locators and elements](#2-locators-and-elements)
3. [Windows, frames, alerts and files](#3-windows-frames-alerts-and-files)
4. [Page Object Model and framework design](#4-page-object-model-and-framework-design)
5. [TestNG](#5-testng)
6. [Maven and running tests](#6-maven-and-running-tests)

---

## 1. Waits and synchronization

### Can we use `Thread.sleep()`? 🟢 ⭐

**Answer:** Technically yes, but not for synchronization. It always waits the full time: too long when the app is fast (slow suites), too short when it is slow (flaky tests).
**Instead:** an explicit wait for the exact condition you need, which continues as soon as it is true.
**In the lab:** no `Thread.sleep` anywhere; every action in [`BasePage`](../src/main/java/com/automation/selenium/pages/BasePage.java) waits explicitly. `interview/synchronization/ExplicitWaitTest`.

### Implicit wait and explicit wait together: what's the harm? 🟢 ⭐

**Answer:** The timeouts interact: an explicit wait that calls `findElement` inside it also triggers the implicit wait on every poll, so waits take far longer than either timeout, and "is it gone?" checks become slow. Pick explicit waits and leave the implicit wait at 0.
**In the lab:** `interview/synchronization/ImplicitWaitTest` turns it on, shows the effect, and resets it to `Duration.ZERO`.

### Presence or visibility? 🟡 ⭐

**Answer:** Presence = in the DOM; visibility = in the DOM *and* displayed. A hidden element is present at once, and clicking or reading it fails. Wait for visibility (or clickability) before you use an element.
**In the lab:** `ExplicitWaitTest.presenceIsNotVisibility`: `presenceOfElementLocated` returns immediately while `isDisplayed()` is still false.

### Is `WebDriverWait` different from `FluentWait`? 🟡

**Answer:** No, not really: `WebDriverWait` *is* a `FluentWait<WebDriver>` with defaults (500 ms polling, ignores `NotFoundException`). `FluentWait` lets you set polling, ignored exceptions and the timeout message yourself.
**In the lab:** `interview/synchronization/FluentWaitTest`.

### My custom wait timed out with "waiting for ...$$Lambda@1a2b". What's wrong? 🟡

**Answer:** Nothing is wrong with the wait; a lambda has no readable name. Add `.withMessage("what you waited for")`, so the failure tells the reader what never happened.
**In the lab:** `FluentWaitTest.customConditionReturnsTheValueItWaitedFor`, `WebTableTest`, `IframeHandlingTest`.

## 2. Locators and elements

### How do you check that an element is *not* there? 🟢 ⭐

**Answer:** With `findElements(...).isEmpty()`, not by catching `NoSuchElementException` from `findElement`. The list form never throws, and it doesn't hide a broken locator behind a catch block.
**In the lab:** `interview/exceptions/SeleniumExceptionsTest.noSuchElementException`; `LoginPage.errorAlerts` (an empty list means no banner).

### CSS can do everything XPath can, right? 🟢

**Answer:** No. CSS cannot match visible text and cannot go up to a parent or ancestor. XPath can (`normalize-space()`, `ancestor::`, `following-sibling::`). CSS is shorter and usually preferred when it is enough.
**In the lab:** `interview/locators/LocatorStrategiesTest.xpathAxesNavigateFromALabel`; `BasePage.inField`.

### Inside `row.findElement(By.xpath("//td"))`, which cell do you get? 🟡 ⭐

**Answer:** The first cell of the **whole page**, not of the row. An XPath starting with `//` always starts at the document root. Use `.//td` to search inside the element.
**In the lab:** `interview/elements/WebTableTest.xpathInsideARowMustStartWithADot`.

### Why not just click a checkbox to check it? 🟢

**Answer:** A click *toggles*: if the box was already checked, you just unchecked it. Read `isSelected()` first and click only when the state must change.
**In the lab:** `interview/elements/CheckboxTest`.

### Can `Select` handle every drop-down? 🟢 ⭐

**Answer:** Only real `<select>` elements. Most modern drop-downs are `div`s: click to open them, then click the option by its text. `Select` throws `UnexpectedTagNameException` on anything else, and refuses a disabled option with `UnsupportedOperationException`.
**In the lab:** `interview/dropdowns/SelectDropdownTest` (native); `BasePage.choose` and `SeleniumScenariosTest.userRoleDropdownOffersAdminAndEss` (custom).

### `clear()` didn't empty the field. Is Selenium broken? 🟡

**Answer:** No. Frameworks such as Vue or React keep the value in their own model and may not notice `clear()`. Send real key presses instead: Ctrl+A, then Delete.
**In the lab:** `BasePage.type`.

### The click is intercepted, so I'll use a JavaScript click. OK? 🟡 ⭐

**Answer:** No. Something covers the element (overlay, spinner, banner); a user couldn't click it either. A JavaScript click goes through the overlay and hides the real problem. Wait for the overlay to disappear instead.
**In the lab:** `SeleniumExceptionsTest.elementClickInterceptedException` (fix) and `interview/javascript/JavaScriptExecutorTest.javascriptClickHidesAnOverlayProblem` (the trap).

### Is `@CacheLookup` always bad? 🟡

**Answer:** No. It is fine for elements that are truly never re-created. On pages that re-render (reload, AJAX, single-page apps), the cached element goes stale and throws `StaleElementReferenceException`.
**In the lab:** `SeleniumScenariosTest.findByFieldSurvivesAReloadButCacheLookupGoesStale`.

### A stale element: just retry until it works? 🟡

**Answer:** No blind retry loops. Find the element again *after* the change; wait for the change with `stalenessOf(oldElement)`; keep `By` locators (or PageFactory proxies) instead of storing `WebElement`s.
**In the lab:** `SeleniumExceptionsTest.staleElementReferenceException`.

## 3. Windows, frames, alerts and files

### After a link opens a new window, does Selenium switch to it? 🟢 ⭐

**Answer:** No. The driver stays on the old window until you call `switchTo().window(handle)`. And `getWindowHandles()` is a set with no order, so "take the last handle" is not reliable: compare the handles before and after the click.
**In the lab:** `interview/windows/MultipleWindowTest`.

### After `driver.close()`, can I keep using the driver? 🟢

**Answer:** Not until you switch to a window that is still open. The driver still points at the closed one, and every call throws `NoSuchWindowException`.
**In the lab:** `SeleniumExceptionsTest.noSuchWindowException`.

### The element is on the page, but `findElement` can't find it. 🟢 ⭐

**Answer:** Check for a frame. The driver only searches the document it is switched to. Switch into the frame (and into each level of nested frames), then back with `defaultContent()`.
**In the lab:** `interview/frames/IframeHandlingTest`, `SeleniumExceptionsTest.noSuchFrameException`.

### Can you find an alert with `findElement`? 🟢

**Answer:** No. A JavaScript alert is a browser dialog, not part of the page. Use `wait.until(alertIsPresent())`, then `getText()`, `accept()`, `dismiss()` or `sendKeys()`. Using the page while an alert is open throws `UnhandledAlertException`.
**In the lab:** `interview/alerts/AlertHandlingTest`, `SeleniumExceptionsTest.alertExceptions`.

### How do you upload a file? Do you need AutoIt or Robot? 🟢 ⭐

**Answer:** No. Call `sendKeys(absolutePath)` on the `<input type="file">`; never click it (that opens the OS dialog). On Selenium Grid, add `setFileDetector(new LocalFileDetector())`.
**In the lab:** `interview/upload/FileUploadTest`.

### Can you skip the login screen in tests? 🔴

**Answer:** Yes, if the login itself isn't under test: log in once, save the session cookie, and add it to a fresh session. The cookie can only be added after a page of that site is open.
**In the lab:** `interview/cookies/CookieTest.savedSessionCookieSkipsTheLoginForm`.

## 4. Page Object Model and framework design

### Is PageFactory required for the Page Object Model? 🟢 ⭐

**Answer:** No. POM is a design pattern (one class per page with its locators and actions). PageFactory is one optional way to create a page's elements (`@FindBy` + `initElements`). POM works just as well with `By` locators.
**In the lab:** [`pages/`](../src/main/java/com/automation/selenium/pages) uses both: `@FindBy` fields for fixed elements, `By` methods for locators built from a value.

### Should page objects contain assertions? 🟡

**Answer:** Generally no: pages return values and states, and tests decide what is correct. A page may still fail fast when it cannot do its job (e.g. the login was rejected).
**In the lab:** `LoginPage.isDisplayed()` returns a boolean; `LoginPage.loginAs()` throws on a rejected login.

### Does `ThreadLocal` make WebDriver thread-safe? 🔴 ⭐

**Answer:** Not strictly. WebDriver itself is still not thread-safe; `ThreadLocal` gives each thread its *own* driver, so no two threads share one, which is what parallel tests need. Call `remove()` after `quit()`, because thread pools reuse threads.
**In the lab:** [`DriverManager`](../src/main/java/com/automation/selenium/driver/DriverManager.java); `interview/framework/ThreadLocalDriverTest`; `interview/testng/ParallelExecutionTest`.

### Can a `static WebDriver` work with parallel tests? 🔴

**Answer:** No. One static field is shared by all threads, so each new browser replaces the last one, and tests drive each other's windows.
**In the lab:** `ParallelExecutionTest` shows a `ThreadLocal` keeping values apart while three threads run at the same time.

## 5. TestNG

### If the test it depends on fails, does the dependent test fail? 🟢 ⭐

**Answer:** No, it is **skipped**. Add `alwaysRun = true` to the dependent test if it should run anyway (a "soft" dependency).
**In the lab:** `tests/ReportDemoTest.skippedBecauseDependencyFailed`; `interview/testng/DependencyTest`.

### `enabled = false` and a skipped test: same thing? 🟢

**Answer:** No. A disabled test is not run *and not reported*. A skipped test is reported as skipped, with a reason.
**In the lab:** `DependencyTest.disabledTestNeverRuns`.

### Can I rely on `priority` to run tests in order across the suite? 🟡

**Answer:** No. `priority` only sorts the methods TestNG schedules together (lower first); it is not a dependency. With parallel runs (here `parallel=classes`, each class on its own thread) there is no dependable order between classes. Tests that need an order should use dependencies, or better, not need one.
**In the lab:** `TestNgFeaturesTest.loginPageIsShown` → `adminLogsInAfterThePageCheck`.

### I used `SoftAssert` and the failures vanished. Why? 🟢 ⭐

**Answer:** `assertAll()` was not called. Soft assertions only *collect* failures; `assertAll()` at the end reports them and fails the test.
**In the lab:** `TestNgFeaturesTest.softAssertChecksTheLoginPage`.

### My `@BeforeMethod` didn't run when I filtered by group. Why? 🟡 ⭐

**Answer:** Configuration methods don't inherit the class's groups. Under `-Dgroups=...` a configuration method without `alwaysRun = true` (or its own groups) is left out, so the browser never starts.
**In the lab:** `BaseTest.startBrowser`; `interview/testng/AnnotationOrderTest`.

### Does `@BeforeTest` run before every test method? 🟢 ⭐

**Answer:** No. It runs once per `<test>` tag in testng.xml. Before every test method is `@BeforeMethod`.
**In the lab:** `AnnotationOrderTest` records the whole order: Suite → Test → Class → Method → @Test → back out.

### `@Listeners` on one class: does the listener only see that class? 🔴

**Answer:** No. TestNG registers it for the **whole suite**. And a listener registered in two places (annotation + testng.xml + ServiceLoader) runs twice.
**In the lab:** `interview/testng/ListenerTest` + `TestEventsListener`; `HtmlReportListener` is registered only through `META-INF/services`.

### Should a `RetryAnalyzer` retry every failure? 🔴 ⭐

**Answer:** No. Retry only failures you know come from the environment (a short outage). Retrying everything hides real defects and makes flaky tests look green.
**In the lab:** `base/RetryOnce` on one test only (`TestNgFeaturesTest.adminLoginIsRetriedOnceOnFailure`). The practice site's known hiccups are handled where they happen (`PracticeSiteTest.open`), and each reload is written to the report.

### A parallel data provider, and my test fails randomly. Why? 🔴

**Answer:** With `@DataProvider(parallel = true)` the rows run at the same time, so the test must not share mutable state (fields, a static driver). Use only the row's parameters, or thread-safe structures.
**In the lab:** `interview/testng/DataProviderVariantsTest.parallelProviderRowsAreIndependent`.

## 6. Maven and running tests

### Why does `-Dgroups=smoke` run everything (or nothing)? 🟡

**Answer:** When Surefire is given `suiteXmlFiles`, it ignores `-Dgroups` and `-Dtest`; the XML decides. This project runs without a suite file in Maven, so both switches work; `testng.xml` is for the IDE.
**In the lab:** [`pom.xml`](../pom.xml) (Surefire configuration).

### A test passes alone but fails in the full parallel run. First suspect? 🔴

**Answer:** Shared state: a static driver or field, shared test data (two tests editing the same record), or an order dependency. Give each thread its own driver and each test its own data.
**In the lab:** `DriverManager` (ThreadLocal); `EmployeeTest` creates its own employee with a random ID and cleans it up.
