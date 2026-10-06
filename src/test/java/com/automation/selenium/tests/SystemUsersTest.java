// Package of the test classes
package com.automation.selenium.tests;

// TestNG assertions: each fails the test with the given message when the check is false
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

// Column values come back as lists
import java.util.List;

// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

// Base class: starts a browser before each test and closes it afterwards
import com.automation.selenium.base.BaseTest;
// Page object of Admin → System Users
import com.automation.selenium.pages.SystemUsersPage;

/**
 * Admin → System Users: searching and filtering the user list. The demo site
 * is shared, so the checks never depend on how many users exist, only on
 * every listed user matching the filters.
 */
// Every test in this class belongs to the "admin" group (run only these with -Dgroups=admin)
@Test(groups = "admin")
public class SystemUsersTest extends BaseTest {

	// Placeholder the drop-down filters show when nothing is chosen
	private static final String NOTHING_CHOSEN = "-- Select --";

	/**
	 * Opens Admin from the side menu and checks the title and that users are listed.
	 */
	// groups = "smoke" is added to the class group, so this test is in both "admin" and "smoke"
	@Test(groups = "smoke", description = "System Users page opens from the Admin menu")
	public void systemUsersPageOpensFromMenu() {
		// Log in, then click Admin in the side menu
		SystemUsersPage users = loginAsAdmin().openAdmin();

		// The header shows the module name "Admin"
		assertEquals(users.moduleTitle(), "Admin", "Module title");
		// At least one user is listed (Admin always exists)
		assertFalse(users.usernames().isEmpty(), "The user list should not be empty");
	}

	/**
	 * Searches for user name "Admin" and checks that every listed user is Admin.
	 */
	@Test(description = "Filtering by username lists only that user")
	public void filterByUsername() {
		// Log in, open Admin, type "Admin" into the Username filter, press Search (methods are chained)
		SystemUsersPage users = loginAsAdmin().openAdmin().filterByUsername("Admin").search();

		// Read the Username column of the results
		List<String> usernames = users.usernames();
		// Something must be found...
		assertFalse(usernames.isEmpty(), "User 'Admin' should be found");
		// ...and every row must be "Admin" ("Admin"::equals is short for name -> "Admin".equals(name))
		assertTrue(usernames.stream().allMatch("Admin"::equals), "Only 'Admin' should be listed, got " + usernames);
	}

	/**
	 * Searches for enabled admins and checks the role and status of every listed user.
	 */
	@Test(description = "Filtering by role and status lists only matching users")
	public void filterByRoleAndStatus() {
		// Choose User Role = Admin and Status = Enabled in the drop-downs, then Search
		SystemUsersPage users = loginAsAdmin().openAdmin().filterByRole("Admin").filterByStatus("Enabled").search();

		// Read the User Role and Status columns
		List<String> roles = users.roles();
		List<String> statuses = users.statuses();
		// At least one enabled admin exists (the Admin user itself)
		assertFalse(roles.isEmpty(), "At least one enabled admin should be found");
		// Every row has role Admin...
		assertTrue(roles.stream().allMatch("Admin"::equals), "Only admins should be listed, got " + roles);
		// ...and status Enabled
		assertTrue(statuses.stream().allMatch("Enabled"::equals), "Only enabled users should be listed, got " + statuses);
	}

	/**
	 * Searches for a user name that does not exist and checks the empty result.
	 */
	@Test(description = "Searching for an unknown username finds no records")
	public void unknownUsernameFindsNoRecords() {
		// Search for a name nobody has
		SystemUsersPage users = loginAsAdmin().openAdmin().filterByUsername("no.such.user.42").search();

		// The text above the table says so
		assertEquals(users.recordCount(), "No Records Found", "Record count");
		// And the table has no rows
		assertTrue(users.usernames().isEmpty(), "The table should be empty");
	}

	/**
	 * Sets all three filters, presses Reset and checks that every filter is empty again.
	 */
	@Test(description = "Reset clears every filter")
	public void resetClearsFilters() {
		// Fill all three filters, then press Reset (no search needed)
		SystemUsersPage users = loginAsAdmin().openAdmin()
				.filterByUsername("Admin").filterByRole("Admin").filterByStatus("Enabled")
				.reset();

		// Text box is empty again
		assertEquals(users.usernameFilter(), "", "Username filter");
		// Both drop-downs show the "-- Select --" placeholder again
		assertEquals(users.roleFilter(), NOTHING_CHOSEN, "User Role filter");
		assertEquals(users.statusFilter(), NOTHING_CHOSEN, "Status filter");
	}
}
