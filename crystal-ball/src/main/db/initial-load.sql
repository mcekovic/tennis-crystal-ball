DELETE FROM tournament_rank_points;
INSERT INTO tournament_rank_points
(level, draw_type, result, rank_points, rank_points_2008, goat_points, additive)
VALUES
-- Grand Slam
('G', 'KO', 'W',   2000, 1000,    8, FALSE),
('G', 'KO', 'F',   1200,  700,    4, FALSE),
('G', 'KO', 'SF',   720,  450,    2, FALSE),
('G', 'KO', 'QF',   360,  250,    1, FALSE),
('G', 'KO', 'R16',  180,  150, NULL, FALSE),
('G', 'KO', 'R32',   90,   75, NULL, FALSE),
('G', 'KO', 'R64',   45,   35, NULL, FALSE),
('G', 'KO', 'R128',  10,    5, NULL, FALSE),
-- Tour Finals
('F', 'RR', 'W',   NULL, NULL, NULL, TRUE),
('F', 'RR', 'F',    500,  250,    2, TRUE),
('F', 'RR', 'SF',   400,  200,    1, TRUE),
('F', 'RR', 'RR',   200,  100,    1, TRUE),
('F', 'KO', 'W',   NULL, NULL,    6, FALSE),
('F', 'KO', 'F',   NULL, NULL,    3, FALSE),
('F', 'KO', 'SF',  NULL, NULL,    1, FALSE),
-- Masters
('M', 'KO', 'W',   1000,  500,    4, FALSE),
('M', 'KO', 'F',    600,  350,    2, FALSE),
('M', 'KO', 'SF',   360,  225,    1, FALSE),
('M', 'KO', 'QF',   180,  125, NULL, FALSE),
('M', 'KO', 'R16',   90,   75, NULL, FALSE),
('M', 'KO', 'R32',   45,   35, NULL, FALSE),
('M', 'KO', 'R64',   10,    5, NULL, FALSE),
-- Olympics
('O', 'KO', 'W',    750,  400,    3, FALSE),
('O', 'KO', 'F',    450,  280,    2, FALSE),
('O', 'KO', 'BR',   340,  205,    1, FALSE),
('O', 'KO', 'SF',   270,  155, NULL, FALSE),
('O', 'KO', 'QF',   135,  100, NULL, FALSE),
('O', 'KO', 'R16',   70,   50, NULL, FALSE),
('O', 'KO', 'R32',   35,   25, NULL, FALSE),
('O', 'KO', 'R64',    5,    5, NULL, FALSE),
-- ATP 500
('A', 'KO', 'W',    500,  300,    2, FALSE),
('A', 'KO', 'F',    300,  210,    1, FALSE),
('A', 'KO', 'SF',   180,  135, NULL, FALSE),
('A', 'KO', 'QF',    90,   75, NULL, FALSE),
('A', 'KO', 'R16',   45,   25, NULL, FALSE),
('A', 'KO', 'R32', NULL, NULL, NULL, FALSE),
-- ATP 250
('B', 'KO', 'W',    250,  250,    1, FALSE),
('B', 'KO', 'F',    150,  175, NULL, FALSE),
('B', 'KO', 'SF',    90,  110, NULL, FALSE),
('B', 'KO', 'QF',    45,   60, NULL, FALSE),
('B', 'KO', 'R16',   20,   25, NULL, FALSE),
('B', 'KO', 'R32', NULL, NULL, NULL, FALSE),
-- Davis Cup
('D', 'KO', 'W',     75, NULL, NULL, TRUE),
('D', 'KO', 'F',     75, NULL,    1, TRUE),
('D', 'KO', 'SF',    70, NULL, NULL, TRUE),
('D', 'KO', 'QF',    65, NULL, NULL, TRUE),
('D', 'KO', 'R16',   40, NULL, NULL, TRUE),
-- Others Team
('T', 'KO', 'F',   NULL, NULL,    1, TRUE);

DELETE FROM tournament_event_rank_factor;
INSERT INTO tournament_event_rank_factor
(rank_from, rank_to, rank_factor)
VALUES
(  1,   1, 100),
(  2,   2,  85),
(  3,   3,  75),
(  4,   4,  67),
(  5,   5,  60),
(  6,   6,  55),
(  7,   8,  50),
(  9,  10,  45),
( 11,  13,  40),
( 14,  16,  35),
( 17,  20,  30),
( 21,  25,  25),
( 26,  30,  20),
( 31,  35,  16),
( 36,  40,  13),
( 41,  45,  10),
( 46,  50,   8),
( 51,  55,   6),
( 56,  60,   5),
( 61,  69,   4),
( 70,  79,   3),
( 80,  99,   2),
(101, 200,   1);

DELETE FROM year_end_rank_goat_points;
INSERT INTO year_end_rank_goat_points
(year_end_rank, goat_points)
VALUES
(1, 8),
(2, 5),
(3, 3),
(4, 2),
(5, 1);

