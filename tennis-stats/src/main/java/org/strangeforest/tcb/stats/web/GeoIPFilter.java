package org.strangeforest.tcb.stats.web;

import java.io.*;
import java.net.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.actuate.metrics.*;
import org.springframework.stereotype.*;

import com.google.api.client.repackaged.com.google.common.base.*;
import com.maxmind.geoip2.*;
import com.maxmind.geoip2.exception.*;
import com.maxmind.geoip2.model.*;
import com.neovisionaries.i18n.*;

@Component
public class GeoIPFilter implements Filter {

	private InputStream db;
	private DatabaseReader reader;
	@Autowired private CounterService counterService;

	private static Logger LOGGER = LoggerFactory.getLogger(GeoIPFilter.class);

	@Override public void init(FilterConfig filterConfig) throws ServletException {
		try {
			db = getClass().getResourceAsStream("/geolite/GeoLite2-Country.mmdb");
			reader = new DatabaseReader.Builder(db).build();
		}
		catch (IOException ex) {
			LOGGER.error("Error initializing GeoIP database.", ex);
		}
	}

	@Override public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (reader != null) {
			String remoteAddr = ((HttpServletRequest)request).getHeader("X-Forwarded-For");
			if (Strings.isNullOrEmpty(remoteAddr))
				remoteAddr = request.getRemoteAddr();
			try {
				CountryResponse country = reader.country(InetAddress.getByName(remoteAddr));
				if (country != null) {
					String isoCode = country.getCountry().getIsoCode();
					if (!Strings.isNullOrEmpty(isoCode)) {
						CountryCode code = CountryCode.getByCode(isoCode);
						counterService.increment("counter.country." + (code != null ? code.getName() : isoCode));
					}
				}
			}
			catch (AddressNotFoundException ex) {
				LOGGER.debug("Error geo-locating country.", ex);
			}
			catch (GeoIp2Exception ex) {
				LOGGER.error("Error geo-locating country.", ex);
			}
		}
		chain.doFilter(request, response);
	}

	@Override public void destroy() {
		if (reader != null) {
			try {
				try {
					reader.close();
				}
				finally {
					db.close();
				}
			}
			catch (IOException ex) {
				LOGGER.error("Error closing GeoIP database.", ex);
			}
		}
	}
}
