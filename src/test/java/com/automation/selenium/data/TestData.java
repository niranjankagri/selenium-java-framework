package com.automation.selenium.data;

import java.util.concurrent.ThreadLocalRandom;

import org.testng.annotations.DataProvider;

/**
 * Test data: the data providers shared by the tests and generated employees.
 */
public final class TestData {

	// Static helper only
	private TestData() {
	}

	/**
	 * A new employee for the PIM tests.
	 *
	 * @param firstName   the first name.
	 * @param lastName    the last name.
	 * @param employeeId  the employee ID (10 characters).
	 */
	public record Employee(String firstName, String lastName, String employeeId) {

		/**
		 * @return String "first name last name", as OrangeHRM shows it.
		 */
		public String fullName() {
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
		// "QA" + 8 digits = 10 characters, the most the Employee Id field accepts
		String number = String.format("%08d", ThreadLocalRandom.current().nextInt(100_000_000));
		return new Employee("Selenium", "Test" + number, "QA" + number);
	}

	/**
	 * Credentials the demo site must reject with "Invalid credentials".
	 *
	 * @return Object[][] rows of { case name, username, password }.
	 */
	@DataProvider(name = "invalidCredentials")
	public static Object[][] invalidCredentials() {
		return new Object[][] {
				{ "wrong password", "Admin", "wrong-password" },
				{ "unknown user", "no.such.user", "admin123" },
				{ "wrong case in user name and password", "admin", "ADMIN123" },
		};
	}
}
