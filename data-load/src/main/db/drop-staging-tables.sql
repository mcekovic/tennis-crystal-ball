DROP FUNCTION stage_player(INTEGER, TEXT, TEXT, TEXT, DATE, TEXT);
DROP FUNCTION stage_ranking(DATE, INTEGER, INTEGER, INTEGER);
DROP FUNCTION stage_match(TEXT, TEXT, TEXT, INTEGER, TEXT, DATE, INTEGER, INTEGER, INTEGER, TEXT, TEXT, TEXT, INTEGER, TEXT, NUMERIC, INTEGER, INTEGER, INTEGER, INTEGER, TEXT, TEXT, TEXT, INTEGER, TEXT, NUMERIC, INTEGER, INTEGER, TEXT, INTEGER, TEXT, INTEGER, INTEGER, INTEGER, INTEGER, INTEGER, INTEGER, INTEGER, INTEGER, INTEGER, INTEGER, INTEGER, INTEGER, INTEGER, INTEGER, INTEGER, INTEGER, INTEGER, INTEGER, INTEGER);

DROP TABLE staging_match;
DROP TABLE staging_ranking;
DROP TABLE staging_player;