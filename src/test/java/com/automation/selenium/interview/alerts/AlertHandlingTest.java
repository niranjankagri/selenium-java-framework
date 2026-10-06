// Interview examples: JavaScript alerts
package com.automation.selenium.interview.alerts;

// TestNG assertions
import static org.testng.Assert.assertEquals;

// The open alert, confirm or prompt
import org.openqa.selenium.Alert;
// A locator (how to find an element)
import org.openqa.selenium.By;
// Ready-made wait conditions (alertIsPresent)
import org.openqa.selenium.support.ui.ExpectedConditions;
// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

// Base class for the practice-site examples
import com.automation.selenium.interview.PracticeSiteTest;
// Writes steps into the HTML report
import com.automation.selenium.utils.Steps;

/**
 * Interview question: How do you handle JavaScript alerts, confirms and
 * prompts?
 * <p>
 * Concept: a JavaScript {@code alert()}, {@code confirm()} or
 * {@code prompt()} is a browser dialog, not part of the page, so it cannot
 * be found with {@code findElement}. {@code driver.switchTo().alert()} gives
 * an {@link Alert} with {@code getText()}, {@code accept()} (OK),
 * {@code dismiss()} (Cancel) and {@code sendKeys()} (type into a prompt).
 * <p>
 * Recommended approach: wait for the dialog with
 * {@code ExpectedConditions.alertIsPresent()} (it also switches to it), then
 * read, answer and check the page's reaction. Always close every dialog you
 * open, or the next command fails with {@code UnhandledAlertException}.
 * <p>
 * Not covered here: browser-native dialogs such as the file chooser or
 * basic-auth login are not JavaScript alerts; Selenium cannot drive them
 * this way (use {@code sendKeys} on the file input, or credentials in the URL).
 * <p>
 * Implementation: {@code /javascript_alerts} has one button per dialog and
 * shows the result below them.
 */
@Test(groups = { "interview", "alerts" })
public class AlertHandlingTest extends PracticeSiteTest {

	// Page with the three dialog buttons
	private static final String ALERTS_PAGE = "/javascript_alerts";
	// Where the page writes what happened
	private static final By RESULT = By.id("result");

	/**
	 * alert(): read the message and press OK.
	 */
	@Test(description = "Alerts: read a JS alert's text and accept it")
	public void alertIsReadAndAccepted() {
		// Open the page and open the alert
		open(ALERTS_PAGE);
		driver().findElement(button("Click for JS Alert")).click();

		// Wait for the alert and switch to it in one step
		Alert alert = explicitWait().until(ExpectedConditions.alertIsPresent());
		// Read the message before answering (after accept() the alert is gone)
		assertEquals(alert.getText(), "I am a JS Alert", "Alert text");
		// Press OK
		alert.accept();
		// Record it in the report
		Steps.log("Accepted the alert \"I am a JS Alert\"");

		// The page continues only after the alert is closed
		assertEquals(driver().findElement(RESULT).getText(), "You successfully clicked an alert", "Result");
	}

	/**
	 * confirm(): OK and Cancel lead to different results, so test both.
	 */
	@Test(description = "Alerts: accept and dismiss a JS confirm")
	public void confirmIsAcceptedAndDismissed() {
		// Open the page
		open(ALERTS_PAGE);
		// The confirm button
		By confirmButton = button("Click for JS Confirm");

		// First: Cancel
		driver().findElement(confirmButton).click();
		explicitWait().until(ExpectedConditions.alertIsPresent()).dismiss();
		assertEquals(driver().findElement(RESULT).getText(), "You clicked: Cancel", "Result after Cancel");

		// Then: OK
		driver().findElement(confirmButton).click();
		explicitWait().until(ExpectedConditions.alertIsPresent()).accept();
		assertEquals(driver().findElement(RESULT).getText(), "You clicked: Ok", "Result after OK");
		// Record it in the report
		Steps.log("Dismissed the confirm (Cancel), then accepted it (OK)");
	}

	/**
	 * prompt(): type an answer, then press OK.
	 */
	@Test(description = "Alerts: type into a JS prompt and accept it")
	public void promptReceivesText() {
		// Open the page and open the prompt
		open(ALERTS_PAGE);
		driver().findElement(button("Click for JS Prompt")).click();

		// Wait for the prompt
		Alert prompt = explicitWait().until(ExpectedConditions.alertIsPresent());
		// Type the answer into the prompt's input
		prompt.sendKeys("Selenium");
		// Press OK
		prompt.accept();
		// Record it in the report
		Steps.log("Typed \"Selenium\" into the prompt and accepted it");

		// The page shows what was typed
		assertEquals(driver().findElement(RESULT).getText(), "You entered: Selenium", "Result after the prompt");
	}

	/**
	 * Locates a button by its text.
	 *
	 * @param text the button's visible text.
	 * @return By  locator of that button.
	 */
	private static By button(String text) {
		// normalize-space() ignores spaces around the text
		return By.xpath("//button[normalize-space()='" + text + "']");
	}
}
