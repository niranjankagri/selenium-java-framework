package com.automation.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * An employee's profile (Personal Details), shown after adding an employee.
 */
public class EmployeeProfilePage extends AppPage {

	// Path of the page, after APP_PATH (followed by the employee number)
	private static final String PATH = "/pim/viewPersonalDetails";
	// Employee name above the profile picture
	private static final By FULL_NAME = By.cssSelector(".orangehrm-edit-employee-name h6");

	/**
	 * @param driver the browser this page works on.
	 */
	public EmployeeProfilePage(WebDriver driver) {
		super(driver);
	}

	/**
	 * Waits until the profile and its form data are loaded.
	 */
	public void waitUntilLoaded() {
		waitForUrl(PATH);
		waitForLoader();
	}

	/**
	 * @return String the employee name in the profile header, e.g. "Jane Doe".
	 */
	public String fullName() {
		// The header is rendered before the name arrives, so wait for text in it
		return wait.until(d -> {
			String name = d.findElement(FULL_NAME).getText().trim();
			return name.isEmpty() ? null : name;
		});
	}

	/**
	 * @return String the Employee Id field of the profile.
	 */
	public String employeeId() {
		return valueOf(input("Employee Id"));
	}
}
