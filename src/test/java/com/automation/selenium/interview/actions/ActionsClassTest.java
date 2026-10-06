// Interview examples: the Actions class
package com.automation.selenium.interview.actions;

// TestNG assertions
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

// A locator (how to find an element)
import org.openqa.selenium.By;
// Special keys (Shift, Escape, arrows)
import org.openqa.selenium.Keys;
// One element on the page
import org.openqa.selenium.WebElement;
// Builds chains of low-level mouse and keyboard events
import org.openqa.selenium.interactions.Actions;
// Ready-made wait conditions
import org.openqa.selenium.support.ui.ExpectedConditions;
// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

// Base class for the practice-site examples
import com.automation.selenium.interview.PracticeSiteTest;
// Writes steps into the HTML report
import com.automation.selenium.utils.Steps;

/**
 * Interview question: When and how do you use the Actions class?
 * <p>
 * Interview answer: I use the Actions class for low-level input that click()
 * and sendKeys() can't do: hover with moveToElement, right-click with
 * contextClick, key combinations with keyDown and keyUp, and drag and drop.
 * The chain only runs when I call perform().
 * <p>
 * Concept: {@link Actions} sends low-level mouse and keyboard input:
 * {@code moveToElement} (hover), {@code contextClick} (right-click),
 * {@code doubleClick}, {@code clickAndHold}/{@code release} and
 * {@code dragAndDrop}, {@code keyDown}/{@code keyUp} (hold Shift or Ctrl)
 * and {@code sendKeys}. Calls only <i>build</i> a chain; nothing happens
 * until {@code perform()}.
 * <p>
 * Recommended approach: use plain {@code click()} and {@code sendKeys()}
 * when they work; use Actions for what they cannot do: hover menus,
 * right-click, key combinations, drag and drop. Forgetting {@code perform()}
 * is the classic mistake: the test runs on and nothing happened.
 * <p>
 * Implementation: {@code /hovers}, {@code /context_menu},
 * {@code /key_presses}, {@code /horizontal_slider}. Keyboard login on
 * OrangeHRM: {@code LoginPage.loginWithKeyboard}.
 */
@Test(groups = { "interview", "actions" })
public class ActionsClassTest extends PracticeSiteTest {

	/**
	 * Hover: the user name under a picture is hidden until the mouse moves
	 * over the picture.
	 */
	@Test(description = "Actions: moveToElement shows a caption that only appears on hover")
	public void hoverShowsTheCaption() {
		// Open the page with the pictures
		open("/hovers");
		// The first picture and its caption
		WebElement picture = driver().findElement(By.cssSelector(".figure img"));
		By caption = By.cssSelector(".figure:nth-of-type(1) .figcaption h5");
		// Before the hover, the caption is hidden
		assertFalse(driver().findElement(caption).isDisplayed(), "Caption before the hover");

		// Move the mouse over the picture (perform() sends it)
		new Actions(driver()).moveToElement(picture).perform();
		// Now the caption is shown
		assertEquals(explicitWait().until(ExpectedConditions.visibilityOfElementLocated(caption)).getText(), "name: user1",
				"Caption after the hover");
		// Record it in the report
		Steps.log("Hovered over the first picture: caption \"name: user1\" appeared");
	}

	/**
	 * Right-click: this page opens a JavaScript alert from its context menu
	 * handler.
	 */
	@Test(description = "Actions: contextClick (right-click) triggers the page's context menu")
	public void rightClickOpensTheContextMenu() {
		// Open the page with the right-click box
		open("/context_menu");

		// Right-click inside the box
		new Actions(driver()).contextClick(driver().findElement(By.id("hot-spot"))).perform();
		// The page reacts with an alert; read and close it
		var alert = explicitWait().until(ExpectedConditions.alertIsPresent());
		assertEquals(alert.getText(), "You selected a context menu", "Alert after the right-click");
		alert.accept();
		// Record it in the report
		Steps.log("Right-clicked the box: the context-menu alert appeared");
	}

	/**
	 * Key combinations: hold Shift while typing "a" gives "A"; a special key
	 * (Escape) is sent the same way. The page reports the last key pressed.
	 */
	@Test(description = "Actions: keyDown/keyUp for Shift and a special key")
	public void keyCombinationsAreSent() {
		// Open the page that reports key presses
		open("/key_presses");
		// Its input field
		WebElement input = driver().findElement(By.id("target"));

		// Click into the field, hold Shift, type "a", release Shift
		new Actions(driver()).click(input).keyDown(Keys.SHIFT).sendKeys("a").keyUp(Keys.SHIFT).perform();
		// Shift + a typed a capital A
		assertEquals(input.getDomProperty("value"), "A", "Typed with Shift held");

		// A special key: Escape
		new Actions(driver()).sendKeys(Keys.ESCAPE).perform();
		// The page names the last key it received
		assertEquals(driver().findElement(By.id("result")).getText(), "You entered: ESCAPE", "Last key pressed");
		// Record it in the report
		Steps.log("Shift+a typed \"A\"; Escape was reported by the page");
	}

	/**
	 * A slider moved with the arrow keys: each Right arrow is one step of
	 * 0.5. Keys are exact; dragging by a pixel offset depends on the
	 * slider's width and is harder to make reliable.
	 */
	@Test(description = "Actions: move a slider with the arrow keys")
	public void sliderMovesWithArrowKeys() {
		// Open the page with the slider
		open("/horizontal_slider");
		// The range input
		WebElement slider = driver().findElement(By.cssSelector(".sliderContainer input[type='range']"));

		// Focus the slider and press Right twice (sendKeys on the element sends the keys to it)
		slider.sendKeys(Keys.ARROW_RIGHT, Keys.ARROW_RIGHT);
		// 0 + 2 × 0.5 = 1; the page shows the value next to the slider
		assertEquals(driver().findElement(By.id("range")).getText(), "1", "Slider value after two steps");
		// Record it in the report
		Steps.log("Two Right-arrow presses moved the slider from 0 to 1");
	}
}
