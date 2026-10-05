package com.automation.selenium.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.annotations.Test;

import com.automation.selenium.base.BaseTest;
import com.automation.selenium.data.TestData;
import com.automation.selenium.data.TestData.Employee;
import com.automation.selenium.pages.EmployeeListPage;
import com.automation.selenium.pages.EmployeeProfilePage;

/**
 * PIM: the life cycle of an employee. Every run creates its own employee with
 * a random ID and deletes it again, so it leaves no data on the demo site.
 */
@Test(groups = "pim")
public class EmployeeTest extends BaseTest {

	@Test(groups = "smoke", description = "An employee can be added, found by ID and deleted")
	public void employeeCanBeAddedFoundAndDeleted() {
		Employee employee = TestData.newEmployee();

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
		list.searchById(employee.employeeId());
		assertEquals(list.recordCount(), "No Records Found", "Record count after deleting");
		assertTrue(list.employeeIds().isEmpty(), "The deleted employee should not be found");
	}

	@Test(description = "Searching for an unknown employee ID finds no records")
	public void unknownEmployeeIdFindsNoRecords() {
		EmployeeListPage list = loginAsAdmin().openPim().searchById("NO-SUCH-ID");

		assertEquals(list.recordCount(), "No Records Found", "Record count");
		assertTrue(list.employeeIds().isEmpty(), "The table should be empty");
	}
}
