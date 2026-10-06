// Package of the page objects (one class per screen of OrangeHRM)
package com.automation.selenium.pages;

// The browser the page works on
import org.openqa.selenium.WebDriver;
// One element on the page (here filled in by PageFactory)
import org.openqa.selenium.WebElement;
// PageFactory annotation that says how to find a field's element
import org.openqa.selenium.support.FindBy;

// Writes steps into the HTML report
import com.automation.selenium.utils.Steps;

/**
 * PIM → Add Employee: the form for a new employee.
 * <p>
 * Extends {@link AppPage}, so it also has the left menu, the header and logout.
 */
public class AddEmployeePage extends AppPage {

	// Path of the page, after APP_PATH
	private static final String PATH = "/pim/addEmployee";

	// Name inputs of the form (found by their name attribute: short and stable)
	@FindBy(name = "firstName")
	private WebElement firstNameField;
	@FindBy(name = "lastName")
	private WebElement lastNameField;
	// Save button at the bottom of the form
	@FindBy(css = "button[type='submit']")
	private WebElement saveButton;

	/**
	 * Creates the page object; {@link BasePage}'s constructor fills the
	 * {@code @FindBy} fields.
	 *
	 * @param driver the browser this page works on.
	 */
	public AddEmployeePage(WebDriver driver) {
		// Hand the browser to AppPage → BasePage
		super(driver);
	}

	/**
	 * Waits until the form is shown and its loading overlay is gone. The
	 * overlay covers the Save button while the form loads, so a click before
	 * it disappears fails with ElementClickInterceptedException.
	 */
	public void waitUntilLoaded() {
		// The URL must be the Add Employee page's
		waitForUrl(PATH);
		// The loading overlay must be gone (otherwise it swallows the clicks)
		waitForLoader();
		// The first name box must be visible, i.e. the form is drawn
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
		// One report step for the whole form, with the values used
		Steps.log("Enter employee \"" + firstName + " " + lastName + "\" with ID \"" + employeeId + "\"");
		// type() clears the box first, then types
		type(firstNameField, firstName);
		type(lastNameField, lastName);
		// The Employee Id box has no name attribute, so it is found by its label
		type(input("Employee Id"), employeeId);
		// Return this page so save() can be chained: fill(...).save()
		return this;
	}

	/**
	 * Clicks Save without expecting it to work, e.g. to check the validation
	 * messages of an incomplete form.
	 *
	 * @return AddEmployeePage this page, still shown.
	 */
	public AddEmployeePage trySave() {
		// Record the step in the report
		Steps.log("Save the form");
		// Click Save (waits until it is clickable)
		click(saveButton);
		// The form stays open, so return this page to read the error messages
		return this;
	}

	/**
	 * Saves the form and waits for the new employee's profile.
	 *
	 * @return EmployeeProfilePage the profile OrangeHRM opens after saving.
	 */
	public EmployeeProfilePage save() {
		// Click Save
		click(saveButton);
		// The step shows OrangeHRM's own confirmation, e.g. "Successfully Saved"
		// (toast() waits for the pop-up message and returns its text)
		Steps.log("Save: \"" + toast() + "\"");
		// OrangeHRM now navigates to the new employee's profile: create its page object
		EmployeeProfilePage profile = new EmployeeProfilePage(driver);
		// Wait until the profile is loaded before handing it back
		profile.waitUntilLoaded();
		// The test continues on the profile page
		return profile;
	}
}
