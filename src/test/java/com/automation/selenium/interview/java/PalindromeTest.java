// Interview examples: Java coding questions
package com.automation.selenium.interview.java;

// TestNG assertions
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

/**
 * Interview question: Check whether a string is a palindrome.
 * <p>
 * Interview answer: I compare characters from both ends with two indexes and
 * stop at the first mismatch. For sentences I skip anything that is not a
 * letter or digit and compare case-insensitively, so "A man, a plan, a canal:
 * Panama" counts. It is O(n) time and O(1) extra space.
 * <p>
 * Concept: a palindrome reads the same forwards and backwards. Reversing the
 * whole string and comparing also works, but builds a second string and
 * always reads every character.
 * <p>
 * Recommended approach: two pointers, and ask the interviewer whether case,
 * spaces and punctuation count; that clarifying question is part of the
 * answer.
 */
@Test(groups = { "interview", "java" })
public class PalindromeTest {

	/**
	 * Checks a phrase, ignoring case and anything that is not a letter or digit.
	 *
	 * @param text     the text to check.
	 * @return boolean true if it reads the same in both directions.
	 */
	static boolean isPalindrome(String text) {
		// Start at both ends
		int left = 0;
		int right = text.length() - 1;
		// Move towards the middle
		while (left < right) {
			// Skip characters that don't count (spaces, punctuation) on the left...
			if (!Character.isLetterOrDigit(text.charAt(left))) {
				left++;
			// ...and on the right
			} else if (!Character.isLetterOrDigit(text.charAt(right))) {
				right--;
			// Both are letters or digits: compare them case-insensitively
			} else if (Character.toLowerCase(text.charAt(left)) != Character.toLowerCase(text.charAt(right))) {
				// First mismatch: not a palindrome, no need to read further
				return false;
			} else {
				// They match: move both pointers inwards
				left++;
				right--;
			}
		}
		// No mismatch found
		return true;
	}

	/** Words and phrases that are palindromes. */
	@Test(description = "Java: palindromes are recognised")
	public void recognisesPalindromes() {
		assertTrue(isPalindrome("level"));
		// Case does not matter
		assertTrue(isPalindrome("Racecar"));
		// Spaces and punctuation are skipped
		assertTrue(isPalindrome("A man, a plan, a canal: Panama"));
		// Digits count too
		assertTrue(isPalindrome("12321"));
	}

	/** Words that are not palindromes, and edge cases. */
	@Test(description = "Java: non-palindromes and edge cases")
	public void rejectsNonPalindromes() {
		assertFalse(isPalindrome("Selenium"));
		// Only the first and last characters differ
		assertFalse(isPalindrome("abca"));
		// Empty and single-character strings read the same both ways
		assertTrue(isPalindrome(""));
		assertTrue(isPalindrome("x"));
	}
}
