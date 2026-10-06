// Interview examples: Java coding questions
package com.automation.selenium.interview.java;

// TestNG assertion
import static org.testng.Assert.assertEquals;

// Collections and stream helpers used below
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

/**
 * Interview question: Count the words (or characters) of a text with Java
 * streams.
 * <p>
 * Interview answer:
 * {@code Arrays.stream(words).collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()))}.
 * {@code groupingBy} puts equal words in one group, {@code counting()} counts
 * each group, and the {@code LinkedHashMap::new} factory keeps the order of
 * first appearance (the default map has no order). For characters:
 * {@code text.chars().mapToObj(c -> (char) c)} and the same collector.
 * <p>
 * Concept: the streams version of the frequency map in
 * {@link CharacterFrequencyTest}; note that {@code counting()} returns
 * {@code Long}, not {@code Integer}.
 * <p>
 * Recommended approach: normalise first (lower case, split on non-word
 * characters, drop empty pieces), then group and count.
 */
@Test(groups = { "interview", "java" })
public class StreamFrequencyTest {

	/**
	 * Counts each word, ignoring case and punctuation.
	 *
	 * @param text the text.
	 * @return Map word → count, in order of first appearance.
	 */
	static Map<String, Long> wordFrequency(String text) {
		// Lower case first, so "The" and "the" are one word
		return Arrays.stream(text.toLowerCase(Locale.ROOT).split("\\W+"))
				// split can produce an empty first piece (e.g. text starting with punctuation)
				.filter(word -> !word.isEmpty())
				// Group equal words, keep first-appearance order, count each group
				.collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()));
	}

	/**
	 * Counts each non-space character.
	 *
	 * @param text the text.
	 * @return Map character → count, in order of first appearance.
	 */
	static Map<Character, Long> characterFrequency(String text) {
		// chars() gives int values; turn each back into a Character
		return text.chars().filter(c -> c != ' ').mapToObj(c -> (char) c)
				// Same collector as for words
				.collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()));
	}

	/** Word counts, ignoring case and punctuation. */
	@Test(description = "Java: word frequency with streams")
	public void countsWords() {
		Map<String, Long> expected = new LinkedHashMap<>();
		expected.put("the", 3L);
		expected.put("cat", 1L);
		expected.put("and", 2L);
		expected.put("hat", 1L);
		expected.put("bat", 1L);
		assertEquals(wordFrequency("The cat and the hat, and THE bat!"), expected);
		// Nothing to count
		assertEquals(wordFrequency("...").size(), 0);
	}

	/** Character counts give the same numbers as the loop version. */
	@Test(description = "Java: character frequency with streams")
	public void countsCharacters() {
		Map<Character, Long> counts = characterFrequency("selenium");
		assertEquals(counts.get('e'), Long.valueOf(2));
		// Same keys and counts as the loop in CharacterFrequencyTest (Integer there, Long here)
		Map<Character, Integer> loop = CharacterFrequencyTest.frequency("selenium");
		assertEquals(counts.keySet(), loop.keySet());
		counts.forEach((c, n) -> assertEquals(n.intValue(), loop.get(c).intValue(), "Count of '" + c + "'"));
	}
}
