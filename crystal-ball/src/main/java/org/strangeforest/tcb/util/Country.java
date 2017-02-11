package org.strangeforest.tcb.util;

import java.util.*;

import com.google.common.collect.*;
import com.neovisionaries.i18n.*;

public class Country {

	public static final String UNKNOWN_ID = "???";
	public static final String UNKNOWN_NAME = "Unknown";
	private static final String UNKNOWN_CODE = "__";

	private final String countryId;

	public Country(String countryId) {
		this.countryId = countryId;
	}

	public String getId() {
		return countryId;
	}

	public String getCode() {
		CountryCode code = code(countryId);
		return code != null ? code.getAlpha2().toLowerCase() : UNKNOWN_CODE;
	}

	public String getName() {
		CountryCode code = code(countryId);
		return code != null ? code.getName() : UNKNOWN_NAME;
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Country)) return false;
		Country country = (Country)o;
		return Objects.equals(countryId, country.countryId);
	}

	@Override public int hashCode() {
		return Objects.hash(countryId);
	}

	@Override public String toString() {
		return getName() + " (" + countryId + ')';
	}


	// Codes

	private static final Map<String, String> OVERRIDES = ImmutableMap.<String, String>builder()
		.put("AHO", "NLD")
		.put("ALG", "DZA")
		.put("ANG", "AGO")
		.put("ANZ", "AUS")
		.put("ARU", "ABW")
		.put("ASA", "ASM")
		.put("BAH", "BHS")
		.put("BAN", "BGD")
		.put("BAR", "BRB")
		.put("BER", "BMU")
		.put("BOT", "BWA")
		.put("BRI", "GBR")
		.put("BRU", "BRN")
		.put("BUL", "BGR")
		.put("CAL", "FRA")
		.put("CAM", "KHM")
		.put("CAR", "CAN")
		.put("CAY", "CYM")
		.put("CEY", "LKA")
		.put("CGO", "COG")
		.put("CHI", "CHL")
		.put("CRC", "CRI")
		.put("CRO", "HRV")
		.put("DEN", "DNK")
		.put("ECA", "ATG")
		.put("ESA", "SLV")
		.put("FIJ", "FJI")
		.put("FRG", "DEU")
		.put("GER", "DEU")
		.put("GRE", "GRC")
		.put("GRN", "GRL")
		.put("GUA", "GTM")
		.put("GUD", "FRA")
		.put("HAI", "HTI")
		.put("HAW", "USA")
		.put("HON", "HND")
		.put("INA", "IDN")
		.put("IRI", "IRN")
		.put("ISV", "IMN")
		.put("ITF", "ITA")
		.put("KSA", "SAU")
		.put("KUW", "KWT")
		.put("LAT", "LVA")
		.put("LBA", "LBY")
		.put("LES", "LSO")
		.put("LIB", "LBN")
		.put("MAD", "MDG")
		.put("MAS", "MYS")
		.put("MGL", "MNG")
		.put("MON", "MCO")
		.put("MTN", "MNE")
		.put("MRI", "MUS")
		.put("MYA", "MMR")
		.put("NCA", "NIC")
		.put("NED", "NLD")
		.put("NEP", "NPL")
		.put("NGR", "NER")
		.put("NIG", "NER")
		.put("OMA", "OMN")
		.put("PAR", "PRY")
		.put("PHI", "PHL")
		.put("POC", "PRI")
		.put("POR", "PRT")
		.put("PUR", "PRI")
		.put("RHO", "ZWE")
		.put("RSA", "ZAF")
		.put("SAM", "WSM")
		.put("SEY", "SYC")
		.put("SIN", "SGP")
		.put("SLO", "SVN")
		.put("SOL", "SLB")
		.put("SRI", "LKA")
		.put("SUD", "SDN")
		.put("SUI", "CHE")
		.put("TAN", "TZA")
		.put("TCH", "CZE")
		.put("TOG", "TGO")
		.put("TPE", "TWN")
		.put("TRI", "TTO")
		.put("UAE", "ARE")
		.put("URS", "UZB")
		.put("URU", "URY")
		.put("VAN", "VUT")
		.put("VIE", "VNM")
		.put("VIN", "FIN")
		.put("YUG", "SRB")
		.put("ZAM", "ZMB")
		.put("ZIM", "ZWE")

		.put("BIZ", "???")
		.put("NMI", "???")
		.put("TKS", "???")
		.put("UNK", "???")
	.build();

	public static CountryCode code(String countryId) {
		if (!UNKNOWN_ID.equals(countryId)) {
			String override = OVERRIDES.get(countryId);
			String isoAlpha3 = override == null ? countryId : override;
			if (!UNKNOWN_ID.equals(isoAlpha3)) {
				CountryCode code = CountryCode.getByCode(isoAlpha3);
				if (code != null)
					return code;
				else
					throw new IllegalArgumentException("Unknown country ID: " + countryId);
			}
		}
		return null;
	}
}
