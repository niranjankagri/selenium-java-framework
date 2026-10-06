// Interview examples: Java coding questions
package com.automation.selenium.interview.java;

// TestNG assertion
import static org.testng.Assert.assertEquals;

// Characters already added to the result
import java.util.HashSet;
import java.util.Set;
// Collects the stream's characters back into a string
import java.util.stream.Collectors;

// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

/**
 * Interview question: Remove the duplicate characters from a string, keeping
 * the first occurrence of each.
 * <p>
 * Interview answer: I loop over the characters with a set of those already
 * used, and append a character to a StringBuilder only when
 * {@code seen.add(c)} returns true, which is the first time. The order of
 * first appearance is kept and it is O(n). With streams:
 * {@code text.chars().distinct()}.
 * <p>
 * Concept: a {@code Set} answers "seen before?" in constant time; a
 * {@code LinkedHashSet} or {@code distinct()} keeps the original order.
 * <p>
 * Recommended approach: the loop for clarity, the stream for brevity; both
 * keep the order.
 */
@Test(groups = { "interview", "java" })
public class RemoveDuplicateCharactersTest {

	/**
	 * Keeps the first occurrence of every character.
	 *
	 * @param text    the text.
	 * @return String the text without repeated characters.
	 */
	static String removeDuplicates(String text) {
		// Characters already in the result
		Set<Character> seen = new HashSet<>();
		// The result, built character by character
		StringBuilder result = new StringBuilder();
		for (char c : text.toCharArray()) {
			// add() is true only the first time a character is seen
			if (seen.add(c)) {
				result.append(c);
			}
		}
		return result.toString();
	}

	/**
	 * The same with streams.
	 *
	 * @param text    the text.
	 * @return String the text without repeated characters.
	 */
	static String removeDuplicatesWithStream(String text) {
		// chars() → int code points; distinct() keeps the first of each, in order
		return text.chars().distinct()
				// Back from int to a one-character string, then join them
				.mapToObj(c -> String.valueOf((char) c)).collect(Collectors.joining());
	}

	/** Typical inputs; both versions agree. */
	@Test(description = "Java: remove duplicate characters, keeping order")
	public void removesDuplicates() {
		assertEquals(removeDuplicates("programming"), "progamin");
		assertEquals(removeDuplicates("selenium"), "selnium");
		// The stream version gives the same result
		assertEquals(removeDuplicatesWithStream("programming"), "progamin");
	}

	/** Nothing to remove, and the empty string. */
	@Test(description = "Java: remove duplicate characters, edge cases")
	public void handlesEdgeCases() {
		assertEquals(removeDuplicates("abc"), "abc");
		assertEquals(removeDuplicates("aaaa"), "a");
		assertEquals(removeDuplicates(""), "");
	}
}
