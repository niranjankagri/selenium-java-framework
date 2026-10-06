// Interview examples: frames and iframes
package com.automation.selenium.interview.frames;

// TestNG assertions
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

// The short wait for the frames
import java.time.Duration;

// A locator (how to find an element)
import org.openqa.selenium.By;
// Runs JavaScript in the page (to check that every frame has loaded)
import org.openqa.selenium.JavascriptExecutor;
// Thrown by a wait that runs out of time
import org.openqa.selenium.TimeoutException;
// One element on the page
import org.openqa.selenium.WebElement;
// Ready-made wait conditions (frameToBeAvailableAndSwitchToIt)
import org.openqa.selenium.support.ui.ExpectedConditions;
// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

// Base class for the practice-site examples
import com.automation.selenium.interview.PracticeSiteTest;
// Writes steps into the HTML report
import com.automation.selenium.utils.Steps;

/**
 * Interview question: How do you work with frames and iframes?
 * <p>
 * Concept: a frame holds a separate document. The driver searches only the
 * document it is switched to, so an element inside a frame is "not found"
 * until you switch into the frame. Switch with
 * {@code switchTo().frame(...)} by name or id, by index, or by the frame's
 * {@code WebElement}; go one level up with {@code parentFrame()} and back to
 * the top page with {@code defaultContent()}.
 * <p>
 * Recommended approach: prefer a name, id or {@code WebElement} over an
 * index (indexes change when frames are added); use
 * {@code frameToBeAvailableAndSwitchToIt} so the frame has loaded; switch
 * back to {@code defaultContent()} when you are done.
 * <p>
 * Implementation: {@code /nested_frames} (a top frame holding left, middle
 * and right frames, and a bottom frame) and {@code /iframe} (an editor in an
 * iframe).
 */
@Test(groups = { "interview", "frames" })
public class IframeHandlingTest extends PracticeSiteTest {

	// Page with frames inside frames
	private static final String NESTED_FRAMES = "/nested_frames";
	// The body of the frame the driver is switched to
	private static final By BODY = By.tagName("body");
	// How many times the frames page is loaded at most until all its frames have arrived
	private static final int MAX_FRAME_LOADS = 3;
	// JavaScript: true when the top, left, middle, right and bottom frames all show text
	private static final String ALL_FRAMES_LOADED = "function txt(w){try{return w.document.body.innerText.trim();}catch(e){return '';}}"
			+ "var top=window.frames['frame-top'],bottom=window.frames['frame-bottom'];"
			+ "return !!(top&&bottom&&txt(top.frames['frame-left'])&&txt(top.frames['frame-middle'])"
			+ "&&txt(top.frames['frame-right'])&&txt(bottom));";

	/**
	 * Nested frames: by name into the top frame, then into the middle one;
	 * {@code parentFrame()} one level up, into the right one; then
	 * {@code defaultContent()} to the top page and into the bottom frame.
	 */
	@Test(description = "Frames: switch into nested frames by name, up with parentFrame, out with defaultContent")
	public void nestedFramesByNameAndParentFrame() {
		// Open the page with the nested frames, and make sure all five frames arrived
		openNestedFramesCompletely();

		// Top page → frame "frame-top" (waits until it is loaded, then switches)
		explicitWait().until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("frame-top"));
		// frame-top → its child "frame-middle" (each frame loads its own document, so wait for it too)
		explicitWait().until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("frame-middle"));
		// The middle frame's text is in #content
		assertEquals(frameText(), "MIDDLE", "Middle frame");

		// One level up (back into frame-top), then into its sibling "frame-right"
		driver().switchTo().parentFrame();
		explicitWait().until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("frame-right"));
		assertEquals(frameText(), "RIGHT", "Right frame");

		// All the way out to the top page, then into "frame-bottom" by its index (0 = top, 1 = bottom)
		driver().switchTo().defaultContent();
		explicitWait().until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(1));
		assertEquals(frameText(), "BOTTOM", "Bottom frame (index 1)");
		// Record the path in the report
		Steps.log("top → middle, parentFrame → right, defaultContent → bottom (index 1)");
		// Leave the driver on the top page
		driver().switchTo().defaultContent();
	}

	/**
	 * An iframe found as a {@code WebElement}: switch in, read the editor's
	 * text, switch back and read the page heading, which is only visible from
	 * the top document.
	 */
	@Test(description = "Frames: switch into an iframe by its WebElement and back to the page")
	public void iframeByWebElement() {
		// Open the page with the editor iframe
		open("/iframe");
		// The iframe element, found on the top page
		WebElement editorFrame = explicitWait().until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("iframe#mce_0_ifr")));

		// Switch into it by the element
		driver().switchTo().frame(editorFrame);
		// Inside the iframe: the editor's body holds the text
		String editorText = driver().findElement(By.id("tinymce")).getText();
		assertTrue(editorText.contains("Your content goes here."), "Editor text inside the iframe: " + editorText);

		// The heading is outside the iframe: from inside, it cannot be found
		assertTrue(driver().findElements(By.tagName("h3")).isEmpty(), "The page heading is not visible from inside the iframe");
		// Back to the top page, where the heading is
		driver().switchTo().defaultContent();
		assertEquals(driver().findElement(By.tagName("h3")).getText(), "An iFrame containing the TinyMCE WYSIWYG Editor", "Page heading");
		// Record it in the report
		Steps.log("Read the editor inside the iframe, then the heading outside it");
	}

	/**
	 * Opens {@code /nested_frames} and waits until all five frames show their
	 * text. Each frame is a separate request to the free Heroku app, and
	 * sometimes one of them comes back empty; then the page is loaded again
	 * (at most {@value #MAX_FRAME_LOADS} times, each reload logged).
	 */
	private void openNestedFramesCompletely() {
		// Open the page (open() already handles a broken top page)
		open(NESTED_FRAMES);
		// Try until all frames are there, or the attempts are used up
		for (int load = 1; load <= MAX_FRAME_LOADS; load++) {
			try {
				// Same-origin frames can be read from the top page with JavaScript; wait until all have text
				explicitWait(Duration.ofSeconds(10)).until(d -> ((JavascriptExecutor) d).executeScript(ALL_FRAMES_LOADED));
				// All five frames are there
				return;
			} catch (TimeoutException e) {
				// A frame stayed empty: say so and load the page again (unless this was the last try)
				if (load < MAX_FRAME_LOADS) {
					Steps.log("A frame of the practice page stayed empty; loading the page again");
					driver().navigate().refresh();
				}
			}
		}
		// Still incomplete: carry on, the test will fail with a screenshot of what arrived
	}

	/**
	 * Reads the text of the frame the driver is switched to. The switch can
	 * happen before the frame's document has finished loading (its body is
	 * then still empty), so this waits until there is text.
	 *
	 * @return String the frame body's text, trimmed.
	 */
	private String frameText() {
		// null (keep waiting) while the body is empty; the text once it is there
		return explicitWait().withMessage("the frame to show its text").until(d -> {
			String text = d.findElement(BODY).getText().trim();
			return text.isEmpty() ? null : text;
		});
	}
}
