DO $$ BEGIN

PERFORM fix_rank_points(DATE '1990-12-31', DATE '1990-12-24');
PERFORM fix_rank_points(DATE '1991-12-30', DATE '1991-12-23');
PERFORM fix_rank_points(DATE '1992-12-28', DATE '1992-12-21');
PERFORM fix_rank_points(DATE '1993-12-27', DATE '1993-12-20');
PERFORM fix_rank_points(DATE '1994-12-26', DATE '1994-12-19');
PERFORM fix_rank_points(DATE '1995-12-25', DATE '1995-12-18');

END $$;

COMMIT;


UPDATE player_ranking
SET rank_points = 14
WHERE player_id = (SELECT player_id FROM player_v WHERE name = 'Pat Cash') AND rank_date = DATE '1990-04-16';

COMMIT;
