DELETE FROM tournament_rank_points;
INSERT INTO tournament_rank_points
(level, result, rank_points, rank_points_2008, goat_points, additive)
VALUES
-- Grand Slam
('G', 'W',   2000, 1000,    8, FALSE),
('G', 'F',   1200,  700,    4, FALSE),
('G', 'SF',   720,  450,    2, FALSE),
('G', 'QF',   360,  250,    1, FALSE),
('G', 'R16',  180,  150, NULL, FALSE),
('G', 'R32',   90,   75, NULL, FALSE),
('G', 'R64',   45,   35, NULL, FALSE),
('G', 'R128',  10,    5, NULL, FALSE),
-- Tour Finals
('F', 'W',   NULL, NULL, NULL, TRUE),
('F', 'F',    500,  250,    2, TRUE),
('F', 'SF',   400,  200,    1, TRUE),
('F', 'RR',   200,  100,    1, TRUE),
-- Masters
('M', 'W',   1000,  500,    4, FALSE),
('M', 'F',    600,  350,    2, FALSE),
('M', 'SF',   360,  225,    1, FALSE),
('M', 'QF',   180,  125, NULL, FALSE),
('M', 'R16',   90,   75, NULL, FALSE),
('M', 'R32',   45,   35, NULL, FALSE),
('M', 'R64',   10,    5, NULL, FALSE),
-- Olympics
('O', 'W',    750,  400,    3, FALSE),
('O', 'F',    450,  280,    2, FALSE),
('O', 'BR',   340,  205,    1, FALSE),
('O', 'SF',   270,  155, NULL, FALSE),
('O', 'QF',   135,  100, NULL, FALSE),
('O', 'R16',   70,   50, NULL, FALSE),
('O', 'R32',   35,   25, NULL, FALSE),
('O', 'R64',    5,    5, NULL, FALSE),
-- ATP 500
('A', 'W',    500,  300,    2, FALSE),
('A', 'F',    300,  210,    1, FALSE),
('A', 'SF',   180,  135, NULL, FALSE),
('A', 'QF',    90,   75, NULL, FALSE),
('A', 'R16',   45,   25, NULL, FALSE),
('A', 'R32', NULL, NULL, NULL, FALSE),
-- ATP 250
('B', 'W',    250,  250,    1, FALSE),
('B', 'F',    150,  175, NULL, FALSE),
('B', 'SF',    90,  110, NULL, FALSE),
('B', 'QF',    45,   60, NULL, FALSE),
('B', 'R16',   20,   25, NULL, FALSE),
('B', 'R32', NULL, NULL, NULL, FALSE),
-- Davis Cup
('D', 'W',     75, NULL, NULL, TRUE),
('D', 'F',     75, NULL,    1, TRUE),
('D', 'SF',    70, NULL, NULL, TRUE),
('D', 'QF',    65, NULL, NULL, TRUE),
('D', 'R16',   40, NULL, NULL, TRUE),
-- Others Team
('T', 'F',   NULL, NULL,    1, TRUE);

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
(rank, rank_factor)
VALUES
( 1, 8),
( 2, 6),
( 3, 5),
( 4, 4),
( 5, 4),
( 6, 3),
( 7, 3),
( 8, 2),
( 9, 2),
(10, 2),
(11, 1),
(12, 1),
(13, 1),
(14, 1),
(15, 1),
(16, 1),
(17, 1),
(18, 1),
(19, 1),
(20, 1);

DELETE FROM weeks_at_no1_goat_points;
INSERT INTO weeks_at_no1_goat_points
(weeks_for_point)
VALUES
(10);

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
('matches', 'Matches', 200, 1),
('grandSlamMatches', 'Grand Slam Matches', 50, 2),
('mastersMatches', 'Masters Matches', 50, 3),
('hardMatches', 'Hard Matches', 100, 4),
('clayMatches', 'Clay Matches', 100, 5),
('grassMatches', 'Grass Matches', 50, 6),
('carpetMatches', 'Carpet Matches', 50, 7),
('decidingSets', 'Deciding Sets', 100, 8),
('fifthSets', 'Fifth Sets', 20, 9),
('finals', 'Finals', 20, 10),
('vsTop10', 'Vs Top 10', 20, 11),
('afterWinningFirstSet', 'After Winning First Set', 100, 12),
('afterLosingFirstSet', 'After Losing First Set', 100, 13),
('tieBreaks', 'Tie Breaks', 100, 14);

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
('mastersMatches', 1, 4),
('mastersMatches', 2, 2),
('mastersMatches', 3, 1),
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
('firstServePct', 'First Serve %', 10000, 3),
('firstServeWonPct', 'First Serve Won %', 10000, 4),
('secondServeWonPct', 'Second Serve Won %', 10000, 5),
('breakPointsSavedPct', 'Break Points Saved %', 10000, 6),
('servicePointsWonPct', 'Service Points Won %', 10000, 7),
('serviceGamesWonPct', 'Service Games Won %', 10000, 8),
-- Return
('firstServeReturnWonPct', 'First Serve Return Won %', 10000, 9),
('secondServeReturnWonPct', 'Second Serve Return Won %', 10000, 10),
('breakPointsPct', 'Break Points %', 10000, 11),
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

