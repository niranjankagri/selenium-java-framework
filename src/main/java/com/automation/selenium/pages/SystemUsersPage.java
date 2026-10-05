package com.automation.selenium.pages;

import java.util.List;

import org.openqa.selenium.WebDriver;

import com.automation.selenium.utils.Steps;

/**
 * Admin → User Management → Users: the list of system users with its filters.
 */
public class SystemUsersPage extends AppPage {

	// Path of the page, after APP_PATH
	private static final String PATH = "/admin/viewSystemUsers";

	/**
	 * @param driver the browser this page works on.
	 */
	public SystemUsersPage(WebDriver driver) {
		super(driver);
	}

	/**
	 * Waits until the page and its user table are shown.
	 */
	public void waitUntilLoaded() {
		waitForUrl(PATH);
		waitForLoader();
		// The record count is shown once the table has loaded
		recordsFound();
	}

	/**
	 * @param username the user name to filter by; empty clears the filter.
	 * @return SystemUsersPage this page.
	 */
	public SystemUsersPage filterByUsername(String username) {
		Steps.log("Filter by username \"" + username + "\"");
		type(input("Username"), username);
		return this;
	}

	/**
	 * @param role the user role to filter by: "Admin" or "ESS".
	 * @return SystemUsersPage this page.
	 */
	public SystemUsersPage filterByRole(String role) {
		Steps.log("Filter by user role \"" + role + "\"");
		choose("User Role", role);
		return this;
	}

	/**
	 * @param status the status to filter by: "Enabled" or "Disabled".
	 * @return SystemUsersPage this page.
	 */
	public SystemUsersPage filterByStatus(String status) {
		Steps.log("Filter by status \"" + status + "\"");
		choose("Status", status);
		return this;
	}

	/**
	 * Runs the search with the current filters and waits for the results.
	 *
	 * @return SystemUsersPage this page.
	 */
	public SystemUsersPage search() {
		Steps.log("Search");
		click(button("Search"));
		waitForLoader();
		return this;
	}

	/**
	 * Clears all filters and waits for the full list to reload.
	 *
	 * @return SystemUsersPage this page.
	 */
	public SystemUsersPage reset() {
		Steps.log("Reset the filters");
		click(button("Reset"));
		waitForLoader();
		return this;
	}

	/**
	 * @return String the record count above the table, e.g. "(3) Records Found".
	 */
	public String recordCount() {
		return recordsFound();
	}

	/**
	 * @return List the user names in the result table.
	 */
	public List<String> usernames() {
		return column("Username");
	}

	/**
	 * @return List the user roles in the result table.
	 */
	public List<String> roles() {
		return column("User Role");
	}

	/**
	 * @return List the statuses in the result table.
	 */
	public List<String> statuses() {
		return column("Status");
	}

	/**
	 * @return List every option of the User Role filter, e.g. [-- Select --, Admin, ESS].
	 */
	public List<String> roleOptions() {
		Steps.log("Read the options of the \"User Role\" drop-down");
		return options("User Role");
	}

	/**
	 * @return String the text in the Username filter.
	 */
	public String usernameFilter() {
		return valueOf(input("Username"));
	}

	/**
	 * @return String the option shown in the User Role filter.
	 */
	public String roleFilter() {
		return chosen("User Role");
	}

	/**
	 * @return String the option shown in the Status filter.
	 */
	public String statusFilter() {
		return chosen("Status");
	}
}
