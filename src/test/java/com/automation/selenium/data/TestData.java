// Package of the test data
package com.automation.selenium.data;

// Random numbers without sharing one Random object across threads
import java.util.concurrent.ThreadLocalRandom;

// TestNG annotation that marks a method as a data source for tests
import org.testng.annotations.DataProvider;

/**
 * Test data: the data providers shared by the tests and generated employees.
 */
// final + private constructor: only static members, never instantiated
public final class TestData {

	// Static helper only
	private TestData() {
	}

	/**
	 * A new employee for the PIM tests.
	 * <p>
	 * A {@code record} is a small immutable data class: Java generates the
	 * constructor, the getters {@code firstName()}, {@code lastName()},
	 * {@code employeeId()}, plus {@code equals}, {@code hashCode} and
	 * {@code toString}.
	 *
	 * @param firstName   the first name.
	 * @param lastName    the last name.
	 * @param employeeId  the employee ID (10 characters).
	 */
	public record Employee(String firstName, String lastName, String employeeId) {

		/**
		 * Joins first and last name.
		 *
		 * @return String "first name last name", as OrangeHRM shows it.
		 */
		public String fullName() {
			// e.g. "Selenium Test01234567"
			return firstName + " " + lastName;
		}
	}

	/**
	 * Creates an employee with a random ID, so runs (and parallel threads) on the
	 * shared demo site never collide. The ID is also in the last name, which
	 * makes test data easy to spot in OrangeHRM.
	 *
	 * @return Employee a new employee.
	 */
	public static Employee newEmployee() {
		// "QA" + 8 digits = 10 characters, the most the Employee Id field accepts.
		// nextInt(100_000_000) gives 0..99,999,999; %08d pads it with zeros to 8 digits.
		String number = String.format("%08d", ThreadLocalRandom.current().nextInt(100_000_000));
		// Same number in the last name and the ID, e.g. "Test01234567" / "QA01234567"
		return new Employee("Selenium", "Test" + number, "QA" + number);
	}

	/**
	 * Credentials the demo site must reject with "Invalid credentials".
	 * <p>
	 * A test uses it with
	 * {@code @Test(dataProvider = "invalidCredentials", dataProviderClass = TestData.class)};
	 * TestNG then runs the test once per row and passes the row's values as
	 * the method's parameters.
	 *
	 * @return Object[][] rows of { case name, username, password }.
	 */
	@DataProvider(name = "invalidCredentials")
	public static Object[][] invalidCredentials() {
		// One row = one test run; the first column only names the case in the report
		return new Object[][] {
				// Right user, wrong password
				{ "wrong password", "Admin", "wrong-password" },
				// User that does not exist, with the real password
				{ "unknown user", "no.such.user", "admin123" },
				// Login is case-sensitive: changed case must also be rejected
				{ "wrong case in user name and password", "admin", "ADMIN123" },
		};
	}
}
