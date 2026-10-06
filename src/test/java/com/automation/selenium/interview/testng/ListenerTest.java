// Interview examples: TestNG
package com.automation.selenium.interview.testng;

// TestNG assertion
import static org.testng.Assert.assertEquals;

// Expected event lists
import java.util.List;

// Registers listeners for the suite from a test class
import org.testng.annotations.Listeners;
// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

// Writes steps into the HTML report
import com.automation.selenium.utils.Steps;

/**
 * Interview question: Which TestNG listeners do you know, and how do you
 * register one?
 * <p>
 * Interview answer: TestNG listeners are callbacks such as ITestListener for
 * each test's start, success, failure and skip, IReporter for a report after
 * the run, and IRetryAnalyzer for retries. I register them with @Listeners,
 * in testng.xml, or through ServiceLoader; @Listeners applies to the whole
 * suite, not just the class.
 * <p>
 * Concept: listeners are callbacks TestNG calls during the run:
 * {@code ITestListener} (each test: start, success, failure, skip),
 * {@code ISuiteListener} (suite start and end), {@code IReporter} (one call
 * after the run, to write a report), {@code IRetryAnalyzer} (retry a failed
 * test), {@code IAnnotationTransformer} (change annotations before the run).
 * <p>
 * Three ways to register one: {@code @Listeners(X.class)} on a test class
 * (here); {@code <listeners>} in testng.xml; or a
 * {@code META-INF/services/org.testng.ITestNGListener} file
 * (ServiceLoader), which is how {@code HtmlReportListener} is registered so
 * it works from Maven, testng.xml and the IDE alike.
 * <p>
 * Trap: {@code @Listeners} looks local to the class but registers the
 * listener for the whole suite. Registering the same listener in two
 * places makes it run twice.
 */
@Listeners(TestEventsListener.class)
@Test(groups = { "interview", "testng" })
public class ListenerTest {

	/**
	 * By the time a test body runs, the listener has already received
	 * {@code onTestStart} for it, and nothing else yet.
	 */
	@Test(description = "TestNG: an ITestListener receives onTestStart before the test body runs")
	public void listenerSawThisTestStart() {
		// What the listener recorded for this very test, so far
		List<String> events = TestEventsListener.eventsOf("ListenerTest.listenerSawThisTestStart");
		// Show it in the report
		Steps.log("Events for this test so far: " + events);
		// Started, not finished yet
		assertEquals(events, List.of("onTestStart"), "Events while the test is running");
	}

	/**
	 * Runs after the test above (dependsOnMethods) and checks that the
	 * listener also received its {@code onTestSuccess}.
	 */
	@Test(dependsOnMethods = "listenerSawThisTestStart",
			description = "TestNG: the listener received onTestSuccess for the finished test")
	public void listenerSawTheFirstTestPass() {
		// The finished test's full record
		List<String> events = TestEventsListener.eventsOf("ListenerTest.listenerSawThisTestStart");
		// Show it in the report
		Steps.log("Events for the finished test: " + events);
		// Started and passed
		assertEquals(events, List.of("onTestStart", "onTestSuccess"), "Events of the finished test");
	}
}
