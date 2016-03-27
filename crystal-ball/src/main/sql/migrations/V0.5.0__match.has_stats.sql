ALTER TABLE match ADD has_stats BOOLEAN;

UPDATE match m
SET has_stats = exists(SELECT s.match_id FROM match_stats s WHERE s.match_id = m.match_id);

COMMIT;