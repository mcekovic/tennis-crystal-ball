ALTER TABLE set_score ADD COLUMN w_tbs SMALLINT;
ALTER TABLE set_score ADD COLUMN l_tbs SMALLINT;

UPDATE set_score
SET w_tbs = CASE WHEN w_games = l_games + 1 AND l_games >= 6 THEN 1 ELSE NULL END,
    l_tbs = CASE WHEN l_games = w_games + 1 AND w_games >= 6 THEN 1 ELSE NULL END;

COMMIT;