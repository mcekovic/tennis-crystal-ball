DO $$ BEGIN

-- Grand Slam

-- Australian Open
PERFORM set_tournament_map_properties('580', NULL, NULL, '{"center": {"lat": -37.8217558, "lng": 144.9799148}, "zoom": 17}'::JSON);
-- Roland Garros
PERFORM set_tournament_map_properties('520', NULL, NULL, '{"center": {"lat": 48.8471083, "lng": 2.2470515}, "zoom": 17}'::JSON);
-- Wimbledon
PERFORM set_tournament_map_properties('540', NULL, NULL, '{"center": {"lat": 51.4340738, "lng": -0.2137516}, "zoom": 17}'::JSON);
-- US Open
PERFORM set_tournament_map_properties('560', NULL, NULL, '{"center": {"lat": 40.7498881, "lng": -73.8463945}, "zoom": 17}'::JSON);


-- Tour Finals

-- London
PERFORM set_tournament_map_properties('605', 2009, NULL, '{"center": {"lat": 51.5030165, "lng": 0.0035802}, "zoom": 17}'::JSON);

END $$;

COMMIT;
