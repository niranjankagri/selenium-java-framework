// Interview examples: Java coding questions
package com.automation.selenium.interview.java;

// TestNG assertion
import static org.testng.Assert.assertEquals;

// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

/**
 * Interview question: Count the words in a sentence.
 * <p>
 * Interview answer: I walk through the characters and count each place where
 * a word starts: a non-space character that follows a space, or the start of
 * the text. That handles leading, trailing and repeated spaces with no
 * splitting and no extra arrays. The short version is
 * {@code text.trim().split("\\s+").length}, but it returns 1 for a blank
 * string, so it needs an {@code isBlank()} check first.
 * <p>
 * Concept: "words" are runs of non-space characters; the edge cases (blank
 * input, several spaces) are where most solutions go wrong, so test them.
 * <p>
 * Recommended approach: either version, with the blank-input case handled
 * and tested.
 */
@Test(groups = { "interview", "java" })
public class CountWordsTest {

	/**
	 * Counts words by counting where each one starts.
	 *
	 * @param text the text.
	 * @return int how many words it contains.
	 */
	static int countWords(String text) {
		// Words found so far
		int count = 0;
		// Whether the previous character was inside a word
		boolean inWord = false;
		for (char c : text.toCharArray()) {
			if (Character.isWhitespace(c)) {
				// Whitespace ends the current word (if any)
				inWord = false;
			} else if (!inWord) {
				// A non-space after whitespace (or at the start) begins a new word
				count++;
				inWord = true;
			}
		}
		return count;
	}

	/**
	 * The split-based version, with the blank-input check it needs.
	 *
	 * @param text the text.
	 * @return int how many words it contains.
	 */
	static int countWordsBySplit(String text) {
		// "".split(...) gives one empty element, so blank input must be handled first
		return text.isBlank() ? 0 : text.trim().split("\\s+").length;
	}

	/** Normal sentences, and both versions agree. */
	@Test(description = "Java: count the words of a sentence")
	public void countsWords() {
		assertEquals(countWords("Selenium is fun"), 3);
		assertEquals(countWords("one"), 1);
		// The split version gives the same answer
		assertEquals(countWordsBySplit("Selenium is fun"), 3);
	}

	/** Extra spaces, tabs and blank input. */
	@Test(description = "Java: count words with extra spaces and blank input")
	public void handlesSpacesAndBlankInput() {
		assertEquals(countWords("   Selenium   is\tfun  "), 3);
		assertEquals(countWordsBySplit("   Selenium   is\tfun  "), 3);
		// Blank input has no words (the naive split would say 1)
		assertEquals(countWords("   "), 0);
		assertEquals(countWordsBySplit("   "), 0);
		assertEquals(countWords(""), 0);
	}
}
