# Manual test cases

The OrangeHRM test cases this lab automates, written as a tester would run them by hand. Every case has
an ID; the TestNG method that covers it starts with the same ID (`tc108_employeeCanBeAddedFoundAndDeleted`)
and so does its description (`[TC-108] An employee can be added, found by ID and deleted`), so a failure
in the report leads straight to the case below.

| ID range | Area | Automated in |
|---|---|---|
| TC-101 to TC-107 | Login and logout | `tests/LoginTest` |
| TC-108 to TC-110 | Employees (PIM) | `tests/EmployeeTest` |
| TC-111 to TC-115 | System users (Admin) | `tests/SystemUsersTest` |
| TC-116 to TC-120 | Menu, drop-down, keyboard, tabs | `tests/SeleniumScenariosTest` |
| TC-121 to TC-122 | Login page and login time | `tests/TestNgFeaturesTest` |

`TestNgFeaturesTest` shows TestNG features (priority, dependsOnMethods, invocationCount, timeOut,
retryAnalyzer, expectedExceptions, @Parameters) on login checks; methods that re-check a login case carry
that case's ID (TC-101, TC-102).

Not listed, on purpose: the `interview/` lessons (Selenium techniques on practice sites: alerts, frames,
waits, exceptions, locators ...) and `interview/java/` (Java coding exercises): they demonstrate techniques
and code, not the behaviour of an application under test. Neither are three technique scenarios in
`SeleniumScenariosTest` (JavaScript reading the page state, @FindBy vs @CacheLookup, an element screenshot)
or `ReportDemoTest`, which fails and skips on purpose.

## Setup

- OrangeHRM demo: <https://opensource-demo.orangehrmlive.com> (public, shared, reset regularly).
- Login: `Admin` / `admin123`.

## Summary

| ID | Title | Type | Priority |
|---|---|---|---|
| TC-101 | Admin logs in and sees the dashboard | Smoke | High |
| TC-102 | Login is rejected for invalid credentials (3 data sets) | Negative | High |
| TC-103 | Username and password are both required | Negative | Medium |
| TC-104 | Password is required when only the username is entered | Negative | Medium |
| TC-105 | Admin logs out and returns to the login page | Smoke | High |
| TC-106 | Back and reload after logout end on the login page | Security | High |
| TC-107 | The dashboard URL redirects to login when not logged in | Security | High |
| TC-108 | An employee can be added, found by ID and deleted | Smoke / E2E | High |
| TC-109 | First and last name are required for a new employee | Negative | Medium |
| TC-110 | Searching an unknown employee ID finds no records | Negative | Low |
| TC-111 | System Users page opens from the Admin menu | Smoke | High |
| TC-112 | Filtering by username lists only that user | Regression | Medium |
| TC-113 | Filtering by role and status lists only matching users | Regression | Medium |
| TC-114 | Searching an unknown username finds no records | Negative | Low |
| TC-115 | Reset clears every filter | Regression | Low |
| TC-116 | The side menu lists the core modules | Regression | Medium |
| TC-117 | The side menu has no broken links | Regression | Medium |
| TC-118 | The User Role drop-down offers Admin and ESS | Regression | Low |
| TC-119 | Admin can log in with the keyboard only | Accessibility | Medium |
| TC-120 | The dashboard opens in a second tab with the same session | Regression | Low |
| TC-121 | The login page shows its title, fields and button | Smoke | Medium |
| TC-122 | Login finishes within one minute | Performance | Low |

---

## Login (`LoginTest`)

### TC-101 Admin logs in and sees the dashboard

Also automated by `TestNgFeaturesTest.tc101_adminLogsInAfterThePageCheck` and `tc101_adminLoginIsRetriedOnceOnFailure`.

| # | Step | Expected result |
|---|---|---|
| 1 | Open the login page, enter `Admin` / `admin123`, click **Login** | The dashboard |
| 2 | Look at the top bar and the page | The logged-in user's name is shown; at least one dashboard widget |

### TC-102 Login is rejected for invalid credentials

Also automated by `TestNgFeaturesTest.tc102_wrongPasswordThrowsTheLoginError` and `tc102_wrongPasswordIsRejectedEveryTime` (runs twice).
For every data set: enter the user and password, click **Login**. Expected: "Invalid credentials"; still
on the login page.

| Data set | Username | Password |
|---|---|---|
| wrong password | `Admin` | `wrong-password` |
| unknown user | `no.such.user` | `admin123` |
| wrong case in user name and password | `admin` | `ADMIN123` |

### TC-103 Username and password are both required

| # | Step | Expected result |
|---|---|---|
| 1 | Click **Login** with both fields empty | "Required" under Username and under Password |

### TC-104 Password is required when only the username is entered

| # | Step | Expected result |
|---|---|---|
| 1 | Enter `Admin`, leave the password empty, click **Login** | "Required" under Password; still on the login page |

