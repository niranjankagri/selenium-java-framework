// Interview examples: cookies
package com.automation.selenium.interview.cookies;

// TestNG assertions
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

// A locator (how to find an element)
import org.openqa.selenium.By;
// A browser cookie (name, value, domain, path, expiry, flags)
import org.openqa.selenium.Cookie;
// Ready-made wait conditions
import org.openqa.selenium.support.ui.ExpectedConditions;
// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

// Base class for the practice-site examples
import com.automation.selenium.interview.PracticeSiteTest;
// Writes steps into the HTML report
import com.automation.selenium.utils.Steps;

/**
 * Interview question: How do you work with cookies in Selenium? Can you skip
 * the login screen?
 * <p>
 * Concept: {@code driver.manage()} reads and changes the cookies of the
 * current site: {@code getCookies()}, {@code getCookieNamed(name)},
 * {@code addCookie(cookie)}, {@code deleteCookieNamed(name)},
 * {@code deleteAllCookies()}. A cookie can only be added for the site the
 * browser is on, so open a page of that site first.
 * <p>
 * The login session usually lives in a cookie: deleting it logs the user
 * out, and copying it into a fresh session logs that session in without the
 * login form, a common way to speed up suites where the login itself is
 * not under test.
 * <p>
 * Implementation: {@code /login} (user {@code tomsmith}, password
 * {@code SuperSecretPassword!}, both shown on the page itself) and the
 * protected page {@code /secure}. The session cookie is {@code rack.session}.
 */
@Test(groups = { "interview", "cookies" })
public class CookieTest extends PracticeSiteTest {

	// Name of the practice site's session cookie
	private static final String SESSION_COOKIE = "rack.session";
	// The message area at the top of the page
	private static final By FLASH = By.id("flash");

	/**
	 * Add, read and delete a cookie of your own.
	 */
	@Test(description = "Cookies: add, read and delete a cookie")
	public void cookieIsAddedReadAndDeleted() {
		// A page of the site must be open before a cookie can be added for it
		open("/login");

		// Add a cookie (name and value; domain and path default to the current page)
		driver().manage().addCookie(new Cookie("lab-theme", "dark"));
		// Read it back by name
		assertEquals(driver().manage().getCookieNamed("lab-theme").getValue(), "dark", "Value of the added cookie");
		// It is also in the full list
		assertTrue(driver().manage().getCookies().stream().anyMatch(c -> c.getName().equals("lab-theme")), "In getCookies()");

		// Delete it by name: reading it now gives null
		driver().manage().deleteCookieNamed("lab-theme");
		assertNull(driver().manage().getCookieNamed("lab-theme"), "Deleted cookie");
		// Delete everything for this site
		driver().manage().deleteAllCookies();
		assertTrue(driver().manage().getCookies().isEmpty(), "No cookies after deleteAllCookies()");
		// Record it in the report
		Steps.log("Added, read and deleted the cookie lab-theme=dark");
	}

	/**
	 * The login creates a session cookie; deleting it ends the session, so
	 * the protected page sends the browser back to the login page.
	 */
	@Test(description = "Cookies: deleting the session cookie logs the user out")
	public void deletingTheSessionCookieLogsOut() {
		// Log in through the form
		logIn();
		// The session lives in a cookie
		assertNotNull(driver().manage().getCookieNamed(SESSION_COOKIE), "Session cookie after login");

		// Delete it and open the protected page again
		driver().manage().deleteCookieNamed(SESSION_COOKIE);
		open("/secure");
		// Without the session the site sends us to the login page
		assertTrue(explicitWait().until(ExpectedConditions.visibilityOfElementLocated(FLASH)).getText()
				.contains("You must login to view the secure area!"), "Message after the session cookie was deleted");
		// Record it in the report
		Steps.log("Deleted " + SESSION_COOKIE + ": /secure redirected to the login page");
	}

	/**
	 * Skipping the login form: log in once, keep the session cookie, clear
	 * the browser's cookies, put only the saved cookie back, and the protected
	 * page opens without logging in again.
	 */
	@Test(description = "Cookies: a saved session cookie logs the browser in without the form")
	public void savedSessionCookieSkipsTheLoginForm() {
		// Log in once and keep the session cookie
		logIn();
		Cookie session = driver().manage().getCookieNamed(SESSION_COOKIE);

		// Forget everything: now the browser is logged out
		driver().manage().deleteAllCookies();
		// Put only the saved session back (we are still on a page of the same site)
		driver().manage().addCookie(session);
		// Open the protected page directly
		open("/secure");

		// It opens: the heading of the secure area is shown, and no login was typed
		assertEquals(driver().findElement(By.tagName("h2")).getText().trim(), "Secure Area", "Page opened with the saved cookie");
		// Record it in the report
		Steps.log("Restored the saved " + SESSION_COOKIE + " cookie: /secure opened without the login form");
	}

	/**
	 * Logs in through the practice site's login form.
	 */
	private void logIn() {
		// Open the login form
		open("/login");
		// The credentials the page itself shows
		driver().findElement(By.id("username")).sendKeys("tomsmith");
		driver().findElement(By.id("password")).sendKeys("SuperSecretPassword!");
		driver().findElement(By.cssSelector("#login button[type='submit']")).click();
		// Logged in: the success message is shown
		assertTrue(explicitWait().until(ExpectedConditions.visibilityOfElementLocated(FLASH)).getText()
				.contains("You logged into a secure area!"), "Login message");
		// Record it in the report
		Steps.log("Logged in as tomsmith through the form");
	}
}
