package com.automation.selenium.pages;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.automation.selenium.utils.Steps;

/**
 * The dashboard, the first page after login.
 */
public class DashboardPage extends AppPage {

	// Path of the dashboard, after APP_PATH (also used by LoginPage)
	static final String PATH = "/dashboard/index";

	// Title of each dashboard widget, e.g. "Time at Work", "Quick Launch"
	@FindBy(css = ".orangehrm-dashboard-widget-name p")
	private List<WebElement> widgetTitles;

	/**
	 * @param driver the browser this page works on.
	 */
	public DashboardPage(WebDriver driver) {
		super(driver);
	}

	/**
	 * Requests the dashboard URL without waiting for the dashboard, e.g. to
	 * check that a logged-out browser is sent to the login page instead.
	 */
	public void visit() {
		Steps.log("Go to the dashboard URL");
		openPath(PATH);
	}

	/**
	 * Opens the dashboard by its URL. The browser must already be logged in;
	 * all tabs of one browser share the session cookie.
	 *
	 * @return DashboardPage this page, loaded.
	 */
	public DashboardPage open() {
		visit();
		waitUntilLoaded();
		return this;
	}

	/**
	 * Waits until the dashboard and its widgets are shown.
	 */
	public void waitUntilLoaded() {
		waitForUrl(PATH);
		widgetTitles();
	}

	/**
	 * @return boolean true if the browser shows the dashboard.
	 */
	public boolean isDisplayed() {
		return waitForUrl(PATH) && "Dashboard".equals(moduleTitle());
	}

	/**
	 * @return List the titles of all widgets on the dashboard.
	 */
	public List<String> widgetTitles() {
		return textsOf(widgetTitles);
	}
}
