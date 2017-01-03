DO $$ BEGIN

-- Grand Slam

-- Australian Open
PERFORM set_tournament_map_properties('580', NULL, NULL, '{"center": {"lat": -37.8217558, "lng": 144.9799148}, "zoom": 18}'::JSON);
-- Roland Garros
PERFORM set_tournament_map_properties('520', NULL, NULL, '{"center": {"lat": 48.8471083, "lng": 2.2470515}, "zoom": 18}'::JSON);
-- Wimbledon
PERFORM set_tournament_map_properties('540', NULL, NULL, '{"center": {"lat": 51.4340738, "lng": -0.2137516}, "zoom": 17}'::JSON);
-- US Open
PERFORM set_tournament_map_properties('560', NULL, NULL, '{"center": {"lat": 40.7498881, "lng": -73.8463945}, "zoom": 18}'::JSON);


-- Tour Finals

-- London
PERFORM set_tournament_map_properties('605', 2009, NULL, '{"center": {"lat": 51.5030165, "lng": 0.0035802}, "zoom": 17}'::JSON);
-- Shanghai
PERFORM set_tournament_map_properties('605', 2005, 2008, '{"center": {"lat": 31.0418772, "lng": 121.3546204}, "zoom": 18}'::JSON);


-- Masters 1000

-- Indian Wells
PERFORM set_tournament_map_properties('404', NULL, NULL, '{"center": {"lat": 33.7236849, "lng": -116.3053698}, "zoom": 18}'::JSON);
-- Miami
PERFORM set_tournament_map_properties('403', NULL, NULL, '{"center": {"lat": 25.7089512, "lng": -80.1599014}, "zoom": 18}'::JSON);
-- Monte Carlo
PERFORM set_tournament_map_properties('410', NULL, NULL, '{"center": {"lat": 43.7523799, "lng": 7.4413013}, "zoom": 18}'::JSON);
-- Madrid
PERFORM set_tournament_map_properties('1536', 2009, NULL, '{"center": {"lat": 40.3694146, "lng": -3.6839787}, "zoom": 18}'::JSON);
-- Rome
PERFORM set_tournament_map_properties('416', NULL, NULL, '{"center": {"lat": 41.9289912, "lng": 12.4571116}, "zoom": 18}'::JSON);
-- Cincinnati
PERFORM set_tournament_map_properties('422', NULL, NULL, '{"center": {"lat": 39.3490604, "lng": -84.2758254}, "zoom": 18}'::JSON);
-- Shanghai
PERFORM set_tournament_map_properties('5014', 2009, NULL, '{"center": {"lat": 31.0418772, "lng": 121.3546204}, "zoom": 18}'::JSON);
-- Paris
PERFORM set_tournament_map_properties('352', NULL, NULL, '{"center": {"lat": 48.838524, "lng": 2.3784512}, "zoom": 18}'::JSON);

END $$;

COMMIT;
