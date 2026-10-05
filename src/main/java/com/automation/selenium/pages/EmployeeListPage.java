package com.automation.selenium.pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.automation.selenium.utils.Steps;

/**
 * PIM → Employee List: search, add and delete employees.
 */
public class EmployeeListPage extends AppPage {

	// Path of the page, relative to the base URL
	private static final String PATH = "/pim/viewEmployeeList";

	private static final By ADD_EMPLOYEE = By.xpath("//nav//a[normalize-space()='Add Employee']");
	private static final By SEARCH = By.xpath("//button[normalize-space()='Search']");
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
		click(SEARCH);
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
		click(By.xpath("//div[contains(@class,'oxd-table-card')][.//div[contains(@class,'oxd-table-cell')][normalize-space()='"
				+ employeeId + "']]//button[.//i[contains(@class,'bi-trash')]]"));
		click(CONFIRM_DELETE);
		String message = toast();
		waitForLoader();
		return message;
	}
}
