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
 * failure and the screenshot taken on failure. It has search, status filters
 * and a light/dark theme, and needs nothing but a browser to open.
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
		long passed = results.stream().filter(r -> status(r).equals("passed")).count();
		long failed = results.stream().filter(r -> status(r).equals("failed")).count();
		long skipped = results.size() - passed - failed;
		int rate = results.isEmpty() ? 0 : (int) Math.round(passed * 100.0 / results.size());
		long start = results.stream().mapToLong(ITestResult::getStartMillis).min().orElse(System.currentTimeMillis());
		long end = results.stream().mapToLong(ITestResult::getEndMillis).max().orElse(start);
		// The longest test fills its duration bar; at least 1 so there is no division by zero
		long longest = Math.max(1, results.stream().mapToLong(r -> r.getEndMillis() - r.getStartMillis()).max().orElse(1));
		String verdict = failed > 0 ? "failed" : results.isEmpty() || passed == 0 ? "skipped" : "passed";

		// One section per test class, in run order
		Map<String, List<ITestResult>> byClass = results.stream().collect(Collectors.groupingBy(
				r -> r.getTestClass().getRealClass().getSimpleName(), LinkedHashMap::new, Collectors.toList()));

		StringBuilder classes = new StringBuilder();
		int id = 0;
		for (Map.Entry<String, List<ITestResult>> entry : byClass.entrySet()) {
			classes.append("<section class=\"group\"><h2>").append(esc(entry.getKey()))
					.append("<span>").append(entry.getValue().size()).append(entry.getValue().size() == 1 ? " test" : " tests")
					.append("</span></h2>");
			for (ITestResult result : entry.getValue()) {
				classes.append(renderTest(result, id++, longest));
			}
			classes.append("</section>");
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
	 * @param result   one test result.
	 * @param id       a unique number for element ids.
	 * @param longest  the longest test duration, for the duration bar.
	 * @return String  the HTML card of the test.
	 */
	private String renderTest(ITestResult result, int id, long longest) {
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

		// Text the search box matches; failed tests start expanded
		String search = (title + " " + result.getMethod().getMethodName() + " " + result.getTestClass().getRealClass().getSimpleName()
				+ " " + String.join(" ", result.getMethod().getGroups())).toLowerCase();
		return "<article class=\"test " + status + "\" data-status=\"" + status + "\" data-search=\"" + esc(search) + "\""
				+ (status.equals("failed") ? " open" : "") + ">"
				+ "<button class=\"head\" aria-expanded=\"false\" aria-controls=\"t" + id + "\">"
				+ "<span class=\"pill " + status + "\">" + status + "</span>"
				+ "<span class=\"name\"><strong>" + esc(title) + "</strong><code>" + esc(result.getMethod().getMethodName() + params) + "</code></span>"
				+ "<span class=\"tags\">" + groups + "</span>"
				+ "<span class=\"time\"><span class=\"bar\"><i style=\"width:" + Math.max(2, millis * 100 / longest) + "%\"></i></span>"
				+ duration(millis) + "</span><span class=\"chev\" aria-hidden=\"true\"></span></button>"
				+ "<div class=\"body\" id=\"t" + id + "\">" + body + "</div></article>";
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
			--pass:#16a34a;--pass-bg:#e8f7ee;--fail:#dc2626;--fail-bg:#fdecec;--skip:#d97706;--skip-bg:#fdf3e2;--code:#f1f2f8;--shadow:0 1px 2px rgba(20,22,60,.06),0 4px 16px rgba(20,22,60,.05)}
			[data-theme=dark]{--bg:#0f1020;--card:#181a30;--ink:#e7e8f6;--muted:#9a9dbb;--line:#2a2d4a;--accent:#818cf8;--accent2:#38bdf8;
			--pass:#4ade80;--pass-bg:#12301f;--fail:#f87171;--fail-bg:#3a1618;--skip:#fbbf24;--skip-bg:#33270e;--code:#11132a;--shadow:none}
			*{box-sizing:border-box}
			body{margin:0;background:var(--bg);color:var(--ink);font:15px/1.5 Inter,system-ui,-apple-system,"Segoe UI",sans-serif}
			code,pre{font-family:"JetBrains Mono",ui-monospace,Consolas,monospace}
			/* Header, summary panel and environment facts */
			header{background:linear-gradient(120deg,#312e81,#4f46e5 55%,#0ea5e9);color:#fff;padding:32px 24px 88px}
			.wrap{max-width:1120px;margin:0 auto}
			.top{display:flex;justify-content:space-between;gap:16px;align-items:flex-start;flex-wrap:wrap}
			.eyebrow{font-size:12px;letter-spacing:.12em;text-transform:uppercase;opacity:.8}
			h1{margin:4px 0 6px;font-size:28px;font-weight:700}
			.meta{opacity:.85;font-size:14px}
			.verdict{display:inline-flex;align-items:center;gap:8px;padding:8px 14px;border-radius:999px;font-weight:600;background:rgba(255,255,255,.16);border:1px solid rgba(255,255,255,.3)}
			.verdict::before{content:"";width:10px;height:10px;border-radius:50%;background:#4ade80;box-shadow:0 0 0 3px rgba(74,222,128,.3)}
			.verdict.failed::before{background:#f87171;box-shadow:0 0 0 3px rgba(248,113,113,.3)}
			.verdict.skipped::before{background:#fbbf24;box-shadow:0 0 0 3px rgba(251,191,36,.3)}
			main{margin-top:-64px;padding:0 24px 48px}
			.panel{background:var(--card);border:1px solid var(--line);border-radius:16px;box-shadow:var(--shadow)}
			.summary{display:grid;grid-template-columns:auto 1fr;gap:28px;padding:24px;align-items:center}
			.ring{position:relative;width:132px;height:132px}
			.ring svg{transform:rotate(-90deg)}
			.ring .val{position:absolute;inset:0;display:grid;place-content:center;text-align:center}
			.ring b{font-size:30px;line-height:1}.ring small{color:var(--muted);font-size:12px}
			.stats{display:grid;grid-template-columns:repeat(4,1fr);gap:12px}
			.stat{padding:12px 14px;border-radius:12px;background:var(--code)}
			.stat span{display:block;color:var(--muted);font-size:12px;text-transform:uppercase;letter-spacing:.06em}
			.stat b{font-size:26px}.stat.p b{color:var(--pass)}.stat.f b{color:var(--fail)}.stat.s b{color:var(--skip)}
			.stack{display:flex;height:10px;border-radius:999px;overflow:hidden;background:var(--line);margin-top:16px}
			.stack i{display:block}.stack .p{background:var(--pass)}.stack .f{background:var(--fail)}.stack .s{background:var(--skip)}
			dl.env{display:grid;grid-template-columns:repeat(auto-fit,minmax(170px,1fr));gap:4px 20px;margin:0;padding:16px 24px;border-top:1px solid var(--line)}
			dl.env dt{color:var(--muted);font-size:12px}dl.env dd{margin:0;font-weight:500;overflow-wrap:anywhere}
			/* Toolbar: search, status filter and buttons; stays at the top while scrolling */
			.toolbar{position:sticky;top:0;z-index:5;display:flex;flex-wrap:wrap;gap:10px;align-items:center;margin:20px 0;padding:12px;background:var(--bg)}
			.toolbar input{flex:1 1 220px;padding:9px 12px;border-radius:10px;border:1px solid var(--line);background:var(--card);color:var(--ink);font:inherit}
			.seg{display:inline-flex;border:1px solid var(--line);border-radius:10px;overflow:hidden;background:var(--card)}
			.seg button,.btn{border:0;background:var(--card);color:var(--ink);padding:8px 12px;font:inherit;font-size:14px;cursor:pointer}
			.seg button+button{border-left:1px solid var(--line)}
			.seg button[aria-pressed=true]{background:var(--accent);color:#fff}
			.btn{border:1px solid var(--line);border-radius:10px}
			/* Test cards, grouped by class */
			.group h2{font-size:15px;margin:24px 0 10px;display:flex;gap:10px;align-items:baseline}
			.group h2 span{font-weight:400;color:var(--muted);font-size:13px}
			.test{background:var(--card);border:1px solid var(--line);border-left:4px solid var(--pass);border-radius:12px;margin-bottom:10px;box-shadow:var(--shadow);overflow:hidden}
			.test.failed{border-left-color:var(--fail)}.test.skipped{border-left-color:var(--skip)}
			.head{all:unset;box-sizing:border-box;width:100%;display:grid;grid-template-columns:auto 1fr auto auto auto;gap:14px;align-items:center;padding:12px 16px;cursor:pointer}
			.head:focus-visible{outline:2px solid var(--accent);outline-offset:-2px}
			.pill{font-size:11px;font-weight:700;text-transform:uppercase;letter-spacing:.06em;padding:3px 8px;border-radius:6px}
			.pill.passed{color:var(--pass);background:var(--pass-bg)}.pill.failed{color:var(--fail);background:var(--fail-bg)}.pill.skipped{color:var(--skip);background:var(--skip-bg)}
			.name{min-width:0}.name strong{display:block;font-weight:600}.name code{color:var(--muted);font-size:12px;overflow-wrap:anywhere}
			.tag{font-size:11px;color:var(--accent);background:var(--code);padding:2px 7px;border-radius:999px;margin-left:4px;white-space:nowrap}
			.time{display:flex;gap:8px;align-items:center;color:var(--muted);font-size:13px;white-space:nowrap}
			.bar{width:70px;height:6px;border-radius:999px;background:var(--line);overflow:hidden}.bar i{display:block;height:100%;background:linear-gradient(90deg,var(--accent),var(--accent2))}
			.chev{width:8px;height:8px;border-right:2px solid var(--muted);border-bottom:2px solid var(--muted);transform:rotate(-45deg);transition:transform .15s}
			.test[open] .chev{transform:rotate(45deg)}
			.body{display:none;padding:4px 20px 18px 20px;border-top:1px solid var(--line)}
			.test[open] .body{display:block}
			/* Card body: steps, error, stack trace and screenshot */
			.steps{margin:14px 0;padding-left:22px}.steps li{padding:3px 0}.steps li::marker{color:var(--muted);font-size:12px}
			q{quotes:none;font-family:"JetBrains Mono",ui-monospace,monospace;font-size:13px;background:var(--code);color:var(--accent);padding:1px 6px;border-radius:5px}
			.error,.skip{border-radius:10px;padding:12px 14px;margin:12px 0;background:var(--fail-bg)}
			.skip{background:var(--skip-bg)}
			.error-title{font-weight:600;color:var(--fail)}.skip .error-title{color:var(--skip)}
			pre{white-space:pre-wrap;overflow-wrap:anywhere;margin:6px 0 0;font-size:12.5px}
			details summary{cursor:pointer;color:var(--muted);font-size:13px;margin-top:8px}
			.trace{background:var(--code);padding:10px;border-radius:8px;max-height:320px;overflow:auto}
			.shot{margin:12px 0 0}.shot img{max-width:100%;border:1px solid var(--line);border-radius:10px;display:block}
			.shot figcaption{color:var(--muted);font-size:12px;margin-top:6px}
			.muted{color:var(--muted)}
			.empty{display:none;text-align:center;color:var(--muted);padding:32px}
			.lightbox{position:fixed;inset:0;background:rgba(0,0,0,.8);display:none;place-content:center;padding:24px;z-index:10;cursor:zoom-out}
			.lightbox img{max-width:96vw;max-height:92vh;border-radius:8px}
			footer{text-align:center;color:var(--muted);font-size:12px;padding:0 0 32px}
			/* Small screens and printing */
			@media (max-width:720px){.summary{grid-template-columns:1fr;justify-items:center}.stats{grid-template-columns:repeat(2,1fr);width:100%}
			.head{grid-template-columns:auto 1fr auto}.tags,.bar{display:none}}
			@media print{header{background:#312e81;-webkit-print-color-adjust:exact;print-color-adjust:exact}.toolbar,.chev{display:none}.body{display:block!important}.test{break-inside:avoid}}
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
			<circle cx="18" cy="18" r="15.9155" fill="none" stroke="var(--pass)" stroke-width="3.2" stroke-linecap="round" stroke-dasharray="{{dash}} 100"/></svg>
			<div class="val"><b>{{rate}}%</b><small>pass rate</small></div></div>
			<div><div class="stats">
			<div class="stat"><span>Total</span><b>{{total}}</b></div>
			<div class="stat p"><span>Passed</span><b>{{passed}}</b></div>
			<div class="stat f"><span>Failed</span><b>{{failed}}</b></div>
			<div class="stat s"><span>Skipped</span><b>{{skipped}}</b></div></div>
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
			var tests=[].slice.call(document.querySelectorAll('.test')),filter='all',q=document.getElementById('q');
			// Open or close a card on click and keep aria-expanded in step
			function sync(t){t.querySelector('.head').setAttribute('aria-expanded',t.hasAttribute('open'))}
			tests.forEach(function(t){sync(t);t.querySelector('.head').addEventListener('click',function(){t.toggleAttribute('open');sync(t)})});
			// Search and status filter; a class section is hidden when none of its tests is shown
			function apply(){var term=q.value.trim().toLowerCase(),shown=0;
			tests.forEach(function(t){var ok=(filter==='all'||t.dataset.status===filter)&&t.dataset.search.indexOf(term)>=0;t.style.display=ok?'':'none';if(ok)shown++});
			document.querySelectorAll('.group').forEach(function(g){g.style.display=[].some.call(g.querySelectorAll('.test'),function(t){return t.style.display!=='none'})?'':'none'});
			document.getElementById('empty').style.display=shown?'none':'block'}
			q.addEventListener('input',apply);
			document.querySelectorAll('.seg button').forEach(function(b){b.addEventListener('click',function(){filter=b.dataset.f;
			document.querySelectorAll('.seg button').forEach(function(x){x.setAttribute('aria-pressed',x===b)});apply()})});
			// Expand all / Collapse all
			var expand=document.getElementById('expand');
			expand.addEventListener('click',function(){var open=expand.textContent==='Expand all';
			tests.forEach(function(t){t.toggleAttribute('open',open);sync(t)});expand.textContent=open?'Collapse all':'Expand all'});
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
			})();
			</script>
			</body>
			</html>
			""";
}
