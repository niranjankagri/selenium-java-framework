// Interview examples: locators
package com.automation.selenium.interview.locators;

// TestNG assertions: each fails the test with the given message when the check is false
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

// Keeps the strategies in the order they are written
import java.util.LinkedHashMap;
import java.util.Map;

// A locator (how to find an element)
import org.openqa.selenium.By;
// One element on the page
import org.openqa.selenium.WebElement;
// Selenium 4 relative locators: above, below, toLeftOf, toRightOf, near
import org.openqa.selenium.support.locators.RelativeLocator;
// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

// Base class for the practice-site examples (open(path), explicitWait())
import com.automation.selenium.interview.PracticeSiteTest;
// Writes steps into the HTML report
import com.automation.selenium.utils.Steps;

/**
 * Interview question: Which locator strategies does Selenium support, and
 * which do you prefer?
 * <p>
 * Concept: Selenium finds elements with eight {@link By} strategies: id,
 * name, className, tagName, linkText, partialLinkText, cssSelector and xpath.
 * Selenium 4 adds relative locators that find an element by its position
 * next to another one.
 * <p>
 * Recommended approach: prefer a unique, stable attribute (id, name, a
 * {@code data-test} attribute), then a short CSS selector. Use XPath when you
 * need the visible text or must move from one element to a related one
 * (parent, sibling). Avoid absolute paths such as {@code /html/body/div[2]/…}:
 * they break with every layout change.
 * <p>
 * Implementation: the login page of the practice site
 * ({@code /login}): a form with a user name, a password and a Login button.
 */
// Every test of this class is in the "interview" group and the topic group "locators"
@Test(groups = { "interview", "locators" })
public class LocatorStrategiesTest extends PracticeSiteTest {

	// Path of the login page on the practice site
	private static final String LOGIN_PAGE = "/login";

	/**
	 * Interview question: Show every locator strategy.
	 * <p>
	 * Each of the eight strategies finds a visible element of the login page.
	 * The map's key is how the strategy would be described in an interview.
	 */
	@Test(description = "Locators: all eight By strategies find elements of the login page")
	public void everyLocatorStrategyFindsAnElement() {
		// Open the login page
		open(LOGIN_PAGE);
		// Strategy description → locator (LinkedHashMap keeps this order for the report)
		Map<String, By> strategies = new LinkedHashMap<>();
		// id: the fastest and clearest, when it is unique and not generated
		strategies.put("id", By.id("username"));
		// name: common on form fields
		strategies.put("name", By.name("password"));
		// className: one class name only (no spaces), here the button's class
		strategies.put("className", By.className("radius"));
		// tagName: usually too broad, fine when the tag is unique on the page
		strategies.put("tagName", By.tagName("form"));
		// linkText: the whole visible text of an <a>
		strategies.put("linkText", By.linkText("Elemental Selenium"));
		// partialLinkText: part of a link's text
		strategies.put("partialLinkText", By.partialLinkText("Elemental"));
		// cssSelector: id, attribute and descendant in one short expression
		strategies.put("cssSelector", By.cssSelector("#login button[type='submit']"));
		// xpath: can match visible text, which CSS cannot
		strategies.put("xpath", By.xpath("//button[contains(normalize-space(),'Login')]"));

		// Check each strategy in turn
		for (Map.Entry<String, By> strategy : strategies.entrySet()) {
			// findElement throws NoSuchElementException if nothing matches, so finding it is half the check
			WebElement element = driver().findElement(strategy.getValue());
			// Show the strategy and what it found in the report
			Steps.log(strategy.getKey() + " → " + strategy.getValue() + " found <" + element.getTagName() + ">");
			// The element must also be visible to a user
			assertTrue(element.isDisplayed(), strategy.getKey() + " should find a visible element");
		}
	}

	/**
	 * Interview question: CSS or XPath, and what are XPath axes?
	 * <p>
	 * XPath can start at an element found by its text and move to a related
	 * element: here from the label "Password" to the input next to it
	 * ({@code following-sibling}) and up to the form ({@code ancestor}). CSS can
	 * only move down and to later siblings, never up to a parent.
	 */
	@Test(description = "Locators: XPath axes move from a label to its input and up to the form")
	public void xpathAxesNavigateFromALabel() {
		// Open the login page
		open(LOGIN_PAGE);

		// From the label with the text "Password" to the next <input> on the same level
		WebElement passwordField = driver().findElement(By.xpath("//label[normalize-space()='Password']/following-sibling::input"));
		// It is the password field
		assertEquals(passwordField.getDomAttribute("id"), "password", "Input after the Password label");

		// From the same label up the tree to the <form> that contains it
		WebElement form = driver().findElement(By.xpath("//label[normalize-space()='Password']/ancestor::form"));
		// It is the login form
		assertEquals(form.getDomAttribute("id"), "login", "Form around the Password label");
		// Record the result in the report
		Steps.log("following-sibling:: found #password, ancestor:: found #login");
	}

	/**
	 * Interview question: What are relative locators (Selenium 4)?
	 * <p>
	 * {@code RelativeLocator.with(tag).below(element)} finds an element by
	 * its position on the screen. Useful when the element has nothing stable
	 * of its own; but it depends on the layout, so a real attribute is still
	 * the better choice when there is one.
	 */
	@Test(description = "Locators: relative locators find fields by their position")
	public void relativeLocatorsFindFieldsByPosition() {
		// Open the login page
		open(LOGIN_PAGE);
		// The anchor element the others are described relative to
		WebElement usernameField = driver().findElement(By.id("username"));

		// "The input below the user name field": the closest one is the password field
		WebElement below = driver().findElement(RelativeLocator.with(By.tagName("input")).below(usernameField));
		assertEquals(below.getDomAttribute("id"), "password", "Input below the username field");

		// "The label above the password field": the closest one is the Password label
		WebElement above = driver().findElement(RelativeLocator.with(By.tagName("label")).above(By.id("password")));
		assertEquals(above.getText(), "Password", "Label above the password field");
		// Record the result in the report
		Steps.log("below(#username) found #password; above(#password) found the \"Password\" label");
	}

	/**
	 * Interview question: How do you locate an element whose value is only
	 * known at run time (a dynamic locator)?
	 * <p>
	 * Build the locator from the value with {@code String.format} instead of
	 * hard-coding one locator per value. Here: the "Due" cell of the table row
	 * that contains a given e-mail address.
	 */
	@Test(description = "Locators: a dynamic XPath built from a value finds the matching table row")
	public void dynamicLocatorIsBuiltFromAValue() {
		// Open the page with the data tables
		open("/tables");
		// The template: %s is replaced by the e-mail; td[4] is the Due column of that row
		String dueOfRowWithEmail = "//table[@id='table1']//tr[td[normalize-space()='%s']]/td[4]";

		// Fill in the value and find the cell
		String due = driver().findElement(By.xpath(String.format(dueOfRowWithEmail, "jdoe@hotmail.com"))).getText();
		// Jason Doe owes $100.00
		assertEquals(due, "$100.00", "Due amount of jdoe@hotmail.com");
		// Same template, another value: no new locator needed
		assertEquals(driver().findElement(By.xpath(String.format(dueOfRowWithEmail, "fbach@yahoo.com"))).getText(), "$51.00",
				"Due amount of fbach@yahoo.com");
	}
}
