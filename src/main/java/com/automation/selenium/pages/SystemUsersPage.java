// Package of the page objects (one class per screen of OrangeHRM)
package com.automation.selenium.pages;

// Column values and drop-down options come back as lists
import java.util.List;

// The browser the page works on
import org.openqa.selenium.WebDriver;

// Writes steps into the HTML report
import com.automation.selenium.utils.Steps;

/**
 * Admin → User Management → Users: the list of system users with its filters.
 * <p>
 * This page has no {@code @FindBy} fields: every element is found by its
 * label or button text through the {@link BasePage} helpers
 * ({@code input}, {@code choose}, {@code button}, {@code column}).
 * The filter methods return {@code this}, so a test can chain them:
 * {@code filterByRole("Admin").filterByStatus("Enabled").search()}.
 */
public class SystemUsersPage extends AppPage {

	// Path of the page, after APP_PATH
	private static final String PATH = "/admin/viewSystemUsers";

	/**
	 * Creates the page object.
	 *
	 * @param driver the browser this page works on.
	 */
	public SystemUsersPage(WebDriver driver) {
		// Hand the browser to AppPage → BasePage
		super(driver);
	}

	/**
	 * Waits until the page and its user table are shown.
	 */
	public void waitUntilLoaded() {
		// The URL must be the System Users page's
		waitForUrl(PATH);
		// The table's loading spinner must be gone
		waitForLoader();
		// The record count is shown once the table has loaded (recordsFound() waits for it)
		recordsFound();
	}

	/**
	 * Types into the Username filter.
	 *
	 * @param username the user name to filter by; empty clears the filter.
	 * @return SystemUsersPage this page.
	 */
	public SystemUsersPage filterByUsername(String username) {
		// Record the step in the report
		Steps.log("Filter by username \"" + username + "\"");
		// Type into the text box under the "Username" label
		type(input("Username"), username);
		// Return this page for chaining
		return this;
	}

	/**
	 * Chooses a role in the User Role filter.
	 *
	 * @param role the user role to filter by: "Admin" or "ESS".
	 * @return SystemUsersPage this page.
	 */
	public SystemUsersPage filterByRole(String role) {
		// Record the step in the report
		Steps.log("Filter by user role \"" + role + "\"");
		// Open the "User Role" drop-down and click the option (a custom drop-down, not a <select>)
		choose("User Role", role);
		// Return this page for chaining
		return this;
	}

	/**
	 * Chooses a status in the Status filter.
	 *
	 * @param status the status to filter by: "Enabled" or "Disabled".
	 * @return SystemUsersPage this page.
	 */
	public SystemUsersPage filterByStatus(String status) {
		// Record the step in the report
		Steps.log("Filter by status \"" + status + "\"");
		// Open the "Status" drop-down and click the option
		choose("Status", status);
		// Return this page for chaining
		return this;
	}

	/**
	 * Runs the search with the current filters and waits for the results.
	 *
	 * @return SystemUsersPage this page.
	 */
	public SystemUsersPage search() {
		// Record the step in the report
		Steps.log("Search");
		// Press the form's Search button
		click(button("Search"));
		// Wait until the table has been reloaded
		waitForLoader();
		// Return this page so the results can be read
		return this;
	}

	/**
	 * Clears all filters and waits for the full list to reload.
	 *
	 * @return SystemUsersPage this page.
	 */
	public SystemUsersPage reset() {
		// Record the step in the report
		Steps.log("Reset the filters");
		// Press the form's Reset button
		click(button("Reset"));
		// Wait until the full list has been reloaded
		waitForLoader();
		// Return this page so the filters can be read
		return this;
	}

	/**
	 * Reads the text above the table.
	 *
	 * @return String the record count above the table, e.g. "(3) Records Found".
	 */
	public String recordCount() {
		// Shared helper from BasePage
		return recordsFound();
	}

	/**
	 * Reads the Username column.
	 *
	 * @return List the user names in the result table.
	 */
	public List<String> usernames() {
		// column(...) reads one column by its header title
		return column("Username");
	}

	/**
	 * Reads the User Role column.
	 *
	 * @return List the user roles in the result table.
	 */
	public List<String> roles() {
		return column("User Role");
	}

	/**
	 * Reads the Status column.
	 *
	 * @return List the statuses in the result table.
	 */
	public List<String> statuses() {
		return column("Status");
	}

	/**
	 * Reads the options of the User Role drop-down.
	 *
	 * @return List every option of the User Role filter, e.g. [-- Select --, Admin, ESS].
	 */
	public List<String> roleOptions() {
		// Record the step in the report
		Steps.log("Read the options of the \"User Role\" drop-down");
		// Opens the drop-down, reads all options, closes it again (see BasePage.options)
		return options("User Role");
	}

	/**
	 * Reads the Username filter box.
	 *
	 * @return String the text in the Username filter.
	 */
	public String usernameFilter() {
		// valueOf reads what is typed in the box (getText() would be empty for an <input>)
		return valueOf(input("Username"));
	}

	/**
	 * Reads the User Role filter.
	 *
	 * @return String the option shown in the User Role filter.
	 */
	public String roleFilter() {
		// The text the closed drop-down shows, e.g. "-- Select --"
		return chosen("User Role");
	}

	/**
	 * Reads the Status filter.
	 *
	 * @return String the option shown in the Status filter.
	 */
	public String statusFilter() {
		return chosen("Status");
	}
}
