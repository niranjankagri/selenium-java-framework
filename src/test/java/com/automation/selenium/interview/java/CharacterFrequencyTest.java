// Interview examples: Java coding questions
package com.automation.selenium.interview.java;

// TestNG assertions
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

// A map that keeps the order in which keys were first added
import java.util.LinkedHashMap;
import java.util.Map;

// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

/**
 * Interview question: Count how often each character occurs in a string.
 * <p>
 * Interview answer: I loop over the characters once and keep a
 * {@code Map<Character, Integer>}; {@code map.merge(c, 1, Integer::sum)} adds
 * one or starts at one. A {@code LinkedHashMap} keeps the order of first
 * appearance, which makes the output easy to read and to assert. O(n) time.
 * <p>
 * Concept: a map from each distinct item to its count is the standard tool
 * for every "how many times" question (characters, words, duplicates).
 * <p>
 * Recommended approach: a plain loop with {@code merge}; the same with
 * streams is in {@link StreamFrequencyTest}. Clarify whether spaces and case
 * count. Here spaces are skipped and case matters.
 */
@Test(groups = { "interview", "java" })
public class CharacterFrequencyTest {

	/**
	 * Counts every non-space character.
	 *
	 * @param text  the text to count.
	 * @return Map  character → count, in order of first appearance.
	 */
	static Map<Character, Integer> frequency(String text) {
		// Insertion-ordered map for a predictable result
		Map<Character, Integer> counts = new LinkedHashMap<>();
		// One pass over the characters
		for (char c : text.toCharArray()) {
			// Spaces are not counted
			if (c != ' ') {
				// Not there yet → 1; already there → old count + 1
				counts.merge(c, 1, Integer::sum);
			}
		}
		return counts;
	}

	/** "selenium" has two e's; everything else once. */
	@Test(description = "Java: count each character of a word")
	public void countsEveryCharacter() {
		// Expected counts, in order of first appearance
		Map<Character, Integer> expected = new LinkedHashMap<>();
		expected.put('s', 1);
		expected.put('e', 2);
		expected.put('l', 1);
		expected.put('n', 1);
		expected.put('i', 1);
		expected.put('u', 1);
		expected.put('m', 1);
		assertEquals(frequency("selenium"), expected);
	}

	/** Spaces are skipped, case counts, empty input gives an empty map. */
	@Test(description = "Java: character counts skip spaces and keep case")
	public void skipsSpacesAndKeepsCase() {
		// "a a A": two lower-case a's, one upper-case A, no spaces counted
		Map<Character, Integer> counts = frequency("a a A");
		assertEquals(counts.get('a'), Integer.valueOf(2));
		assertEquals(counts.get('A'), Integer.valueOf(1));
		assertEquals(counts.size(), 2, "Distinct characters");
		// Nothing to count
		assertTrue(frequency("").isEmpty());
	}
}
