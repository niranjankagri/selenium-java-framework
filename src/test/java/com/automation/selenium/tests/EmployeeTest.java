package com.automation.selenium.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.annotations.Test;

import com.automation.selenium.base.BaseTest;
import com.automation.selenium.data.TestData;
import com.automation.selenium.data.TestData.Employee;
import com.automation.selenium.pages.AddEmployeePage;
import com.automation.selenium.pages.DashboardPage;
import com.automation.selenium.pages.EmployeeListPage;
import com.automation.selenium.pages.EmployeeProfilePage;
import com.automation.selenium.utils.Steps;

/**
 * PIM: the life cycle of an employee. Every run creates its own employee with
 * a random ID and deletes it again, so it leaves no data on the demo site.
 * If the test fails before its delete step, {@link #cleanUp()} deletes the
 * employee instead.
 */
@Test(groups = "pim")
public class EmployeeTest extends BaseTest {

	// ID of an employee this test created and has not deleted yet; null when there is none.
	// A plain field is enough: with parallel=classes, one thread runs all methods of this class.
	private String undeletedEmployeeId;

	/**
	 * Adds an employee with a random ID, checks the new profile, finds the
	 * employee by ID in the list, deletes it and checks it is gone.
	 */
	@Test(groups = "smoke", description = "An employee can be added, found by ID and deleted")
	public void employeeCanBeAddedFoundAndDeleted() {
		Employee employee = TestData.newEmployee();
		// Set before saving: a failure right after Save may still have created the employee
		undeletedEmployeeId = employee.employeeId();

		// Add
		EmployeeProfilePage profile = loginAsAdmin().openPim().addEmployee()
				.fill(employee.firstName(), employee.lastName(), employee.employeeId())
				.save();
		assertEquals(profile.fullName(), employee.fullName(), "Name on the new profile");
		assertEquals(profile.employeeId(), employee.employeeId(), "Employee Id on the new profile");

		// Find
		EmployeeListPage list = profile.openPim().searchById(employee.employeeId());
		assertEquals(list.employeeIds(), List.of(employee.employeeId()), "Employees found by ID");
		assertEquals(list.employeeNames(), List.of(employee.fullName()), "Name in the employee list");

		// Delete
		assertEquals(list.delete(employee.employeeId()), "Successfully Deleted", "Delete confirmation");
		undeletedEmployeeId = null;
		list.searchById(employee.employeeId());
		assertEquals(list.recordCount(), "No Records Found", "Record count after deleting");
		assertTrue(list.employeeIds().isEmpty(), "The deleted employee should not be found");
	}

	/**
	 * Saves the Add Employee form without names and checks "Required" under
	 * first and last name. Nothing is saved, so there is nothing to clean up.
	 */
	@Test(description = "First and last name are required for a new employee")
	public void firstAndLastNameAreRequired() {
		AddEmployeePage form = loginAsAdmin().openPim().addEmployee().trySave();

		assertEquals(form.validationErrors(), List.of("Required", "Required"), "Messages under the name fields");
	}

	/**
	 * Searches for an employee ID that does not exist and checks the empty result.
	 */
	@Test(description = "Searching for an unknown employee ID finds no records")
	public void unknownEmployeeIdFindsNoRecords() {
		EmployeeListPage list = loginAsAdmin().openPim().searchById("NO-SUCH-ID");

		assertEquals(list.recordCount(), "No Records Found", "Record count");
		assertTrue(list.employeeIds().isEmpty(), "The table should be empty");
	}

	/**
	 * Deletes the employee a failed test left behind. Runs with the browser
	 * still open, after the failure screenshot; does nothing when the test
	 * deleted the employee itself.
	 */
	@Override
	protected void cleanUp() {
		if (undeletedEmployeeId == null) {
			return;
		}
		String employeeId = undeletedEmployeeId;
		// Cleared first, so a failing cleanup is not repeated after the next test
		undeletedEmployeeId = null;
		Steps.log("Cleanup: delete employee ID \"" + employeeId + "\" if it exists");
		// The test may have failed on any page; the dashboard URL is a known starting point
		EmployeeListPage list = new DashboardPage(driver()).open().openPim().searchById(employeeId);
		if (list.employeeIds().contains(employeeId)) {
			list.delete(employeeId);
		}
	}
}
