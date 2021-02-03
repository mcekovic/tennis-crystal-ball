package org.strangeforest.tcb.util;

import java.util.*;

import com.neovisionaries.i18n.*;

import static java.util.Comparator.*;
import static java.util.Map.*;
import static java.util.stream.Collectors.*;

public class Country {

	public static final String UNKNOWN_ID = "???";
	public static final String UNKNOWN_NAME = "Unknown";
	private static final String UNKNOWN_CODE = "__";

	public static final Country UNKNOWN = new Country(UNKNOWN_ID);

	private final String countryId;

	public Country(String countryId) {
		this.countryId = countryId;
	}

	public String getId() {
		return countryId;
	}

	public String getCode() {
		var code = code(countryId);
		return code != null ? code.getAlpha2().toLowerCase() : UNKNOWN_CODE;
	}

	public String getName() {
		var code = code(countryId);
		return code != null ? code.getName() : UNKNOWN_NAME;
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Country)) return false;
		var country = (Country)o;
		return Objects.equals(countryId, country.countryId);
	}

	@Override public int hashCode() {
		return Objects.hash(countryId);
	}

	@Override public String toString() {
		return getName() + " (" + countryId + ')';
	}


	// Codes

	private static final Map<String, String> OVERRIDES = Map.ofEntries(
		entry("AHO", "NLD"),
		entry("ALG", "DZA"),
		entry("ANG", "AGO"),
		entry("ANZ", "AUS"),
		entry("ARU", "ABW"),
		entry("ASA", "ASM"),
		entry("BAH", "BHS"),
		entry("BAN", "BGD"),
		entry("BAR", "BRB"),
		entry("BER", "BMU"),
		entry("BOT", "BWA"),
		entry("BRI", "GBR"),
		entry("BRU", "BRN"),
		entry("BUL", "BGR"),
		entry("CAL", "FRA"),
		entry("CAM", "KHM"),
		entry("CAR", "CAN"),
		entry("CAY", "CYM"),
		entry("CEY", "LKA"),
		entry("CGO", "COG"),
		entry("CHI", "CHL"),
		entry("CRC", "CRI"),
		entry("CRO", "HRV"),
		entry("DEN", "DNK"),
		entry("ECA", "ATG"),
		entry("ESA", "SLV"),
		entry("FIJ", "FJI"),
		entry("FRG", "DEU"),
		entry("GER", "DEU"),
		entry("GRE", "GRC"),
		entry("GRN", "GRL"),
		entry("GUA", "GTM"),
		entry("GUD", "FRA"),
		entry("HAI", "HTI"),
		entry("HAW", "USA"),
		entry("HON", "HND"),
		entry("INA", "IDN"),
		entry("IRI", "IRN"),
		entry("ISV", "IMN"),
		entry("ITF", "ITA"),
		entry("KSA", "SAU"),
		entry("KUW", "KWT"),
		entry("LAT", "LVA"),
		entry("LBA", "LBY"),
		entry("LES", "LSO"),
		entry("LIB", "LBN"),
		entry("MAD", "MDG"),
		entry("MAS", "MYS"),
		entry("MGL", "MNG"),
		entry("MON", "MCO"),
		entry("MTN", "MNE"),
		entry("MRI", "MUS"),
		entry("MYA", "MMR"),
		entry("NCA", "NIC"),
		entry("NED", "NLD"),
		entry("NEP", "NPL"),
		entry("NGR", "NGA"),
		entry("NIG", "NER"),
		entry("OMA", "OMN"),
		entry("PAR", "PRY"),
		entry("PHI", "PHL"),
		entry("POC", "PRI"),
		entry("POR", "PRT"),
		entry("PUR", "PRI"),
		entry("RHO", "ZWE"),
		entry("RSA", "ZAF"),
		entry("SAM", "WSM"),
		entry("SEY", "SYC"),
		entry("SIN", "SGP"),
		entry("SLO", "SVN"),
		entry("SOL", "SLB"),
		entry("SRI", "LKA"),
		entry("SUD", "SDN"),
		entry("SUI", "CHE"),
		entry("TAN", "TZA"),
		entry("TCH", "CZE"),
		entry("TOG", "TGO"),
		entry("TPE", "TWN"),
		entry("TRI", "TTO"),
		entry("UAE", "ARE"),
		entry("URS", "UZB"),
		entry("URU", "URY"),
		entry("VAN", "VUT"),
		entry("VIE", "VNM"),
		entry("VIN", "FIN"),
		entry("YUG", "SRB"),
		entry("ZAM", "ZMB"),
		entry("ZIM", "ZWE"),

		entry("BIZ", "???"),
		entry("NMI", "???"),
		entry("TKS", "???"),
		entry("UNK", "???")
	);

	public static CountryCode code(String countryId) {
		if (!UNKNOWN_ID.equals(countryId)) {
			var isoAlpha3 = OVERRIDES.getOrDefault(countryId, countryId);
			if (!UNKNOWN_ID.equals(isoAlpha3)) {
				var code = CountryCode.getByCode(isoAlpha3);
				if (code != null)
					return code;
				else
					throw new IllegalArgumentException("Unknown country ID: " + countryId);
			}
		}
		return null;
	}

	public static List<CountryCode> codes(Collection<String> countryIds) {
		return countryIds.stream().map(Country::code).filter(code -> code != null && code.getAlpha3() != null).distinct().sorted(comparing(CountryCode::getName)).collect(toList());
	}
}
