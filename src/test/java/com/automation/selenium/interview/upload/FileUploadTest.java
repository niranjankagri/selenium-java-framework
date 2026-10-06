// Interview examples: file upload
package com.automation.selenium.interview.upload;

// TestNG assertions
import static org.testng.Assert.assertEquals;

// Thrown when the test file cannot be written
import java.io.IOException;
// File helpers (create folders and files)
import java.nio.file.Files;
import java.nio.file.Path;

// A locator (how to find an element)
import org.openqa.selenium.By;
// Ready-made wait conditions
import org.openqa.selenium.support.ui.ExpectedConditions;
// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

// Base class for the practice-site examples
import com.automation.selenium.interview.PracticeSiteTest;
// Writes steps into the HTML report
import com.automation.selenium.utils.Steps;

/**
 * Interview question: How do you upload a file with Selenium?
 * <p>
 * Concept: the file chooser window belongs to the operating system, and
 * Selenium cannot drive it. It doesn't need to: call {@code sendKeys} with
 * the file's <b>absolute path</b> on the {@code <input type="file">}
 * element, then submit the form.
 * <p>
 * Traps: don't {@code click()} the file input (that opens the OS dialog and
 * the test hangs); the path must be absolute; on Selenium Grid the file is
 * on another machine, so set {@code ((RemoteWebDriver) driver)
 * .setFileDetector(new LocalFileDetector())} to send it along. Avoid tools
 * such as AutoIt or Robot: they type into the OS dialog, break headless and
 * remote runs, and are rarely needed.
 * <p>
 * Implementation: {@code /upload}, which shows the uploaded file's name.
 */
@Test(groups = { "interview", "upload" })
public class FileUploadTest extends PracticeSiteTest {

	// Folder for the generated upload file (inside target/, so "mvn clean" removes it)
	private static final Path UPLOAD_DIR = Path.of("target", "uploads");

	/**
	 * Creates a small text file, uploads it through the file input and
	 * checks the name the site reports.
	 *
	 * @throws IOException if the test file cannot be written.
	 */
	@Test(description = "Upload: sendKeys the absolute path to the file input, then submit")
	public void fileIsUploadedWithSendKeys() throws IOException {
		// Create the file to upload, with a unique name
		Files.createDirectories(UPLOAD_DIR);
		Path file = UPLOAD_DIR.resolve("lab-upload-" + System.currentTimeMillis() + ".txt");
		Files.writeString(file, "Uploaded by FileUploadTest");

		// Open the upload page
		open("/upload");
		// Give the file input the absolute path (no click: that would open the OS file dialog)
		driver().findElement(By.id("file-upload")).sendKeys(file.toAbsolutePath().toString());
		// Submit the form
		driver().findElement(By.id("file-submit")).click();

		// The result page confirms the upload...
		assertEquals(explicitWait().until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h3"))).getText(), "File Uploaded!",
				"Heading after the upload");
		// ...and names the file it received
		assertEquals(driver().findElement(By.id("uploaded-files")).getText().trim(), file.getFileName().toString(), "Uploaded file name");
		// Record it in the report
		Steps.log("Uploaded \"" + file.getFileName() + "\" by sendKeys on input[type=file]");
	}
}
