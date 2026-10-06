// Interview examples: Selenium exceptions
package com.automation.selenium.interview.exceptions;

// TestNG assertions; assertThrows/expectThrows pass only if the code throws that exception
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.expectThrows;

// Time spans for the short wait
import java.time.Duration;

// A locator (how to find an element)
import org.openqa.selenium.By;
// The Selenium exceptions reproduced below
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.InvalidSelectorException;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.UnhandledAlertException;
// Runs JavaScript in the page (used to put an overlay on the page)
import org.openqa.selenium.JavascriptExecutor;
// One element on the page
import org.openqa.selenium.WebElement;
// Ready-made wait conditions
import org.openqa.selenium.support.ui.ExpectedConditions;
// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

// Base class for the practice-site examples
import com.automation.selenium.interview.PracticeSiteTest;
// Writes steps into the HTML report
import com.automation.selenium.utils.Steps;

/**
 * Interview question: Which Selenium exceptions do you know, what causes
 * them, and how do you handle them?
 * <p>
 * Interview answer: The common ones are NoSuchElement (wrong locator, not
 * there yet, or in a frame), StaleElementReference (the element was re-
 * rendered: find it again), ElementNotInteractable (hidden),
 * ElementClickIntercepted (something covers it: wait for it to go), Timeout
 * (read which condition failed), NoSuchWindow and NoSuchFrame (switch
 * correctly). I fix the cause; I don't hide it with try/catch, retries or
 * JavaScript clicks.
 * <p>
 * Each test reproduces one exception <b>on purpose</b> (checked with
 * {@code assertThrows}, so the test passes when the exception happens), then
 * shows the right way to avoid or handle it. The Javadoc of each test gives
 * the cause, the fix, and the workaround <b>not</b> to use.
 * <p>
 * General rule: an exception is information about the page or the test.
 * Fix the cause (wrong locator, missing wait, wrong window); don't wrap
 * everything in try/catch or retry until it passes.
 */
@Test(groups = { "interview", "exceptions" })
public class SeleniumExceptionsTest extends PracticeSiteTest {

	// Login page of the practice site
	private static final String LOGIN_PAGE = "/login";
	// Its Login button
	private static final By LOGIN_BUTTON = By.cssSelector("#login button[type='submit']");

	/**
	 * <b>NoSuchElementException</b>: {@code findElement} found nothing.
	 * <ul>
	 * <li>Cause: wrong locator, the element is not there yet, or it is in
	 * another frame or window.</li>
	 * <li>Handle: fix the locator; wait for the element; switch to its frame.
	 * To check that something is <i>absent</i>, use {@code findElements} and
	 * test for an empty list: it never throws.</li>
	 * <li>Don't: catch the exception to mean "not there"; it costs the full
	 * implicit wait every time, and hides a broken locator.</li>
	 * </ul>
	 */
	@Test(description = "Exceptions: NoSuchElementException, and findElements for absence checks")
	public void noSuchElementException() {
		// Open the login page
		open(LOGIN_PAGE);
		// A locator that matches nothing
		By missing = By.id("no-such-element");

		// Reproduce: findElement throws when nothing matches
		assertThrows(NoSuchElementException.class, () -> driver().findElement(missing));
		// Handle an absence check: findElements returns an empty list instead
		assertTrue(driver().findElements(missing).isEmpty(), "findElements returns an empty list");
		// Record it in the report
		Steps.log("findElement threw NoSuchElementException; findElements returned []");
	}

