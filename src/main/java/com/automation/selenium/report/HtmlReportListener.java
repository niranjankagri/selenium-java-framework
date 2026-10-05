package com.automation.selenium.report;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.xml.XmlSuite;

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
 */
public class HtmlReportListener implements IReporter {

	/** Result attribute holding the Base64 PNG screenshot of a failed test. */
	public static final String SCREENSHOT_ATTRIBUTE = "screenshot";

	// Name of the report file in TestNG's output directory
	static final String FILE_NAME = "selenium-test-report.html";
	// Report title when the run has no suite name of its own (Maven names its suite "Surefire suite")
	private static final String DEFAULT_TITLE = "OrangeHRM UI Suite";
	// Start time format in the report header
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
		List<ITestResult> results = new ArrayList<>();
		String suiteName = DEFAULT_TITLE;
		for (ISuite suite : suites) {
			// Keep a real suite name (testng.xml); Maven's and TestNG's default names are replaced
			if (!suite.getName().startsWith("Surefire") && !suite.getName().startsWith("Default")) {
				suiteName = suite.getName();
			}
			// Every result of every <test>, whatever its status
			for (ISuiteResult suiteResult : suite.getResults().values()) {
				ITestContext context = suiteResult.getTestContext();
				results.addAll(context.getPassedTests().getAllResults());
				results.addAll(context.getFailedTests().getAllResults());
				results.addAll(context.getFailedButWithinSuccessPercentageTests().getAllResults());
				results.addAll(context.getSkippedTests().getAllResults());
			}
		}
		// The report lists the tests in the order they ran
		results.sort(Comparator.comparingLong(ITestResult::getStartMillis));
		Path file = Path.of(outputDirectory, FILE_NAME);
		try {
			Files.createDirectories(file.getParent());
			Files.writeString(file, render(suiteName, results), StandardCharsets.UTF_8);
			System.out.println("HTML report: " + file.toAbsolutePath());
		} catch (IOException e) {
			throw new UncheckedIOException("Could not write " + file, e);
		}
	}

	/**
	 * @param suiteName the suite name for the title.
	 * @param results   every test result, in start order.
	 * @return String   the complete HTML page.
	 */
	private String render(String suiteName, List<ITestResult> results) {
		// Counts, pass rate and run time for the summary panel
		long passed = count(results, "passed");
		long failed = count(results, "failed");
		long skipped = results.size() - passed - failed;
		int rate = results.isEmpty() ? 0 : (int) Math.round(passed * 100.0 / results.size());
		long start = results.stream().mapToLong(ITestResult::getStartMillis).min().orElse(System.currentTimeMillis());
		long end = results.stream().mapToLong(ITestResult::getEndMillis).max().orElse(start);
		// The longest test fills its duration bar; at least 1 so there is no division by zero
		long longest = Math.max(1, results.stream().mapToLong(r -> r.getEndMillis() - r.getStartMillis()).max().orElse(1));
		String verdict = failed > 0 ? "failed" : results.isEmpty() || passed == 0 ? "skipped" : "passed";

		// One section per test class, tests in run order inside it
		Map<String, List<ITestResult>> byClass = results.stream().collect(Collectors.groupingBy(
				r -> r.getTestClass().getRealClass().getSimpleName(), LinkedHashMap::new, Collectors.toList()));
		// Classes with a failure or skip come first, the rest by name, so the order is the same every run
		List<Map.Entry<String, List<ITestResult>>> groups = new ArrayList<>(byClass.entrySet());
		groups.sort(Comparator.comparing((Map.Entry<String, List<ITestResult>> g) -> count(g.getValue(), "passed") == g.getValue().size())
				.thenComparing(Map.Entry::getKey));

		StringBuilder classes = new StringBuilder();
		int groupId = 0;
		int testId = 0;
		for (Map.Entry<String, List<ITestResult>> group : groups) {
			classes.append(renderGroup(group.getKey(), group.getValue(), groupId++, testId, longest));
			testId += group.getValue().size();
		}

		// Fill the page template; the ring's circle has a circumference of 100,
		// so the pass rate is also its dash length
		return TEMPLATE
				.replace("{{suite}}", esc(suiteName))
				.replace("{{verdict}}", verdict)
				.replace("{{verdictText}}", switch (verdict) {
				case "failed" -> failed + (failed == 1 ? " test failed" : " tests failed");
				case "passed" -> "All tests passed" + (skipped > 0 ? " (" + skipped + " skipped)" : "");
				default -> "No test passed";
				})
				.replace("{{started}}", TIME.format(Instant.ofEpochMilli(start)))
				.replace("{{duration}}", duration(end - start))
				.replace("{{total}}", String.valueOf(results.size()))
				.replace("{{passed}}", String.valueOf(passed))
				.replace("{{failed}}", String.valueOf(failed))
				.replace("{{skipped}}", String.valueOf(skipped))
				.replace("{{rate}}", String.valueOf(rate))
				.replace("{{dash}}", String.valueOf(rate))
				.replace("{{pPassed}}", percent(passed, results.size()))
				.replace("{{pFailed}}", percent(failed, results.size()))
				.replace("{{pSkipped}}", percent(skipped, results.size()))
				.replace("{{env}}", environment())
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
		long passed = count(results, "passed");
		long failed = count(results, "failed");
		long skipped = results.size() - passed - failed;
		long millis = results.stream().mapToLong(r -> r.getEndMillis() - r.getStartMillis()).sum();
		// The worst outcome colours the section and decides whether it starts open
		String worst = failed > 0 ? "failed" : skipped > 0 ? "skipped" : "passed";
		boolean open = !worst.equals("passed");

		// A chip per outcome that occurred, e.g. "7 passed", "2 failed"
		StringBuilder chips = new StringBuilder();
		if (passed > 0) {
			chips.append("<span class=\"chip passed\">").append(passed).append(" passed</span>");
		}
		if (failed > 0) {
			chips.append("<span class=\"chip failed\">").append(failed).append(" failed</span>");
		}
		if (skipped > 0) {
			chips.append("<span class=\"chip skipped\">").append(skipped).append(" skipped</span>");
		}
		StringBuilder cards = new StringBuilder();
		for (int i = 0; i < results.size(); i++) {
			cards.append(renderTest(results.get(i), firstId + i, i, longest));
		}

		return "<section class=\"group " + worst + (open ? " open" : "") + "\" data-default=\"" + (open ? "open" : "closed") + "\">"
				+ "<button class=\"ghead\" aria-expanded=\"" + open + "\" aria-controls=\"g" + groupId + "\">"
				+ "<span class=\"gname\">" + esc(name) + "<small>" + results.size() + (results.size() == 1 ? " test" : " tests") + "</small></span>"
				+ "<span class=\"chips\">" + chips + "</span>"
				+ "<span class=\"gbar\" aria-hidden=\"true\"><i class=\"p\" style=\"width:" + percent(passed, results.size())
				+ "%\"></i><i class=\"f\" style=\"width:" + percent(failed, results.size()) + "%\"></i><i class=\"s\" style=\"width:"
				+ percent(skipped, results.size()) + "%\"></i></span>"
				+ "<span class=\"gtime\" title=\"Sum of the test durations\">" + duration(millis) + "</span>"
				+ "<span class=\"chev\" aria-hidden=\"true\"></span></button>"
				+ "<div class=\"gbody\" id=\"g" + groupId + "\"><div class=\"clip\"><div class=\"glist\">" + cards + "</div></div></div></section>";
	}

	/**
	 * @param result   one test result.
	 * @param id       a unique number for element ids.
	 * @param index    the card's position in its class, for the staggered fade-in.
	 * @param longest  the longest test duration, for the duration bar.
	 * @return String  the HTML card of the test.
	 */
	private String renderTest(ITestResult result, int id, int index, long longest) {
		String status = status(result);
		long millis = result.getEndMillis() - result.getStartMillis();
		// Title: the @Test description, else the method name as a sentence;
		// a data-provider row adds its first parameter (the case name)
		String description = result.getMethod().getDescription();
		String title = description == null || description.isBlank() ? readable(result.getMethod().getMethodName()) : description;
		String params = result.getParameters().length == 0 ? "" : Arrays.stream(result.getParameters()).map(String::valueOf)
				.collect(Collectors.joining(", ", "(", ")"));
		if (result.getParameters().length > 0) {
			title += " — " + result.getParameters()[0];
		}
		// TestNG groups, shown as @tags
		String groups = Arrays.stream(result.getMethod().getGroups()).sorted().distinct()
				.map(g -> "<span class=\"tag\">@" + esc(g) + "</span>").collect(Collectors.joining());

		// Steps recorded with Steps.log during the test
		StringBuilder body = new StringBuilder();
		List<String> steps = Reporter.getOutput(result);
		if (!steps.isEmpty()) {
			body.append("<ol class=\"steps\">");
			for (String step : steps) {
				body.append("<li>").append(highlight(esc(step))).append("</li>");
			}
			body.append("</ol>");
		}
		// Failure or skip reason; the stack trace is shown for failures only
		Throwable error = result.getThrowable();
		if (error != null) {
			String kind = status.equals("skipped") ? "skip" : "error";
			body.append("<div class=\"").append(kind).append("\"><div class=\"error-title\">")
					.append(status.equals("skipped") ? "Skipped: " : "")
					.append(esc(error.getClass().getSimpleName())).append("</div><pre class=\"message\">")
					.append(esc(message(error))).append("</pre>");
			if (!status.equals("skipped")) {
				body.append("<details><summary>Stack trace</summary><pre class=\"trace\">").append(esc(trace(error))).append("</pre></details>");
			}
			body.append("</div>");
		}
		// Screenshot taken by BaseTest when the test failed
		Object screenshot = result.getAttribute(SCREENSHOT_ATTRIBUTE);
		if (screenshot != null) {
			body.append("<figure class=\"shot\"><a href=\"#\" class=\"zoom\" title=\"Click to enlarge\"><img alt=\"Screenshot at failure\" src=\"data:image/png;base64,")
					.append(screenshot).append("\"></a><figcaption>Browser at the moment of failure</figcaption></figure>");
		}
		if (body.isEmpty()) {
			body.append("<p class=\"muted\">No steps recorded.</p>");
		}

		// Text the search box matches; every card starts closed and opens on click
		String search = (title + " " + result.getMethod().getMethodName() + " " + result.getTestClass().getRealClass().getSimpleName()
				+ " " + String.join(" ", result.getMethod().getGroups())).toLowerCase();
		return "<article class=\"test " + status + "\" data-status=\"" + status + "\" data-search=\"" + esc(search) + "\""
				+ " style=\"--i:" + index + "\">"
				+ "<button class=\"head\" aria-expanded=\"false\" aria-controls=\"t" + id + "\">"
				+ "<span class=\"pill " + status + "\">" + status + "</span>"
				+ "<span class=\"name\"><strong>" + esc(title) + "</strong><code>" + esc(result.getMethod().getMethodName() + params) + "</code></span>"
				+ "<span class=\"tags\">" + groups + "</span>"
				+ "<span class=\"time\"><span class=\"bar\"><i style=\"width:" + Math.max(2, millis * 100 / longest) + "%\"></i></span>"
				+ duration(millis) + "</span><span class=\"chev\" aria-hidden=\"true\"></span></button>"
				+ "<div class=\"body\" id=\"t" + id + "\"><div class=\"clip\"><div class=\"pad\">" + body + "</div></div></div></article>";
	}

	/**
	 * @return String the environment facts as definition-list items.
	 */
	private String environment() {
		Map<String, String> facts = new LinkedHashMap<>();
		try {
			Config config = Config.get();
			facts.put("Application", config.baseUrl());
			facts.put("Browser", config.browser() + (config.headless() ? " (headless)" : ""));
		} catch (RuntimeException | LinkageError e) {
			// The report is still useful without the test settings
		}
		facts.put("Java", System.getProperty("java.version"));
		facts.put("OS", System.getProperty("os.name") + " " + System.getProperty("os.version"));
		facts.put("User", System.getProperty("user.name"));
		return facts.entrySet().stream()
				.map(f -> "<div><dt>" + esc(f.getKey()) + "</dt><dd>" + esc(f.getValue()) + "</dd></div>")
				.collect(Collectors.joining());
	}

	/**
	 * @param results  test results.
	 * @param status   passed, failed or skipped.
	 * @return long    how many of the results have that status.
	 */
	private static long count(List<ITestResult> results, String status) {
		return results.stream().filter(r -> status(r).equals(status)).count();
	}

	/**
	 * @param result   one test result.
	 * @return String  passed, failed or skipped.
	 */
	private static String status(ITestResult result) {
		return switch (result.getStatus()) {
		case ITestResult.SUCCESS -> "passed";
		case ITestResult.FAILURE, ITestResult.SUCCESS_PERCENTAGE_FAILURE -> "failed";
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
		return escaped.replaceAll("&quot;(.*?)&quot;", "<q>$1</q>");
	}

	/**
	 * @param error    the failure or skip reason.
	 * @return String  its message (the exception itself if it has none), without
	 *                 the " on instance Class@1a2b3c" object ids TestNG adds to
	 *                 the message of a test skipped because a dependency failed.
	 */
	private static String message(Throwable error) {
		String message = error.getMessage() == null ? error.toString() : error.getMessage();
		return message.replaceAll(" on instance [\\w.$]+@\\p{XDigit}+", "");
	}

	/**
	 * @param error    the failure.
	 * @return String  the stack trace without TestNG, reflection and Maven frames.
	 */
	private static String trace(Throwable error) {
		StringBuilder out = new StringBuilder();
		// Walk the cause chain, stopping at a throwable that is its own cause
		for (Throwable t = error; t != null; t = t.getCause() == t ? null : t.getCause()) {
			out.append(t == error ? "" : "Caused by: ").append(t).append('\n');
			for (StackTraceElement element : t.getStackTrace()) {
				String line = element.toString();
				if (NOISE.stream().noneMatch(line::startsWith)) {
					out.append("    at ").append(line).append('\n');
				}
			}
		}
		return out.toString().trim();
	}

	/**
	 * @param name     a method name, e.g. "adminCanLogIn".
	 * @return String  a sentence, e.g. "Admin can log in".
	 */
	private static String readable(String name) {
		String words = name.replaceAll("([a-z0-9])([A-Z])", "$1 $2").toLowerCase();
		return Character.toUpperCase(words.charAt(0)) + words.substring(1);
	}

	/**
	 * @param millis   a duration in milliseconds.
	 * @return String  e.g. "850 ms", "12.4 s" or "2 min 05 s".
	 */
	private static String duration(long millis) {
		if (millis < 1000) {
			return millis + " ms";
		}
		if (millis < 60_000) {
			return String.format(Locale.ROOT, "%.1f s", millis / 1000.0);
		}
		return String.format("%d min %02d s", millis / 60_000, millis / 1000 % 60);
	}

	/**
	 * @param part     a count.
	 * @param total    the total count.
	 * @return String  the share in percent, for CSS widths.
	 */
	private static String percent(long part, long total) {
		return total == 0 ? "0" : String.format(Locale.ROOT, "%.2f", part * 100.0 / total);
	}

	/**
	 * @param text     any text.
	 * @return String  the text, safe to put into HTML content and attributes.
	 */
	private static String esc(String text) {
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
	}

	// The page; {{name}} placeholders are filled in by render()
	private static final String TEMPLATE = """
			<!doctype html>
			<html lang="en">
			<head>
			<meta charset="utf-8">
			<meta name="viewport" content="width=device-width, initial-scale=1">
			<title>{{suite}} – Selenium test report</title>
			<link rel="preconnect" href="https://fonts.googleapis.com">
			<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500&display=swap" rel="stylesheet">
			<style>
			/* Colours: light theme, then the dark theme */
			:root{--bg:#f4f5fb;--card:#fff;--ink:#1c1e3a;--muted:#6a6f8e;--line:#e3e5f0;--accent:#4f46e5;--accent2:#0ea5e9;
			--pass:#16a34a;--pass-bg:#e8f7ee;--fail:#dc2626;--fail-bg:#fdecec;--skip:#d97706;--skip-bg:#fdf3e2;--code:#f1f2f8;--hover:#f8f8fd;
			--shadow:0 1px 2px rgba(20,22,60,.06),0 4px 16px rgba(20,22,60,.05);--lift:0 2px 4px rgba(20,22,60,.06),0 10px 28px rgba(20,22,60,.10)}
			[data-theme=dark]{--bg:#0f1020;--card:#181a30;--ink:#e7e8f6;--muted:#9a9dbb;--line:#2a2d4a;--accent:#818cf8;--accent2:#38bdf8;
			--pass:#4ade80;--pass-bg:#12301f;--fail:#f87171;--fail-bg:#3a1618;--skip:#fbbf24;--skip-bg:#33270e;--code:#11132a;--hover:#1e2140;
			--shadow:none;--lift:0 8px 24px rgba(0,0,0,.35)}
			*{box-sizing:border-box}
			body{margin:0;background:var(--bg);color:var(--ink);font:15px/1.5 Inter,system-ui,-apple-system,"Segoe UI",sans-serif}
			code,pre{font-family:"JetBrains Mono",ui-monospace,Consolas,monospace}
			/* Header (slowly drifting gradient), summary panel and environment facts */
			header{background:linear-gradient(120deg,#312e81,#4f46e5,#0ea5e9,#4f46e5,#312e81);background-size:300% 300%;
			animation:drift 18s ease-in-out infinite;color:#fff;padding:32px 24px 88px}
			@keyframes drift{0%,100%{background-position:0% 50%}50%{background-position:100% 50%}}
			.wrap{max-width:1120px;margin:0 auto}
			.top{display:flex;justify-content:space-between;gap:16px;align-items:flex-start;flex-wrap:wrap}
			.eyebrow{font-size:12px;letter-spacing:.12em;text-transform:uppercase;opacity:.8}
			h1{margin:4px 0 6px;font-size:28px;font-weight:700}
			.meta{opacity:.85;font-size:14px}
			.verdict{display:inline-flex;align-items:center;gap:8px;padding:8px 14px;border-radius:999px;font-weight:600;background:rgba(255,255,255,.16);border:1px solid rgba(255,255,255,.3);backdrop-filter:blur(4px)}
			.verdict::before{content:"";width:10px;height:10px;border-radius:50%;background:#4ade80;box-shadow:0 0 0 3px rgba(74,222,128,.3)}
			.verdict.failed::before{background:#f87171;animation:pulse 1.8s ease-out infinite}
			.verdict.skipped::before{background:#fbbf24;box-shadow:0 0 0 3px rgba(251,191,36,.3)}
			@keyframes pulse{0%{box-shadow:0 0 0 0 rgba(248,113,113,.7)}100%{box-shadow:0 0 0 12px rgba(248,113,113,0)}}
			main{margin-top:-64px;padding:0 24px 48px}
			main>*{animation:rise .5s ease both}main>:nth-child(2){animation-delay:.08s}main>:nth-child(3){animation-delay:.16s}
			@keyframes rise{from{opacity:0;transform:translateY(8px)}to{opacity:1;transform:none}}
			.panel{background:var(--card);border:1px solid var(--line);border-radius:16px;box-shadow:var(--shadow)}
			.summary{display:grid;grid-template-columns:auto 1fr;gap:28px;padding:24px;align-items:center}
			.ring{position:relative;width:132px;height:132px}
			.ring svg{transform:rotate(-90deg)}
			.arc{transition:stroke-dasharray 1.2s cubic-bezier(.2,.8,.2,1)}
			.ring .val{position:absolute;inset:0;display:grid;place-content:center;text-align:center}
			.ring b{font-size:30px;line-height:1}.ring small{color:var(--muted);font-size:12px}
			.stats{display:grid;grid-template-columns:repeat(4,1fr);gap:12px}
			.stat{padding:12px 14px;border-radius:12px;background:var(--code);transition:transform .2s}
			.stat:hover{transform:translateY(-2px)}
			.stat span{display:block;color:var(--muted);font-size:12px;text-transform:uppercase;letter-spacing:.06em}
			.stat b{font-size:26px;font-variant-numeric:tabular-nums}.stat.p b{color:var(--pass)}.stat.f b{color:var(--fail)}.stat.s b{color:var(--skip)}
			.stack,.gbar{display:flex;border-radius:999px;overflow:hidden;background:var(--line)}
			.stack{height:10px;margin-top:16px}
			.stack i,.gbar i{display:block;transition:width 1s cubic-bezier(.2,.8,.2,1)}
			:is(.stack,.gbar) .p{background:var(--pass)}:is(.stack,.gbar) .f{background:var(--fail)}:is(.stack,.gbar) .s{background:var(--skip)}
			dl.env{display:grid;grid-template-columns:repeat(auto-fit,minmax(170px,1fr));gap:4px 20px;margin:0;padding:16px 24px;border-top:1px solid var(--line)}
			dl.env dt{color:var(--muted);font-size:12px}dl.env dd{margin:0;font-weight:500;overflow-wrap:anywhere}
			/* Toolbar: search, status filter and buttons; stays at the top while scrolling */
			.toolbar{position:sticky;top:0;z-index:5;display:flex;flex-wrap:wrap;gap:10px;align-items:center;margin:20px 0;padding:12px;background:var(--bg)}
			.toolbar input{flex:1 1 220px;padding:9px 12px;border-radius:10px;border:1px solid var(--line);background:var(--card);color:var(--ink);font:inherit;transition:border-color .2s,box-shadow .2s}
			.toolbar input:focus{outline:none;border-color:var(--accent);box-shadow:0 0 0 3px color-mix(in srgb,var(--accent) 20%,transparent)}
			.seg{display:inline-flex;border:1px solid var(--line);border-radius:10px;overflow:hidden;background:var(--card)}
			.seg button,.btn{border:0;background:var(--card);color:var(--ink);padding:8px 12px;font:inherit;font-size:14px;cursor:pointer;transition:background .2s,color .2s}
			.seg button:hover,.btn:hover{background:var(--hover)}
			.seg button+button{border-left:1px solid var(--line)}
			.seg button[aria-pressed=true]{background:var(--accent);color:#fff}
			.btn{border:1px solid var(--line);border-radius:10px}
			/* Class groups: a summary row that opens to the test cards; the left edge shows the worst outcome */
			.group{background:var(--card);border:1px solid var(--line);border-left:4px solid var(--pass);border-radius:14px;margin-bottom:12px;box-shadow:var(--shadow);overflow:hidden}
			.group.failed{border-left-color:var(--fail)}.group.skipped{border-left-color:var(--skip)}
			.ghead{all:unset;box-sizing:border-box;width:100%;display:grid;grid-template-columns:minmax(0,1fr) auto 150px 76px auto;gap:16px;align-items:center;padding:14px 18px;cursor:pointer;transition:background .2s}
			.ghead:hover{background:var(--hover)}
			.ghead:focus-visible{outline:2px solid var(--accent);outline-offset:-2px}
			.gname{font-weight:600}.gname small{display:block;color:var(--muted);font-weight:400;font-size:12px}
			.chips{display:flex;gap:6px;flex-wrap:wrap;justify-content:flex-end}
			.chip{font-size:12px;font-weight:600;padding:2px 9px;border-radius:999px;white-space:nowrap}
			.chip.passed{color:var(--pass);background:var(--pass-bg)}.chip.failed{color:var(--fail);background:var(--fail-bg)}.chip.skipped{color:var(--skip);background:var(--skip-bg)}
			.gbar{height:8px}
			.gtime{color:var(--muted);font-size:13px;white-space:nowrap;text-align:right;font-variant-numeric:tabular-nums}
			/* Smooth open and close: the row height animates from 0 to the content's height */
			.gbody,.body{display:grid;grid-template-rows:0fr;transition:grid-template-rows .35s ease}
			.group.open .gbody,.test[open] .body{grid-template-rows:1fr}
			.clip{overflow:hidden;min-height:0}
			.glist{padding:2px 14px 14px}
			/* Test cards: they fade in one after another when their group opens, and lift on hover */
			.test{background:var(--card);border:1px solid var(--line);border-left:4px solid var(--pass);border-radius:12px;margin-top:10px;transition:transform .2s,box-shadow .2s}
			.group.open .test{animation:rise .35s ease both;animation-delay:calc(var(--i) * 40ms)}
			.test:hover{transform:translateY(-1px);box-shadow:var(--lift)}
			.test.failed{border-left-color:var(--fail)}.test.skipped{border-left-color:var(--skip)}
			.head{all:unset;box-sizing:border-box;width:100%;display:grid;grid-template-columns:auto 1fr auto auto auto;gap:14px;align-items:center;padding:11px 16px;cursor:pointer}
			.head:focus-visible{outline:2px solid var(--accent);outline-offset:-2px}
			.pill{font-size:11px;font-weight:700;text-transform:uppercase;letter-spacing:.06em;padding:3px 8px;border-radius:6px}
			.pill.passed{color:var(--pass);background:var(--pass-bg)}.pill.failed{color:var(--fail);background:var(--fail-bg)}.pill.skipped{color:var(--skip);background:var(--skip-bg)}
			.name{min-width:0}.name strong{display:block;font-weight:600}.name code{color:var(--muted);font-size:12px;overflow-wrap:anywhere}
			.tag{font-size:11px;color:var(--accent);background:var(--code);padding:2px 7px;border-radius:999px;margin-left:4px;white-space:nowrap}
			.time{display:flex;gap:8px;align-items:center;color:var(--muted);font-size:13px;white-space:nowrap}
			.bar{width:70px;height:6px;border-radius:999px;background:var(--line);overflow:hidden}.bar i{display:block;height:100%;background:linear-gradient(90deg,var(--accent),var(--accent2))}
			.chev{width:8px;height:8px;border-right:2px solid var(--muted);border-bottom:2px solid var(--muted);transform:rotate(-45deg);transition:transform .25s}
			.test[open] .chev,.group.open>.ghead .chev{transform:rotate(45deg)}
			.pad{padding:4px 20px 18px;border-top:1px solid var(--line)}
			/* Card body: steps, error, stack trace and screenshot */
			.steps{margin:14px 0;padding-left:22px}.steps li{padding:3px 0}.steps li::marker{color:var(--muted);font-size:12px}
			q{quotes:none;font-family:"JetBrains Mono",ui-monospace,monospace;font-size:13px;background:var(--code);color:var(--accent);padding:1px 6px;border-radius:5px}
			.error,.skip{border-radius:10px;padding:12px 14px;margin:12px 0;background:var(--fail-bg)}
			.skip{background:var(--skip-bg)}
			.error-title{font-weight:600;color:var(--fail)}.skip .error-title{color:var(--skip)}
			pre{white-space:pre-wrap;overflow-wrap:anywhere;margin:6px 0 0;font-size:12.5px}
			details summary{cursor:pointer;color:var(--muted);font-size:13px;margin-top:8px}
			.trace{background:var(--code);padding:10px;border-radius:8px;max-height:320px;overflow:auto}
			.shot{margin:12px 0 0}.shot img{max-width:100%;border:1px solid var(--line);border-radius:10px;display:block;transition:transform .25s}
			.shot a:hover img{transform:scale(1.01)}
			.shot figcaption{color:var(--muted);font-size:12px;margin-top:6px}
			.muted{color:var(--muted)}
			.empty{display:none;text-align:center;color:var(--muted);padding:32px}
			.lightbox{position:fixed;inset:0;background:rgba(0,0,0,.8);display:none;place-content:center;padding:24px;z-index:10;cursor:zoom-out;animation:fade .2s ease}
			@keyframes fade{from{opacity:0}}
			.lightbox img{max-width:96vw;max-height:92vh;border-radius:8px}
			footer{text-align:center;color:var(--muted);font-size:12px;padding:0 0 32px}
			/* Small screens, printing, and readers who asked their system for less motion */
			@media (max-width:720px){.summary{grid-template-columns:1fr;justify-items:center}.stats{grid-template-columns:repeat(2,1fr);width:100%}
			.ghead{grid-template-columns:minmax(0,1fr) auto auto}.gbar,.gtime{display:none}
			.head{grid-template-columns:auto 1fr auto}.tags,.bar{display:none}}
			@media print{header{animation:none;background:#312e81;-webkit-print-color-adjust:exact;print-color-adjust:exact}.toolbar,.chev{display:none}
			.gbody,.body{grid-template-rows:1fr!important}.test,.group{break-inside:avoid}}
			@media (prefers-reduced-motion:reduce){*,*::before,*::after{animation:none!important;transition:none!important}}
			</style>
			</head>
			<body>
			<header><div class="wrap top">
			<div><div class="eyebrow">Selenium · TestNG · OrangeHRM</div><h1>{{suite}}</h1>
			<div class="meta">Started {{started}} · took {{duration}}</div></div>
			<div class="verdict {{verdict}}">{{verdictText}}</div>
			</div></header>
			<main class="wrap">
			<section class="panel">
			<div class="summary">
			<div class="ring" role="img" aria-label="Pass rate {{rate}} percent">
			<svg width="132" height="132" viewBox="0 0 36 36"><circle cx="18" cy="18" r="15.9155" fill="none" stroke="var(--line)" stroke-width="3.2"/>
			<circle class="arc" cx="18" cy="18" r="15.9155" fill="none" stroke="var(--pass)" stroke-width="3.2" stroke-linecap="round" style="stroke-dasharray:{{dash}} 100"/></svg>
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
			<div class="toolbar">
			<input id="q" type="search" placeholder="Search tests, classes or @groups" aria-label="Search tests">
			<div class="seg" role="group" aria-label="Filter by status">
			<button data-f="all" aria-pressed="true">All</button><button data-f="passed" aria-pressed="false">Passed</button>
			<button data-f="failed" aria-pressed="false">Failed</button><button data-f="skipped" aria-pressed="false">Skipped</button></div>
			<button class="btn" id="expand">Expand all</button>
			<button class="btn" id="theme" aria-label="Toggle dark mode">Dark mode</button>
			</div>
			<div id="tests">{{tests}}</div>
			<p class="empty" id="empty">No tests match the filter.</p>
			</main>
			<footer>Generated by HtmlReportListener</footer>
			<div class="lightbox" id="lightbox"><img alt="Screenshot"></div>
			<script>
			(function(){
			var tests=[].slice.call(document.querySelectorAll('.test')),groups=[].slice.call(document.querySelectorAll('.group')),filter='all',q=document.getElementById('q');
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
			tests.forEach(function(t){var ok=(filter==='all'||t.dataset.status===filter)&&t.dataset.search.indexOf(term)>=0;t.style.display=ok?'':'none';if(ok)shown++});
			groups.forEach(function(g){var any=[].some.call(g.querySelectorAll('.test'),function(t){return t.style.display!=='none'});g.style.display=any?'':'none';
			setGroup(g,active?any:(g.dataset.user?g.dataset.user==='1':g.dataset.default==='open'))});
			document.getElementById('empty').style.display=shown?'none':'block'}
			q.addEventListener('input',apply);
			document.querySelectorAll('.seg button').forEach(function(b){b.addEventListener('click',function(){filter=b.dataset.f;
			document.querySelectorAll('.seg button').forEach(function(x){x.setAttribute('aria-pressed',x===b)});apply()})});
			// Expand all / Collapse all: every group and every card
			var expand=document.getElementById('expand');
			expand.addEventListener('click',function(){var open=expand.textContent==='Expand all';
			groups.forEach(function(g){g.dataset.user=open?'1':'0';setGroup(g,open)});
			tests.forEach(function(t){t.toggleAttribute('open',open);syncTest(t)});expand.textContent=open?'Collapse all':'Expand all'});
			// Light/dark theme: the saved choice, else the system setting
			var root=document.documentElement,theme=document.getElementById('theme');
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
			var arc=document.querySelector('.arc'),dash=arc.style.strokeDasharray,bars=[].slice.call(document.querySelectorAll('.stack i,.gbar i')),
			widths=bars.map(function(b){return b.style.width}),nums=[].slice.call(document.querySelectorAll('[data-count]'));
			arc.style.strokeDasharray='0 100';bars.forEach(function(b){b.style.width='0'});
			requestAnimationFrame(function(){requestAnimationFrame(function(){arc.style.strokeDasharray=dash;bars.forEach(function(b,i){b.style.width=widths[i]})})});
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
