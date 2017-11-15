ALTER TABLE match ADD COLUMN w_tbs SMALLINT;
ALTER TABLE match ADD COLUMN l_tbs SMALLINT;

ALTER TABLE match_stats DROP COLUMN w_win;
ALTER TABLE match_stats DROP COLUMN w_fh_win;
ALTER TABLE match_stats DROP COLUMN w_bh_win;
ALTER TABLE match_stats DROP COLUMN w_uf_err;
ALTER TABLE match_stats DROP COLUMN w_fh_uf_err;
ALTER TABLE match_stats DROP COLUMN w_bh_uf_err;
ALTER TABLE match_stats DROP COLUMN w_fc_err;
ALTER TABLE match_stats DROP COLUMN w_n_pt;
ALTER TABLE match_stats DROP COLUMN w_n_pt_won;
ALTER TABLE match_stats DROP COLUMN l_win;
ALTER TABLE match_stats DROP COLUMN l_fh_win;
ALTER TABLE match_stats DROP COLUMN l_bh_win;
ALTER TABLE match_stats DROP COLUMN l_uf_err;
ALTER TABLE match_stats DROP COLUMN l_fh_uf_err;
ALTER TABLE match_stats DROP COLUMN l_bh_uf_err;
ALTER TABLE match_stats DROP COLUMN l_fc_err;
ALTER TABLE match_stats DROP COLUMN l_n_pt;
ALTER TABLE match_stats DROP COLUMN l_n_pt_won;

UPDATE match m
SET w_tbs = (SELECT count(s.set) FILTER (WHERE s.w_games = s.l_games + 1 AND s.l_games >= 6) FROM set_score s WHERE s.match_id = m.match_id),
    l_tbs = (SELECT count(s.set) FILTER (WHERE s.l_games = s.w_games + 1 AND s.w_games >= 6) FROM set_score s WHERE s.match_id = m.match_id);

COMMIT;