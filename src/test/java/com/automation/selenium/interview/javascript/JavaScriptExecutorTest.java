// Interview examples: JavascriptExecutor
package com.automation.selenium.interview.javascript;

// TestNG assertions
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

// A locator (how to find an element)
import org.openqa.selenium.By;
// Thrown when another element covers the one being clicked
import org.openqa.selenium.ElementClickInterceptedException;
// Runs JavaScript in the page
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
 * Interview question: When do you use JavascriptExecutor, and what is the
 * risk?
 * <p>
 * Concept: {@code ((JavascriptExecutor) driver).executeScript(script, args)}
 * runs JavaScript in the page. Values passed in are {@code arguments[0]},
 * {@code arguments[1]}, …; a {@code return} hands a value back, converted to
 * Java: number → {@code Long} or {@code Double}, string → {@code String},
 * boolean → {@code Boolean}, DOM element → {@code WebElement}, array →
 * {@code List}, nothing → {@code null}.
 * <p>
 * Use it for what WebDriver cannot do: read page state
 * ({@code document.readyState}), scroll, read values hidden from the API.
 * Risk: a JavaScript click or value change skips what a real user goes
 * through (visibility, overlays, key events), so the test can pass on a
 * page a user cannot use.
 * <p>
 * Implementation: {@code /login} and {@code /infinite_scroll}. Page state
 * on OrangeHRM: {@code SeleniumScenariosTest.javascriptReadsThePageState}.
 */
@Test(groups = { "interview", "javascript" })
public class JavaScriptExecutorTest extends PracticeSiteTest {

	// Login page of the practice site
	private static final String LOGIN_PAGE = "/login";

	/**
	 * Arguments in, values out: what Java type each JavaScript result becomes.
	 */
	@Test(description = "JavaScript: pass arguments and read the returned Java types")
	public void argumentsAndReturnTypes() {
		// Open the login page
		open(LOGIN_PAGE);
		// The executor is the driver itself, seen through the JavascriptExecutor interface
		JavascriptExecutor js = (JavascriptExecutor) driver();
		// An element to pass in
		WebElement username = driver().findElement(By.id("username"));

		// A JavaScript number comes back as a Long
		assertEquals(js.executeScript("return document.querySelectorAll('input').length;"), 2L, "Number → Long");
		// An element passed in as arguments[0]; a string comes back as a String
		assertEquals(js.executeScript("return arguments[0].id;", username), "username", "String from an argument");
		// A boolean comes back as a Boolean
		assertEquals(js.executeScript("return document.readyState === 'complete';"), Boolean.TRUE, "Boolean");
		// A DOM element comes back as a WebElement: the same element findElement found
		assertEquals(js.executeScript("return document.getElementById('username');"), username, "Element → WebElement");
		// Two arguments: set a field's value directly (no key events: a framework like React would not notice)
		js.executeScript("arguments[0].value = arguments[1];", username, "set-by-script");
		assertEquals(username.getDomProperty("value"), "set-by-script", "Value set by JavaScript");
		// Record it in the report
		Steps.log("JavaScript returned Long, String, Boolean and WebElement; set a value with two arguments");
	}

	/**
	 * Scrolling: the page loads more paragraphs each time it is scrolled to
	 * the bottom. WebDriver has no scroll command for the window, so
	 * JavaScript does it.
	 */
	@Test(description = "JavaScript: scroll to the bottom to load more content")
	public void scrollingLoadsMoreContent() {
		// Open the infinite-scroll page
		open("/infinite_scroll");
		// The paragraphs the page adds while scrolling
		By added = By.cssSelector(".jscroll-added");
		// How many there are at first
		int before = driver().findElements(added).size();

		// Scroll to the bottom of the page
		((JavascriptExecutor) driver()).executeScript("window.scrollTo(0, document.body.scrollHeight);");
		// Wait until more paragraphs have been added
		int after = explicitWait().withMessage("more paragraphs after scrolling")
				.until(d -> d.findElements(added).size() > before ? d.findElements(added).size() : null);
		// Something was loaded
		assertTrue(after > before, "Paragraphs before " + before + ", after scrolling " + after);
		// Record it in the report
		Steps.log("Scrolled to the bottom: paragraphs " + before + " → " + after);
	}

	/**
	 * The risk: a page covered by an overlay. A real click is refused (as a
	 * user's click would be), but a JavaScript click goes straight to the
	 * button and the form is submitted. A test using the JavaScript click
	 * would pass although a user could not log in.
	 */
	@Test(description = "JavaScript: a JS click goes through an overlay a user could not click through")
	public void javascriptClickHidesAnOverlayProblem() {
		// Open the login page
		open(LOGIN_PAGE);
		// Cover the whole page with an overlay that never goes away (a broken page)
		((JavascriptExecutor) driver()).executeScript("var d=document.createElement('div');"
				+ "d.style.cssText='position:fixed;inset:0;background:rgba(0,0,0,.3);z-index:9999';document.body.appendChild(d);");
		// The Login button under the overlay
		WebElement loginButton = driver().findElement(By.cssSelector("#login button[type='submit']"));

		// A real click is refused: this is the correct signal that the page is broken
		assertThrows(ElementClickInterceptedException.class, loginButton::click);
		// A JavaScript click ignores the overlay...
		((JavascriptExecutor) driver()).executeScript("arguments[0].click();", loginButton);
		// ...and the form really is submitted (the empty form is rejected with a message)
		assertTrue(explicitWait().until(ExpectedConditions.visibilityOfElementLocated(By.id("flash"))).getText()
				.contains("Your username is invalid!"), "The JavaScript click submitted the form through the overlay");
		// Record the lesson in the report
		Steps.log("Real click: ElementClickInterceptedException; JavaScript click: went through the overlay");
	}
}
