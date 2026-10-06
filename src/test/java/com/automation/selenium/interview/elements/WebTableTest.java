// Interview examples: form elements and tables
package com.automation.selenium.interview.elements;

// TestNG assertions
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

// Exact money sums (double would give rounding errors)
import java.math.BigDecimal;
// Collections used to read the table
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// A locator (how to find an element)
import org.openqa.selenium.By;
// One element on the page
import org.openqa.selenium.WebElement;
// Marks a method (or every public method of a class) as a TestNG test
import org.testng.annotations.Test;

// Base class for the practice-site examples
import com.automation.selenium.interview.PracticeSiteTest;
// Writes steps into the HTML report
import com.automation.selenium.utils.Steps;

/**
 * Interview question: How do you read and work with a web table?
 * <p>
 * Interview answer: I read the header titles once, then every row's cells,
 * and map them by position into column-to-value maps; after that every
 * question is plain Java. Inside a row I search with .// or CSS, because an
 * XPath starting with // searches the whole page.
 * <p>
 * Concept: a table is rows ({@code tr}) of cells ({@code td}) under a header
 * ({@code th}). Read the header titles once, then each row's cells, and map
 * them by position: <i>column title → cell text</i>. Then any question
 * ("what does Doe owe?", "is the column sorted?", "what is the total?") is
 * plain Java on that data.
 * <p>
 * Trap: inside {@code row.findElement(By.xpath(...))}, an XPath that starts
 * with {@code //} searches the <b>whole page</b>, not the row. Start it with
 * {@code .//} (or use CSS) to search inside the row.
 * <p>
 * Implementation: {@code /tables}, table 1: last name, first name, e-mail,
 * amount due, web site and edit/delete links; table 2 has the same data with
 * a class name on every cell.
 */
@Test(groups = { "interview", "elements" })
public class WebTableTest extends PracticeSiteTest {

	// Page with the tables
	private static final String TABLES_PAGE = "/tables";
	// Header cells and body rows of table 1
	private static final By HEADERS = By.cssSelector("#table1 thead th");
	private static final By ROWS = By.cssSelector("#table1 tbody tr");
	// Cells of one row, searched inside that row
	private static final By CELLS = By.tagName("td");

	/**
	 * Reads the table into rows of <i>column → value</i>, finds a row by one
	 * column and reads another column of it.
	 */
	@Test(description = "Web tables: read the table into rows and find a row by a column value")
	public void tableIsReadIntoRows() {
		// Open the page and read table 1
		open(TABLES_PAGE);
		List<Map<String, String>> rows = readTable();

		// Four people in the table
		assertEquals(rows.size(), 4, "Rows");
		// The row whose Last Name is Doe...
		Map<String, String> doe = rows.stream().filter(r -> r.get("Last Name").equals("Doe")).findFirst().orElseThrow();
		// ...has these values in its other columns
		assertEquals(doe.get("Email"), "jdoe@hotmail.com", "Doe's e-mail");
		assertEquals(doe.get("Due"), "$100.00", "Doe's amount due");
		// Record it in the report
		Steps.log("Doe's row: " + doe);
	}

	/**
	 * A typical coding task: add up a money column. BigDecimal keeps cents exact.
	 */
	@Test(description = "Web tables: add up the Due column")
	public void dueColumnIsSummed() {
		// Open the page and read table 1
		open(TABLES_PAGE);
		// "$50.00" → 50.00, then add them all up
		BigDecimal total = readTable().stream()
				.map(r -> money(r.get("Due")))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		// 50 + 51 + 100 + 50
		assertEquals(total, new BigDecimal("251.00"), "Total due");
		// Record it in the report
		Steps.log("Total due: $" + total);
	}

