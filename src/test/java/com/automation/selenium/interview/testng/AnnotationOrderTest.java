// Interview examples: TestNG
package com.automation.selenium.interview.testng;

// TestNG assertion
import static org.testng.Assert.assertEquals;

// The recorded calls, safe to add to from several threads
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// The configuration annotations, in the order they run
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

// Writes steps into the HTML report
import com.automation.selenium.utils.Steps;

/**
 * Interview question: In what order do the TestNG annotations run?
 * <p>
 * Interview answer: From the outside in and back out: {@code @BeforeSuite},
 * {@code @BeforeTest} (once per {@code <test>} tag), {@code @BeforeClass},
 * {@code @BeforeMethod}, the {@code @Test}, then {@code @AfterMethod},
 * {@code @AfterClass}, {@code @AfterTest} and {@code @AfterSuite}.
 * Configuration methods need {@code alwaysRun = true} to run under a group
 * filter.
 * <p>
 * Concept: from the outside in, then back out:
 * <pre>
 * &#64;BeforeSuite   once, before everything in the suite
 *  &#64;BeforeTest   once per &lt;test&gt; tag in testng.xml
 *   &#64;BeforeClass once per class, before its first test
 *    &#64;BeforeMethod before every test method
 *     &#64;Test
 *    &#64;AfterMethod  after every test method
 *   &#64;AfterClass  after the class's last test
 *  &#64;AfterTest    after the &lt;test&gt; tag
 * &#64;AfterSuite    once, at the very end
 * </pre>
 * Every method here records its name; the test checks the "before" half,
 * and {@code @AfterSuite} prints the full list to the console.
 * <p>
 * Trap: configuration methods do not inherit the class-level
 * {@code groups}. When the run filters by group ({@code -Dgroups=testng}),
 * a configuration method without {@code alwaysRun = true} (or its own
 * groups) is silently left out. That is why every one below has it, and
 * why {@code BaseTest.startBrowser} has it.
 * <p>
 * No browser is needed, so this class does not extend {@code BaseTest}.
 */
@Test(groups = { "interview", "testng" })
public class AnnotationOrderTest {

	// Every call, in the order it happened (synchronized: test classes run in parallel)
	private static final List<String> CALLS = Collections.synchronizedList(new ArrayList<>());

	/** Runs once, before any test of the suite. */
	@BeforeSuite(alwaysRun = true)
	public void beforeSuite() {
		CALLS.add("@BeforeSuite");
	}

	/** Runs once per {@code <test>} tag (Maven without testng.xml makes one). */
	@BeforeTest(alwaysRun = true)
	public void beforeTest() {
		CALLS.add("@BeforeTest");
	}

	/** Runs once, before the first test of this class. */
	@BeforeClass(alwaysRun = true)
	public void beforeClass() {
		CALLS.add("@BeforeClass");
	}

	/** Runs before every test method of this class. */
	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() {
		CALLS.add("@BeforeMethod");
	}

	/**
	 * The only test: by the time it runs, the four "before" methods have
	 * run in order.
	 */
	@Test(description = "TestNG: configuration annotations run from suite down to method")
	public void configurationMethodsRunInOrder() {
		// The test itself
		CALLS.add("@Test");
		// Show the order in the report
		Steps.log("Order so far: " + CALLS);
		// Suite → test → class → method → the test
		assertEquals(CALLS, List.of("@BeforeSuite", "@BeforeTest", "@BeforeClass", "@BeforeMethod", "@Test"), "Call order");
	}

	/** Runs after every test method of this class. */
	@AfterMethod(alwaysRun = true)
	public void afterMethod() {
		CALLS.add("@AfterMethod");
	}

	/** Runs once, after the last test of this class. */
	@AfterClass(alwaysRun = true)
	public void afterClass() {
		CALLS.add("@AfterClass");
	}

	/** Runs once per {@code <test>} tag, after all its classes. */
	@AfterTest(alwaysRun = true)
	public void afterTest() {
		CALLS.add("@AfterTest");
	}

	/** Runs once, at the very end; prints the whole order to the console. */
	@AfterSuite(alwaysRun = true)
	public void afterSuite() {
		CALLS.add("@AfterSuite");
		// After the suite there is no test to attach a report step to, so print it
		System.out.println("TestNG annotation order: " + CALLS);
	}
}
