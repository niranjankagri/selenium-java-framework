// Interview examples: form elements and tables
package com.automation.selenium.interview.elements;

// TestNG assertions
import static org.testng.Assert.assertEquals;

// States come as a list
import java.util.List;

// A locator (how to find an element)
import org.openqa.selenium.By;
// One element on the page
import org.openqa.selenium.WebElement;
// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

// Base class for the practice-site examples
import com.automation.selenium.interview.PracticeSiteTest;
// Writes steps into the HTML report
import com.automation.selenium.utils.Steps;

/**
 * Interview question: How do you handle checkboxes (and radio buttons)?
 * <p>
 * Interview answer: I read isSelected() first and click only when the state
 * has to change, because a click toggles a checkbox. Radio buttons work the
 * same, except that clicking a selected radio button doesn't unselect it.
 * <p>
 * Concept: {@code isSelected()} tells whether a checkbox or radio button is
 * checked; {@code click()} toggles a checkbox. Clicking blindly is a common
 * bug: if the box was already checked, the click <i>unchecks</i> it.
 * <p>
 * Recommended approach: read the state first and click only when it differs
 * from the wanted state ({@link #setChecked}). Radio buttons work the same
 * way, except that clicking a selected radio button never unselects it: you
 * select another one in the group. (The practice site has no radio buttons,
 * so they are described here only.)
 * <p>
 * Implementation: {@code /checkboxes}: checkbox 1 starts unchecked,
 * checkbox 2 starts checked.
 */
@Test(groups = { "interview", "elements" })
public class CheckboxTest extends PracticeSiteTest {

	// Both checkboxes of the page
	private static final By CHECKBOXES = By.cssSelector("#checkboxes input[type='checkbox']");

	/**
	 * Reads the starting states, then sets both boxes to a wanted state with
	 * a helper that only clicks when needed. Calling it twice changes nothing
	 * the second time, which a blind click would not guarantee.
	 */
	@Test(description = "Checkboxes: read the state and click only when it must change")
	public void checkboxesAreSetToAWantedState() {
		// Open the page
		open("/checkboxes");
		// findElements: both boxes, in page order
		List<WebElement> boxes = driver().findElements(CHECKBOXES);

		// Starting state: first unchecked, second checked
		assertEquals(states(boxes), List.of(false, true), "Starting states");

		// Wanted: first checked, second unchecked
		setChecked(boxes.get(0), true);
		setChecked(boxes.get(1), false);
		assertEquals(states(boxes), List.of(true, false), "After setting the wanted states");

		// The same calls again change nothing (a blind click would toggle them back)
		setChecked(boxes.get(0), true);
		setChecked(boxes.get(1), false);
		assertEquals(states(boxes), List.of(true, false), "After setting the same states again");
	}

	/**
	 * Checks or unchecks a checkbox, clicking only when its state differs.
	 *
	 * @param box     the checkbox.
	 * @param checked true to check it, false to uncheck it.
	 */
	private static void setChecked(WebElement box, boolean checked) {
		// Already in the wanted state: do nothing
		if (box.isSelected() == checked) {
			Steps.log("Checkbox already " + (checked ? "checked" : "unchecked") + ", no click");
			return;
		}
		// Otherwise one click toggles it
		box.click();
		Steps.log("Clicked the checkbox to " + (checked ? "check" : "uncheck") + " it");
	}

	/**
	 * Reads whether each box is checked.
	 *
	 * @param boxes  the checkboxes.
	 * @return List  true for each checked box, in the same order.
	 */
	private static List<Boolean> states(List<WebElement> boxes) {
		// isSelected() is true for a checked checkbox or a selected radio button / option
		return boxes.stream().map(WebElement::isSelected).toList();
	}
}
