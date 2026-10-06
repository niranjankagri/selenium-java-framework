// Interview examples: synchronization (waits)
package com.automation.selenium.interview.synchronization;

// TestNG assertions
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;

// Time spans for the wait
import java.time.Duration;

// A locator (how to find an element)
import org.openqa.selenium.By;
// Thrown by findElement when nothing matches (after the implicit wait, if one is set)
import org.openqa.selenium.NoSuchElementException;
// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

// Base class for the practice-site examples
import com.automation.selenium.interview.PracticeSiteTest;
// Writes steps into the HTML report
import com.automation.selenium.utils.Steps;

/**
 * Interview question: What is an implicit wait, and why does this framework
 * not use one?
 * <p>
 * Concept: {@code driver.manage().timeouts().implicitlyWait(t)} makes every
 * {@code findElement} keep retrying for up to {@code t} before it throws
 * {@link NoSuchElementException}. It is set once and applies to the whole
 * driver session.
 * <p>
 * Why the framework avoids it: it only waits for an element to <i>exist</i>
 * (not to be visible or clickable); it slows down every check that expects
 * an element to be absent; and mixed with explicit waits the timeouts add up
 * unpredictably. Explicit waits wait for exactly the condition you need.
 * <p>
 * Implementation: {@code /dynamic_loading/2}: after Start, the text "Hello
 * World!" is <b>added</b> to the page about 5 seconds later.
 */
@Test(groups = { "interview", "synchronization" })
public class ImplicitWaitTest extends PracticeSiteTest {

	// Page that adds an element to the DOM after a delay
	private static final String RENDERED_LATER = "/dynamic_loading/2";
	// The Start button
	private static final By START = By.cssSelector("#start button");
	// The text that is added after the delay
	private static final By FINISH_TEXT = By.cssSelector("#finish h4");

	/**
	 * Without any wait, {@code findElement} looks once and fails at once,
	 * because the element does not exist yet. This is the problem waits solve.
	 */
	@Test(description = "Waits: without a wait, findElement fails at once for an element added later")
	public void withoutAWaitTheLookupFailsImmediately() {
		// Open the page and start the loading
		open(RENDERED_LATER);
		driver().findElement(START).click();

		// No implicit wait is set (the default is 0), so the lookup does not retry
		assertThrows(NoSuchElementException.class, () -> driver().findElement(FINISH_TEXT));
		// Record what happened in the report
		Steps.log("findElement right after Start threw NoSuchElementException: the text is not in the page yet");
	}

	/**
	 * With an implicit wait, the same {@code findElement} retries until the
	 * element exists, so the test passes without any extra code at the lookup.
	 * The wait is reset to 0 afterwards; in a real suite it would stay on for
	 * every lookup of the session, which is exactly its drawback.
	 */
	@Test(description = "Waits: an implicit wait makes findElement retry until the element exists")
	public void implicitWaitRetriesTheLookup() {
		// Open the page
		open(RENDERED_LATER);
		// Turn the implicit wait on for this browser: every findElement now retries for up to 10 s
		driver().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
		try {
			// Start the loading
			driver().findElement(START).click();
			// Same lookup as above, but now it keeps retrying until the element is added (about 5 s)
			String text = driver().findElement(FINISH_TEXT).getText();
			// The added text
			assertEquals(text, "Hello World!", "Text added after loading");
			// Record what happened in the report
			Steps.log("With implicitlyWait(10 s), findElement waited until the text was added");
		} finally {
			// Back to 0 (the default), so no other lookup in this session is slowed down
			driver().manage().timeouts().implicitlyWait(Duration.ZERO);
		}
	}
}
