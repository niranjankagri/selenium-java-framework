// Package of the custom HTML report
package com.automation.selenium.report;

// Thrown when the report file cannot be written
import java.io.IOException;
// Unchecked wrapper for IOException
import java.io.UncheckedIOException;
// The report is written as UTF-8
import java.nio.charset.StandardCharsets;
// File helpers (create folders, write text)
import java.nio.file.Files;
// A file or folder path
import java.nio.file.Path;
// A point in time (the run's start)
import java.time.Instant;
// The computer's time zone, for the start time
import java.time.ZoneId;
// Formats the start time, e.g. "05 Oct 2026, 14:03:12"
import java.time.format.DateTimeFormatter;
// Collections and helpers used while building the page
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
// Collectors.groupingBy / joining for streams
import java.util.stream.Collectors;

// TestNG interface for listeners that write a report after the whole run
import org.testng.IReporter;
// One suite's results
import org.testng.ISuite;
// The results of one <test> inside a suite
import org.testng.ISuiteResult;
// Passed / failed / skipped results of one <test>
import org.testng.ITestContext;
// The result of one test method run
import org.testng.ITestResult;
// Reads back the steps logged with Steps.log (Reporter.log)
import org.testng.Reporter;
// The suite definition from testng.xml
import org.testng.xml.XmlSuite;

// Settings, shown in the report's environment facts
import com.automation.selenium.config.Config;

/**
 * Writes one self-contained HTML report ({@value #FILE_NAME}) after the run:
 * pass rate, the run environment, and for every test its steps, duration,
 * failure and the screenshot taken on failure. Tests are grouped by class in
 * collapsible sections with their own counts, so a long run stays short to
 * read. It has search, status filters, a light/dark theme and light
 * animations (off when the reader's system asks for reduced motion), and
 * needs nothing but a browser to open.
 * <p>
 * Registered through {@code META-INF/services/org.testng.ITestNGListener}, so
 * it runs from Maven, testng.xml and the IDE alike.
 * <p>
 * How it is built: the page is one big HTML template ({@link #TEMPLATE}) with
 * {@code {{name}}} placeholders. {@link #render} fills them; the test list is
 * made by {@link #renderGroup} (one section per class) and
 * {@link #renderTest} (one card per test). Every value from the run passes
 * through {@link #esc} first, so a {@code <} in an error message cannot break
 * the page.
 */
// IReporter: TestNG calls generateReport once, after all tests have finished
public class HtmlReportListener implements IReporter {

	/** Result attribute holding the Base64 PNG screenshot of a failed test. */
	// BaseTest stores the screenshot under this name; this class reads it back
	public static final String SCREENSHOT_ATTRIBUTE = "screenshot";

	// Name of the report file in TestNG's output directory
	static final String FILE_NAME = "selenium-test-report.html";
	// Report title when the run has no suite name of its own (Maven names its suite "Surefire suite")
	private static final String DEFAULT_TITLE = "OrangeHRM UI Suite";
	// Start time format in the report header (an Instant has no time zone, so withZone adds the computer's)
	private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss").withZone(ZoneId.systemDefault());
	// Stack trace lines from these packages are framework noise and are left out
	private static final List<String> NOISE = List.of("org.testng.", "java.base/jdk.internal.", "java.base/java.lang.reflect.",
			"java.base/java.util.", "jdk.internal.", "sun.reflect.", "org.apache.maven.");

