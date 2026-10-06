// Package of the test classes
package com.automation.selenium.tests;

// TestNG assertions: each fails the test with the given message when the check is false
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

// List.of(...) builds the expected lists
import java.util.List;

// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

// Base class: starts a browser before each test and closes it afterwards
import com.automation.selenium.base.BaseTest;
// Generates test employees
import com.automation.selenium.data.TestData;
// The employee record (first name, last name, ID)
import com.automation.selenium.data.TestData.Employee;
// Page objects used by these tests
import com.automation.selenium.pages.AddEmployeePage;
import com.automation.selenium.pages.DashboardPage;
import com.automation.selenium.pages.EmployeeListPage;
import com.automation.selenium.pages.EmployeeProfilePage;
// Writes steps into the HTML report
import com.automation.selenium.utils.Steps;

/**
 * PIM: the life cycle of an employee. Every run creates its own employee with
 * a random ID and deletes it again, so it leaves no data on the demo site.
 * If the test fails before its delete step, {@link #cleanUp()} deletes the
 * employee instead.
 */
// Every test in this class belongs to the "pim" group
@Test(groups = "pim")
public class EmployeeTest extends BaseTest {

	// ID of an employee this test created and has not deleted yet; null when there is none.
	// A plain field is enough: with parallel=classes, one thread runs all methods of this class.
	private String undeletedEmployeeId;

	/**
	 * Adds an employee with a random ID, checks the new profile, finds the
	 * employee by ID in the list, deletes it and checks it is gone.
	 */
	// Also in the "smoke" group, besides the class group "pim"
	@Test(groups = "smoke", description = "An employee can be added, found by ID and deleted")
	public void employeeCanBeAddedFoundAndDeleted() {
		// A new employee with a random 10-character ID, e.g. QA01234567
		Employee employee = TestData.newEmployee();
		// Set before saving: a failure right after Save may still have created the employee
		undeletedEmployeeId = employee.employeeId();

		// Add: log in → PIM → Add Employee → fill the form → Save (lands on the new profile)
		EmployeeProfilePage profile = loginAsAdmin().openPim().addEmployee()
				.fill(employee.firstName(), employee.lastName(), employee.employeeId())
				.save();
		// The profile header shows the full name we entered
		assertEquals(profile.fullName(), employee.fullName(), "Name on the new profile");
		// The Employee Id field kept our ID (not the one OrangeHRM suggested)
		assertEquals(profile.employeeId(), employee.employeeId(), "Employee Id on the new profile");

		// Find: back to the PIM list and search by the ID
		EmployeeListPage list = profile.openPim().searchById(employee.employeeId());
		// Exactly one row, with our ID (List equality also checks there is nothing else)
		assertEquals(list.employeeIds(), List.of(employee.employeeId()), "Employees found by ID");
		// That row shows our name
		assertEquals(list.employeeNames(), List.of(employee.fullName()), "Name in the employee list");

		// Delete: trash button + confirm; delete() returns the confirmation pop-up text
		assertEquals(list.delete(employee.employeeId()), "Successfully Deleted", "Delete confirmation");
		// Deleted by the test itself, so cleanUp() has nothing left to do
		undeletedEmployeeId = null;
		// Search again for the same ID...
		list.searchById(employee.employeeId());
		// ...and expect nothing
		assertEquals(list.recordCount(), "No Records Found", "Record count after deleting");
		assertTrue(list.employeeIds().isEmpty(), "The deleted employee should not be found");
	}

	/**
	 * Saves the Add Employee form without names and checks "Required" under
	 * first and last name. Nothing is saved, so there is nothing to clean up.
	 */
	@Test(description = "First and last name are required for a new employee")
	public void firstAndLastNameAreRequired() {
		// Open the empty form and press Save without typing anything
		AddEmployeePage form = loginAsAdmin().openPim().addEmployee().trySave();

		// Two "Required" messages: one under first name, one under last name
		assertEquals(form.validationErrors(), List.of("Required", "Required"), "Messages under the name fields");
	}

	/**
	 * Searches for an employee ID that does not exist and checks the empty result.
	 */
	@Test(description = "Searching for an unknown employee ID finds no records")
	public void unknownEmployeeIdFindsNoRecords() {
		// Search for an ID nobody has
		EmployeeListPage list = loginAsAdmin().openPim().searchById("NO-SUCH-ID");

		// The text above the table says so...
		assertEquals(list.recordCount(), "No Records Found", "Record count");
		// ...and the table has no rows
		assertTrue(list.employeeIds().isEmpty(), "The table should be empty");
	}

	/**
	 * Deletes the employee a failed test left behind. Runs with the browser
	 * still open, after the failure screenshot; does nothing when the test
	 * deleted the employee itself.
	 * <p>
	 * BaseTest calls this hook after every test of the class (pass or fail);
	 * the default version in BaseTest does nothing.
	 */
	@Override
	protected void cleanUp() {
		// Nothing left behind (the test passed, or never got to create the employee)
		if (undeletedEmployeeId == null) {
			return;
		}
		// Keep the ID in a local variable...
		String employeeId = undeletedEmployeeId;
		// Cleared first, so a failing cleanup is not repeated after the next test
		undeletedEmployeeId = null;
		// The cleanup steps also appear in the report, under the failed test
		Steps.log("Cleanup: delete employee ID \"" + employeeId + "\" if it exists");
		// The test may have failed on any page; the dashboard URL is a known starting point
		EmployeeListPage list = new DashboardPage(driver()).open().openPim().searchById(employeeId);
		// The failure may have happened before Save worked, so delete only if it was really created
		if (list.employeeIds().contains(employeeId)) {
			list.delete(employeeId);
		}
	}
}
