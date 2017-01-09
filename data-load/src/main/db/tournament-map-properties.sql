DO $$ BEGIN

-- Grand Slam

-- Australian Open
PERFORM set_tournament_map_properties('580', NULL, NULL, NULL, '{"center": {"lat": -37.8217558, "lng": 144.9799148}, "zoom": 18}'::JSON);
-- Roland Garros
PERFORM set_tournament_map_properties('520', NULL, NULL, NULL, '{"center": {"lat": 48.8471083, "lng": 2.2470515}, "zoom": 18}'::JSON);
-- Wimbledon
PERFORM set_tournament_map_properties('540', NULL, NULL, NULL, '{"center": {"lat": 51.4340738, "lng": -0.2137516}, "zoom": 17}'::JSON);
-- US Open
PERFORM set_tournament_map_properties('560', NULL, NULL, NULL, '{"center": {"lat": 40.7498881, "lng": -73.8463945}, "zoom": 18}'::JSON);


-- Tour Finals

-- London
PERFORM set_tournament_map_properties('605', 2009, NULL, NULL, '{"center": {"lat": 51.5030165, "lng": 0.0035802}, "zoom": 17}'::JSON);
-- Shanghai
PERFORM set_tournament_map_properties('605', 2005, 2008, NULL, '{"center": {"lat": 31.0418772, "lng": 121.3546204}, "zoom": 18}'::JSON);


-- Masters 1000

-- Indian Wells
PERFORM set_tournament_map_properties('404', NULL, NULL, NULL, '{"center": {"lat": 33.7236849, "lng": -116.3053698}, "zoom": 18}'::JSON);
-- Miami
PERFORM set_tournament_map_properties('403', NULL, NULL, NULL, '{"center": {"lat": 25.7089512, "lng": -80.1599014}, "zoom": 18}'::JSON);
-- Monte Carlo
PERFORM set_tournament_map_properties('410', NULL, NULL, NULL, '{"center": {"lat": 43.7523799, "lng": 7.4413013}, "zoom": 18}'::JSON);
-- Madrid
PERFORM set_tournament_map_properties('1536', 2009, NULL, NULL, '{"center": {"lat": 40.3694146, "lng": -3.6839787}, "zoom": 18}'::JSON);
-- Rome
PERFORM set_tournament_map_properties('416', NULL, NULL, NULL, '{"center": {"lat": 41.9289912, "lng": 12.4571116}, "zoom": 18}'::JSON);
-- Toronto
PERFORM set_tournament_map_properties('421', NULL, 1980, ARRAY[1982, 1984, 1986, 1988, 1990, 1992, 1994, 1996, 1998, 2000, 2002, 2004, 2006, 2008, 2010, 2012, 2014, 2016],
                                      '{"center": {"lat": 43.7706578, "lng": -79.5113256}, "zoom": 18}'::JSON);
-- Montreal
PERFORM set_tournament_map_properties('421', NULL, 1967, ARRAY[1981, 1983, 1985, 1987, 1989, 1991, 1993, 1995, 1997, 1999, 2001, 2003, 2005, 2007, 2009, 2011, 2013, 2015],
                                      '{"center": {"lat": 45.5333716, "lng": -73.6277015}, "zoom": 18}'::JSON);
-- Cincinnati
PERFORM set_tournament_map_properties('422', NULL, NULL, NULL, '{"center": {"lat": 39.3490604, "lng": -84.2758254}, "zoom": 18}'::JSON);
-- Shanghai
PERFORM set_tournament_map_properties('5014', 2009, NULL, NULL, '{"center": {"lat": 31.0418772, "lng": 121.3546204}, "zoom": 18}'::JSON);
-- Paris
PERFORM set_tournament_map_properties('352', NULL, NULL, NULL, '{"center": {"lat": 48.838524, "lng": 2.3784512}, "zoom": 18}'::JSON);

END $$;

COMMIT;
