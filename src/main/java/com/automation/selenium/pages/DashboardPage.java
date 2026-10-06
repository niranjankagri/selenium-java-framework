// Package of the page objects (one class per screen of OrangeHRM)
package com.automation.selenium.pages;

// The widget titles are read as a list
import java.util.List;

// The browser the page works on
import org.openqa.selenium.WebDriver;
// One element on the page (here filled in by PageFactory)
import org.openqa.selenium.WebElement;
// PageFactory annotation that says how to find a field's element(s)
import org.openqa.selenium.support.FindBy;

// Writes steps into the HTML report
import com.automation.selenium.utils.Steps;

/**
 * The dashboard, the first page after login.
 * <p>
 * Extends {@link AppPage}, so it also has the left menu, the header and logout.
 */
public class DashboardPage extends AppPage {

	// Path of the dashboard, after APP_PATH (also used by LoginPage)
	// (package-private, no "private", so LoginPage in the same package can wait for it)
	static final String PATH = "/dashboard/index";

	// Title of each dashboard widget, e.g. "Time at Work", "Quick Launch"
	// (a List field: PageFactory fills it with all matching elements, found again on every use)
	@FindBy(css = ".orangehrm-dashboard-widget-name p")
	private List<WebElement> widgetTitles;

	/**
	 * Creates the page object; {@link BasePage}'s constructor fills the
	 * {@code @FindBy} fields.
	 *
	 * @param driver the browser this page works on.
	 */
	public DashboardPage(WebDriver driver) {
		// Hand the browser to AppPage → BasePage
		super(driver);
	}

	/**
	 * Requests the dashboard URL without waiting for the dashboard, e.g. to
	 * check that a logged-out browser is sent to the login page instead.
	 */
	public void visit() {
		// Record the step in the report
		Steps.log("Go to the dashboard URL");
		// Load base URL + /web/index.php + /dashboard/index
		openPath(PATH);
	}

	/**
	 * Opens the dashboard by its URL. The browser must already be logged in;
	 * all tabs of one browser share the session cookie.
	 *
	 * @return DashboardPage this page, loaded.
	 */
	public DashboardPage open() {
		// Go to the URL...
		visit();
		// ...and wait until the dashboard is really there
		waitUntilLoaded();
		// Return this page so calls can be chained
		return this;
	}

	/**
	 * Waits until the dashboard and its widgets are shown.
	 */
	public void waitUntilLoaded() {
		// The URL must be the dashboard's
		waitForUrl(PATH);
		// textsOf waits for at least one widget title, so this also waits for the widgets (the result is not needed)
		widgetTitles();
	}

	/**
	 * Checks whether the browser is on the dashboard.
	 *
	 * @return boolean true if the browser shows the dashboard.
	 */
	public boolean isDisplayed() {
		// Both: the URL is the dashboard's and the header says "Dashboard" (&& stops early if the URL is wrong)
		return waitForUrl(PATH) && "Dashboard".equals(moduleTitle());
	}

	/**
	 * Reads the widget titles.
	 *
	 * @return List the titles of all widgets on the dashboard.
	 */
	public List<String> widgetTitles() {
		// Wait for the widgets and return their visible texts
		return textsOf(widgetTitles);
	}
}
