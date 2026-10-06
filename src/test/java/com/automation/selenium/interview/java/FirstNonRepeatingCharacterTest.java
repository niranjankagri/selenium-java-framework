// Interview examples: Java coding questions
package com.automation.selenium.interview.java;

// TestNG assertion
import static org.testng.Assert.assertEquals;

// Counts in order of first appearance
import java.util.LinkedHashMap;
import java.util.Map;
// "Maybe a value": empty when there is no answer
import java.util.Optional;

// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

/**
 * Interview question: Find the first character of a string that does not
 * repeat.
 * <p>
 * Interview answer: two passes. First I count every character in a
 * {@code LinkedHashMap}, which keeps the order of first appearance; then I
 * return the first entry whose count is 1. Both passes are O(n). If every
 * character repeats I return {@code Optional.empty()} rather than a magic
 * value.
 * <p>
 * Concept: one pass cannot know whether a character repeats later, so count
 * first, decide second. Checking {@code indexOf(c) == lastIndexOf(c)} for each
 * character also works but is O(n²).
 * <p>
 * Recommended approach: count, then scan; return {@code Optional} for "no
 * answer".
 */
@Test(groups = { "interview", "java" })
public class FirstNonRepeatingCharacterTest {

	/**
	 * Finds the first character that occurs exactly once.
	 *
	 * @param text      the text to search.
	 * @return Optional the character, or empty if every character repeats.
	 */
	static Optional<Character> firstNonRepeating(String text) {
		// Pass 1: count each character, keeping first-appearance order
		Map<Character, Integer> counts = new LinkedHashMap<>();
		for (char c : text.toCharArray()) {
			counts.merge(c, 1, Integer::sum);
		}
		// Pass 2: the first entry with count 1 is the answer
		for (Map.Entry<Character, Integer> entry : counts.entrySet()) {
			if (entry.getValue() == 1) {
				return Optional.of(entry.getKey());
			}
		}
		// Every character repeats
		return Optional.empty();
	}

	/** Typical inputs. */
	@Test(description = "Java: first non-repeating character")
	public void findsTheFirstUniqueCharacter() {
		// s repeats (3×), w is the first unique one
		assertEquals(firstNonRepeating("swiss"), Optional.of('w'));
		// In "selenium" only e repeats, so s is the first unique one
		assertEquals(firstNonRepeating("selenium"), Optional.of('s'));
		// The unique character can be at the end
		assertEquals(firstNonRepeating("aabbc"), Optional.of('c'));
	}

	/** No unique character, and empty input. */
	@Test(description = "Java: no non-repeating character")
	public void returnsEmptyWhenEverythingRepeats() {
		assertEquals(firstNonRepeating("aabb"), Optional.empty());
		assertEquals(firstNonRepeating(""), Optional.empty());
	}
}
