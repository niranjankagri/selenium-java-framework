// Interview examples: Java coding questions
package com.automation.selenium.interview.java;

// TestNG assertion
import static org.testng.Assert.assertEquals;

// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

/**
 * Interview question: Reverse the order of the words in a sentence.
 * <p>
 * Interview answer: I trim the sentence, split it on one or more spaces
 * ({@code "\\s+"}), and join the words back from last to first with single
 * spaces. Splitting on {@code "\\s+"} instead of {@code " "} keeps repeated
 * spaces from producing empty words. O(n).
 * <p>
 * Concept: this reverses the word order, not the letters ("Selenium with
 * Java" → "Java with Selenium"). Reversing the letters of each word is a
 * different question; ask which one is meant.
 * <p>
 * Recommended approach: split, then walk the array backwards into a
 * StringBuilder. {@code Collections.reverse} on a list also works.
 */
@Test(groups = { "interview", "java" })
public class ReverseWordsTest {

	/**
	 * Reverses the word order of a sentence.
	 *
	 * @param sentence the sentence.
	 * @return String  the words from last to first, single-spaced.
	 */
	static String reverseWords(String sentence) {
		// Blank input: nothing to reverse (split would give one empty word)
		if (sentence.isBlank()) {
			return "";
		}
		// trim() removes outer spaces; "\\s+" splits on any run of whitespace
		String[] words = sentence.trim().split("\\s+");
		// Collect the words from the last one backwards
		StringBuilder reversed = new StringBuilder();
		for (int i = words.length - 1; i >= 0; i--) {
			reversed.append(words[i]);
			// A space between words, none after the last one
			if (i > 0) {
				reversed.append(' ');
			}
		}
		return reversed.toString();
	}

	/** A normal sentence. */
	@Test(description = "Java: reverse the words of a sentence")
	public void reversesWordOrder() {
		assertEquals(reverseWords("Selenium WebDriver with Java"), "Java with WebDriver Selenium");
		// One word stays as it is
		assertEquals(reverseWords("TestNG"), "TestNG");
	}

	/** Extra spaces and blank input. */
	@Test(description = "Java: reverse words with extra spaces")
	public void handlesExtraSpaces() {
		// Leading, trailing and repeated spaces collapse to single spaces
		assertEquals(reverseWords("  learn   Selenium  "), "Selenium learn");
		assertEquals(reverseWords("   "), "");
	}
}