	/**
	 * Builds the report from all suites of the run.
	 *
	 * @param xmlSuites       the suite definitions (unused).
	 * @param suites          the suite results.
	 * @param outputDirectory TestNG's output folder, e.g. target/surefire-reports.
	 */
	@Override
	public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
		// Every test result of the run, collected below
		List<ITestResult> results = new ArrayList<>();
		// Title for the report; replaced by a real suite name if there is one
		String suiteName = DEFAULT_TITLE;
		// Usually one suite; testng.xml could define more
		for (ISuite suite : suites) {
			// Keep a real suite name (testng.xml); Maven's and TestNG's default names are replaced
			if (!suite.getName().startsWith("Surefire") && !suite.getName().startsWith("Default")) {
				suiteName = suite.getName();
			}
			// Every result of every <test>, whatever its status
			for (ISuiteResult suiteResult : suite.getResults().values()) {
				// The context holds the results sorted by outcome
				ITestContext context = suiteResult.getTestContext();
				// Add each outcome's results to the one list
				results.addAll(context.getPassedTests().getAllResults());
				results.addAll(context.getFailedTests().getAllResults());
				results.addAll(context.getFailedButWithinSuccessPercentageTests().getAllResults());
				results.addAll(context.getSkippedTests().getAllResults());
			}
		}
		// The report lists the tests in the order they ran
		results.sort(Comparator.comparingLong(ITestResult::getStartMillis));
		// e.g. target/surefire-reports/selenium-test-report.html
		Path file = Path.of(outputDirectory, FILE_NAME);
		try {
			// Make sure the folder exists
			Files.createDirectories(file.getParent());
			// Build the page and write it in one go
			Files.writeString(file, render(suiteName, results), StandardCharsets.UTF_8);
			// Print where the report is, so it can be opened straight from the console
			System.out.println("HTML report: " + file.toAbsolutePath());
		} catch (IOException e) {
			// Could not write: stop with the file name
			throw new UncheckedIOException("Could not write " + file, e);
		}
	}

	/**
	 * Builds the whole page.
	 *
	 * @param suiteName the suite name for the title.
	 * @param results   every test result, in start order.
	 * @return String   the complete HTML page.
	 */
	private String render(String suiteName, List<ITestResult> results) {
		// Counts, pass rate and run time for the summary panel
		long passed = count(results, "passed");
		long failed = count(results, "failed");
		// Everything that is neither passed nor failed counts as skipped
		long skipped = results.size() - passed - failed;
		// Whole percent of passed tests (0 for an empty run, so no division by zero)
		int rate = results.isEmpty() ? 0 : (int) Math.round(passed * 100.0 / results.size());
		// Run start = the earliest test start (now, if there are no tests)
		long start = results.stream().mapToLong(ITestResult::getStartMillis).min().orElse(System.currentTimeMillis());
		// Run end = the latest test end
		long end = results.stream().mapToLong(ITestResult::getEndMillis).max().orElse(start);
		// The longest test fills its duration bar; at least 1 so there is no division by zero
		long longest = Math.max(1, results.stream().mapToLong(r -> r.getEndMillis() - r.getStartMillis()).max().orElse(1));
		// Overall result: any failure → failed; nothing passed → skipped; else passed
		String verdict = failed > 0 ? "failed" : results.isEmpty() || passed == 0 ? "skipped" : "passed";

		// One section per test class, tests in run order inside it
		// (groupingBy with LinkedHashMap keeps the order in which classes first appear)
		Map<String, List<ITestResult>> byClass = results.stream().collect(Collectors.groupingBy(
				r -> r.getTestClass().getRealClass().getSimpleName(), LinkedHashMap::new, Collectors.toList()));
		// Classes with a failure or skip come first, the rest by name, so the order is the same every run
		List<Map.Entry<String, List<ITestResult>>> groups = new ArrayList<>(byClass.entrySet());
		// Sort key 1: "all passed?" (false sorts before true); key 2: the class name
		groups.sort(Comparator.comparing((Map.Entry<String, List<ITestResult>> g) -> count(g.getValue(), "passed") == g.getValue().size())
				.thenComparing(Map.Entry::getKey));

		// HTML of all class sections
		StringBuilder classes = new StringBuilder();
		// Running numbers for unique element ids (g0, g1, ... and t0, t1, ...)
		int groupId = 0;
		int testId = 0;
		// Render each class section
		for (Map.Entry<String, List<ITestResult>> group : groups) {
			// groupId++ uses the current value, then adds 1
			classes.append(renderGroup(group.getKey(), group.getValue(), groupId++, testId, longest));
			// The next section's cards continue the numbering
			testId += group.getValue().size();
		}

		// Fill the page template; the ring's circle has a circumference of 100,
		// so the pass rate is also its dash length
		return TEMPLATE
				// Title and header (escaped: the name could contain HTML characters)
				.replace("{{suite}}", esc(suiteName))
				// CSS class of the verdict badge: passed, failed or skipped
				.replace("{{verdict}}", verdict)
				// Text of the verdict badge (a switch expression picks it)
				.replace("{{verdictText}}", switch (verdict) {
				case "failed" -> failed + (failed == 1 ? " test failed" : " tests failed");
				case "passed" -> "All tests passed" + (skipped > 0 ? " (" + skipped + " skipped)" : "");
				default -> "No test passed";
				})
				// When the run started and how long it took
				.replace("{{started}}", TIME.format(Instant.ofEpochMilli(start)))
				.replace("{{duration}}", duration(end - start))
				// The four numbers of the summary panel
				.replace("{{total}}", String.valueOf(results.size()))
				.replace("{{passed}}", String.valueOf(passed))
				.replace("{{failed}}", String.valueOf(failed))
				.replace("{{skipped}}", String.valueOf(skipped))
				// Pass rate text and the ring's filled length
				.replace("{{rate}}", String.valueOf(rate))
				.replace("{{dash}}", String.valueOf(rate))
				// Widths of the three parts of the result bar
				.replace("{{pPassed}}", percent(passed, results.size()))
				.replace("{{pFailed}}", percent(failed, results.size()))
				.replace("{{pSkipped}}", percent(skipped, results.size()))
				// Environment facts (application, browser, Java, OS, user)
				.replace("{{env}}", environment())
				// All class sections with their test cards
				.replace("{{tests}}", classes.toString());
	}

	/**
	 * Renders one test class as a collapsible section: a summary row with its
	 * counts, a small result bar and its total test time, then its test cards.
	 * A class with a failure or skip starts open; the others start closed.
	 *
	 * @param name     the test class name.
	 * @param results  the results of that class, in start order.
	 * @param groupId  a unique number for the section's element id.
	 * @param firstId  the element id number of its first test card.
	 * @param longest  the longest test duration of the run, for the duration bars.
	 * @return String  the HTML section.
	 */
	private String renderGroup(String name, List<ITestResult> results, int groupId, int firstId, long longest) {
		// Counts for this class only
		long passed = count(results, "passed");
		long failed = count(results, "failed");
		long skipped = results.size() - passed - failed;
		// Sum of the test durations (tests of a class run one after another)
		long millis = results.stream().mapToLong(r -> r.getEndMillis() - r.getStartMillis()).sum();
		// The worst outcome colours the section and decides whether it starts open
		String worst = failed > 0 ? "failed" : skipped > 0 ? "skipped" : "passed";
		// Open the section when something needs attention
		boolean open = !worst.equals("passed");

		// A chip per outcome that occurred, e.g. "7 passed", "2 failed"
		StringBuilder chips = new StringBuilder();
		// Only outcomes that happened get a chip
		if (passed > 0) {
			chips.append("<span class=\"chip passed\">").append(passed).append(" passed</span>");
		}
		if (failed > 0) {
			chips.append("<span class=\"chip failed\">").append(failed).append(" failed</span>");
		}
		if (skipped > 0) {
			chips.append("<span class=\"chip skipped\">").append(skipped).append(" skipped</span>");
		}
		// The test cards of this class
		StringBuilder cards = new StringBuilder();
		// i = position inside the class (for the fade-in delay); firstId + i = unique id
		for (int i = 0; i < results.size(); i++) {
			cards.append(renderTest(results.get(i), firstId + i, i, longest));
		}

		// <section>: CSS class = worst outcome (+ "open"); data-default remembers the start state for the filters
		return "<section class=\"group " + worst + (open ? " open" : "") + "\" data-default=\"" + (open ? "open" : "closed") + "\">"
				// The clickable summary row; aria-* tell screen readers whether it is open and what it controls
				+ "<button class=\"ghead\" aria-expanded=\"" + open + "\" aria-controls=\"g" + groupId + "\">"
				// Class name with its test count below
				+ "<span class=\"gname\">" + esc(name) + "<small>" + results.size() + (results.size() == 1 ? " test" : " tests") + "</small></span>"
				// The outcome chips
				+ "<span class=\"chips\">" + chips + "</span>"
				// Small result bar: passed, failed and skipped parts, widths in percent
				+ "<span class=\"gbar\" aria-hidden=\"true\"><i class=\"p\" style=\"width:" + percent(passed, results.size())
				+ "%\"></i><i class=\"f\" style=\"width:" + percent(failed, results.size()) + "%\"></i><i class=\"s\" style=\"width:"
				+ percent(skipped, results.size()) + "%\"></i></span>"
				// Total time of the class
				+ "<span class=\"gtime\" title=\"Sum of the test durations\">" + duration(millis) + "</span>"
				// The arrow that turns when the section opens
				+ "<span class=\"chev\" aria-hidden=\"true\"></span></button>"
				// The body with the cards (the two wrappers make the open/close animation work)
				+ "<div class=\"gbody\" id=\"g" + groupId + "\"><div class=\"clip\"><div class=\"glist\">" + cards + "</div></div></div></section>";
	}

	/**
	 * Renders one test as a card.
	 *
	 * @param result   one test result.
	 * @param id       a unique number for element ids.
	 * @param index    the card's position in its class, for the staggered fade-in.
	 * @param longest  the longest test duration, for the duration bar.
	 * @return String  the HTML card of the test.
	 */
	private String renderTest(ITestResult result, int id, int index, long longest) {
		// passed, failed or skipped
		String status = status(result);
		// How long the test took
		long millis = result.getEndMillis() - result.getStartMillis();
		// Title: the @Test description, else the method name as a sentence;
		// a data-provider row adds its first parameter (the case name)
		String description = result.getMethod().getDescription();
		String title = description == null || description.isBlank() ? readable(result.getMethod().getMethodName()) : description;
		// The parameters as "(a, b, c)" after the method name; empty for tests without parameters
		String params = result.getParameters().length == 0 ? "" : Arrays.stream(result.getParameters()).map(String::valueOf)
				.collect(Collectors.joining(", ", "(", ")"));
		// Data-provider rows: add the first parameter (the case name) to the title
		if (result.getParameters().length > 0) {
			title += " — " + result.getParameters()[0];
		}
		// TestNG groups, shown as @tags
		String groups = Arrays.stream(result.getMethod().getGroups()).sorted().distinct()
				.map(g -> "<span class=\"tag\">@" + esc(g) + "</span>").collect(Collectors.joining());

		// Steps recorded with Steps.log during the test
		StringBuilder body = new StringBuilder();
		// Reporter.getOutput returns the messages logged for exactly this result
		List<String> steps = Reporter.getOutput(result);
		// A numbered list, if there are steps
		if (!steps.isEmpty()) {
			body.append("<ol class=\"steps\">");
			for (String step : steps) {
				// Escape first, then highlight the quoted values
				body.append("<li>").append(highlight(esc(step))).append("</li>");
			}
			body.append("</ol>");
		}
		// Failure or skip reason; the stack trace is shown for failures only
		Throwable error = result.getThrowable();
		if (error != null) {
			// CSS class: "skip" (amber box) or "error" (red box)
			String kind = status.equals("skipped") ? "skip" : "error";
			// Box with the exception name as its title and the message below
			body.append("<div class=\"").append(kind).append("\"><div class=\"error-title\">")
					.append(status.equals("skipped") ? "Skipped: " : "")
					.append(esc(error.getClass().getSimpleName())).append("</div><pre class=\"message\">")
					.append(esc(message(error))).append("</pre>");
			// Failures also get a collapsible stack trace (<details> opens on click without JavaScript)
			if (!status.equals("skipped")) {
				body.append("<details><summary>Stack trace</summary><pre class=\"trace\">").append(esc(trace(error))).append("</pre></details>");
			}
			body.append("</div>");
		}
		// Screenshot taken by BaseTest when the test failed
		Object screenshot = result.getAttribute(SCREENSHOT_ATTRIBUTE);
		if (screenshot != null) {
			// Embedded as a data: URL, so the report needs no separate image files
			body.append("<figure class=\"shot\"><a href=\"#\" class=\"zoom\" title=\"Click to enlarge\"><img alt=\"Screenshot at failure\" src=\"data:image/png;base64,")
					.append(screenshot).append("\"></a><figcaption>Browser at the moment of failure</figcaption></figure>");
		}
		// Nothing at all to show (no steps, no error, no screenshot)
		if (body.isEmpty()) {
			body.append("<p class=\"muted\">No steps recorded.</p>");
		}

		// Text the search box matches; every card starts closed and opens on click
		String search = (title + " " + result.getMethod().getMethodName() + " " + result.getTestClass().getRealClass().getSimpleName()
				+ " " + String.join(" ", result.getMethod().getGroups())).toLowerCase();
		// <article>: status as CSS class and data attribute (the filter reads it); --i delays the fade-in
		return "<article class=\"test " + status + "\" data-status=\"" + status + "\" data-search=\"" + esc(search) + "\""
				+ " style=\"--i:" + index + "\">"
				// The clickable card head
				+ "<button class=\"head\" aria-expanded=\"false\" aria-controls=\"t" + id + "\">"
				// Status pill
				+ "<span class=\"pill " + status + "\">" + status + "</span>"
				// Title, with the method name and parameters in small code below
				+ "<span class=\"name\"><strong>" + esc(title) + "</strong><code>" + esc(result.getMethod().getMethodName() + params) + "</code></span>"
				// The @group tags
				+ "<span class=\"tags\">" + groups + "</span>"
				// Duration bar (relative to the longest test, at least 2% so it is visible) and the time
				+ "<span class=\"time\"><span class=\"bar\"><i style=\"width:" + Math.max(2, millis * 100 / longest) + "%\"></i></span>"
				+ duration(millis) + "</span><span class=\"chev\" aria-hidden=\"true\"></span></button>"
				// The card body: steps, error, screenshot
				+ "<div class=\"body\" id=\"t" + id + "\"><div class=\"clip\"><div class=\"pad\">" + body + "</div></div></div></article>";
	}

	/**
	 * Lists the facts about the run's environment.
	 *
	 * @return String the environment facts as definition-list items.
	 */
	private String environment() {
		// LinkedHashMap keeps the order the facts are shown in
		Map<String, String> facts = new LinkedHashMap<>();
		try {
			// The test settings, if they can be read
			Config config = Config.get();
			facts.put("Application", config.baseUrl());
			facts.put("Browser", config.browser() + (config.headless() ? " (headless)" : ""));
		} catch (RuntimeException | LinkageError e) {
			// The report is still useful without the test settings
			// (LinkageError: Config failed in its static initialiser, e.g. config.properties missing)
		}
		// Facts every JVM knows
		facts.put("Java", System.getProperty("java.version"));
		facts.put("OS", System.getProperty("os.name") + " " + System.getProperty("os.version"));
		facts.put("User", System.getProperty("user.name"));
		// Each fact as <div><dt>name</dt><dd>value</dd></div>, all joined into one string
		return facts.entrySet().stream()
				.map(f -> "<div><dt>" + esc(f.getKey()) + "</dt><dd>" + esc(f.getValue()) + "</dd></div>")
				.collect(Collectors.joining());
	}

	/**
	 * Counts results with one status.
	 *
	 * @param results  test results.
	 * @param status   passed, failed or skipped.
	 * @return long    how many of the results have that status.
	 */
	private static long count(List<ITestResult> results, String status) {
		return results.stream().filter(r -> status(r).equals(status)).count();
	}

	/**
	 * Turns TestNG's status number into a word.
	 *
	 * @param result   one test result.
	 * @return String  passed, failed or skipped.
	 */
	private static String status(ITestResult result) {
		// A switch expression on TestNG's status constants
		return switch (result.getStatus()) {
		case ITestResult.SUCCESS -> "passed";
		// SUCCESS_PERCENTAGE_FAILURE: failed, but within the allowed successPercentage; still shown as failed
		case ITestResult.FAILURE, ITestResult.SUCCESS_PERCENTAGE_FAILURE -> "failed";
		// SKIP (and any other state)
		default -> "skipped";
		};
	}

	/**
	 * Marks quoted values in a step, e.g. Filter by username "Admin".
	 *
	 * @param escaped  the HTML-escaped step text.
	 * @return String  the step with each quoted value wrapped in a {@code <q>}.
	 */
	private static String highlight(String escaped) {
		// The text is already escaped, so quotes are &quot;. ".*?" is lazy: it stops at the next quote.
		// $1 = the text between the quotes
		return escaped.replaceAll("&quot;(.*?)&quot;", "<q>$1</q>");
	}

	/**
	 * Reads the message of a failure or skip.
	 *
	 * @param error    the failure or skip reason.
	 * @return String  its message (the exception itself if it has none), without
	 *                 the " on instance Class@1a2b3c" object ids TestNG adds to
	 *                 the message of a test skipped because a dependency failed.
	 */
	private static String message(Throwable error) {
		// No message → use toString(), which is at least the exception class name
		String message = error.getMessage() == null ? error.toString() : error.getMessage();
		// Regex: " on instance " + a class name (letters, dots, $) + "@" + a hex hash code
		return message.replaceAll(" on instance [\\w.$]+@\\p{XDigit}+", "");
	}

	/**
	 * Formats a stack trace without the noise.
	 *
	 * @param error    the failure.
	 * @return String  the stack trace without TestNG, reflection and Maven frames.
	 */
	private static String trace(Throwable error) {
		// The trace text, built line by line
		StringBuilder out = new StringBuilder();
		// Walk the cause chain, stopping at a throwable that is its own cause
		for (Throwable t = error; t != null; t = t.getCause() == t ? null : t.getCause()) {
			// First line: the exception itself; causes get "Caused by: " like in Java's own output
			out.append(t == error ? "" : "Caused by: ").append(t).append('\n');
			// Each frame of this exception
			for (StackTraceElement element : t.getStackTrace()) {
				// e.g. "com.automation.selenium.pages.LoginPage.loginAs(LoginPage.java:120)"
				String line = element.toString();
				// Keep it only if it starts with none of the noise prefixes
				if (NOISE.stream().noneMatch(line::startsWith)) {
					out.append("    at ").append(line).append('\n');
				}
			}
		}
		// Remove the last line break
		return out.toString().trim();
	}

	/**
	 * Turns a camelCase method name into a sentence.
	 *
	 * @param name     a method name, e.g. "adminCanLogIn".
	 * @return String  a sentence, e.g. "Admin can log in".
	 */
	private static String readable(String name) {
		// Put a space before every capital that follows a small letter or digit, then lower-case everything
		String words = name.replaceAll("([a-z0-9])([A-Z])", "$1 $2").toLowerCase();
		// Capitalise the first letter again
		return Character.toUpperCase(words.charAt(0)) + words.substring(1);
	}

	/**
	 * Formats a duration for people.
	 *
	 * @param millis   a duration in milliseconds.
	 * @return String  e.g. "850 ms", "12.4 s" or "2 min 05 s".
	 */
	private static String duration(long millis) {
		// Under a second: milliseconds
		if (millis < 1000) {
			return millis + " ms";
		}
		// Under a minute: seconds with one decimal (Locale.ROOT: always a dot, never a comma)
		if (millis < 60_000) {
			return String.format(Locale.ROOT, "%.1f s", millis / 1000.0);
		}
		// Otherwise minutes and two-digit seconds
		return String.format("%d min %02d s", millis / 60_000, millis / 1000 % 60);
	}

	/**
	 * Calculates a share in percent.
	 *
	 * @param part     a count.
	 * @param total    the total count.
	 * @return String  the share in percent, for CSS widths.
	 */
	private static String percent(long part, long total) {
		// Two decimals with a dot (CSS does not accept "33,33"); 0 when there is nothing to divide
		return total == 0 ? "0" : String.format(Locale.ROOT, "%.2f", part * 100.0 / total);
	}

	/**
	 * Escapes text for HTML.
	 *
	 * @param text     any text.
	 * @return String  the text, safe to put into HTML content and attributes.
	 */
	private static String esc(String text) {
		// & first, otherwise the & of the other replacements would be escaped again
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
	}

	// The page; {{name}} placeholders are filled in by render().
	// A text block (""" ... """, Java 15+) keeps the HTML readable without escaping every quote.
	// Comments inside it are CSS, HTML and JavaScript comments and end up in the report file.
	private static final String TEMPLATE = """
			<!doctype html>
			<html lang="en">
			<head>
			<!-- Text encoding and mobile-friendly width -->
			<meta charset="utf-8">
			<meta name="viewport" content="width=device-width, initial-scale=1">
			<!-- Browser tab title -->
			<title>{{suite}} – Selenium test report</title>
			<!-- Web fonts; without internet the system fonts are used -->
			<link rel="preconnect" href="https://fonts.googleapis.com">
			<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500&display=swap" rel="stylesheet">
			<style>
			/* Colours: light theme, then the dark theme */
			/* Light theme: page, card, text, lines, accents, result colours, shadows */
			:root{--bg:#f4f5fb;--card:#fff;--ink:#1c1e3a;--muted:#6a6f8e;--line:#e3e5f0;--accent:#4f46e5;--accent2:#0ea5e9;
			--pass:#16a34a;--pass-bg:#e8f7ee;--fail:#dc2626;--fail-bg:#fdecec;--skip:#d97706;--skip-bg:#fdf3e2;--code:#f1f2f8;--hover:#f8f8fd;
			--shadow:0 1px 2px rgba(20,22,60,.06),0 4px 16px rgba(20,22,60,.05);--lift:0 2px 4px rgba(20,22,60,.06),0 10px 28px rgba(20,22,60,.10)}
			/* Dark theme: the same variables with dark values (set by the theme button) */
			[data-theme=dark]{--bg:#0f1020;--card:#181a30;--ink:#e7e8f6;--muted:#9a9dbb;--line:#2a2d4a;--accent:#818cf8;--accent2:#38bdf8;
			--pass:#4ade80;--pass-bg:#12301f;--fail:#f87171;--fail-bg:#3a1618;--skip:#fbbf24;--skip-bg:#33270e;--code:#11132a;--hover:#1e2140;
			--shadow:none;--lift:0 8px 24px rgba(0,0,0,.35)}
			/* Widths include padding and border */
			*{box-sizing:border-box}
			/* Page background, text colour and font */
			body{margin:0;background:var(--bg);color:var(--ink);font:15px/1.5 Inter,system-ui,-apple-system,"Segoe UI",sans-serif}
			/* Monospace font for code and traces */
			code,pre{font-family:"JetBrains Mono",ui-monospace,Consolas,monospace}
			/* Header (slowly drifting gradient), summary panel and environment facts */
			/* The gradient is 3x wider than the header; the animation slides it back and forth */
			header{background:linear-gradient(120deg,#312e81,#4f46e5,#0ea5e9,#4f46e5,#312e81);background-size:300% 300%;
			animation:drift 18s ease-in-out infinite;color:#fff;padding:32px 24px 88px}
			@keyframes drift{0%,100%{background-position:0% 50%}50%{background-position:100% 50%}}
			/* Centred content column */
			.wrap{max-width:1120px;margin:0 auto}
			/* Title on the left, verdict on the right */
			.top{display:flex;justify-content:space-between;gap:16px;align-items:flex-start;flex-wrap:wrap}
			/* Small caps line above the title */
			.eyebrow{font-size:12px;letter-spacing:.12em;text-transform:uppercase;opacity:.8}
			/* Suite name */
			h1{margin:4px 0 6px;font-size:28px;font-weight:700}
			/* Start time and duration */
			.meta{opacity:.85;font-size:14px}
			/* Verdict badge with a coloured dot (::before) */
			.verdict{display:inline-flex;align-items:center;gap:8px;padding:8px 14px;border-radius:999px;font-weight:600;background:rgba(255,255,255,.16);border:1px solid rgba(255,255,255,.3);backdrop-filter:blur(4px)}
			.verdict::before{content:"";width:10px;height:10px;border-radius:50%;background:#4ade80;box-shadow:0 0 0 3px rgba(74,222,128,.3)}
			/* Red, pulsing dot on failure; amber dot when nothing passed */
			.verdict.failed::before{background:#f87171;animation:pulse 1.8s ease-out infinite}
			.verdict.skipped::before{background:#fbbf24;box-shadow:0 0 0 3px rgba(251,191,36,.3)}
			@keyframes pulse{0%{box-shadow:0 0 0 0 rgba(248,113,113,.7)}100%{box-shadow:0 0 0 12px rgba(248,113,113,0)}}
			/* The content overlaps the bottom of the header */
			main{margin-top:-64px;padding:0 24px 48px}
			/* The first blocks rise into view one after another */
			main>*{animation:rise .5s ease both}main>:nth-child(2){animation-delay:.08s}main>:nth-child(3){animation-delay:.16s}
			@keyframes rise{from{opacity:0;transform:translateY(8px)}to{opacity:1;transform:none}}
			/* White rounded box */
			.panel{background:var(--card);border:1px solid var(--line);border-radius:16px;box-shadow:var(--shadow)}
			/* Ring on the left, numbers on the right */
			.summary{display:grid;grid-template-columns:auto 1fr;gap:28px;padding:24px;align-items:center}
			/* Pass-rate ring: an SVG circle whose dash length is the pass rate; rotated so it starts at the top */
			.ring{position:relative;width:132px;height:132px}
			.ring svg{transform:rotate(-90deg)}
			.arc{transition:stroke-dasharray 1.2s cubic-bezier(.2,.8,.2,1)}
			/* The percentage in the middle of the ring */
			.ring .val{position:absolute;inset:0;display:grid;place-content:center;text-align:center}
			.ring b{font-size:30px;line-height:1}.ring small{color:var(--muted);font-size:12px}
			/* Four number boxes: total, passed, failed, skipped */
			.stats{display:grid;grid-template-columns:repeat(4,1fr);gap:12px}
			.stat{padding:12px 14px;border-radius:12px;background:var(--code);transition:transform .2s}
			.stat:hover{transform:translateY(-2px)}
			.stat span{display:block;color:var(--muted);font-size:12px;text-transform:uppercase;letter-spacing:.06em}
			.stat b{font-size:26px;font-variant-numeric:tabular-nums}.stat.p b{color:var(--pass)}.stat.f b{color:var(--fail)}.stat.s b{color:var(--skip)}
			/* Result bars (the big one under the numbers and the small one per class) */
			.stack,.gbar{display:flex;border-radius:999px;overflow:hidden;background:var(--line)}
			.stack{height:10px;margin-top:16px}
			.stack i,.gbar i{display:block;transition:width 1s cubic-bezier(.2,.8,.2,1)}
			/* Bar part colours, limited to the bars (the stat boxes also use .p/.f/.s) */
			:is(.stack,.gbar) .p{background:var(--pass)}:is(.stack,.gbar) .f{background:var(--fail)}:is(.stack,.gbar) .s{background:var(--skip)}
			/* Environment facts in as many columns as fit */
			dl.env{display:grid;grid-template-columns:repeat(auto-fit,minmax(170px,1fr));gap:4px 20px;margin:0;padding:16px 24px;border-top:1px solid var(--line)}
			dl.env dt{color:var(--muted);font-size:12px}dl.env dd{margin:0;font-weight:500;overflow-wrap:anywhere}
			/* Toolbar: search, status filter and buttons; stays at the top while scrolling */
			.toolbar{position:sticky;top:0;z-index:5;display:flex;flex-wrap:wrap;gap:10px;align-items:center;margin:20px 0;padding:12px;background:var(--bg)}
			/* Search box takes the free space; a glow when focused */
			.toolbar input{flex:1 1 220px;padding:9px 12px;border-radius:10px;border:1px solid var(--line);background:var(--card);color:var(--ink);font:inherit;transition:border-color .2s,box-shadow .2s}
			.toolbar input:focus{outline:none;border-color:var(--accent);box-shadow:0 0 0 3px color-mix(in srgb,var(--accent) 20%,transparent)}
			/* Segmented status filter (All / Passed / Failed / Skipped) and the plain buttons */
			.seg{display:inline-flex;border:1px solid var(--line);border-radius:10px;overflow:hidden;background:var(--card)}
			.seg button,.btn{border:0;background:var(--card);color:var(--ink);padding:8px 12px;font:inherit;font-size:14px;cursor:pointer;transition:background .2s,color .2s}
			.seg button:hover,.btn:hover{background:var(--hover)}
			.seg button+button{border-left:1px solid var(--line)}
			/* The active filter */
			.seg button[aria-pressed=true]{background:var(--accent);color:#fff}
			.btn{border:1px solid var(--line);border-radius:10px}
			/* Class groups: a summary row that opens to the test cards; the left edge shows the worst outcome */
			.group{background:var(--card);border:1px solid var(--line);border-left:4px solid var(--pass);border-radius:14px;margin-bottom:12px;box-shadow:var(--shadow);overflow:hidden}
			.group.failed{border-left-color:var(--fail)}.group.skipped{border-left-color:var(--skip)}
			/* Summary row columns: name, chips, bar, time, arrow ("all:unset" removes the button look) */
			.ghead{all:unset;box-sizing:border-box;width:100%;display:grid;grid-template-columns:minmax(0,1fr) auto 150px 76px auto;gap:16px;align-items:center;padding:14px 18px;cursor:pointer;transition:background .2s}
			.ghead:hover{background:var(--hover)}
			/* Visible focus ring for keyboard users */
			.ghead:focus-visible{outline:2px solid var(--accent);outline-offset:-2px}
			.gname{font-weight:600}.gname small{display:block;color:var(--muted);font-weight:400;font-size:12px}
			/* Outcome chips */
			.chips{display:flex;gap:6px;flex-wrap:wrap;justify-content:flex-end}
			.chip{font-size:12px;font-weight:600;padding:2px 9px;border-radius:999px;white-space:nowrap}
			.chip.passed{color:var(--pass);background:var(--pass-bg)}.chip.failed{color:var(--fail);background:var(--fail-bg)}.chip.skipped{color:var(--skip);background:var(--skip-bg)}
			.gbar{height:8px}
			.gtime{color:var(--muted);font-size:13px;white-space:nowrap;text-align:right;font-variant-numeric:tabular-nums}
			/* Smooth open and close: the row height animates from 0 to the content's height */
			.gbody,.body{display:grid;grid-template-rows:0fr;transition:grid-template-rows .35s ease}
			.group.open .gbody,.test[open] .body{grid-template-rows:1fr}
			/* Hides the content while the row is (almost) 0 high */
			.clip{overflow:hidden;min-height:0}
			.glist{padding:2px 14px 14px}
			/* Test cards: they fade in one after another when their group opens, and lift on hover */
			.test{background:var(--card);border:1px solid var(--line);border-left:4px solid var(--pass);border-radius:12px;margin-top:10px;transition:transform .2s,box-shadow .2s}
			/* --i is the card's position, set by renderTest: 40 ms more delay per card */
			.group.open .test{animation:rise .35s ease both;animation-delay:calc(var(--i) * 40ms)}
			.test:hover{transform:translateY(-1px);box-shadow:var(--lift)}
			.test.failed{border-left-color:var(--fail)}.test.skipped{border-left-color:var(--skip)}
			/* Card head columns: pill, name, tags, time, arrow */
			.head{all:unset;box-sizing:border-box;width:100%;display:grid;grid-template-columns:auto 1fr auto auto auto;gap:14px;align-items:center;padding:11px 16px;cursor:pointer}
			.head:focus-visible{outline:2px solid var(--accent);outline-offset:-2px}
			/* Status pill */
			.pill{font-size:11px;font-weight:700;text-transform:uppercase;letter-spacing:.06em;padding:3px 8px;border-radius:6px}
			.pill.passed{color:var(--pass);background:var(--pass-bg)}.pill.failed{color:var(--fail);background:var(--fail-bg)}.pill.skipped{color:var(--skip);background:var(--skip-bg)}
			/* Title and method name (min-width:0 lets long names wrap inside the grid) */
			.name{min-width:0}.name strong{display:block;font-weight:600}.name code{color:var(--muted);font-size:12px;overflow-wrap:anywhere}
			/* @group tags */
			.tag{font-size:11px;color:var(--accent);background:var(--code);padding:2px 7px;border-radius:999px;margin-left:4px;white-space:nowrap}
			/* Duration bar and time */
			.time{display:flex;gap:8px;align-items:center;color:var(--muted);font-size:13px;white-space:nowrap}
			.bar{width:70px;height:6px;border-radius:999px;background:var(--line);overflow:hidden}.bar i{display:block;height:100%;background:linear-gradient(90deg,var(--accent),var(--accent2))}
			/* Arrow drawn from two borders; turns down when open */
			.chev{width:8px;height:8px;border-right:2px solid var(--muted);border-bottom:2px solid var(--muted);transform:rotate(-45deg);transition:transform .25s}
			.test[open] .chev,.group.open>.ghead .chev{transform:rotate(45deg)}
			.pad{padding:4px 20px 18px;border-top:1px solid var(--line)}
			/* Card body: steps, error, stack trace and screenshot */
			.steps{margin:14px 0;padding-left:22px}.steps li{padding:3px 0}.steps li::marker{color:var(--muted);font-size:12px}
			/* Quoted values in steps, made by highlight() */
			q{quotes:none;font-family:"JetBrains Mono",ui-monospace,monospace;font-size:13px;background:var(--code);color:var(--accent);padding:1px 6px;border-radius:5px}
			/* Red error box, amber skip box */
			.error,.skip{border-radius:10px;padding:12px 14px;margin:12px 0;background:var(--fail-bg)}
			.skip{background:var(--skip-bg)}
			.error-title{font-weight:600;color:var(--fail)}.skip .error-title{color:var(--skip)}
			/* Long messages and traces wrap instead of scrolling sideways */
			pre{white-space:pre-wrap;overflow-wrap:anywhere;margin:6px 0 0;font-size:12.5px}
			details summary{cursor:pointer;color:var(--muted);font-size:13px;margin-top:8px}
			.trace{background:var(--code);padding:10px;border-radius:8px;max-height:320px;overflow:auto}
			/* Failure screenshot, slightly zoomed on hover */
			.shot{margin:12px 0 0}.shot img{max-width:100%;border:1px solid var(--line);border-radius:10px;display:block;transition:transform .25s}
			.shot a:hover img{transform:scale(1.01)}
			.shot figcaption{color:var(--muted);font-size:12px;margin-top:6px}
			.muted{color:var(--muted)}
			/* "No tests match the filter." (shown by the script) */
			.empty{display:none;text-align:center;color:var(--muted);padding:32px}
			/* Full-screen dark overlay for an enlarged screenshot */
			.lightbox{position:fixed;inset:0;background:rgba(0,0,0,.8);display:none;place-content:center;padding:24px;z-index:10;cursor:zoom-out;animation:fade .2s ease}
			@keyframes fade{from{opacity:0}}
			.lightbox img{max-width:96vw;max-height:92vh;border-radius:8px}
			footer{text-align:center;color:var(--muted);font-size:12px;padding:0 0 32px}
			/* Small screens, printing, and readers who asked their system for less motion */
			/* Phones: one column summary, fewer columns in the rows */
			@media (max-width:720px){.summary{grid-template-columns:1fr;justify-items:center}.stats{grid-template-columns:repeat(2,1fr);width:100%}
			.ghead{grid-template-columns:minmax(0,1fr) auto auto}.gbar,.gtime{display:none}
			.head{grid-template-columns:auto 1fr auto}.tags,.bar{display:none}}
			/* Print: no animation or toolbar, everything open, no card split over two pages */
			@media print{header{animation:none;background:#312e81;-webkit-print-color-adjust:exact;print-color-adjust:exact}.toolbar,.chev{display:none}
			.gbody,.body{grid-template-rows:1fr!important}.test,.group{break-inside:avoid}}
			/* Reduced motion: switch off every animation and transition */
			@media (prefers-reduced-motion:reduce){*,*::before,*::after{animation:none!important;transition:none!important}}
			</style>
			</head>
			<body>
			<!-- Header: suite name, start time, duration and the verdict badge -->
			<header><div class="wrap top">
			<div><div class="eyebrow">Selenium · TestNG · OrangeHRM</div><h1>{{suite}}</h1>
			<div class="meta">Started {{started}} · took {{duration}}</div></div>
			<div class="verdict {{verdict}}">{{verdictText}}</div>
			</div></header>
			<main class="wrap">
			<!-- Summary panel: pass-rate ring, the four numbers, the result bar and the environment -->
			<section class="panel">
			<div class="summary">
			<div class="ring" role="img" aria-label="Pass rate {{rate}} percent">
			<!-- r=15.9155 gives a circumference of 100, so the dash length is the percentage -->
			<svg width="132" height="132" viewBox="0 0 36 36"><circle cx="18" cy="18" r="15.9155" fill="none" stroke="var(--line)" stroke-width="3.2"/>
			<circle class="arc" cx="18" cy="18" r="15.9155" fill="none" stroke="var(--pass)" stroke-width="3.2" stroke-linecap="round" style="stroke-dasharray:{{dash}} 100"/></svg>
			<!-- data-count: the number the intro animation counts up to -->
			<div class="val"><b data-count="{{rate}}" data-suffix="%">{{rate}}%</b><small>pass rate</small></div></div>
			<div><div class="stats">
			<div class="stat"><span>Total</span><b data-count="{{total}}">{{total}}</b></div>
			<div class="stat p"><span>Passed</span><b data-count="{{passed}}">{{passed}}</b></div>
			<div class="stat f"><span>Failed</span><b data-count="{{failed}}">{{failed}}</b></div>
			<div class="stat s"><span>Skipped</span><b data-count="{{skipped}}">{{skipped}}</b></div></div>
			<div class="stack" aria-hidden="true"><i class="p" style="width:{{pPassed}}%"></i><i class="f" style="width:{{pFailed}}%"></i><i class="s" style="width:{{pSkipped}}%"></i></div></div>
			</div>
			<dl class="env">{{env}}</dl>
			</section>
			<!-- Toolbar: search, status filter, expand/collapse all, theme -->
			<div class="toolbar">
			<input id="q" type="search" placeholder="Search tests, classes or @groups" aria-label="Search tests">
			<div class="seg" role="group" aria-label="Filter by status">
			<button data-f="all" aria-pressed="true">All</button><button data-f="passed" aria-pressed="false">Passed</button>
			<button data-f="failed" aria-pressed="false">Failed</button><button data-f="skipped" aria-pressed="false">Skipped</button></div>
			<button class="btn" id="expand">Expand all</button>
			<button class="btn" id="theme" aria-label="Toggle dark mode">Dark mode</button>
			</div>
			<!-- The class groups with their test cards -->
			<div id="tests">{{tests}}</div>
			<p class="empty" id="empty">No tests match the filter.</p>
			</main>
			<footer>Generated by HtmlReportListener</footer>
			<!-- Overlay for an enlarged screenshot -->
			<div class="lightbox" id="lightbox"><img alt="Screenshot"></div>
			<script>
			// Everything runs inside one function, so no names leak into the page
			(function(){
			// All test cards, all class groups, the active status filter and the search box
			var tests=[].slice.call(document.querySelectorAll('.test')),groups=[].slice.call(document.querySelectorAll('.group')),filter='all',q=document.getElementById('q');
			// true when the reader's system asks for less motion
			var calm=window.matchMedia&&matchMedia('(prefers-reduced-motion: reduce)').matches;
			// Open or close a test card on click and keep aria-expanded in step
			function syncTest(t){t.querySelector('.head').setAttribute('aria-expanded',t.hasAttribute('open'))}
			tests.forEach(function(t){syncTest(t);t.querySelector('.head').addEventListener('click',function(){t.toggleAttribute('open');syncTest(t)})});
			// Open or close a class group; a click remembers the reader's choice
			function setGroup(g,open){g.classList.toggle('open',open);g.querySelector('.ghead').setAttribute('aria-expanded',open)}
			groups.forEach(function(g){g.querySelector('.ghead').addEventListener('click',function(){var open=!g.classList.contains('open');g.dataset.user=open?'1':'0';setGroup(g,open)})});
			// Search and status filter: groups with a match open, groups without one are hidden;
			// with no filter, each group goes back to the reader's choice or its default
			function apply(){var term=q.value.trim().toLowerCase(),active=term!==''||filter!=='all',shown=0;
			// Show a card only if it has the chosen status and its search text contains the term
			tests.forEach(function(t){var ok=(filter==='all'||t.dataset.status===filter)&&t.dataset.search.indexOf(term)>=0;t.style.display=ok?'':'none';if(ok)shown++});
			// Hide a group with no visible card; open or restore the others
			groups.forEach(function(g){var any=[].some.call(g.querySelectorAll('.test'),function(t){return t.style.display!=='none'});g.style.display=any?'':'none';
			setGroup(g,active?any:(g.dataset.user?g.dataset.user==='1':g.dataset.default==='open'))});
			// "No tests match the filter." when nothing is shown
			document.getElementById('empty').style.display=shown?'none':'block'}
			// Filter again on every key press in the search box
			q.addEventListener('input',apply);
			// A filter button becomes the active one, then the list is filtered
			document.querySelectorAll('.seg button').forEach(function(b){b.addEventListener('click',function(){filter=b.dataset.f;
			document.querySelectorAll('.seg button').forEach(function(x){x.setAttribute('aria-pressed',x===b)});apply()})});
			// Expand all / Collapse all: every group and every card
			var expand=document.getElementById('expand');
			expand.addEventListener('click',function(){var open=expand.textContent==='Expand all';
			groups.forEach(function(g){g.dataset.user=open?'1':'0';setGroup(g,open)});
			tests.forEach(function(t){t.toggleAttribute('open',open);syncTest(t)});expand.textContent=open?'Collapse all':'Expand all'});
			// Light/dark theme: the saved choice, else the system setting
			var root=document.documentElement,theme=document.getElementById('theme');
			// Set the theme, update the button text and remember the choice (try: storage may be blocked for file:// pages)
			function setTheme(d){root.dataset.theme=d?'dark':'light';theme.textContent=d?'Light mode':'Dark mode';try{localStorage.setItem('report-theme',root.dataset.theme)}catch(e){}}
			var saved=null;try{saved=localStorage.getItem('report-theme')}catch(e){}
			setTheme(saved?saved==='dark':window.matchMedia&&matchMedia('(prefers-color-scheme: dark)').matches);
			theme.addEventListener('click',function(){setTheme(root.dataset.theme!=='dark')});
			// Click a screenshot to see it full size; click again to close
			var box=document.getElementById('lightbox');
			document.querySelectorAll('.zoom').forEach(function(a){a.addEventListener('click',function(e){e.preventDefault();box.querySelector('img').src=a.querySelector('img').src;box.style.display='grid'})});
			box.addEventListener('click',function(){box.style.display='none'});
			// Intro: the ring and bars grow from zero and the numbers count up, once.
			// The page is written with the final values, so it is complete without this.
			if(calm||!window.requestAnimationFrame)return;
			// Remember the final ring length and bar widths, and find the numbers to count up
			var arc=document.querySelector('.arc'),dash=arc.style.strokeDasharray,bars=[].slice.call(document.querySelectorAll('.stack i,.gbar i')),
			widths=bars.map(function(b){return b.style.width}),nums=[].slice.call(document.querySelectorAll('[data-count]'));
			// Start everything at zero
			arc.style.strokeDasharray='0 100';bars.forEach(function(b){b.style.width='0'});
			// Two frames later (after the browser has drawn the zeros) set the final values; the CSS transitions animate the change
			requestAnimationFrame(function(){requestAnimationFrame(function(){arc.style.strokeDasharray=dash;bars.forEach(function(b,i){b.style.width=widths[i]})})});
			// Count the numbers up over 900 ms, easing out (fast at first, slow at the end)
			var t0=null;
			function count(now){if(t0===null)t0=now;var p=Math.min(1,(now-t0)/900),e=1-Math.pow(1-p,3);
			nums.forEach(function(n){n.textContent=Math.round(n.dataset.count*e)+(n.dataset.suffix||'')});if(p<1)requestAnimationFrame(count)}
			requestAnimationFrame(count);
			})();
			</script>
			</body>
			</html>
			""";
}
