package com.automation.selenium.utils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Checks links over HTTP, outside the browser. Selenium can read a link's
 * URL but not its HTTP status, so broken-link checks call the URL directly
 * with Java's {@link HttpClient}.
 */
public final class Links {

	// A link is broken when its final status (after redirects) is this or higher
	public static final int FIRST_ERROR_STATUS = 400;
	// One client for all checks; it follows redirects like a browser does
	private static final HttpClient CLIENT = HttpClient.newBuilder()
			.followRedirects(HttpClient.Redirect.NORMAL)
			.connectTimeout(Duration.ofSeconds(15))
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
		HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(REQUEST_TIMEOUT).GET().build();
		try {
			return CLIENT.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
		} catch (IOException e) {
			throw new UncheckedIOException("Could not reach " + url, e);
		} catch (InterruptedException e) {
			// Keep the interrupt flag, so the test runner can still see it
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while checking " + url, e);
		}
	}

	/**
	 * @param status   an HTTP status code.
	 * @return boolean true for 4xx and 5xx (client and server errors).
	 */
	public static boolean isBroken(int status) {
		return status >= FIRST_ERROR_STATUS;
	}
}