	/**
	 * <b>StaleElementReferenceException</b>: the element you hold was removed
	 * from the page (reload, re-render, AJAX update).
	 * <ul>
	 * <li>Cause: a {@code WebElement} is a reference to one DOM node; when the
	 * page replaces the node, the reference is dead, even if an identical
	 * element is shown again.</li>
	 * <li>Handle: find the element again after the change; wait for the change
	 * with {@code stalenessOf(old)}; keep locators ({@code By}) rather than
	 * elements in fields.</li>
	 * <li>Don't: catch it and retry blindly in a loop.</li>
	 * </ul>
	 * Page {@code /dynamic_controls}: "Remove" deletes the checkbox, "Add"
	 * creates a new one.
	 */
	@Test(description = "Exceptions: StaleElementReferenceException after the element is replaced")
	public void staleElementReferenceException() {
		// Open the page with the remove/add checkbox
		open("/dynamic_controls");
		// One locator that matches the checkbox before and after it is re-created
		By checkbox = By.cssSelector("#checkbox-example input[type='checkbox']");
		// The Remove/Add button and the message below it
		By button = By.cssSelector("#checkbox-example button");
		By message = By.id("message");
		// Find the checkbox and keep the reference
		WebElement old = driver().findElement(checkbox);

		// Remove it, and wait until our reference is stale (the node left the DOM)
		driver().findElement(button).click();
		explicitWait().until(ExpectedConditions.stalenessOf(old));
		// Reproduce: any call on the old reference throws
		assertThrows(StaleElementReferenceException.class, old::isSelected);
		Steps.log("The removed checkbox's old reference threw StaleElementReferenceException");

		// Bring a new checkbox back: the same button now says "Add"
		explicitWait().until(ExpectedConditions.textToBe(message, "It's gone!"));
		driver().findElement(button).click();
		explicitWait().until(ExpectedConditions.textToBe(message, "It's back!"));
		// Handle: find it again; the new element works
		WebElement fresh = driver().findElement(checkbox);
		assertTrue(fresh.isDisplayed(), "The re-found checkbox is usable");
	}

	/**
	 * <b>ElementNotInteractableException</b>: the element exists but cannot be
	 * used, typically because it is hidden or has no size.
	 * <ul>
	 * <li>Cause: acting before the element is shown, or on a hidden duplicate
	 * (e.g. the mobile menu's copy of a link).</li>
	 * <li>Handle: wait for visibility or clickability; make the locator point
	 * at the visible element.</li>
	 * <li>Don't: click it with JavaScript; a user can't click a hidden
	 * element either, so the test would pass while the page is broken.</li>
	 * </ul>
	 * Page {@code /dynamic_loading/1}: the result text is in the page but
	 * hidden until loading ends.
	 */
	@Test(description = "Exceptions: ElementNotInteractableException on a hidden element")
	public void elementNotInteractableException() {
		// Open the page with the hidden result
		open("/dynamic_loading/1");
		// The hidden text
		By result = By.cssSelector("#finish h4");

		// Reproduce: it exists (findElement works) but is hidden, so a click is refused
		assertThrows(ElementNotInteractableException.class, () -> driver().findElement(result).click());
		Steps.log("Clicking the hidden result threw ElementNotInteractableException");

		// Handle: do what reveals it, then wait until it is visible
		driver().findElement(By.cssSelector("#start button")).click();
		WebElement shown = explicitWait().until(ExpectedConditions.visibilityOfElementLocated(result));
		// Now it is usable
		assertEquals(shown.getText(), "Hello World!", "Text once visible");
	}