	/**
	 * When the cells have meaningful class names (table 2 has
	 * {@code td.last-name}, {@code td.dues}, …), read a column by its class
	 * instead of by its position: the test keeps working when a column is
	 * added or moved. Then a typical task: who owes the most?
	 * <p>
	 * (The site's header sorting is not used: its sorting plugin does not
	 * load in the browser, so clicking a header changes nothing.)
	 */
	@Test(description = "Web tables: read columns by stable class names and find the highest amount due")
	public void columnsAreReadByClassName() {
		// Open the page
		open(TABLES_PAGE);
		// Table 2's columns, each by its cells' class name; both lists are in row order
		List<String> lastNames = texts(By.cssSelector("#table2 tbody td.last-name"));
		List<String> dues = texts(By.cssSelector("#table2 tbody td.dues"));
		// Same data as table 1, which has no classes and must be read by position
		assertEquals(dues, column("Due"), "Due column by class (table 2) and by position (table 1)");

		// Index of the highest amount ("$100.00" → 100.00)
		int highest = 0;
		for (int i = 1; i < dues.size(); i++) {
			if (money(dues.get(i)).compareTo(money(dues.get(highest))) > 0) {
				highest = i;
			}
		}
		// The same index in the Last Name column is that person
		assertEquals(lastNames.get(highest), "Doe", "Person with the highest amount due");
		// Record it in the report
		Steps.log("Highest amount due: " + dues.get(highest) + " by " + lastNames.get(highest));
	}

	/**
	 * The {@code //} trap: from a row, {@code //td} finds the first cell of
	 * the whole page; {@code .//td} finds the first cell of that row.
	 * The same rule picks the right "edit" link of a row.
	 */
	@Test(description = "Web tables: XPath inside a row must start with .// to stay in the row")
	public void xpathInsideARowMustStartWithADot() {
		// Open the page
		open(TABLES_PAGE);
		// The third row (Doe)
		WebElement doeRow = driver().findElements(ROWS).get(2);

		// Wrong: "//td" ignores the row and returns the first cell of the page (Smith)
		assertEquals(doeRow.findElement(By.xpath("//td")).getText(), "Smith", "//td from the row searches the whole page");
		// Right: ".//td" searches inside the row
		assertEquals(doeRow.findElement(By.xpath(".//td")).getText(), "Doe", ".//td searches inside the row");
		// Record it in the report
		Steps.log("From Doe's row: //td → Smith (whole page), .//td → Doe (the row)");

		// Use it: click the row's own "edit" link
		doeRow.findElement(By.xpath(".//a[normalize-space()='edit']")).click();
		// The link only adds #edit to the URL
		assertTrue(driver().getCurrentUrl().endsWith("#edit"), "The row's edit link was clicked");
	}

	/**
	 * Reads table 1 into one map per row: column title → cell text.
	 *
	 * @return List the rows, top to bottom.
	 */
	private List<Map<String, String>> readTable() {
		// Header titles, left to right
		List<String> headers = driver().findElements(HEADERS).stream().map(h -> h.getText().trim()).toList();
		// One map per row
		List<Map<String, String>> rows = new ArrayList<>();
		for (WebElement row : driver().findElements(ROWS)) {
			// This row's cells (findElements on the row searches inside it)
			List<WebElement> cells = row.findElements(CELLS);
			// Cell i belongs to header i; LinkedHashMap keeps the column order
			Map<String, String> values = new LinkedHashMap<>();
			for (int i = 0; i < cells.size() && i < headers.size(); i++) {
				values.put(headers.get(i), cells.get(i).getText().trim());
			}
			rows.add(values);
		}
		return rows;
	}

	/**
	 * Reads one column of table 1.
	 *
	 * @param title  the column title.
	 * @return List  the column's texts, top to bottom.
	 */
	private List<String> column(String title) {
		return readTable().stream().map(r -> r.get(title)).toList();
	}

	/**
	 * Reads the text of every element a locator finds.
	 *
	 * @param locator the elements to read.
	 * @return List   their texts, in page order.
	 */
	private List<String> texts(By locator) {
		return driver().findElements(locator).stream().map(e -> e.getText().trim()).toList();
	}

	/**
	 * Turns a money text into a number.
	 *
	 * @param text       e.g. "$50.00".
	 * @return BigDecimal e.g. 50.00.
	 */
	private static BigDecimal money(String text) {
		// Drop the currency sign; BigDecimal keeps the cents exact
		return new BigDecimal(text.replace("$", ""));
	}
}