DELETE FROM best_rank_goat_points;
INSERT INTO best_rank_goat_points
(best_rank, goat_points)
VALUES
(1, 8),
(2, 5),
(3, 3),
(4, 2),
(5, 1);

DELETE FROM best_elo_rating_goat_points;
INSERT INTO best_elo_rating_goat_points
(best_elo_rating_rank, goat_points)
VALUES
( 1, 16),
( 2, 12),
( 3,  9),
( 4,  7),
( 5,  5),
( 6,  4),
( 7,  3),
( 8,  2),
( 9,  1),
(10,  1);

DELETE FROM weeks_at_no1_goat_points;
INSERT INTO weeks_at_no1_goat_points
(weeks_for_point)
VALUES
(10);

DELETE FROM big_win_match_factor;
INSERT INTO big_win_match_factor
(level, round, match_factor)
VALUES
-- Grand Slam
('G', 'F',   8),
('G', 'SF',  4),
('G', 'QF',  2),
('G', 'R16', 1),
-- Tour Finals
('F', 'F',  6),
('F', 'SF', 3),
('F', 'QF', 1),
('F', 'RR', 1),
-- Masters
('M', 'F',  4),
('M', 'SF', 2),
('M', 'QF', 1),
-- Olympics
('O', 'F',  3),
('O', 'BR', 1),
('O', 'SF', 1),
-- ATP 500
('A', 'F',  2),
('A', 'SF', 1),
-- ATP 250
('B', 'F',  1),
-- Davis Cup
('D', 'F',  1),
-- Others Team
('T', 'F',  1);

DELETE FROM big_win_rank_factor;
INSERT INTO big_win_rank_factor
(rank_from, rank_to, rank_factor)
VALUES
( 1,  1, 8),
( 2,  2, 6),
( 3,  3, 5),
( 4,  5, 4),
( 6,  7, 3),
( 8, 10, 2),
(11, 20, 1);

DELETE FROM h2h_rank_factor;
INSERT INTO h2h_rank_factor
(rank_from, rank_to, rank_factor)
VALUES
( 1,  1, 8),
( 2,  2, 6),
( 3,  3, 5),
( 4,  5, 4),
( 6,  7, 3),
( 8, 10, 2),
(11, 20, 1);

DELETE FROM grand_slam_goat_points;
INSERT INTO grand_slam_goat_points
(career_grand_slam, season_grand_slam)
VALUES
(8, 8);

DELETE FROM best_season_goat_points;
INSERT INTO best_season_goat_points
(season_rank, goat_points)
VALUES
(1, 8),
(2, 5),
(3, 3),
(4, 2),
(5, 1);

DELETE FROM greatest_rivalries_goat_points;
INSERT INTO greatest_rivalries_goat_points
(rivalry_rank, goat_points)
VALUES
(1, 8),
(2, 5),
(3, 3),
(4, 2),
(5, 1);

DELETE FROM performance_category;
INSERT INTO performance_category
(category_id, name, min_entries, sort_order)
VALUES
('matches',              'Overall Matches',         200,  1),
('grandSlamMatches',     'Grand Slam Matches',       50,  2),
('tourFinalsMatches',    'Tour Finals Matches',      10,  3),
('mastersMatches',       'Masters Matches',          50,  4),
('olympicsMatches',      'Olympics Matches',          5,  5),
('hardMatches',          'Hard Matches',            100,  6),
('clayMatches',          'Clay Matches',            100,  7),
('grassMatches',         'Grass Matches',            50,  8),
('carpetMatches',        'Carpet Matches',           50,  9),
('decidingSets',         'Deciding Sets',           100, 10),
('fifthSets',            'Fifth Sets',               20, 11),
('finals',               'Finals',                   20, 12),
('vsNo1',                'Vs No. 1',                 10, 13),
('vsTop5',               'Vs Top 5',                 20, 14),
('vsTop10',              'Vs Top 10',                20, 15),
('afterWinningFirstSet', 'After Winning First Set', 100, 16),
('afterLosingFirstSet',  'After Losing First Set',  100, 17),
('tieBreaks',            'Tie Breaks',              100, 18);

