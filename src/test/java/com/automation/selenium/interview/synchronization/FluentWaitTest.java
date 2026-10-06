// Interview examples: synchronization (waits)
package com.automation.selenium.interview.synchronization;

// TestNG assertions
import static org.testng.Assert.assertEquals;

// Time spans for the wait
import java.time.Duration;

// A locator (how to find an element)
import org.openqa.selenium.By;
// The exception the fluent wait ignores while polling
import org.openqa.selenium.NoSuchElementException;
// The browser
import org.openqa.selenium.WebDriver;
// One element on the page
import org.openqa.selenium.WebElement;
// The general wait class; WebDriverWait is a FluentWait<WebDriver> with defaults
import org.openqa.selenium.support.ui.FluentWait;
// Common interface of all waits
import org.openqa.selenium.support.ui.Wait;
// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

// Base class for the practice-site examples
import com.automation.selenium.interview.PracticeSiteTest;
// Writes steps into the HTML report
import com.automation.selenium.utils.Steps;

/**
 * Interview question: What is a FluentWait, and how is it different from
 * WebDriverWait? How do you write a custom wait condition?
 * <p>
 * Interview answer: A FluentWait lets me set the timeout, the polling
 * interval, the exceptions to ignore and the timeout message; WebDriverWait
 * is a FluentWait with sensible defaults. For anything ExpectedConditions
 * doesn't cover, I write a lambda that returns null to keep waiting or a
 * value to stop.
 * <p>
 * Concept: {@link FluentWait} lets you set the timeout, the polling interval,
 * the exceptions to ignore while polling, and the message for the timeout.
 * {@code WebDriverWait} <i>is</i> a {@code FluentWait<WebDriver>} with ready
 * defaults (500 ms polling, ignores {@code NotFoundException}). A wait
 * condition is any function: returning {@code null} or {@code false} means
 * "keep waiting", any other value ends the wait and is returned.
 * <p>
 * Implementation: {@code /dynamic_loading/2}: the text is added to the
 * page about 5 seconds after Start.
 */
@Test(groups = { "interview", "synchronization" })
public class FluentWaitTest extends PracticeSiteTest {

	// Page that adds an element to the DOM after a delay
	private static final String RENDERED_LATER = "/dynamic_loading/2";
	// The Start button
	private static final By START = By.cssSelector("#start button");
	// The text that is added after the delay
	private static final By FINISH_TEXT = By.cssSelector("#finish h4");

	/**
	 * A FluentWait set up by hand: 15 s timeout, a check every 250 ms, and
	 * {@code NoSuchElementException} ignored, so {@code findElement} can be
	 * called inside the condition until the element exists.
	 */
	@Test(description = "Waits: a FluentWait with its own polling and ignored exceptions")
	public void fluentWaitPollsUntilTheElementExists() {
		// Open the page and start the loading
		open(RENDERED_LATER);
		driver().findElement(START).click();

		// Build the wait around the driver
		Wait<WebDriver> wait = new FluentWait<>(driver())
				// Give up after 15 s...
				.withTimeout(Duration.ofSeconds(15))
				// ...checking every 250 ms...
				.pollingEvery(Duration.ofMillis(250))
				// ...treating "not found yet" as "keep waiting", not as an error...
				.ignoring(NoSuchElementException.class)
				// ...and saying what we waited for if it times out
				.withMessage("the loaded text to be added to the page");

		// The condition: find the element (throws until it exists, which is ignored)
		WebElement text = wait.until(d -> d.findElement(FINISH_TEXT));
		// Found after about 5 s
		assertEquals(text.getText(), "Hello World!", "Text found by the fluent wait");
		// Record it in the report
		Steps.log("FluentWait (15 s, every 250 ms, ignoring NoSuchElementException) found the text");
	}

	/**
	 * A custom condition that returns a value: wait until the element exists
	 * <i>and</i> has non-empty text, and hand that text back. No ready-made
	 * {@code ExpectedConditions} does exactly this.
	 */
	@Test(description = "Waits: a custom wait condition returns the value it waited for")
	public void customConditionReturnsTheValueItWaitedFor() {
		// Open the page and start the loading
		open(RENDERED_LATER);
		driver().findElement(START).click();

		// explicitWait() is a WebDriverWait: already ignores NotFoundException, polls every 500 ms.
		// withMessage: a lambda has no readable name, so without it a timeout says "waiting for ...$$Lambda@1a2b"
		String text = explicitWait().withMessage("the loaded text to have content").until(d -> {
			// findElements never throws: an empty list means "not there yet"
			var found = d.findElements(FINISH_TEXT);
			// Not there yet, or there but still empty → null → keep waiting
			if (found.isEmpty() || found.get(0).getText().isBlank()) {
				return null;
			}
			// Done: return the text itself, so the caller gets it from until(...)
			return found.get(0).getText();
		});
		// The value the condition returned
		assertEquals(text, "Hello World!", "Text returned by the custom condition");
	}
}
