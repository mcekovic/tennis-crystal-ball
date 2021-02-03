package org.strangeforest.tcb.dataload

import java.util.logging.*

import com.google.common.base.*
import org.jsoup.*
import org.jsoup.nodes.*
import org.openqa.selenium.*
import org.openqa.selenium.firefox.*

abstract class LoaderUtil {

	private static final int TIMEOUT = 30 * 1000
	private static final int RETRY_COUNT = 5
	private static final int TEMPORARY_RETRY_COUNT = 10
	private static final long RETRY_DELAY = 1000L
	private static final long TEMPORARY_RETRY_DELAY = 15000L
	private static final String USER_AGENT = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:80.0) Gecko/20100101 Firefox/80.0'

	private static final int BAD_REQUEST = 400
	private static final int NOT_FOUND = 404
	private static final int TOO_MANY_REQUESTS = 429

	static retriedGetDoc(String url, boolean useWebDriver = true) {
		retry { useWebDriver ? getWebDriverDoc(url) : getDoc(url) }
	}

	static Document getDoc(String url) {
		Jsoup.connect(url).timeout(TIMEOUT).userAgent(USER_AGENT).get()
	}

	static Document getWebDriverDoc(String url) {
		def webDriver = createWebDriver()
		try {
			webDriver.get(url)
			if (webDriver.title?.toLowerCase()?.contains('cloudflare'))
				throw new HttpStatusException('Cloudflare captcha', TOO_MANY_REQUESTS, url)
			Jsoup.parse(webDriver.pageSource)
		}
		finally {
			webDriver.quit()
		}
	}

	private static synchronized WebDriver createWebDriver() {
		Logger.getLogger('org.openqa.selenium').setLevel(Level.OFF)
		System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, '/dev/null')
		def options = new FirefoxOptions()
		options.addArguments('-headless')
		def webDriver = new FirefoxDriver(options)
		webDriver.logLevel = Level.SEVERE
		webDriver
	}

	static retry(int count = Math.max(RETRY_COUNT, TEMPORARY_RETRY_COUNT), long delay = RETRY_DELAY, Closure<Object> closure, Closure<Boolean> retryableFunction = { Throwable rootCause -> isRetryable(rootCause) }) {
		for (int retry = 0; retry <= count; retry++) {
			try {
				return closure.call(retry)
			}
			catch (Throwable th) {
				def rootCause = Throwables.getRootCause(th)
				if (isNonRecoverable(rootCause))
					throw th
				def retryable = retryableFunction.call(rootCause)
				if (retryable) {
					def temporary = isTemporary(rootCause)
					if (retry < (temporary ? TEMPORARY_RETRY_COUNT : RETRY_COUNT)) {
						System.err.println "Exception occurred: ${rootCause} [retry ${retry + 1} follows]"
						Thread.sleep(temporary ? TEMPORARY_RETRY_DELAY : delay)
					}
					else {
						print isCaptcha(th) ? '?' : '!'
						break
					}
				}
				else
					throw th
			}
		}
		return null
	}

	static <E> List<Collection<E>> tile(Collection<E> col) {
		int size = col.size()
		if (size <= 1)
			[col]
		else {
			def part1 = [], part2 = []
			def part1Size = (size + 1) / 2
			def i = 0
			col.each { (++i <= part1Size ? part1 : part2) << it }
			[part1, part2]
		}
	}

	static boolean isNonRecoverable(Throwable th) {
		(th instanceof HttpStatusException && (th as HttpStatusException).statusCode in [BAD_REQUEST, NOT_FOUND] ||
		(th instanceof IOException) && th.message?.startsWith('Too many redirects'))
	}

	static boolean isRetryable(Throwable th) {
		th instanceof HttpStatusException ||
		th instanceof SessionNotCreatedException
	}

	static boolean isTemporary(Throwable th) {
		(th instanceof HttpStatusException && (th as HttpStatusException).statusCode == TOO_MANY_REQUESTS) ||
		(th instanceof SessionNotCreatedException) && th.message?.contains('temporarily')
	}

	static boolean isCaptcha(Throwable th) {
		(th instanceof HttpStatusException && (th as HttpStatusException).statusCode == TOO_MANY_REQUESTS) && th.message?.contains('captcha')
	}
}