DELETE FROM performance_goat_points;
INSERT INTO performance_goat_points
(category_id, rank, goat_points)
VALUES
('matches', 1, 4),
('matches', 2, 2),
('matches', 3, 1),
('grandSlamMatches', 1, 4),
('grandSlamMatches', 2, 2),
('grandSlamMatches', 3, 1),
('tourFinalsMatches', 1, 4),
('tourFinalsMatches', 2, 2),
('tourFinalsMatches', 3, 1),
('mastersMatches', 1, 4),
('mastersMatches', 2, 2),
('mastersMatches', 3, 1),
('olympicsMatches', 1, 2),
('olympicsMatches', 2, 1),
('hardMatches', 1, 4),
('hardMatches', 2, 2),
('hardMatches', 3, 1),
('clayMatches', 1, 4),
('clayMatches', 2, 2),
('clayMatches', 3, 1),
('grassMatches', 1, 4),
('grassMatches', 2, 2),
('grassMatches', 3, 1),
('carpetMatches', 1, 4),
('carpetMatches', 2, 2),
('carpetMatches', 3, 1),
('decidingSets', 1, 4),
('decidingSets', 2, 2),
('decidingSets', 3, 1),
('fifthSets', 1, 4),
('fifthSets', 2, 2),
('fifthSets', 3, 1),
('finals', 1, 4),
('finals', 2, 2),
('finals', 3, 1),
('vsNo1', 1, 4),
('vsNo1', 2, 2),
('vsNo1', 3, 1),
('vsTop5', 1, 4),
('vsTop5', 2, 2),
('vsTop5', 3, 1),
('vsTop10', 1, 4),
('vsTop10', 2, 2),
('vsTop10', 3, 1),
('afterWinningFirstSet', 1, 4),
('afterWinningFirstSet', 2, 2),
('afterWinningFirstSet', 3, 1),
('afterLosingFirstSet', 1, 4),
('afterLosingFirstSet', 2, 2),
('afterLosingFirstSet', 3, 1),
('tieBreaks', 1, 4),
('tieBreaks', 2, 2),
('tieBreaks', 3, 1);

DELETE FROM statistics_category;
INSERT INTO statistics_category
(category_id, name, min_entries, sort_order)
VALUES
-- Serve
('acePct', 'Ace %', 10000, 1),
('doubleFaultPct', 'Double Fault %', 10000, 2),
('firstServePct', '1st Serve %', 10000, 3),
('firstServeWonPct', '1st Serve Won %', 10000, 4),
('secondServeWonPct', '2nd Serve Won %', 10000, 5),
('breakPointsSavedPct', 'Break Points Saved %', 10000, 6),
('servicePointsWonPct', 'Service Points Won %', 10000, 7),
('serviceGamesWonPct', 'Service Games Won %', 10000, 8),
-- Return
('firstServeReturnWonPct', '1st Serve Return Won %', 10000, 9),
('secondServeReturnWonPct', '2nd Serve Return Won %', 10000, 10),
('breakPointsPct', 'Break Points Won %', 10000, 11),
('returnPointsWonPct', 'Return Points Won %', 10000, 12),
('returnGamesWonPct', 'Return Games Won %', 10000, 13),
-- Total
('pointsDominanceRatio', 'Points Dominance Ratio', 10000, 14),
('gamesDominanceRatio', 'Games Dominance Ratio', 10000, 15),
('breakPointsRatio', 'Break Points Ratio', 10000, 15),
('overPerformingRatio', 'Over-Performing Ratio', 10000, 17),
('totalPointsWonPct', 'Total Points Won %', 10000, 18),
('totalGamesWonPct', 'Total Games Won %', 200, 19),
('setsWonPct', 'Sets Won %', 200, 20);

DELETE FROM statistics_goat_points;
INSERT INTO statistics_goat_points
(category_id, rank, goat_points)
VALUES
-- Serve
('acePct', 1, 2),
('acePct', 2, 1),
('doubleFaultPct', 1, 2),
('doubleFaultPct', 2, 1),
('firstServePct', 1, 2),
('firstServePct', 2, 1),
('firstServeWonPct', 1, 2),
('firstServeWonPct', 2, 1),
('secondServeWonPct', 1, 2),
('secondServeWonPct', 2, 1),
('breakPointsSavedPct', 1, 2),
('breakPointsSavedPct', 2, 1),
('servicePointsWonPct', 1, 2),
('servicePointsWonPct', 2, 1),
('serviceGamesWonPct', 1, 2),
('serviceGamesWonPct', 2, 1),
-- Return
('firstServeReturnWonPct', 1, 2),
('firstServeReturnWonPct', 2, 1),
('secondServeReturnWonPct', 1, 2),
('secondServeReturnWonPct', 2, 1),
('breakPointsPct', 1, 2),
('breakPointsPct', 2, 1),
('returnPointsWonPct', 1, 2),
('returnPointsWonPct', 2, 1),
('returnGamesWonPct', 1, 2),
('returnGamesWonPct', 2, 1),
-- Total
('pointsDominanceRatio', 1, 2),
('pointsDominanceRatio', 2, 1),
('gamesDominanceRatio', 1, 2),
('gamesDominanceRatio', 2, 1),
('breakPointsRatio', 1, 2),
('breakPointsRatio', 2, 1),
('overPerformingRatio', 1, 2),
('overPerformingRatio', 2, 1),
('totalPointsWonPct', 1, 2),
('totalPointsWonPct', 2, 1),
('totalGamesWonPct', 1, 2),
('totalGamesWonPct', 2, 1),
('setsWonPct', 1, 2),
('setsWonPct', 2, 1);

COMMIT;

