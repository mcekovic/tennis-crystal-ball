package org.strangeforest.tcb.util;

import java.util.*;

import com.neovisionaries.i18n.*;

public abstract class CountryUtil {

	public static final String UNKNOWN = "???";

	private static final Map<String, String> OVERRIDES = new HashMap<String, String>() {{
		put("AHO", "NLD");
		put("ALG", "DZA");
		put("ANG", "AGO");
		put("ANZ", "AUS");
		put("ARU", "ABW");
		put("ASA", "ASM");
		put("BAH", "BHS");
		put("BAN", "BGD");
		put("BAR", "BRB");
		put("BER", "BMU");
		put("BOT", "BWA");
		put("BRI", "GBR");
		put("BRU", "BRN");
		put("BUL", "BGR");
		put("CAL", "FRA");
		put("CAM", "KHM");
		put("CAR", "CAN");
		put("CAY", "CYM");
		put("CEY", "LKA");
		put("CGO", "COG");
		put("CHI", "CHL");
		put("CRC", "CRI");
		put("CRO", "HRV");
		put("DEN", "DNK");
		put("ECA", "ATG");
		put("ESA", "SLV");
		put("FIJ", "FJI");
		put("FRG", "DEU");
		put("GER", "DEU");
		put("GRE", "GRC");
		put("GRN", "GRL");
		put("GUA", "GTM");
		put("GUD", "FRA");
		put("HAI", "HTI");
		put("HAW", "USA");
		put("HON", "HND");
		put("INA", "IDN");
		put("IRI", "IRN");
		put("ISV", "IMN");
		put("KSA", "SAU");
		put("KUW", "KWT");
		put("LAT", "LVA");
		put("LBA", "LBY");
		put("LES", "LSO");
		put("LIB", "LBN");
		put("MAD", "MDG");
		put("MAS", "MYS");
		put("MGL", "MNG");
		put("MON", "MCO");
		put("MRI", "MUS");
		put("MYA", "MMR");
		put("NCA", "NIC");
		put("NED", "NLD");
		put("NEP", "NPL");
		put("NGR", "NER");
		put("NIG", "NER");
		put("OMA", "OMN");
		put("PAR", "PRY");
		put("PHI", "PHL");
		put("POR", "PRT");
		put("PUR", "PRI");
		put("RHO", "ZWE");
		put("RSA", "ZAF");
		put("SAM", "WSM");
		put("SEY", "SYC");
		put("SIN", "SGP");
		put("SLO", "SVN");
		put("SOL", "SLB");
		put("SRI", "LKA");
		put("SUD", "SDN");
		put("SUI", "CHE");
		put("TAN", "TZA");
		put("TCH", "CZE");
		put("TOG", "TGO");
		put("TPE", "TWN");
		put("TRI", "TTO");
		put("UAE", "ARE");
		put("URS", "UZB");
		put("URU", "URY");
		put("VAN", "VUT");
		put("VIE", "VNM");
		put("VIN", "FIN");
		put("YUG", "SRB");
		put("ZAM", "ZMB");
		put("ZIM", "ZWE");

		put("BIZ", "???");
		put("NMI", "???");
		put("TKS", "???");
		put("UNK", "???");
	}};

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
		return code != null ? code.getAlpha2().toLowerCase() : "__";
	}

	public static String getCountryName(String countryId) {
		CountryCode code = code(countryId);
		return code != null ? code.getName() : "Unknown";
	}
}