	/**
	 * <b>ElementClickInterceptedException</b>: another element is on top of
	 * the one you click (overlay, spinner, cookie banner, sticky header).
	 * <ul>
	 * <li>Cause: clicking while a loader or dialog covers the page.</li>
	 * <li>Handle: wait for the covering element to disappear
	 * ({@code invisibilityOfElementLocated}), close the banner, or scroll the
	 * target out from under a sticky header.</li>
	 * <li>Don't: switch to a JavaScript click; it goes "through" the overlay,
	 * which a real user cannot do.</li>
	 * </ul>
	 * The overlay is added by the test (a loading cover that removes itself
	 * after 2 seconds), so the example does not depend on timing on the site.
	 */
	@Test(description = "Exceptions: ElementClickInterceptedException while an overlay covers the page")
	public void elementClickInterceptedException() {
		// Open the login page
		open(LOGIN_PAGE);
		// Put a full-page cover over everything, like a loading overlay; it removes itself after 2 s
		((JavascriptExecutor) driver()).executeScript(
				"var d=document.createElement('div');d.id='lab-overlay';"
						+ "d.style.cssText='position:fixed;inset:0;background:rgba(0,0,0,.3);z-index:9999';"
						+ "document.body.appendChild(d);setTimeout(function(){d.remove();},2000);");

		// Reproduce: the browser would hit the overlay, not the button, so Selenium refuses the click
		assertThrows(ElementClickInterceptedException.class, () -> driver().findElement(LOGIN_BUTTON).click());
		Steps.log("Clicking Login under the overlay threw ElementClickInterceptedException");

		// Handle: wait until the overlay is gone, then click normally
		explicitWait().until(ExpectedConditions.invisibilityOfElementLocated(By.id("lab-overlay")));
		driver().findElement(LOGIN_BUTTON).click();
		// The click reached the button: the empty form is rejected with a message
		assertTrue(explicitWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("flash"))).getText()
				.contains("Your username is invalid!"), "Login was submitted after the overlay went away");
	}

	/**
	 * <b>TimeoutException</b>: an explicit wait's condition was not met in time.
	 * <ul>
	 * <li>Cause: wrong locator, a page that never reached the state, or a
	 * timeout too short for the environment.</li>
	 * <li>Handle: read the message; it names the condition and the time
	 * tried. Fix the locator or the expectation; raise the timeout only if
	 * the app really is that slow.</li>
	 * <li>Don't: catch it and continue as if the element were there.</li>
	 * </ul>
	 */
	@Test(description = "Exceptions: TimeoutException names the condition that was not met")
	public void timeoutException() {
		// Open the login page
		open(LOGIN_PAGE);

		// Reproduce: wait 2 s for an element that never appears; expectThrows returns the exception
		TimeoutException e = expectThrows(TimeoutException.class,
				() -> explicitWait(Duration.ofSeconds(2)).until(ExpectedConditions.visibilityOfElementLocated(By.id("never-shown"))));
		// Handle: the message says what was waited for, which is where debugging starts, e.g.
		// "Expected condition failed: waiting for visibility of element found by By.id: never-shown (tried for 2 second(s) ...)"
		// (only the stable parts are checked; the exact wording differs between Selenium versions)
		assertTrue(e.getMessage().contains("waiting for visibility of element") && e.getMessage().contains("never-shown"),
				"The message names the condition: " + e.getMessage());
		// Show the first line of the message in the report
		Steps.log("TimeoutException: " + e.getMessage().lines().findFirst().orElse(""));
	}

	/**
	 * <b>NoSuchWindowException</b>: the driver points at a window that no
	 * longer exists.
	 * <ul>
	 * <li>Cause: calling the driver after {@code close()} of the current
	 * window, or switching to a handle that was closed.</li>
	 * <li>Handle: after {@code close()}, always {@code switchTo().window(...)}
	 * a window that is still open; keep the original handle in a variable.</li>
	 * </ul>
	 */
	@Test(description = "Exceptions: NoSuchWindowException after closing the current window")
	public void noSuchWindowException() {
		// Open the page with a link that opens a new window
		open("/windows");
		// Keep the handle of this window to come back to
		String original = driver().getWindowHandle();
		// Open the new window and wait until there are two
		driver().findElement(By.linkText("Click Here")).click();
		explicitWait().until(ExpectedConditions.numberOfWindowsToBe(2));
		// Switch to the window that is not the original one
		for (String handle : driver().getWindowHandles()) {
			if (!handle.equals(original)) {
				driver().switchTo().window(handle);
			}
		}
		// Close it: the driver still points at it
		driver().close();

		// Reproduce: any call now targets a closed window
		assertThrows(NoSuchWindowException.class, () -> driver().getTitle());
		Steps.log("getTitle() after close() threw NoSuchWindowException");
		// Handle: switch back to a window that is still open
		driver().switchTo().window(original);
		assertEquals(driver().getTitle(), "The Internet", "Title of the original window");
	}

	/**
	 * <b>NoSuchFrameException</b>: the frame to switch to does not exist.
	 * <ul>
	 * <li>Cause: wrong name, id or index; the frame is nested inside another
	 * one (switch to the parent first); or it has not loaded yet.</li>
	 * <li>Handle: switch level by level; wait with
	 * {@code frameToBeAvailableAndSwitchToIt}.</li>
	 * </ul>
	 */
	@Test(description = "Exceptions: NoSuchFrameException for a frame that is not there")
	public void noSuchFrameException() {
		// Open the page with nested frames: frame-top (left, middle, right) and frame-bottom
		open("/nested_frames");

		// Reproduce: no frame has this name
		assertThrows(NoSuchFrameException.class, () -> driver().switchTo().frame("no-such-frame"));
		// Also: "frame-left" exists, but inside frame-top, so it is not found from the top page
		assertThrows(NoSuchFrameException.class, () -> driver().switchTo().frame("frame-left"));
		Steps.log("switchTo().frame() threw NoSuchFrameException for a wrong and for a nested name");

		// Handle: switch level by level, waiting for each frame
		explicitWait().until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("frame-top"));
		explicitWait().until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("frame-left"));
		// Inside the left frame now
		assertEquals(driver().findElement(By.tagName("body")).getText(), "LEFT", "Text of the nested left frame");
		// Back to the top page for whatever comes next
		driver().switchTo().defaultContent();
	}

	/**
	 * <b>InvalidSelectorException</b>: the locator itself is not valid CSS or
	 * XPath, so the browser cannot even run the search.
	 * <ul>
	 * <li>Cause: a typo such as an unclosed bracket, or XPath syntax inside
	 * {@code By.cssSelector}.</li>
	 * <li>Handle: fix the expression; try it first in the browser's DevTools
	 * ({@code $x("…")} for XPath, {@code $$("…")} for CSS).</li>
	 * </ul>
	 */
	@Test(description = "Exceptions: InvalidSelectorException for a malformed XPath")
	public void invalidSelectorException() {
		// Open the login page
		open(LOGIN_PAGE);

		// Reproduce: the bracket is never closed
		assertThrows(InvalidSelectorException.class, () -> driver().findElement(By.xpath("//button[")));
		Steps.log("By.xpath(\"//button[\") threw InvalidSelectorException");
		// Handle: the corrected expression works
		assertTrue(driver().findElement(By.xpath("//button[@type='submit']")).isDisplayed(), "Fixed XPath finds the button");
	}

	/**
	 * <b>NoAlertPresentException</b> and <b>UnhandledAlertException</b>.
	 * <ul>
	 * <li>NoAlertPresent: {@code switchTo().alert()} when no alert is open,
	 * usually because the test did not wait for it. Handle: wait with
	 * {@code alertIsPresent()}.</li>
	 * <li>UnhandledAlert: the test talked to the page while an alert was
	 * open. Chrome's default is to dismiss the alert and report it with this
	 * exception. Handle: accept or dismiss every alert you open.</li>
	 * </ul>
	 */
	@Test(description = "Exceptions: NoAlertPresentException and UnhandledAlertException")
	public void alertExceptions() {
		// Open the page with the alert buttons
		open("/javascript_alerts");
		// The button that opens a simple alert
		By jsAlert = By.xpath("//button[normalize-space()='Click for JS Alert']");

		// Reproduce NoAlertPresent: there is no alert yet
		assertThrows(NoAlertPresentException.class, () -> driver().switchTo().alert());
		Steps.log("switchTo().alert() without an alert threw NoAlertPresentException");

		// Reproduce UnhandledAlert: open an alert, then ignore it and use the page
		driver().findElement(jsAlert).click();
		assertThrows(UnhandledAlertException.class, () -> driver().findElement(By.id("result")).getText());
		Steps.log("Using the page while an alert was open threw UnhandledAlertException");
		// Chrome dismissed that alert when it reported it, so no alert is open any more
		assertFalse(isAlertOpen(), "The unhandled alert was dismissed by the browser");

		// Handle: open it again, wait for it and accept it
		driver().findElement(jsAlert).click();
		explicitWait().until(ExpectedConditions.alertIsPresent()).accept();
		// The page confirms the click
		assertEquals(driver().findElement(By.id("result")).getText(), "You successfully clicked an alert", "Result after accepting");
	}

	/**
	 * Checks for an open alert without throwing.
	 *
	 * @return boolean true if an alert is open.
	 */
	private boolean isAlertOpen() {
		try {
			// Throws NoAlertPresentException when there is none
			driver().switchTo().alert();
			return true;
		} catch (NoAlertPresentException e) {
			return false;
		}
	}
}
