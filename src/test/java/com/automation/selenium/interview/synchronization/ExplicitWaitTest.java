// Interview examples: synchronization (waits)
package com.automation.selenium.interview.synchronization;

// TestNG assertions
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

// A locator (how to find an element)
import org.openqa.selenium.By;
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
 * Interview question: What is an explicit wait? What is the difference
 * between presence and visibility?
 * <p>
 * Concept: a {@code WebDriverWait} polls one condition (every 500 ms by
 * default) until it is true or the timeout passes, then returns the result
 * or throws {@code TimeoutException}. {@code ExpectedConditions} has the
 * common conditions ready-made.
 * <p>
 * <b>Presence</b> = the element is in the DOM. <b>Visibility</b> = it is in
 * the DOM <i>and</i> displayed (not hidden, has a size). A hidden element is
 * present but not visible, and clicking or reading it fails.
 * <p>
 * Recommended approach: wait for the state you are about to use: visible
 * before reading text, clickable before clicking, invisible after a spinner,
 * and never {@code Thread.sleep}.
 * <p>
 * Implementation: {@code /dynamic_loading/1} (the text is in the page but
 * hidden until loading ends) and {@code /dynamic_controls} (an input is
 * enabled a few seconds after a click).
 */
@Test(groups = { "interview", "synchronization" })
public class ExplicitWaitTest extends PracticeSiteTest {

	// Page whose result text is present but hidden until loading ends
	private static final String HIDDEN_UNTIL_LOADED = "/dynamic_loading/1";
	// Start button and loading indicator of that page
	private static final By START = By.cssSelector("#start button");
	private static final By LOADING = By.id("loading");
	// The result block and its text
	private static final By FINISH = By.id("finish");
	private static final By FINISH_TEXT = By.cssSelector("#finish h4");

	/**
	 * Presence is reached at once (the element is in the HTML from the
	 * start), but the element is still hidden; only the visibility wait waits
	 * until a user can actually see it.
	 */
	@Test(description = "Waits: presence is not visibility, a hidden element is present at once")
	public void presenceIsNotVisibility() {
		// Open the page and start the loading
		open(HIDDEN_UNTIL_LOADED);
		driver().findElement(START).click();

		// presenceOfElementLocated returns immediately: the block is in the DOM, only hidden
		WebElement finish = explicitWait().until(ExpectedConditions.presenceOfElementLocated(FINISH));
		// Present, but not displayed yet
		assertFalse(finish.isDisplayed(), "Right after Start the result is present but hidden");
		// Record it in the report
		Steps.log("presenceOfElementLocated(#finish) returned at once; isDisplayed() = false");

		// visibilityOfElementLocated waits until it is shown (about 5 s)
		WebElement text = explicitWait().until(ExpectedConditions.visibilityOfElementLocated(FINISH_TEXT));
		// Now it can be read
		assertEquals(text.getText(), "Hello World!", "Text after loading");
	}

	/**
	 * Waiting for a loading indicator to disappear, then for the result:
	 * the standard pattern for AJAX pages and spinners.
	 */
	@Test(description = "Waits: wait for the loading indicator to disappear, then read the result")
	public void waitUntilTheLoadingIndicatorDisappears() {
		// Open the page and start the loading
		open(HIDDEN_UNTIL_LOADED);
		driver().findElement(START).click();

		// The indicator is shown first...
		explicitWait().until(ExpectedConditions.visibilityOfElementLocated(LOADING));
		// ...then waited away; invisibilityOfElementLocated returns true when it is hidden or gone
		assertTrue(explicitWait().until(ExpectedConditions.invisibilityOfElementLocated(LOADING)), "Loading indicator gone");
		// Record it in the report
		Steps.log("Loading indicator appeared and disappeared");
		// The result is shown now
		assertTrue(driver().findElement(FINISH_TEXT).isDisplayed(), "Result shown after loading");
	}

	/**
	 * A disabled input cannot be typed into. After "Enable", the page enables
	 * it a few seconds later; {@code elementToBeClickable} (visible and
	 * enabled) waits for exactly that, and {@code textToBe} waits for the
	 * confirmation message.
	 */
	@Test(description = "Waits: elementToBeClickable waits until a disabled input is enabled")
	public void waitUntilAnInputIsEnabled() {
		// Open the page with the enable/disable example
		open("/dynamic_controls");
		// The input starts disabled
		WebElement input = driver().findElement(By.cssSelector("#input-example input"));
		assertFalse(input.isEnabled(), "The input starts disabled");

		// Press Enable
		driver().findElement(By.cssSelector("#input-example button")).click();
		// Wait until the input is visible and enabled, then type into it
		explicitWait().until(ExpectedConditions.elementToBeClickable(input)).sendKeys("typed after the wait");
		// The page confirms it (textToBe waits until the element has exactly this text)
		explicitWait().until(ExpectedConditions.textToBe(By.id("message"), "It's enabled!"));
		// What was typed is in the field
		assertEquals(input.getDomProperty("value"), "typed after the wait", "Value typed into the enabled input");
	}
}
