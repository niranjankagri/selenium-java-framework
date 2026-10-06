// Interview examples: Java coding questions
package com.automation.selenium.interview.java;

// TestNG assertion
import static org.testng.Assert.assertEquals;

// Arrays.sort for comparison, Arrays.copyOf to keep the input unchanged
import java.util.Arrays;

// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

/**
 * Interview question: Sort an array without {@code Arrays.sort}.
 * <p>
 * Interview answer: bubble sort: compare neighbours and swap them when they
 * are in the wrong order; after each pass the largest remaining value has
 * "bubbled" to the end. I stop early when a pass makes no swap, so an
 * already sorted array costs one pass. O(n²) in general, which is fine for an
 * interview; in real code I use {@code Arrays.sort} (a dual-pivot quicksort
 * for primitives, O(n log n)).
 * <p>
 * Concept: knowing one simple sort and its complexity is expected; knowing
 * that production code uses the library is just as important.
 * <p>
 * Recommended approach: bubble sort with the early exit, on a copy so the
 * caller's array is not changed; then say you'd use {@code Arrays.sort}.
 */
@Test(groups = { "interview", "java" })
public class SortArrayTest {

	/**
	 * Sorts a copy of the array in ascending order with bubble sort.
	 *
	 * @param input  the array (left unchanged).
	 * @return int[] a sorted copy.
	 */
	static int[] bubbleSort(int[] input) {
		// Work on a copy so the caller's array stays as it was
		int[] numbers = Arrays.copyOf(input, input.length);
		// After pass i, the last i elements are in their final place
		for (int pass = 0; pass < numbers.length - 1; pass++) {
			// Did this pass change anything?
			boolean swapped = false;
			for (int i = 0; i < numbers.length - 1 - pass; i++) {
				// Neighbours in the wrong order: swap them
				if (numbers[i] > numbers[i + 1]) {
					int temp = numbers[i];
					numbers[i] = numbers[i + 1];
					numbers[i + 1] = temp;
					swapped = true;
				}
			}
			// No swaps: already sorted, stop early
			if (!swapped) {
				break;
			}
		}
		return numbers;
	}

	/** Unsorted, with duplicates and negatives; same result as Arrays.sort. */
	@Test(description = "Java: sort an array without Arrays.sort")
	public void sortsLikeTheLibrary() {
		int[] input = { 5, -1, 3, 3, 0, 9, 2 };
		// The library's result to compare with
		int[] expected = Arrays.copyOf(input, input.length);
		Arrays.sort(expected);
		assertEquals(bubbleSort(input), expected);
		// The input itself is unchanged
		assertEquals(input, new int[] { 5, -1, 3, 3, 0, 9, 2 });
	}

	/** Already sorted, reversed, one element, empty. */
	@Test(description = "Java: sort an array, edge cases")
	public void handlesEdgeCases() {
		assertEquals(bubbleSort(new int[] { 1, 2, 3 }), new int[] { 1, 2, 3 });
		assertEquals(bubbleSort(new int[] { 3, 2, 1 }), new int[] { 1, 2, 3 });
		assertEquals(bubbleSort(new int[] { 42 }), new int[] { 42 });
		assertEquals(bubbleSort(new int[0]), new int[0]);
	}
}
