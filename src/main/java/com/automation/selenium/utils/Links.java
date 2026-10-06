// Package of the small helpers used by pages and tests
package com.automation.selenium.utils;

// Thrown by HttpClient when the connection fails
import java.io.IOException;
// Unchecked wrapper for IOException, so callers need no throws clause
import java.io.UncheckedIOException;
// A parsed URL for the request
import java.net.URI;
// Java's built-in HTTP client (Java 11+), no extra library needed
import java.net.http.HttpClient;
// One HTTP request (URL, method, timeout)
import java.net.http.HttpRequest;
// The response and its body handlers
import java.net.http.HttpResponse;
// Time spans for the timeouts
import java.time.Duration;

/**
 * Checks links over HTTP, outside the browser. Selenium can read a link's
 * URL but not its HTTP status, so broken-link checks call the URL directly
 * with Java's {@link HttpClient}.
 * <p>
 * Typical use in a test: collect every {@code a[href]} with
 * {@code findElements}, read each {@code href}, then call
 * {@link #statusCode(String)} and {@link #isBroken(int)}.
 */
// final + private constructor: only static members, never instantiated
public final class Links {

	// A link is broken when its final status (after redirects) is this or higher
	public static final int FIRST_ERROR_STATUS = 400;
	// One client for all checks (it is thread-safe and reuses connections); it follows redirects like a browser does
	private static final HttpClient CLIENT = HttpClient.newBuilder()
			// NORMAL = follow redirects, except from https to http
			.followRedirects(HttpClient.Redirect.NORMAL)
			// Give up when the server cannot be connected to within 15 s
			.connectTimeout(Duration.ofSeconds(15))
			// Create the client with these settings
			.build();
	// Longest wait for one response
	private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

	// Static helper only
	private Links() {
	}

	/**
	 * Requests the URL and returns the status of the final response. The body
	 * is discarded, since only the status matters.
	 *
	 * @param url  an absolute http or https URL.
	 * @return int the HTTP status code, e.g. 200 or 404.
	 * @throws UncheckedIOException  if the server cannot be reached at all.
	 * @throws IllegalStateException if the thread is interrupted while waiting.
	 */
	public static int statusCode(String url) {
		// Build a GET request for the URL, with the response timeout
		HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(REQUEST_TIMEOUT).GET().build();
		try {
			// Send it and wait; discarding() throws the body away, we only need the status code
			return CLIENT.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
		} catch (IOException e) {
			// Network error (no connection, DNS, timeout): rethrow unchecked, with the URL in the message
			throw new UncheckedIOException("Could not reach " + url, e);
		} catch (InterruptedException e) {
			// Keep the interrupt flag, so the test runner can still see it
			Thread.currentThread().interrupt();
			// Then stop this check with a clear message
			throw new IllegalStateException("Interrupted while checking " + url, e);
		}
	}

	/**
	 * Decides whether a status code means the link is broken.
	 *
	 * @param status   an HTTP status code.
	 * @return boolean true for 4xx and 5xx (client and server errors).
	 */
	public static boolean isBroken(int status) {
		// 1xx–3xx are fine (redirects were already followed); 400+ is an error
		return status >= FIRST_ERROR_STATUS;
	}
}
