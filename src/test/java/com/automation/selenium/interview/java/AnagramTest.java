// Interview examples: Java coding questions
package com.automation.selenium.interview.java;

// TestNG assertions
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

// Arrays.sort and Arrays.equals for char arrays
import java.util.Arrays;
// Locale-independent lower-casing
import java.util.Locale;

// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

/**
 * Interview question: Check whether two strings are anagrams.
 * <p>
 * Interview answer: I normalise both strings (lower case, spaces removed),
 * sort their characters and compare the sorted arrays: anagrams have exactly
 * the same letters. That is O(n log n). For a linear solution I count the
 * letters of one string and subtract those of the other.
 * <p>
 * Concept: two words are anagrams when one is a rearrangement of the other
 * ("listen" / "silent"). Different lengths after normalising means "no" at
 * once.
 * <p>
 * Recommended approach: sorting is short and clear; counting is faster for
 * long inputs. State which characters you ignore.
 */
@Test(groups = { "interview", "java" })
public class AnagramTest {

	/**
	 * Compares two phrases, ignoring case and spaces.
	 *
	 * @param first    the first phrase.
	 * @param second   the second phrase.
	 * @return boolean true if both use exactly the same letters.
	 */
	static boolean areAnagrams(String first, String second) {
		// Normalise both: no spaces, lower case
		char[] a = normalise(first);
		char[] b = normalise(second);
		// Different lengths can never be anagrams
		if (a.length != b.length) {
			return false;
		}
		// Sort both character arrays...
		Arrays.sort(a);
		Arrays.sort(b);
		// ...and compare them element by element
		return Arrays.equals(a, b);
	}

	/**
	 * Removes spaces and lower-cases the text.
	 *
	 * @param text    the phrase.
	 * @return char[] its characters, ready to compare.
	 */
	private static char[] normalise(String text) {
		// Locale.ROOT: lower-casing must not depend on the machine's language
		return text.replace(" ", "").toLowerCase(Locale.ROOT).toCharArray();
	}

	/** Pairs that are anagrams. */
	@Test(description = "Java: anagrams are recognised")
	public void recognisesAnagrams() {
		assertTrue(areAnagrams("listen", "silent"));
		// Case is ignored
		assertTrue(areAnagrams("Listen", "Silent"));
		// Spaces are ignored
		assertTrue(areAnagrams("Dormitory", "Dirty room"));
	}

	/** Pairs that are not anagrams. */
	@Test(description = "Java: non-anagrams are rejected")
	public void rejectsNonAnagrams() {
		assertFalse(areAnagrams("hello", "world"));
		// Same letters, different counts
		assertFalse(areAnagrams("aab", "abb"));
		// Different lengths
		assertFalse(areAnagrams("abc", "abcd"));
	}
}
