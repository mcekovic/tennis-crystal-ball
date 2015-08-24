package org.strangeforest.tcb.util;

import java.util.*;

import com.neovisionaries.i18n.*;

public abstract class CountryUtil {

	public static final String UNKNOWN = "???";

	private static final Map<String, String> OVERRIDES = new HashMap<>();
	static {
		OVERRIDES.put("AHO", "NLD");
		OVERRIDES.put("ALG", "DZA");
		OVERRIDES.put("ANG", "AGO");
		OVERRIDES.put("ANZ", "AUS");
		OVERRIDES.put("ARU", "ABW");
		OVERRIDES.put("ASA", "ASM");
		OVERRIDES.put("BAH", "BHS");
		OVERRIDES.put("BAN", "BGD");
		OVERRIDES.put("BAR", "BRB");
		OVERRIDES.put("BER", "BMU");
		OVERRIDES.put("BOT", "BWA");
		OVERRIDES.put("BRI", "GBR");
		OVERRIDES.put("BRU", "BRN");
		OVERRIDES.put("BUL", "BGR");
		OVERRIDES.put("CAL", "FRA");
		OVERRIDES.put("CAM", "KHM");
		OVERRIDES.put("CAR", "CAN");
		OVERRIDES.put("CAY", "CYM");
		OVERRIDES.put("CEY", "LKA");
		OVERRIDES.put("CGO", "COG");
		OVERRIDES.put("CHI", "CHL");
		OVERRIDES.put("CRC", "CRI");
		OVERRIDES.put("CRO", "HRV");
		OVERRIDES.put("DEN", "DNK");
		OVERRIDES.put("ECA", "ATG");
		OVERRIDES.put("ESA", "SLV");
		OVERRIDES.put("FIJ", "FJI");
		OVERRIDES.put("FRG", "DEU");
		OVERRIDES.put("GER", "DEU");
		OVERRIDES.put("GRE", "GRC");
		OVERRIDES.put("GRN", "GRL");
		OVERRIDES.put("GUA", "GTM");
		OVERRIDES.put("GUD", "FRA");
		OVERRIDES.put("HAI", "HTI");
		OVERRIDES.put("HAW", "USA");
		OVERRIDES.put("HON", "HND");
		OVERRIDES.put("INA", "IDN");
		OVERRIDES.put("IRI", "IRN");
		OVERRIDES.put("ISV", "IMN");
		OVERRIDES.put("KSA", "SAU");
		OVERRIDES.put("KUW", "KWT");
		OVERRIDES.put("LAT", "LVA");
		OVERRIDES.put("LBA", "LBY");
		OVERRIDES.put("LES", "LSO");
		OVERRIDES.put("LIB", "LBN");
		OVERRIDES.put("MAD", "MDG");
		OVERRIDES.put("MAS", "MYS");
		OVERRIDES.put("MGL", "MNG");
		OVERRIDES.put("MON", "MCO");
		OVERRIDES.put("MRI", "MUS");
		OVERRIDES.put("MYA", "MMR");
		OVERRIDES.put("NCA", "NIC");
		OVERRIDES.put("NED", "NLD");
		OVERRIDES.put("NEP", "NPL");
		OVERRIDES.put("NGR", "NER");
		OVERRIDES.put("NIG", "NER");
		OVERRIDES.put("OMA", "OMN");
		OVERRIDES.put("PAR", "PRY");
		OVERRIDES.put("PHI", "PHL");
		OVERRIDES.put("POR", "PRT");
		OVERRIDES.put("PUR", "PRI");
		OVERRIDES.put("RHO", "ZWE");
		OVERRIDES.put("RSA", "ZAF");
		OVERRIDES.put("SAM", "WSM");
		OVERRIDES.put("SEY", "SYC");
		OVERRIDES.put("SIN", "SGP");
		OVERRIDES.put("SLO", "SVN");
		OVERRIDES.put("SOL", "SLB");
		OVERRIDES.put("SRI", "LKA");
		OVERRIDES.put("SUD", "SDN");
		OVERRIDES.put("SUI", "CHE");
		OVERRIDES.put("TAN", "TZA");
		OVERRIDES.put("TCH", "CZE");
		OVERRIDES.put("TOG", "TGO");
		OVERRIDES.put("TPE", "TWN");
		OVERRIDES.put("TRI", "TTO");
		OVERRIDES.put("UAE", "ARE");
		OVERRIDES.put("URS", "UZB");
		OVERRIDES.put("URU", "URY");
		OVERRIDES.put("VAN", "VUT");
		OVERRIDES.put("VIE", "VNM");
		OVERRIDES.put("VIN", "FIN");
		OVERRIDES.put("YUG", "SRB");
		OVERRIDES.put("ZAM", "ZMB");
		OVERRIDES.put("ZIM", "ZWE");

		OVERRIDES.put("BIZ", "???");
		OVERRIDES.put("NMI", "???");
		OVERRIDES.put("TKS", "???");
		OVERRIDES.put("UNK", "???");
	}

	public static CountryCode code(String countryId) {
		if (!UNKNOWN.equals(countryId)) {
			String override = OVERRIDES.get(countryId);
			String isoAlpha3 = override == null ? countryId : override;
			if (!UNKNOWN.equals(isoAlpha3)) {
				CountryCode code = CountryCode.getByCode(isoAlpha3);
				if (code != null)
					return code;
				else
					throw new IllegalArgumentException("Unknown country ID: " + countryId);
			}
		}
		return null;
	}

	public static String getISOAlpha2Code(String countryId) {
		CountryCode code = code(countryId);
		return code != null ? code.getAlpha2().toLowerCase() : null;
	}

	public static String getCountryName(String countryId) {
		CountryCode code = code(countryId);
		return code != null ? code.getName() : "Unknown";
	}
}
