package com.automation.selenium.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.annotations.Test;

import com.automation.selenium.base.BaseTest;
import com.automation.selenium.pages.SystemUsersPage;

/**
 * Admin → System Users: searching and filtering the user list. The demo site
 * is shared, so the checks never depend on how many users exist, only on
 * every listed user matching the filters.
 */
@Test(groups = "admin")
public class SystemUsersTest extends BaseTest {

	// Placeholder the drop-down filters show when nothing is chosen
	private static final String NOTHING_CHOSEN = "-- Select --";

	/**
	 * Opens Admin from the side menu and checks the title and that users are listed.
	 */
	@Test(groups = "smoke", description = "System Users page opens from the Admin menu")
	public void systemUsersPageOpensFromMenu() {
		SystemUsersPage users = loginAsAdmin().openAdmin();

		assertEquals(users.moduleTitle(), "Admin", "Module title");
		assertFalse(users.usernames().isEmpty(), "The user list should not be empty");
	}

	/**
	 * Searches for user name "Admin" and checks that every listed user is Admin.
	 */
	@Test(description = "Filtering by username lists only that user")
	public void filterByUsername() {
		SystemUsersPage users = loginAsAdmin().openAdmin().filterByUsername("Admin").search();

		List<String> usernames = users.usernames();
		assertFalse(usernames.isEmpty(), "User 'Admin' should be found");
		assertTrue(usernames.stream().allMatch("Admin"::equals), "Only 'Admin' should be listed, got " + usernames);
	}

	/**
	 * Searches for enabled admins and checks the role and status of every listed user.
	 */
	@Test(description = "Filtering by role and status lists only matching users")
	public void filterByRoleAndStatus() {
		SystemUsersPage users = loginAsAdmin().openAdmin().filterByRole("Admin").filterByStatus("Enabled").search();

		List<String> roles = users.roles();
		List<String> statuses = users.statuses();
		assertFalse(roles.isEmpty(), "At least one enabled admin should be found");
		assertTrue(roles.stream().allMatch("Admin"::equals), "Only admins should be listed, got " + roles);
		assertTrue(statuses.stream().allMatch("Enabled"::equals), "Only enabled users should be listed, got " + statuses);
	}

	/**
	 * Searches for a user name that does not exist and checks the empty result.
	 */
	@Test(description = "Searching for an unknown username finds no records")
	public void unknownUsernameFindsNoRecords() {
		SystemUsersPage users = loginAsAdmin().openAdmin().filterByUsername("no.such.user.42").search();

		assertEquals(users.recordCount(), "No Records Found", "Record count");
		assertTrue(users.usernames().isEmpty(), "The table should be empty");
	}

	/**
	 * Sets all three filters, presses Reset and checks that every filter is empty again.
	 */
	@Test(description = "Reset clears every filter")
	public void resetClearsFilters() {
		SystemUsersPage users = loginAsAdmin().openAdmin()
				.filterByUsername("Admin").filterByRole("Admin").filterByStatus("Enabled")
				.reset();

		assertEquals(users.usernameFilter(), "", "Username filter");
		assertEquals(users.roleFilter(), NOTHING_CHOSEN, "User Role filter");
		assertEquals(users.statusFilter(), NOTHING_CHOSEN, "Status filter");
	}
}
