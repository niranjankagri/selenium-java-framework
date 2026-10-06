// Interview examples: Java coding questions
package com.automation.selenium.interview.java;

// TestNG assertion
import static org.testng.Assert.assertEquals;

// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

/**
 * Interview question: Swap two numbers without a third variable.
 * <p>
 * Interview answer: with arithmetic: {@code a = a + b; b = a - b; a = a - b;}
 * or with XOR: {@code a ^= b; b ^= a; a ^= b;}. In Java the arithmetic
 * version even works when {@code a + b} overflows, because {@code int}
 * arithmetic wraps around and the subtraction wraps back. In real code a
 * temporary variable is clearer, and that is what I would write.
 * <p>
 * Concept: Java passes primitives by value, so a method cannot swap the
 * caller's two variables; the swap happens inside the method, which returns
 * the swapped pair.
 * <p>
 * Recommended approach: show one trick, explain why it works, then say a
 * temporary variable is the readable choice.
 */
@Test(groups = { "interview", "java" })
public class SwapNumbersTest {

	/**
	 * Swaps with addition and subtraction.
	 *
	 * @param a      the first number.
	 * @param b      the second number.
	 * @return int[] {b, a}.
	 */
	static int[] swapWithArithmetic(int a, int b) {
		// a now holds the sum (it may overflow; that is fine, see below)
		a = a + b;
		// sum - b = the original a
		b = a - b;
		// sum - original a = the original b
		a = a - b;
		return new int[] { a, b };
	}

	/**
	 * Swaps with XOR.
	 *
	 * @param a      the first number.
	 * @param b      the second number.
	 * @return int[] {b, a}.
	 */
	static int[] swapWithXor(int a, int b) {
		// a holds the bits that differ between a and b
		a ^= b;
		// (a ^ b) ^ b = the original a
		b ^= a;
		// (a ^ b) ^ original a = the original b
		a ^= b;
		return new int[] { a, b };
	}

	/** Both tricks swap ordinary values. */
	@Test(description = "Java: swap two numbers without a temporary variable")
	public void swapsNumbers() {
		assertEquals(swapWithArithmetic(5, 10), new int[] { 10, 5 });
		assertEquals(swapWithXor(5, 10), new int[] { 10, 5 });
		// Negative numbers and zero
		assertEquals(swapWithArithmetic(-3, 0), new int[] { 0, -3 });
		assertEquals(swapWithXor(-3, 0), new int[] { 0, -3 });
	}

	/** Overflow in a + b still gives the right result, because int arithmetic wraps. */
	@Test(description = "Java: the arithmetic swap survives int overflow")
	public void arithmeticSwapSurvivesOverflow() {
		// MAX_VALUE + 1 overflows to MIN_VALUE, and the subtractions wrap back
		assertEquals(swapWithArithmetic(Integer.MAX_VALUE, 1), new int[] { 1, Integer.MAX_VALUE });
		assertEquals(swapWithXor(Integer.MAX_VALUE, 1), new int[] { 1, Integer.MAX_VALUE });
	}
}
