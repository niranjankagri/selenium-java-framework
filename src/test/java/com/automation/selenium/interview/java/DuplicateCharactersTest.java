// Interview examples: Java coding questions
package com.automation.selenium.interview.java;

// TestNG assertion
import static org.testng.Assert.assertEquals;

// Sets: one for what was seen, one (ordered) for the duplicates
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

/**
 * Interview question: Find the duplicate characters in a string.
 * <p>
 * Interview answer: I keep a set of characters already seen. For each
 * character, {@code seen.add(c)} returns false if it was there before; then
 * it is a duplicate and goes into a second, ordered set, so every duplicate
 * is reported once, in the order it first repeats. One pass, O(n).
 * <p>
 * Concept: {@code Set.add} returning a boolean is the shortest way to detect
 * "seen before". A nested loop comparing every pair also works but is O(n²).
 * <p>
 * Recommended approach: two sets as below; a frequency map
 * ({@link CharacterFrequencyTest}) works too when the counts are also needed.
 */
@Test(groups = { "interview", "java" })
public class DuplicateCharactersTest {

	/**
	 * Lists the characters that occur more than once.
	 *
	 * @param text  the text to check.
	 * @return List each duplicate once, in the order it first repeats.
	 */
	static List<Character> duplicates(String text) {
		// Characters met so far
		Set<Character> seen = new HashSet<>();
		// Duplicates, each once, in order (LinkedHashSet keeps insertion order)
		Set<Character> duplicates = new LinkedHashSet<>();
		for (char c : text.toCharArray()) {
			// add() returns false when the character was already in the set
			if (!seen.add(c)) {
				duplicates.add(c);
			}
		}
		// Hand back an unmodifiable list
		return List.copyOf(duplicates);
	}

	/** "programming" repeats r (5th letter), then m (8th), then g (11th). */
	@Test(description = "Java: find the duplicate characters of a word")
	public void findsDuplicates() {
		assertEquals(duplicates("programming"), List.of('r', 'm', 'g'));
		// A character repeated three times is still reported once
		assertEquals(duplicates("aaab"), List.of('a'));
	}

	/** No duplicates, and the empty string. */
	@Test(description = "Java: no duplicate characters")
	public void reportsNothingWithoutDuplicates() {
		assertEquals(duplicates("selenium").size(), 1, "Only 'e' repeats in selenium");
		assertEquals(duplicates("java"), List.of('a'));
		assertEquals(duplicates("abc"), List.of());
		assertEquals(duplicates(""), List.of());
	}
}
