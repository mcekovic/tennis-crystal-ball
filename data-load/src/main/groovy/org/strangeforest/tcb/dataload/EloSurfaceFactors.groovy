package org.strangeforest.tcb.dataload

import static org.strangeforest.tcb.util.DateUtil.*

abstract class EloSurfaceFactors {


	static hardKFactor(Date date) {
		def season = toLocalDate(date).year
		switch (season) {
			case 1968: return 2.3
			case 1969: return 2.3
			case 1970: return 2.3
			case 1971: return 2.3
			case 1972: return 2.2
			case 1973: return 2.2
			case 1974: return 2.1
			case 1975: return 2.1
			case 1976: return 2.1
			case 1977: return 2.1
			case 1978: return 2.1
			case 1979: return 2.1
			case 1980: return 2.1
			case 1981: return 2.1
			case 1982: return 2.1
			case 1983: return 2.1
			case 1984: return 2.0
			case 1985: return 1.95
			case 1986: return 1.9
			case 1987: return 1.7
			case 1988: return 1.6
			case 1989: return 1.55
			case 1990: return 1.55
			case 1991: return 1.55
			case 1992: return 1.5
			case 1993: return 1.5
			case 1994: return 1.5
			case 1995: return 1.5
			case 1996: return 1.5
			case 1997: return 1.5
			case 1998: return 1.45
			case 1999: return 1.45
			case 2000: return 1.45
			case 2001: return 1.45
			case 2002: return 1.45
			case 2003: return 1.45
			case 2004: return 1.45
			case 2005: return 1.45
			case 2006: return 1.45
			case 2007: return 1.4
			case 2008: return 1.4
			case 2009: return 1.4
			case 2010: return 1.4
			case 2011: return 1.4
			case 2012: return 1.4
			case 2013: return 1.4
			case 2014: return 1.4
			case 2015: return 1.4
			case 2016: return 1.4
			case 2017: return 1.4
			case 2018: return 1.4
			default: return 1.4
		}
	}
}
