// Package of the page objects (one class per screen of OrangeHRM)
package com.automation.selenium.pages;

// The browser the page works on
import org.openqa.selenium.WebDriver;
// One element on the page (here filled in by PageFactory)
import org.openqa.selenium.WebElement;
// PageFactory annotation that says how to find a field's element
import org.openqa.selenium.support.FindBy;

/**
 * An employee's profile (Personal Details), shown after adding an employee.
 * <p>
 * Extends {@link AppPage}, so it also has the left menu, the header and logout.
 */
public class EmployeeProfilePage extends AppPage {

	// Path of the page, after APP_PATH (followed by the employee number)
	private static final String PATH = "/pim/viewPersonalDetails";

	// Employee name above the profile picture
	// (a PageFactory proxy: the element is looked up again every time the field is used)
	@FindBy(css = ".orangehrm-edit-employee-name h6")
	private WebElement fullNameHeader;

	/**
	 * Creates the page object; {@link BasePage}'s constructor fills the
	 * {@code @FindBy} fields.
	 *
	 * @param driver the browser this page works on.
	 */
	public EmployeeProfilePage(WebDriver driver) {
		// Hand the browser to AppPage → BasePage (which sets up the wait and PageFactory)
		super(driver);
	}

	/**
	 * Waits until the profile and its form data are loaded.
	 */
	public void waitUntilLoaded() {
		// First the URL must change to the profile page...
		waitForUrl(PATH);
		// ...then the spinner that shows while the form data loads must be gone
		waitForLoader();
	}

	/**
	 * Reads the name in the profile header.
	 *
	 * @return String the employee name in the profile header, e.g. "Jane Doe".
	 */
	public String fullName() {
		// The header is rendered before the name arrives, so wait for text in it.
		// A wait condition that returns null means "not yet"; any other value ends the wait.
		return wait.until(d -> {
			// Read the header text (the proxy finds the element fresh each time)
			String name = fullNameHeader.getText().trim();
			// Empty → null → keep waiting; otherwise return the name
			return name.isEmpty() ? null : name;
		});
	}

	/**
	 * Reads the Employee Id field of the profile form.
	 *
	 * @return String the Employee Id field of the profile.
	 */
	public String employeeId() {
		// input(...) finds the text box under the "Employee Id" label; valueOf reads what is typed in it
		return valueOf(input("Employee Id"));
	}
}
