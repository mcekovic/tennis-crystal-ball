-- Grand Slam

-- Australian Open
CALL set_tournament_map_properties('580', NULL, NULL, NULL, '{"center": {"lat": -37.8217558, "lng": 144.9799148}, "zoom": 18}'::JSON);
-- Roland Garros
CALL set_tournament_map_properties('520', NULL, NULL, NULL, '{"center": {"lat": 48.8471083, "lng": 2.2470515}, "zoom": 18}'::JSON);
-- Wimbledon
CALL set_tournament_map_properties('540', NULL, NULL, NULL, '{"center": {"lat": 51.4340738, "lng": -0.2137516}, "zoom": 17}'::JSON);
-- US Open
CALL set_tournament_map_properties('560', NULL, NULL, NULL, '{"center": {"lat": 40.7498881, "lng": -73.8463945}, "zoom": 18}'::JSON);


-- Tour Finals

-- London
CALL set_tournament_map_properties('605', 2009, NULL, NULL, '{"center": {"lat": 51.5030165, "lng": 0.0035802}, "zoom": 17}'::JSON);
-- Shanghai
CALL set_tournament_map_properties('605', 2005, 2008, NULL, '{"center": {"lat": 31.0418772, "lng": 121.3546204}, "zoom": 18}'::JSON);


-- Masters 1000

-- Indian Wells
CALL set_tournament_map_properties('404', NULL, NULL, NULL, '{"center": {"lat": 33.7236849, "lng": -116.3053698}, "zoom": 18}'::JSON);
-- Miami
CALL set_tournament_map_properties('403', NULL, NULL, NULL, '{"center": {"lat": 25.7089512, "lng": -80.1599014}, "zoom": 18}'::JSON);
-- Monte Carlo
CALL set_tournament_map_properties('410', NULL, NULL, NULL, '{"center": {"lat": 43.7523799, "lng": 7.4413013}, "zoom": 18}'::JSON);
-- Madrid
CALL set_tournament_map_properties('1536', 2009, NULL, NULL, '{"center": {"lat": 40.3694146, "lng": -3.6839787}, "zoom": 18}'::JSON);
-- Rome
CALL set_tournament_map_properties('416', NULL, NULL, NULL, '{"center": {"lat": 41.9289912, "lng": 12.4571116}, "zoom": 18}'::JSON);
-- Toronto
CALL set_tournament_map_properties('421', NULL, 1980, ARRAY[1982, 1984, 1986, 1988, 1990, 1992, 1994, 1996, 1998, 2000, 2002, 2004, 2006, 2008, 2010, 2012, 2014, 2016],
                                      '{"center": {"lat": 43.7706578, "lng": -79.5113256}, "zoom": 18}'::JSON);
-- Montreal
CALL set_tournament_map_properties('421', NULL, 1967, ARRAY[1981, 1983, 1985, 1987, 1989, 1991, 1993, 1995, 1997, 1999, 2001, 2003, 2005, 2007, 2009, 2011, 2013, 2015],
                                      '{"center": {"lat": 45.5333716, "lng": -73.6277015}, "zoom": 18}'::JSON);
-- Cincinnati
CALL set_tournament_map_properties('422', NULL, NULL, NULL, '{"center": {"lat": 39.3490604, "lng": -84.2758254}, "zoom": 18}'::JSON);
-- Shanghai
CALL set_tournament_map_properties('5014', 2009, NULL, NULL, '{"center": {"lat": 31.0418772, "lng": 121.3546204}, "zoom": 18}'::JSON);
-- Paris
CALL set_tournament_map_properties('352', NULL, NULL, NULL, '{"center": {"lat": 48.838524, "lng": 2.3784512}, "zoom": 18}'::JSON);


-- ATP 500

-- Rotterdam
CALL set_tournament_map_properties('407', NULL, NULL, NULL, '{"center": {"lat": 51.8828525, "lng": 4.4881466}, "zoom": 18}'::JSON);

-- Rio de Janeiro
CALL set_tournament_map_properties('6932', NULL, NULL, NULL, '{"center": {"lat": -22.9741851, "lng": -43.2193048}, "zoom": 18}'::JSON);

-- Dubai
CALL set_tournament_map_properties('495', NULL, NULL, NULL, '{"center": {"lat": 25.2392831, "lng": 55.3504042}, "zoom": 18}'::JSON);

-- Acapulco
CALL set_tournament_map_properties('807', NULL, NULL, NULL, '{"center": {"lat": 16.7881978, "lng": -99.8118322}, "zoom": 18}'::JSON);

-- Barcelona
CALL set_tournament_map_properties('425', NULL, NULL, NULL, '{"center": {"lat": 41.3935601, "lng": 2.1173231}, "zoom": 18}'::JSON);

-- Queens
CALL set_tournament_map_properties('311', NULL, NULL, NULL, '{"center": {"lat": 51.4878388, "lng": -0.2116654}, "zoom": 18}'::JSON);

-- Halle
CALL set_tournament_map_properties('500', NULL, NULL, NULL, '{"center": {"lat": 52.0636084, "lng": 8.3493754}, "zoom": 18}'::JSON);

-- Hamburg
CALL set_tournament_map_properties('414', NULL, NULL, NULL, '{"center": {"lat": 53.57357, "lng": 9.9908494}, "zoom": 18}'::JSON);

-- Washington
CALL set_tournament_map_properties('418', NULL, NULL, NULL, '{"center": {"lat": 38.954078, "lng": -77.0377643}, "zoom": 18}'::JSON);

-- Tokyo 
CALL set_tournament_map_properties('329', NULL, NULL, NULL, '{"center": {"lat": 35.6895768, "lng": 139.6927919}, "zoom": 18}'::JSON);

-- Beijing 
CALL set_tournament_map_properties('747', NULL, NULL, NULL, '{"center": {"lat": 39.8506515, "lng": 116.413618}, "zoom": 18}'::JSON);

-- Vienna
CALL set_tournament_map_properties('337', NULL, NULL, NULL, '{"center": {"lat": 48.2024032, "lng": 16.3330703}, "zoom": 18}'::JSON);

-- Basel 
CALL set_tournament_map_properties('328', NULL, NULL, NULL, '{"center": {"lat": 47.5391808, "lng": 7.6186382}, "zoom": 18}'::JSON);

COMMIT;
