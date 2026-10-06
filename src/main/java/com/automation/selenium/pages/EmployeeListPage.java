// Package of the page objects (one class per screen of OrangeHRM)
package com.automation.selenium.pages;

// Column values come back as lists
import java.util.List;

// A locator; used for the delete button, which depends on the employee ID
import org.openqa.selenium.By;
// The browser the page works on
import org.openqa.selenium.WebDriver;
// One element on the page (here filled in by PageFactory)
import org.openqa.selenium.WebElement;
// PageFactory annotation that says how to find a field's element
import org.openqa.selenium.support.FindBy;

// Writes steps into the HTML report
import com.automation.selenium.utils.Steps;

/**
 * PIM → Employee List: search, add and delete employees.
 * <p>
 * Extends {@link AppPage}, so it also has the left menu, the header and logout.
 */
public class EmployeeListPage extends AppPage {

	// Path of the page, after APP_PATH
	private static final String PATH = "/pim/viewEmployeeList";

	// "Add Employee" tab in the PIM top navigation
	// (XPath because the link is found by its visible text; normalize-space() ignores extra spaces)
	@FindBy(xpath = "//nav//a[normalize-space()='Add Employee']")
	private WebElement addEmployeeTab;
	// Button in the "Are you Sure?" dialog (role='document' is the dialog box)
	@FindBy(xpath = "//div[@role='document']//button[normalize-space()='Yes, Delete']")
	private WebElement confirmDeleteButton;

	/**
	 * Creates the page object; {@link BasePage}'s constructor fills the
	 * {@code @FindBy} fields.
	 *
	 * @param driver the browser this page works on.
	 */
	public EmployeeListPage(WebDriver driver) {
		// Hand the browser to AppPage → BasePage
		super(driver);
	}

	/**
	 * Waits until the page and its employee table are shown.
	 */
	public void waitUntilLoaded() {
		// The URL must be the employee list's
		waitForUrl(PATH);
		// The table's loading spinner must be gone
		waitForLoader();
		// The record count is shown once the table has loaded (recordsFound() waits for it; the text is not needed)
		recordsFound();
	}

	/**
	 * Opens the Add Employee form from the top navigation.
	 *
	 * @return AddEmployeePage the opened form.
	 */
	public AddEmployeePage addEmployee() {
		// Record the step in the report
		Steps.log("Open \"Add Employee\"");
		// Click the tab
		click(addEmployeeTab);
		// The browser now shows the form: create its page object
		AddEmployeePage page = new AddEmployeePage(driver);
		// Wait until it can be used
		page.waitUntilLoaded();
		// The test continues on the form
		return page;
	}

	/**
	 * Searches for an employee by employee ID and waits for the results.
	 *
	 * @param employeeId       the ID to search for.
	 * @return EmployeeListPage this page.
	 */
	public EmployeeListPage searchById(String employeeId) {
		// Record the step in the report
		Steps.log("Search for employee ID \"" + employeeId + "\"");
		// Type the ID into the box under the "Employee Id" label
		type(input("Employee Id"), employeeId);
		// Press the form's Search button
		click(button("Search"));
		// Wait until the results have been reloaded
		waitForLoader();
		// Return this page so the results can be read in a chain
		return this;
	}

	/**
	 * Reads the text above the table.
	 *
	 * @return String the record count above the table, e.g. "(1) Record Found".
	 */
	public String recordCount() {
		// Shared helper from BasePage
		return recordsFound();
	}

	/**
	 * Reads the Id column.
	 *
	 * @return List the employee IDs in the result table.
	 */
	public List<String> employeeIds() {
		// column(...) reads one column by its header title
		return column("Id");
	}

	/**
	 * Reads the names in the table.
	 *
	 * @return List "first name last name" of every employee in the result table.
	 */
	public List<String> employeeNames() {
		// tableRows() gives each row as a map "column title → text"; join the two name columns per row
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
		// Record the step in the report
		Steps.log("Delete employee ID \"" + employeeId + "\"");
		// The trash button of the row that has a cell with exactly this ID; it depends
		// on the ID, so it is built here instead of being a @FindBy field.
		// Read the XPath as: a table row (oxd-table-card) [that contains a cell whose text is the ID]
		// → inside that row, the button [that holds the trash icon].
		click(By.xpath("//div[contains(@class,'oxd-table-card')][.//div[contains(@class,'oxd-table-cell')][normalize-space()='"
				+ employeeId + "']]//button[.//i[contains(@class,'bi-trash')]]"));
		// Confirm "Yes, Delete" in the dialog
		click(confirmDeleteButton);
		// Read the toast first: it disappears again while the list reloads
		String message = toast();
		// Wait for the reloaded list
		waitForLoader();
		// Hand the confirmation text to the test to check
		return message;
	}
}
