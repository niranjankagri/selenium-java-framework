package com.automation.selenium.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.automation.selenium.utils.Steps;

/**
 * PIM → Add Employee: the form for a new employee.
 */
public class AddEmployeePage extends AppPage {

	// Path of the page, after APP_PATH
	private static final String PATH = "/pim/addEmployee";

	// Name inputs of the form
	@FindBy(name = "firstName")
	private WebElement firstNameField;
	@FindBy(name = "lastName")
	private WebElement lastNameField;
	// Save button at the bottom of the form
	@FindBy(css = "button[type='submit']")
	private WebElement saveButton;

	/**
	 * @param driver the browser this page works on.
	 */
	public AddEmployeePage(WebDriver driver) {
		super(driver);
	}

	/**
	 * Waits until the form is shown and its loading overlay is gone. The
	 * overlay covers the Save button while the form loads, so a click before
	 * it disappears fails with ElementClickInterceptedException.
	 */
	public void waitUntilLoaded() {
		waitForUrl(PATH);
		waitForLoader();
		waitVisible(firstNameField);
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
		type(firstNameField, firstName);
		type(lastNameField, lastName);
		type(input("Employee Id"), employeeId);
		return this;
	}

	/**
	 * Clicks Save without expecting it to work, e.g. to check the validation
	 * messages of an incomplete form.
	 *
	 * @return AddEmployeePage this page, still shown.
	 */
	public AddEmployeePage trySave() {
		Steps.log("Save the form");
		click(saveButton);
		return this;
	}

	/**
	 * Saves the form and waits for the new employee's profile.
	 *
	 * @return EmployeeProfilePage the profile OrangeHRM opens after saving.
	 */
	public EmployeeProfilePage save() {
		click(saveButton);
		// The step shows OrangeHRM's own confirmation, e.g. "Successfully Saved"
		Steps.log("Save: \"" + toast() + "\"");
		EmployeeProfilePage profile = new EmployeeProfilePage(driver);
		profile.waitUntilLoaded();
		return profile;
	}
}
