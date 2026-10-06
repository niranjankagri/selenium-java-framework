// Interview examples: windows and tabs
package com.automation.selenium.interview.windows;

// TestNG assertions
import static org.testng.Assert.assertEquals;

// Window handles come as a set
import java.util.HashSet;
import java.util.Set;

// A locator (how to find an element)
import org.openqa.selenium.By;
// Ready-made wait conditions (numberOfWindowsToBe)
import org.openqa.selenium.support.ui.ExpectedConditions;
// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

// Base class for the practice-site examples
import com.automation.selenium.interview.PracticeSiteTest;
// Writes steps into the HTML report
import com.automation.selenium.utils.Steps;

/**
 * Interview question: How do you handle a link that opens a new window?
 * <p>
 * Concept: every window or tab has a <b>handle</b> (a string ID).
 * {@code getWindowHandle()} is the current one, {@code getWindowHandles()}
 * all open ones. Selenium does <i>not</i> follow a new window by itself: the
 * driver stays on the old one until you {@code switchTo().window(handle)}.
 * <p>
 * Recommended approach: remember the original handle before the click; wait
 * until the number of windows grows; take the handle that was not there
 * before (a set difference, not "the last one": the set has no order);
 * switch, work, {@code close()}, and switch back.
 * <p>
 * Implementation: {@code /windows}, whose "Click Here" link opens
 * {@code /windows/new} in a new window. Opening a tab yourself with
 * {@code newWindow(WindowType.TAB)} is shown in
 * {@code SeleniumScenariosTest.dashboardOpensInASecondTab}.
 */
@Test(groups = { "interview", "windows" })
public class MultipleWindowTest extends PracticeSiteTest {

	// Page with the link that opens a new window
	private static final String WINDOWS_PAGE = "/windows";
	// The link
	private static final By OPEN_NEW_WINDOW = By.linkText("Click Here");

	/**
	 * Finds the new window by comparing the handles before and after the
	 * click, works in it, closes it and returns.
	 */
	@Test(description = "Windows: find the new window by its handle, switch, close and switch back")
	public void newWindowIsFoundBySetDifference() {
		// Open the page
		open(WINDOWS_PAGE);
		// Remember the window we are in and all windows before the click
		String original = driver().getWindowHandle();
		Set<String> before = driver().getWindowHandles();

		// Open the new window and wait until it exists
		driver().findElement(OPEN_NEW_WINDOW).click();
		explicitWait().until(ExpectedConditions.numberOfWindowsToBe(before.size() + 1));

		// All handles now, minus the ones from before = the new window
		Set<String> added = new HashSet<>(driver().getWindowHandles());
		added.removeAll(before);
		// Exactly one new window
		assertEquals(added.size(), 1, "New windows");
		// Switch to it: only now do driver calls go to the new window
		driver().switchTo().window(added.iterator().next());
		assertEquals(driver().getTitle(), "New Window", "Title of the new window");
		assertEquals(driver().findElement(By.tagName("h3")).getText(), "New Window", "Heading of the new window");
		// Record it in the report
		Steps.log("Switched to the new window by set difference of the handles");

		// Close the new window; the driver must then be switched back explicitly
		driver().close();
		driver().switchTo().window(original);
		assertEquals(driver().getTitle(), "The Internet", "Title of the original window");
	}

	/**
	 * Finds a window by its title: loop over the handles, switch to each and
	 * stop at the matching one. Useful when several windows are open.
	 */
	@Test(description = "Windows: switch to a window by its title")
	public void windowIsFoundByTitle() {
		// Open the page and open the new window
		open(WINDOWS_PAGE);
		String original = driver().getWindowHandle();
		driver().findElement(OPEN_NEW_WINDOW).click();
		explicitWait().until(ExpectedConditions.numberOfWindowsToBe(2));

		// Switch to the window titled "New Window"
		switchToWindowTitled("New Window");
		assertEquals(driver().getTitle(), "New Window", "Window found by title");
		// Back to the original one by its title too (the new window stays open; the browser quits after the test)
		switchToWindowTitled("The Internet");
		assertEquals(driver().getWindowHandle(), original, "Back in the original window");
	}

	/**
	 * Switches to the first window with the given title.
	 *
	 * @param title the window title to look for.
	 * @throws IllegalStateException if no open window has that title.
	 */
	private void switchToWindowTitled(String title) {
		// Try each open window
		for (String handle : driver().getWindowHandles()) {
			// Switching is needed to read a window's title
			driver().switchTo().window(handle);
			// Found it: stay in this window
			if (driver().getTitle().equals(title)) {
				Steps.log("Switched to the window titled \"" + title + "\"");
				return;
			}
		}
		// None matched: fail with a clear message
		throw new IllegalStateException("No window titled \"" + title + "\"");
	}
}
