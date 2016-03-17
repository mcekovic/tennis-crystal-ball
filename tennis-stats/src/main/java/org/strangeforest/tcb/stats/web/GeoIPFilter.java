package org.strangeforest.tcb.stats.web;

import java.io.*;
import java.net.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.actuate.metrics.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;

import com.google.common.base.*;
import com.maxmind.geoip2.*;
import com.maxmind.geoip2.exception.*;
import com.maxmind.geoip2.model.*;

@Component @Profile("!dev")
public class GeoIPFilter implements Filter {

	@Autowired private CounterService counterService;
	private InputStream db;
	private DatabaseReader reader;

	private static Logger LOGGER = LoggerFactory.getLogger(GeoIPFilter.class);

	@Override public void init(FilterConfig filterConfig) throws ServletException {
		try {
			db = getClass().getResourceAsStream("/GeoLite2-Country.mmdb");
			reader = new DatabaseReader.Builder(db).build();
		}
		catch (IOException ex) {
			LOGGER.error("Error initializing GeoIP database.", ex);
		}
	}

	@Override public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (reader != null) {
			try {
				String remoteAddr = ((HttpServletRequest)request).getHeader("X-Forwarded-For");
				if (Strings.isNullOrEmpty(remoteAddr))
					remoteAddr = request.getRemoteAddr();
				int commaPos = remoteAddr.indexOf(',');
				if (commaPos > 0)
					remoteAddr = remoteAddr.substring(0, commaPos);
				CountryResponse country = reader.country(InetAddress.getByName(remoteAddr));
				if (country != null) {
					String countryName = country.getCountry().getName();
					if (!Strings.isNullOrEmpty(countryName))
						counterService.increment("counter.country." + countryName);
				}
			}
			catch (AddressNotFoundException | UnknownHostException ex) {
				LOGGER.debug("Error geo-locating country.", ex);
			}
			catch (Exception ex) {
				LOGGER.error("Error geo-locating country.", ex);
			}
		}
		chain.doFilter(request, response);
	}

	@Override public void destroy() {
		try (InputStream ignored = db) {
			if (reader != null)
				reader.close();
		}
		catch (IOException ex) {
			LOGGER.error("Error closing GeoIP database.", ex);
		}
	}
}
