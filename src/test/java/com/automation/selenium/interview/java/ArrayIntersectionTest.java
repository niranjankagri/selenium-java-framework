// Interview examples: Java coding questions
package com.automation.selenium.interview.java;

// TestNG assertion
import static org.testng.Assert.assertEquals;

// Sets for fast lookups and an ordered, duplicate-free result
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

/**
 * Interview question: Find the common elements of two arrays.
 * <p>
 * Interview answer: I put the second array into a {@code HashSet}, then walk
 * the first array and keep every element the set contains, collecting them
 * in a {@code LinkedHashSet} so each common element appears once, in the
 * first array's order. O(n + m) instead of the O(n × m) nested loop.
 * {@code set1.retainAll(set2)} is the one-line collection version.
 * <p>
 * Concept: turn the "is it in the other array?" question into a constant-time
 * set lookup.
 * <p>
 * Recommended approach: a set for the lookup; clarify whether duplicates
 * should be kept (here: no) and whether the order matters (here: the first
 * array's).
 */
@Test(groups = { "interview", "java" })
public class ArrayIntersectionTest {

	/**
	 * Elements present in both arrays.
	 *
	 * @param first   the first array (its order is kept).
	 * @param second  the second array.
	 * @return List   each common element once, in the first array's order.
	 */
	static List<Integer> intersection(int[] first, int[] second) {
		// Everything in the second array, for constant-time lookups
		Set<Integer> inSecond = new HashSet<>();
		for (int n : second) {
			inSecond.add(n);
		}
		// Common elements, each once, in the first array's order
		Set<Integer> common = new LinkedHashSet<>();
		for (int n : first) {
			if (inSecond.contains(n)) {
				common.add(n);
			}
		}
		return List.copyOf(common);
	}

	/** Overlapping arrays, with duplicates on both sides. */
	@Test(description = "Java: intersection of two arrays")
	public void findsCommonElements() {
		assertEquals(intersection(new int[] { 1, 2, 2, 3, 4 }, new int[] { 4, 2, 6, 2 }), List.of(2, 4));
		// Order follows the first array
		assertEquals(intersection(new int[] { 9, 1, 5 }, new int[] { 5, 9 }), List.of(9, 5));
		// Same result as retainAll on two sets
		Set<Integer> a = new LinkedHashSet<>(List.of(1, 2, 3, 4));
		a.retainAll(Set.of(4, 2, 6));
		assertEquals(intersection(new int[] { 1, 2, 3, 4 }, new int[] { 4, 2, 6 }), List.copyOf(a));
	}

	/** Nothing in common, and empty arrays. */
	@Test(description = "Java: intersection with nothing in common")
	public void returnsEmptyWithoutCommonElements() {
		assertEquals(intersection(new int[] { 1, 2 }, new int[] { 3, 4 }), List.of());
		assertEquals(intersection(new int[0], new int[] { 1 }), List.of());
	}
}
