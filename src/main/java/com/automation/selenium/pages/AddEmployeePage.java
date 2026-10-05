package com.automation.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.automation.selenium.utils.Steps;

/**
 * PIM → Add Employee: the form for a new employee.
 */
public class AddEmployeePage extends AppPage {

	// Path of the page, relative to the base URL
	private static final String PATH = "/pim/addEmployee";

	private static final By FIRST_NAME = By.name("firstName");
	private static final By LAST_NAME = By.name("lastName");
	private static final By SAVE = By.cssSelector("button[type='submit']");

	/**
	 * @param driver the browser this page works on.
	 */
	public AddEmployeePage(WebDriver driver) {
		super(driver);
	}

	/**
	 * Waits until the form is shown.
	 */
	public void waitUntilLoaded() {
		waitForUrl(PATH);
		waitVisible(FIRST_NAME);
	}

	/**
	 * Fills in the form. The employee ID OrangeHRM suggests is replaced, so the
	 * test knows the ID to search for later.
	 *
	 * @param firstName        the first name.
	 * @param lastName         the last name.
	 * @param employeeId       the employee ID (at most 10 characters).
	 * @return AddEmployeePage this page.
	 */
	public AddEmployeePage fill(String firstName, String lastName, String employeeId) {
		Steps.log("Enter employee \"" + firstName + " " + lastName + "\" with ID \"" + employeeId + "\"");
		type(FIRST_NAME, firstName);
		type(LAST_NAME, lastName);
		type(input("Employee Id"), employeeId);
		return this;
	}

	/**
	 * Saves the form and waits for the new employee's profile.
	 *
	 * @return EmployeeProfilePage the profile OrangeHRM opens after saving.
	 */
	public EmployeeProfilePage save() {
		click(SAVE);
		Steps.log("Save: \"" + toast() + "\"");
		EmployeeProfilePage profile = new EmployeeProfilePage(driver);
		profile.waitUntilLoaded();
		return profile;
	}
}
