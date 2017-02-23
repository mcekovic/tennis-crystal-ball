DO $$ BEGIN

-- 1970
PERFORM set_tournament_event_surface(1970, 'Casablanca WCT', 'H', FALSE);

-- 1971
PERFORM set_tournament_event_surface(1971, 'New York', 'H', TRUE);
PERFORM set_tournament_event_surface(1971, 'Queen''s Club', 'G', FALSE);
PERFORM set_tournament_event_surface(1971, 'Quebec WCT', 'H', TRUE);
PERFORM set_tournament_event_surface(1971, 'Vancouver WCT', 'H', FALSE);

-- 1972
PERFORM set_tournament_event_surface(1972, 'Des Moines', 'H', TRUE);
PERFORM set_tournament_event_surface(1972, 'Kansas City', 'H', TRUE);
PERFORM set_tournament_event_surface(1972, 'Los Angeles', 'H', TRUE);
PERFORM set_tournament_event_surface(1972, 'New York', 'H', TRUE);
PERFORM set_tournament_event_surface(1972, 'Quebec WCT', 'H', TRUE);
PERFORM set_tournament_event_surface(1972, 'Tanglewood', 'H', FALSE);
PERFORM set_tournament_event_surface(1972, 'Montreal WCT', 'H', FALSE);
PERFORM set_tournament_event_surface(1972, 'Sacramento', 'H', FALSE);
PERFORM set_tournament_event_surface(1972, 'Seattle', 'H', FALSE);
PERFORM set_tournament_event_surface(1972, 'Alamo WCT', 'H', FALSE);
PERFORM set_tournament_event_surface(1972, 'Vancouver WCT', 'H', FALSE);

-- 1973
PERFORM set_tournament_event_surface(1973, 'Calgary', 'H', TRUE);
PERFORM set_tournament_event_surface(1973, 'Vancouver WCT', 'H', FALSE);
PERFORM set_tournament_event_surface(1973, 'Tanglewood', 'H', FALSE);
PERFORM set_tournament_event_surface(1973, 'Seattle', 'H', TRUE);
PERFORM set_tournament_event_surface(1973, 'Quebec', 'H', TRUE);
PERFORM set_tournament_event_surface(1973, 'Tokyo', 'H', FALSE);
PERFORM set_tournament_event_surface(1973, 'New Delhi', 'H', FALSE);
PERFORM set_tournament_event_surface(1973, 'Djkarta', 'H', FALSE);
PERFORM set_tournament_event_surface(1973, 'Christchurch', 'H', FALSE);

END $$;

COMMIT;
