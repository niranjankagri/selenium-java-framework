# Avoid vs Prefer

Common Selenium and TestNG mistakes next to the better version. Each pair says *why*, and links to where this lab does it the right way. In an interview, explaining the "why" counts as much as writing the code.

**Levels:** 🟢 Beginner · 🟡 Intermediate · 🔴 Advanced · ⭐ Frequently asked

## Contents

1. [Waiting](#1-waiting)
2. [Finding elements](#2-finding-elements)
3. [Interacting](#3-interacting)
4. [Driver and parallel runs](#4-driver-and-parallel-runs)
5. [Test design](#5-test-design)
6. [TestNG](#6-testng)

---

## 1. Waiting

### Fixed sleeps 🟢 ⭐

**Avoid**
```java
driver.findElement(By.id("start")).click();
Thread.sleep(5000);                                   // hope 5 s is enough
driver.findElement(By.id("finish")).getText();
```

**Prefer**
```java
driver.findElement(By.id("start")).click();
String text = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("finish"))).getText();
```

**Why:** the sleep always costs 5 s, and still fails when the page needs 6. The wait continues the moment the element is visible.
**In the lab:** [`ExplicitWaitTest`](../src/test/java/com/automation/selenium/interview/synchronization/ExplicitWaitTest.java), every action in [`BasePage`](../src/main/java/com/automation/selenium/pages/BasePage.java).

### Implicit and explicit waits together 🟢 ⭐

**Avoid**
```java
driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
wait.until(ExpectedConditions.invisibilityOfElementLocated(spinner));   // each poll may wait 10 s
```

**Prefer**
```java
// No implicit wait (the default is 0); explicit waits only
wait.until(ExpectedConditions.invisibilityOfElementLocated(spinner));
```

**Why:** the implicit wait applies to every lookup, also those inside explicit conditions, so timeouts add up and "is it gone?" checks become slow.
**In the lab:** [`ImplicitWaitTest`](../src/test/java/com/automation/selenium/interview/synchronization/ImplicitWaitTest.java).

### Waiting for presence when you need visibility 🟡

**Avoid**
```java
wait.until(ExpectedConditions.presenceOfElementLocated(result)).click();   // present, but maybe hidden
```

**Prefer**
```java
wait.until(ExpectedConditions.elementToBeClickable(result)).click();       // visible and enabled
```

**Why:** an element can be in the DOM long before it is shown; clicking it then throws `ElementNotInteractableException`.
**In the lab:** `ExplicitWaitTest.presenceIsNotVisibility`.

### A custom wait without a message 🟡

**Avoid**
```java
wait.until(d -> d.findElements(rows).size() > 3 ? true : null);
// timeout: "waiting for MyTest$$Lambda/0x1a2b@3c4d"
```

**Prefer**
```java
wait.withMessage("more than 3 table rows").until(d -> d.findElements(rows).size() > 3 ? true : null);
```

**Why:** a lambda has no readable name, so the timeout message says nothing; `withMessage` names what never happened.
**In the lab:** [`FluentWaitTest`](../src/test/java/com/automation/selenium/interview/synchronization/FluentWaitTest.java).

## 2. Finding elements

### Absolute XPath 🟢 ⭐

**Avoid**
```java
By.xpath("/html/body/div[2]/div/form/div[1]/input")
```

**Prefer**
```java
By.id("username")                                     // or name, or a data-test attribute
By.cssSelector("#login button[type='submit']")
By.xpath("//label[normalize-space()='Password']/following-sibling::input")   // when text matters
```

**Why:** an absolute path breaks with any layout change; a stable attribute or a short relative path survives it.
**In the lab:** [`LocatorStrategiesTest`](../src/test/java/com/automation/selenium/interview/locators/LocatorStrategiesTest.java).

### Catching the exception to check absence 🟢 ⭐

**Avoid**
```java
try {
    driver.findElement(errorBanner);
    return true;
} catch (NoSuchElementException e) {
    return false;
}
```

**Prefer**
```java
return !driver.findElements(errorBanner).isEmpty();
```

**Why:** `findElements` never throws; the list form is shorter and doesn't hide a broken locator inside a catch block.
**In the lab:** `SeleniumExceptionsTest.noSuchElementException`, `LoginPage.errorAlerts`.

### `//` inside an element 🟡 ⭐

**Avoid**
```java
WebElement row = rows.get(2);
row.findElement(By.xpath("//td"));                    // first cell of the WHOLE PAGE
```

**Prefer**
```java
row.findElement(By.xpath(".//td"));                   // first cell of this row
row.findElement(By.cssSelector("td"));                // CSS always searches inside the element
```

**Why:** an XPath starting with `//` starts at the document root, whatever element you call it on.
**In the lab:** `WebTableTest.xpathInsideARowMustStartWithADot`.

### Keeping elements across a page change 🟡 ⭐

**Avoid**
```java
WebElement checkbox = driver.findElement(checkboxLocator);
driver.findElement(removeButton).click();             // the page re-renders
checkbox.isSelected();                                // StaleElementReferenceException
```

**Prefer**
```java
driver.findElement(removeButton).click();
wait.until(ExpectedConditions.stalenessOf(checkbox)); // wait for the change itself
checkbox = driver.findElement(checkboxLocator);       // then find it again
```

**Why:** a `WebElement` points to one DOM node; when the page replaces the node, the reference is dead. Keep locators (or plain PageFactory proxies), not elements.
**In the lab:** `SeleniumExceptionsTest.staleElementReferenceException`; `@CacheLookup` going stale in `SeleniumScenariosTest`.

### Building many locators by hand 🟢

**Avoid**
```java
By firstUser  = By.xpath("//tr[td='Admin']/td[3]");
By secondUser = By.xpath("//tr[td='tomsmith']/td[3]");
```

**Prefer**
```java
private static final String ROLE_OF_USER = "//tr[td='%s']/td[3]";
By.xpath(String.format(ROLE_OF_USER, username));
```

**Why:** one template for any value; the test data stays out of the locator.
**In the lab:** `LocatorStrategiesTest.dynamicLocatorIsBuiltFromAValue`, `BasePage.button(text)`.

## 3. Interacting

### A JavaScript click to "fix" an intercepted click 🟡 ⭐

**Avoid**
```java
// ElementClickInterceptedException? Click it with JavaScript instead!
((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
```

**Prefer**
```java
wait.until(ExpectedConditions.invisibilityOfElementLocated(overlay));
button.click();
```

**Why:** something covers the button and a user couldn't click it either; the JavaScript click passes a test on a broken page.
**In the lab:** `SeleniumExceptionsTest.elementClickInterceptedException` (the fix), `JavaScriptExecutorTest.javascriptClickHidesAnOverlayProblem` (the trap).

### Clicking a checkbox blindly 🟢

**Avoid**
```java
checkbox.click();                                     // unchecks it if it was already checked
```

**Prefer**
```java
if (checkbox.isSelected() != wanted) {
    checkbox.click();
}
```

**Why:** a click toggles; reading the state first makes the step repeatable.
**In the lab:** [`CheckboxTest`](../src/test/java/com/automation/selenium/interview/elements/CheckboxTest.java).

### `Select` on a custom drop-down 🟢

**Avoid**
```java
new Select(driver.findElement(By.cssSelector(".oxd-select-text")));   // UnexpectedTagNameException
```

**Prefer**
```java
click(dropdownBox);                                                    // open the div-based list
click(By.xpath("//div[@role='option'][normalize-space()='Admin']"));   // then the option
```

**Why:** `Select` only wraps a real `<select>`; most modern drop-downs are `div`s.
**In the lab:** [`SelectDropdownTest`](../src/test/java/com/automation/selenium/interview/dropdowns/SelectDropdownTest.java) (native), `BasePage.choose` (custom).

### "The last handle is the new window" 🟢

**Avoid**
```java
List<String> handles = new ArrayList<>(driver.getWindowHandles());
driver.switchTo().window(handles.get(handles.size() - 1));
```

**Prefer**
```java
Set<String> before = driver.getWindowHandles();
link.click();
wait.until(ExpectedConditions.numberOfWindowsToBe(before.size() + 1));
Set<String> added = new HashSet<>(driver.getWindowHandles());
added.removeAll(before);
driver.switchTo().window(added.iterator().next());
```

**Why:** `getWindowHandles()` is a set with no guaranteed order; the set difference is always the new window.
**In the lab:** [`MultipleWindowTest`](../src/test/java/com/automation/selenium/interview/windows/MultipleWindowTest.java).

### `clear()` on a framework field 🟡

**Avoid**
```java
field.clear();
field.sendKeys("new value");                          // Vue/React may keep the old value
```

**Prefer**
```java
field.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
field.sendKeys("new value");
```

**Why:** `clear()` changes the DOM value without the key events the page's own model listens to.
**In the lab:** `BasePage.type`.

### Uploading through the OS dialog 🟢 ⭐

**Avoid**
```java
fileInput.click();                                    // opens the OS dialog: the test hangs
new Robot().keyPress(...);                            // types into the dialog; breaks headless and Grid
```

**Prefer**
```java
fileInput.sendKeys(file.toAbsolutePath().toString());
submitButton.click();
```

**Why:** Selenium can fill a file input directly; the OS dialog is never needed.
**In the lab:** [`FileUploadTest`](../src/test/java/com/automation/selenium/interview/upload/FileUploadTest.java).

## 4. Driver and parallel runs

### A static driver 🔴 ⭐

**Avoid**
```java
public static WebDriver driver;                       // one variable for all threads
```

**Prefer**
```java
private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();
public static WebDriver driver() { return DRIVER.get(); }
```

**Why:** with parallel tests, each new browser overwrites the static field and tests drive each other's windows; a `ThreadLocal` gives every thread its own.
**In the lab:** [`DriverManager`](../src/main/java/com/automation/selenium/driver/DriverManager.java), [`ThreadLocalDriverTest`](../src/test/java/com/automation/selenium/interview/framework/ThreadLocalDriverTest.java), [`ParallelExecutionTest`](../src/test/java/com/automation/selenium/interview/testng/ParallelExecutionTest.java).

### `close()` in teardown, and a forgotten ThreadLocal 🟡

**Avoid**
```java
@AfterMethod
public void tearDown() {
    driver.close();                                   // other windows and the driver process stay
}
```

**Prefer**
```java
@AfterMethod(alwaysRun = true)
public void tearDown() {
    try {
        DRIVER.get().quit();                          // every window, and the session
    } finally {
        DRIVER.remove();                              // thread pools reuse threads
    }
}
```

**Why:** `close()` closes one window; `quit()` ends the session. `alwaysRun` makes teardown run after failures and under group filters; `remove()` keeps a dead driver from reaching the next test on that thread.
**In the lab:** `DriverManager.quit`, `BaseTest.stopBrowser`.

## 5. Test design

### Locators and raw driver calls in tests 🟢 ⭐

**Avoid**
```java
@Test
public void filterByUsername() {
    driver.findElement(By.xpath("//label[.='Username']/../..//input")).sendKeys("Admin");
    driver.findElement(By.xpath("//button[.=' Search ']")).click();
    // ...
}
```

**Prefer**
```java
@Test
public void filterByUsername() {
    SystemUsersPage users = loginAsAdmin().openAdmin().filterByUsername("Admin").search();
    assertTrue(users.usernames().stream().allMatch("Admin"::equals));
}
```

**Why:** with the Page Object Model a UI change is fixed in one page class, and the test reads like the business flow.
**In the lab:** [`pages/`](../src/main/java/com/automation/selenium/pages), [`SystemUsersTest`](../src/test/java/com/automation/selenium/tests/SystemUsersTest.java). (The `interview/` examples call Selenium directly on purpose: there the API call is the topic.)

### Hard-coded test data 🟢

**Avoid**
```java
loginPage.loginAs("admin", "admin123");
addEmployee("John", "Smith", "0001");                 // collides with other runs
```

**Prefer**
```java
loginPage.loginAs(Config.get().username(), Config.get().password());   // overridable with -D
Employee employee = TestData.newEmployee();                            // unique random ID
```

**Why:** settings change per environment; fixed records collide when tests run in parallel or on a shared site.
**In the lab:** [`Config`](../src/main/java/com/automation/selenium/config/Config.java), [`TestData`](../src/test/java/com/automation/selenium/data/TestData.java).

### Depending on what is already on a shared site 🔴

**Avoid**
```java
assertEquals(users.recordCount(), "(12) Records Found");   // someone else adds a user tomorrow
```

**Prefer**
```java
assertTrue(users.usernames().stream().allMatch("Admin"::equals));   // every listed row matches the filter
// and tests that need data create it, then delete it in cleanUp(), even when they fail
```

**Why:** shared environments change under you; check the rule, not the count, and own your data.
**In the lab:** [`SystemUsersTest`](../src/test/java/com/automation/selenium/tests/SystemUsersTest.java), `EmployeeTest.cleanUp`.

### Swallowing exceptions 🟢

**Avoid**
```java
try {
    loginPage.loginAs(user, password);
} catch (Exception e) {
    e.printStackTrace();                              // the test goes on, and fails somewhere else
}
```

**Prefer**
```java
loginPage.loginAs(user, password);                    // throws "Login as "Admin" failed: Invalid credentials"
```

**Why:** a failure should stop the test where it happened, with a message that says what went wrong.
**In the lab:** `LoginPage.loginAs`.

## 6. TestNG

### `SoftAssert` without `assertAll()` 🟢 ⭐

**Avoid**
```java
SoftAssert soft = new SoftAssert();
soft.assertEquals(title, "OrangeHRM");
soft.assertEquals(buttonText, "Login");               // failures are collected... and forgotten
```

**Prefer**
```java
SoftAssert soft = new SoftAssert();
soft.assertEquals(title, "OrangeHRM");
soft.assertEquals(buttonText, "Login");
soft.assertAll();                                     // reports every failure, fails the test
```

**Why:** soft assertions only collect; `assertAll()` is what fails the test.
**In the lab:** `TestNgFeaturesTest.softAssertChecksTheLoginPage`.

### Configuration methods under a group filter 🟡 ⭐

**Avoid**
```java
@BeforeMethod
public void startBrowser() { ... }                    // skipped under -Dgroups=smoke: no browser
```

**Prefer**
```java
@BeforeMethod(alwaysRun = true)
public void startBrowser() { ... }
```

**Why:** configuration methods don't inherit class groups; without `alwaysRun` they are left out of a group run.
**In the lab:** [`BaseTest`](../src/test/java/com/automation/selenium/base/BaseTest.java), [`AnnotationOrderTest`](../src/test/java/com/automation/selenium/interview/testng/AnnotationOrderTest.java).

### Ordering tests with priority across classes 🟡

**Avoid**
```java
@Test(priority = 1) public void createUser() { ... }  // in UserTest
@Test(priority = 2) public void loginAsUser() { ... } // in LoginTest: no guaranteed order
```

**Prefer**
```java
@Test public void createdUserCanLogIn() {             // one independent test that makes its own data
    User user = createUser();
    loginAs(user);
}
```

**Why:** `priority` only sorts the methods TestNG schedules together; with `parallel=classes` each class runs on its own thread, so there is no dependable order between classes. Independent tests can run anywhere, in any order.
**In the lab:** `EmployeeTest.employeeCanBeAddedFoundAndDeleted` (add, find and delete in one test).

### Retrying every failure 🔴 ⭐

**Avoid**
```java
// An IAnnotationTransformer that sets a retry analyzer on every test in the suite
annotation.setRetryAnalyzer(RetryThreeTimes.class);
```

**Prefer**
```java
@Test(retryAnalyzer = RetryOnce.class)                // only where an environment hiccup is known
public void adminLoginIsRetriedOnceOnFailure() { ... }
```

**Why:** blanket retries hide real defects and flaky tests; a targeted retry, visible in the report, does not.
**In the lab:** [`RetryOnce`](../src/test/java/com/automation/selenium/base/RetryOnce.java), `TestNgFeaturesTest.adminLoginIsRetriedOnceOnFailure`; the practice site's known hiccups are handled (and logged) in `PracticeSiteTest.open`.

### Shared state in a parallel data provider 🔴

**Avoid**
```java
private int counter;                                  // shared by all rows

@DataProvider(parallel = true) ...
@Test(dataProvider = "rows")
public void check(int a, int b) { counter++; ... }    // rows race on the field
```

**Prefer**
```java
@Test(dataProvider = "rows")
public void check(int a, int b, int sum) {
    assertEquals(a + b, sum);                         // uses only its own parameters
}
```

**Why:** with `parallel = true`, rows run at the same time; anything shared must be thread-safe or avoided.
**In the lab:** [`DataProviderVariantsTest`](../src/test/java/com/automation/selenium/interview/testng/DataProviderVariantsTest.java).
