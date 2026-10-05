package com.automation.selenium.pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.automation.selenium.config.Config;
import com.automation.selenium.utils.Steps;

/**
 * The dashboard, the first page after login.
 */
public class DashboardPage extends AppPage {

	// Path of the dashboard, relative to the base URL (also used by LoginPage)
	static final String PATH = "/dashboard/index";
	// Title of each dashboard widget, e.g. "Time at Work", "Quick Launch"
	private static final By WIDGET_TITLES = By.cssSelector(".orangehrm-dashboard-widget-name p");

	/**
	 * @param driver the browser this page works on.
	 */
	public DashboardPage(WebDriver driver) {
		super(driver);
	}

	/**
	 * Opens the dashboard by its URL. The browser must already be logged in;
	 * all tabs of one browser share the session cookie.
	 *
	 * @return DashboardPage this page, loaded.
	 */
	public DashboardPage open() {
		Steps.log("Open the dashboard by its URL");
		driver.get(Config.get().baseUrl() + "/web/index.php" + PATH);
		waitUntilLoaded();
		return this;
	}

	/**
	 * Waits until the dashboard and its widgets are shown.
	 */
	public void waitUntilLoaded() {
		waitForUrl(PATH);
		waitVisible(WIDGET_TITLES);
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
		return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(WIDGET_TITLES))
				.stream().map(WebElement::getText).map(String::trim).toList();
	}
}
