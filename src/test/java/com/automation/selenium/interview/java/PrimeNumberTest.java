// Interview examples: Java coding questions
package com.automation.selenium.interview.java;

// TestNG assertions
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

// The primes up to a limit
import java.util.List;
import java.util.stream.IntStream;

// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

/**
 * Interview question: Check whether a number is prime, and list the primes
 * up to n.
 * <p>
 * Interview answer: numbers below 2 are not prime; 2 is; other even numbers
 * are not. For the rest I only try odd divisors up to the square root,
 * because a factor above the root would pair with one below it. That is
 * O(√n) per number. For all primes up to a large n, the Sieve of Eratosthenes
 * is faster.
 * <p>
 * Concept: stop at √n (written {@code i * i <= n} to avoid floating point)
 * and handle 0, 1 and 2 explicitly; these edge cases are what the
 * interviewer checks.
 * <p>
 * Recommended approach: the √n loop with the edge cases; mention the sieve.
 */
@Test(groups = { "interview", "java" })
public class PrimeNumberTest {

	/**
	 * Checks one number.
	 *
	 * @param n        the number.
	 * @return boolean true if it is prime.
	 */
	static boolean isPrime(int n) {
		// 0, 1 and negative numbers are not prime
		if (n < 2) {
			return false;
		}
		// 2 is the only even prime
		if (n % 2 == 0) {
			return n == 2;
		}
		// Odd divisors up to √n; (long) so i * i cannot overflow near Integer.MAX_VALUE
		for (long i = 3; i * i <= n; i += 2) {
			if (n % i == 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Lists the primes from 2 up to a limit.
	 *
	 * @param limit the largest number to check.
	 * @return List the primes, ascending.
	 */
	static List<Integer> primesUpTo(int limit) {
		// Check every number in the range with isPrime
		return IntStream.rangeClosed(2, limit).filter(PrimeNumberTest::isPrime).boxed().toList();
	}

	/** Primes and non-primes, including the edge cases. */
	@Test(description = "Java: check whether a number is prime")
	public void checksSingleNumbers() {
		assertTrue(isPrime(2));
		assertTrue(isPrime(17));
		assertTrue(isPrime(7_919));
		// The largest int is prime too (needs the long loop variable)
		assertTrue(isPrime(Integer.MAX_VALUE));
		assertFalse(isPrime(0));
		assertFalse(isPrime(1));
		assertFalse(isPrime(-7));
		// 9 = 3 × 3: the √n bound must include the root itself
		assertFalse(isPrime(9));
		assertFalse(isPrime(100));
	}

	/** All primes up to 30. */
	@Test(description = "Java: list the primes up to n")
	public void listsPrimesUpToALimit() {
		assertEquals(primesUpTo(30), List.of(2, 3, 5, 7, 11, 13, 17, 19, 23, 29));
		assertEquals(primesUpTo(1), List.of());
	}
}