### TC-105 Admin logs out and returns to the login page

| # | Step | Expected result |
|---|---|---|
| 1 | Log in; open the user menu (top right), click **Logout** | The login page |

### TC-106 Back and reload after logout end on the login page

| # | Step | Expected result |
|---|---|---|
| 1 | Log in, log out (TC-105) | The login page |
| 2 | Press the browser's **Back** button, then reload | Still the login page, not the dashboard |

### TC-107 The dashboard URL redirects to login when not logged in

| # | Step | Expected result |
|---|---|---|
| 1 | In a fresh private window open `.../web/index.php/dashboard/index` | Redirect to the login page |

## Employees (`EmployeeTest`)

Precondition for all: logged in as Admin.

### TC-108 An employee can be added, found by ID and deleted

Test data: a random first and last name and a unique employee ID.

| # | Step | Expected result |
|---|---|---|
| 1 | **PIM** › **Add**, enter first name, last name and employee ID, **Save** | The new profile shows the full name and the employee ID |
| 2 | **PIM**, search by the employee ID | Exactly one row with that ID and name |
| 3 | Delete the row, confirm | "Successfully Deleted" |
| 4 | Search the ID again | "No Records Found" |

### TC-109 First and last name are required for a new employee

| # | Step | Expected result |
|---|---|---|
| 1 | **PIM** › **Add**, click **Save** without names | "Required" under first name and under last name; nothing saved |

### TC-110 Searching an unknown employee ID finds no records

| # | Step | Expected result |
|---|---|---|
| 1 | **PIM**, search employee ID `NO-SUCH-ID` | "No Records Found"; empty table |

## System users (`SystemUsersTest`)

Precondition for all: logged in as Admin, **Admin** opened from the side menu.

### TC-111 System Users page opens from the Admin menu

| # | Step | Expected result |
|---|---|---|
| 1 | Click **Admin** in the side menu | Module title "Admin"; users are listed |

### TC-112 Filtering by username lists only that user

| # | Step | Expected result |
|---|---|---|
| 1 | Username `Admin`, **Search** | At least one row; every username is "Admin" |

### TC-113 Filtering by role and status lists only matching users

| # | Step | Expected result |
|---|---|---|
| 1 | User Role **Admin**, Status **Enabled**, **Search** | At least one row; every row has role Admin and status Enabled |

### TC-114 Searching an unknown username finds no records

| # | Step | Expected result |
|---|---|---|
| 1 | Username `no.such.user.42`, **Search** | "No Records Found"; empty table |

### TC-115 Reset clears every filter

| # | Step | Expected result |
|---|---|---|
| 1 | Username `Admin`, User Role **Admin**, Status **Enabled**, then **Reset** | Username empty; User Role and Status show "-- Select --" |

## Menu, drop-down, keyboard, tabs (`SeleniumScenariosTest`)

Precondition for TC-116 to TC-118 and TC-120: logged in as Admin.

### TC-116 The side menu lists the core modules

| # | Step | Expected result |
|---|---|---|
| 1 | Read the side menu entries | They include Admin, PIM, Time, Dashboard; no entry is blank |

### TC-117 The side menu has no broken links

| # | Step | Expected result |
|---|---|---|
| 1 | Open every side menu link (or check its address with an HTTP client) | Every link answers without an error status (no 4xx/5xx) |

### TC-118 The User Role drop-down offers Admin and ESS

| # | Step | Expected result |
|---|---|---|
| 1 | **Admin**, open the **User Role** filter | Options exactly: "-- Select --", "Admin", "ESS" |

### TC-119 Admin can log in with the keyboard only

| # | Step | Expected result |
|---|---|---|
| 1 | On the login page type `Admin`, press Tab, type `admin123`, press Enter | The dashboard (no mouse needed) |

### TC-120 The dashboard opens in a second tab with the same session

| # | Step | Expected result |
|---|---|---|
| 1 | Open a new tab, go to the dashboard URL | The dashboard, without logging in again; two tabs open |
| 2 | Close the new tab, return to the first | Only the first tab is left, still on the dashboard |

## Login page and login time (`TestNgFeaturesTest`)

### TC-121 The login page shows its title, fields and button

Automated by `tc121_softAssertChecksTheLoginPage` (every check, reported together), `tc121_loginPageIsShown` and `tc121_titleMatchesTheSuiteParameter`.

| # | Step | Expected result |
|---|---|---|
| 1 | Open the login page | Page title "OrangeHRM"; the login form is shown |
| 2 | Look at the fields and the button | Placeholders "Username" and "Password"; button "Login". Note **every** deviation before failing the case |

### TC-122 Login finishes within one minute

| # | Step | Expected result |
|---|---|---|
| 1 | Log in as Admin with a stopwatch | The dashboard within 60 seconds |
