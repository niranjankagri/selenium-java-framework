// Interview examples: drop-downs
package com.automation.selenium.interview.dropdowns;

// TestNG assertions
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertThrows;

// Option texts come as a list
import java.util.List;

// A locator (how to find an element)
import org.openqa.selenium.By;
// One element on the page
import org.openqa.selenium.WebElement;
// Selenium's helper for native <select> elements
import org.openqa.selenium.support.ui.Select;
// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

// Base class for the practice-site examples
import com.automation.selenium.interview.PracticeSiteTest;
// Writes steps into the HTML report
import com.automation.selenium.utils.Steps;

/**
 * Interview question: How do you handle a {@code <select>} drop-down?
 * <p>
 * Interview answer: For a real {@code <select>} I use Selenium's Select class:
 * selectByVisibleText, selectByValue or selectByIndex, and getOptions or
 * getFirstSelectedOption to read it. Select only works on {@code <select>}; custom
 * div drop-downs are clicked open and the option is clicked by its text.
 * <p>
 * Concept: Selenium's {@link Select} class wraps a native {@code <select>}
 * element: {@code selectByVisibleText}, {@code selectByValue},
 * {@code selectByIndex}, {@code getOptions}, {@code getFirstSelectedOption},
 * {@code isMultiple} and, for multi-selects, the {@code deselect…} methods.
 * <p>
 * Trap: {@code Select} only works on a real {@code <select>}. Most modern
 * apps (OrangeHRM too) draw drop-downs with {@code div}s; those are clicked
 * open and the option is clicked by its text. See
 * {@code SeleniumScenariosTest.userRoleDropdownOffersAdminAndEss}.
 * <p>
 * Recommended approach: select by visible text (what the user sees) or by
 * value (stable when texts are translated); avoid index, which changes when
 * options are added.
 * <p>
 * Implementation: {@code /dropdown}: a placeholder option (disabled) and
 * "Option 1" (value 1) and "Option 2" (value 2).
 */
@Test(groups = { "interview", "dropdowns" })
public class SelectDropdownTest extends PracticeSiteTest {

	// Page with the native drop-down
	private static final String DROPDOWN_PAGE = "/dropdown";
	// The <select> element
	private static final By DROPDOWN = By.id("dropdown");

	/**
	 * The three ways to select, each checked with the selected option.
	 */
	@Test(description = "Drop-downs: select by visible text, value and index")
	public void selectByTextValueAndIndex() {
		// Open the page and wrap the <select> in a Select
		open(DROPDOWN_PAGE);
		Select dropdown = new Select(driver().findElement(DROPDOWN));

		// By the text the user sees
		dropdown.selectByVisibleText("Option 2");
		assertEquals(dropdown.getFirstSelectedOption().getText(), "Option 2", "After selectByVisibleText");
		// By the option's value attribute (<option value="1">)
		dropdown.selectByValue("1");
		assertEquals(dropdown.getFirstSelectedOption().getText(), "Option 1", "After selectByValue");
		// By position, counting from 0 (0 is the placeholder)
		dropdown.selectByIndex(2);
		assertEquals(dropdown.getFirstSelectedOption().getText(), "Option 2", "After selectByIndex");
		// Record it in the report
		Steps.log("Selected by visible text, by value and by index");
	}

	/**
	 * Reading the drop-down: every option, the default choice and whether
	 * several options can be chosen.
	 */
	@Test(description = "Drop-downs: read all options, the default option and isMultiple")
	public void optionsAndDefaultAreRead() {
		// Open the page and wrap the <select>
		open(DROPDOWN_PAGE);
		Select dropdown = new Select(driver().findElement(DROPDOWN));

		// Every option's text, in order
		List<String> options = dropdown.getOptions().stream().map(WebElement::getText).toList();
		assertEquals(options, List.of("Please select an option", "Option 1", "Option 2"), "Options");
		// Before any choice, the placeholder is selected
		assertEquals(dropdown.getFirstSelectedOption().getText(), "Please select an option", "Default option");
		// A single-choice drop-down (no "multiple" attribute), so deselect… methods would throw
		assertFalse(dropdown.isMultiple(), "Single choice");
		// Record it in the report
		Steps.log("Options: " + options);
	}

	/**
	 * A disabled option cannot be selected: Selenium 4 refuses with an
	 * {@code UnsupportedOperationException} instead of silently doing
	 * nothing, which is what a user would experience.
	 */
	@Test(description = "Drop-downs: a disabled option cannot be selected")
	public void disabledOptionCannotBeSelected() {
		// Open the page and wrap the <select>
		open(DROPDOWN_PAGE);
		Select dropdown = new Select(driver().findElement(DROPDOWN));
		// Choose a real option first
		dropdown.selectByValue("1");

		// The placeholder (index 0) is disabled: selecting it is refused
		assertThrows(UnsupportedOperationException.class, () -> dropdown.selectByIndex(0));
		// The previous choice is kept
		assertEquals(dropdown.getFirstSelectedOption().getText(), "Option 1", "Choice after the refused selection");
		// Record it in the report
		Steps.log("selectByIndex(0) on the disabled placeholder threw UnsupportedOperationException");
	}
}
