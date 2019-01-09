package org.strangeforest.tcb.stats.visitors;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.annotation.*;

import org.slf4j.*;
import org.springframework.stereotype.*;

import com.maxmind.geoip2.*;
import com.maxmind.geoip2.exception.*;
import com.maxmind.geoip2.model.*;
import com.maxmind.geoip2.record.*;

@Service @VisitorSupport
public class GeoIPService {

	private InputStream db;
	private DatabaseReader reader;

	private static Logger LOGGER = LoggerFactory.getLogger(GeoIPService.class);

	@PostConstruct
	public void init() {
		try {
			db = getClass().getResourceAsStream("/GeoLite2-Country.mmdb");
			reader = new DatabaseReader.Builder(db).build();
		}
		catch (IOException ex) {
			LOGGER.error("Error initializing GeoIP database.", ex);
		}
	}

	@PreDestroy
	public void destroy() {
		if (db == null)
			return;
		try (InputStream ignored = db) {
			if (reader != null)
				reader.close();
		}
		catch (IOException ex) {
			LOGGER.error("Error closing GeoIP database.", ex);
		}
	}

	public Optional<Country> getCountry(String ipAddress) {
		if (reader != null) {
			try {
				CountryResponse country = reader.country(InetAddress.getByName(ipAddress));
				if (country != null)
					return Optional.of(country.getCountry());
			}
			catch (AddressNotFoundException | UnknownHostException ex) {
				LOGGER.debug("Error geo-locating country.", ex);
			}
			catch (Exception ex) {
				LOGGER.error("Error geo-locating country.", ex);
			}
		}
		return Optional.empty();
	}
}
