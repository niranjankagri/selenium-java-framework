// Interview examples: Java coding questions
package com.automation.selenium.interview.java;

// TestNG assertions
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;

// Fails fast with a clear message on null input
import java.util.Objects;

// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

/**
 * Interview question: Reverse a string without using
 * {@code StringBuilder.reverse()}.
 * <p>
 * Interview answer: I copy the characters into a char array and swap them
 * from both ends towards the middle with two indexes, which is O(n) time and
 * needs no extra string per step. In real code I would simply use
 * {@code new StringBuilder(text).reverse()}.
 * <p>
 * Concept: Java strings are immutable, so "reversing" always builds a new
 * string. Concatenating in a loop ({@code result = c + result}) creates a new
 * string on every step (O(n²)); a char array or a StringBuilder does not.
 * <p>
 * Recommended approach: the two-pointer swap shows the algorithm; the
 * StringBuilder one-liner is what production code should use.
 */
// No browser: the "java" group runs in about a second (-Dgroups=java)
@Test(groups = { "interview", "java" })
public class ReverseStringTest {

	/**
	 * Reverses a string by swapping characters from both ends.
	 *
	 * @param text    the string to reverse.
	 * @return String the characters in reverse order.
	 */
	static String reverse(String text) {
		// A null input is a caller mistake: fail with a clear message
		Objects.requireNonNull(text, "text must not be null");
		// Work on a copy of the characters (the string itself cannot change)
		char[] chars = text.toCharArray();
		// One index from the left, one from the right
		for (int left = 0, right = chars.length - 1; left < right; left++, right--) {
			// Swap the two characters through a temporary variable
			char temp = chars[left];
			chars[left] = chars[right];
			chars[right] = temp;
		}
		// Build the new string from the swapped array
		return new String(chars);
	}

	/** A word, a sentence and the built-in answer agree. */
	@Test(description = "Java: reverse a string with two pointers")
	public void reversesWordsAndSentences() {
		// A single word
		assertEquals(reverse("Selenium"), "muineleS");
		// Spaces and punctuation are characters too
		assertEquals(reverse("Hello, World!"), "!dlroW ,olleH");
		// Same result as the library method
		assertEquals(reverse("TestNG"), new StringBuilder("TestNG").reverse().toString());
	}

	/** Edge cases: empty, one character, even and odd lengths. */
	@Test(description = "Java: reverse a string, edge cases")
	public void handlesEdgeCases() {
		// Nothing to reverse
		assertEquals(reverse(""), "");
		// One character is its own reverse
		assertEquals(reverse("a"), "a");
		// Even length: every character is swapped
		assertEquals(reverse("ab"), "ba");
		// Odd length: the middle character stays
		assertEquals(reverse("abc"), "cba");
		// null is rejected instead of returning something misleading
		assertThrows(NullPointerException.class, () -> reverse(null));
	}
}
