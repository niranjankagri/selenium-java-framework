// Interview examples: Java coding questions
package com.automation.selenium.interview.java;

// TestNG assertion
import static org.testng.Assert.assertEquals;

// Collections used below
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

/**
 * Interview question: Remove the duplicates from a list, keeping the
 * original order.
 * <p>
 * Interview answer: {@code new ArrayList<>(new LinkedHashSet<>(list))}: a
 * {@code LinkedHashSet} drops duplicates and keeps insertion order. With
 * streams: {@code list.stream().distinct().toList()}. A plain
 * {@code HashSet} also removes duplicates but loses the order, which is the
 * usual trap.
 * <p>
 * Concept: which collection gives which guarantee: {@code HashSet} (unique,
 * no order), {@code LinkedHashSet} (unique, insertion order),
 * {@code TreeSet} (unique, sorted).
 * <p>
 * Recommended approach: {@code LinkedHashSet} or {@code distinct()}; both keep
 * the first occurrence of each element.
 */
@Test(groups = { "interview", "java" })
public class RemoveDuplicatesFromListTest {

	/**
	 * Removes duplicates with a LinkedHashSet.
	 *
	 * @param list  the list (left unchanged).
	 * @return List a new list with the first occurrence of each element.
	 */
	static List<String> withLinkedHashSet(List<String> list) {
		// The set drops duplicates and keeps insertion order; copy back into a list
		return new ArrayList<>(new LinkedHashSet<>(list));
	}

	/**
	 * Removes duplicates with a stream.
	 *
	 * @param list  the list (left unchanged).
	 * @return List a new list with the first occurrence of each element.
	 */
	static List<String> withStream(List<String> list) {
		// distinct() keeps the first occurrence, in encounter order
		return list.stream().distinct().toList();
	}

	/** Both ways keep the order of first occurrence. */
	@Test(description = "Java: remove duplicates from a list, keeping order")
	public void removesDuplicatesInOrder() {
		List<String> browsers = List.of("chrome", "firefox", "chrome", "edge", "firefox");
		assertEquals(withLinkedHashSet(browsers), List.of("chrome", "firefox", "edge"));
		assertEquals(withStream(browsers), List.of("chrome", "firefox", "edge"));
	}

	/** The trap: a plain HashSet removes duplicates but does not promise any order. */
	@Test(description = "Java: a HashSet removes duplicates but not in order")
	public void hashSetRemovesDuplicatesWithoutOrder() {
		List<String> browsers = List.of("chrome", "firefox", "chrome", "edge", "firefox");
		Set<String> unordered = new HashSet<>(browsers);
		// Same elements, so the size and contents are right...
		assertEquals(unordered, Set.of("chrome", "firefox", "edge"));
		// ...but only LinkedHashSet / distinct() guarantee the original order
		assertEquals(withLinkedHashSet(List.of()), List.of());
	}
}
