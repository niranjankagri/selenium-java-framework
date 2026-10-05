package com.automation.selenium.pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.automation.selenium.utils.Steps;

/**
 * PIM → Employee List: search, add and delete employees.
 */
public class EmployeeListPage extends AppPage {

	// Path of the page, after APP_PATH
	private static final String PATH = "/pim/viewEmployeeList";

	// "Add Employee" tab in the PIM top navigation
	private static final By ADD_EMPLOYEE = By.xpath("//nav//a[normalize-space()='Add Employee']");
	// Button in the "Are you Sure?" dialog
	private static final By CONFIRM_DELETE = By.xpath("//div[@role='document']//button[normalize-space()='Yes, Delete']");

	/**
	 * @param driver the browser this page works on.
	 */
	public EmployeeListPage(WebDriver driver) {
		super(driver);
	}

	/**
	 * Waits until the page and its employee table are shown.
	 */
	public void waitUntilLoaded() {
		waitForUrl(PATH);
		waitForLoader();
		// The record count is shown once the table has loaded
		recordsFound();
	}

	/**
	 * Opens the Add Employee form from the top navigation.
	 *
	 * @return AddEmployeePage the opened form.
	 */
	public AddEmployeePage addEmployee() {
		Steps.log("Open \"Add Employee\"");
		click(ADD_EMPLOYEE);
		AddEmployeePage page = new AddEmployeePage(driver);
		page.waitUntilLoaded();
		return page;
	}

	/**
	 * Searches for an employee by employee ID and waits for the results.
	 *
	 * @param employeeId       the ID to search for.
	 * @return EmployeeListPage this page.
	 */
	public EmployeeListPage searchById(String employeeId) {
		Steps.log("Search for employee ID \"" + employeeId + "\"");
		type(input("Employee Id"), employeeId);
		click(button("Search"));
		waitForLoader();
		return this;
	}

	/**
	 * @return String the record count above the table, e.g. "(1) Record Found".
	 */
	public String recordCount() {
		return recordsFound();
	}

	/**
	 * @return List the employee IDs in the result table.
	 */
	public List<String> employeeIds() {
		return column("Id");
	}

	/**
	 * @return List "first name last name" of every employee in the result table.
	 */
	public List<String> employeeNames() {
		return tableRows().stream().map(row -> row.get("First (& Middle) Name") + " " + row.get("Last Name")).toList();
	}

	/**
	 * Deletes the employee in the result table with the given ID and confirms
	 * the dialog.
	 *
	 * @param employeeId  the ID of the row to delete.
	 * @return String     the confirmation toast, e.g. "Successfully Deleted".
	 */
	public String delete(String employeeId) {
		Steps.log("Delete employee ID \"" + employeeId + "\"");
		// The trash button of the row that has a cell with exactly this ID
		click(By.xpath("//div[contains(@class,'oxd-table-card')][.//div[contains(@class,'oxd-table-cell')][normalize-space()='"
				+ employeeId + "']]//button[.//i[contains(@class,'bi-trash')]]"));
		click(CONFIRM_DELETE);
		// Read the toast first: it disappears again while the list reloads
		String message = toast();
		waitForLoader();
		return message;
	}
}
