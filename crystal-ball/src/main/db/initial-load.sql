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
('F', 'KO', 'W',   1500,  750,    6, FALSE),
('F', 'KO', 'F',   1000,  600,    3, FALSE),
('F', 'KO', 'SF',   500,  300,    1, FALSE),
-- Alt. Finals
('L', 'KO', 'W',   1000,  500,    4, FALSE),
('L', 'KO', 'F',    600,  350,    2, FALSE),
('L', 'KO', 'SF',   360,  225,    1, FALSE),
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
-- World Team Cup
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
( 51,  60,   6),
( 61,  70,   5),
( 71,  80,   4),
( 81, 100,   3),
(101, 150,   2),
(151, 200,   1);

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

DELETE FROM weeks_at_no1_goat_points;
INSERT INTO weeks_at_no1_goat_points
(weeks_for_point)
VALUES
(10);

DELETE FROM weeks_at_elo_topn_goat_points;
INSERT INTO weeks_at_elo_topn_goat_points
(rank, weeks_for_point)
VALUES
(1, 10),
(2, 20),
(3, 30),
(4, 50),
(5, 80);

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

DELETE FROM best_surface_elo_rating_goat_points;
INSERT INTO best_surface_elo_rating_goat_points
(best_elo_rating_rank, goat_points)
VALUES
( 1, 8),
( 2, 5),
( 3, 3),
( 4, 2),
( 5, 1);

DELETE FROM grand_slam_goat_points;
INSERT INTO grand_slam_goat_points
(career_grand_slam, season_grand_slam, season_3_grand_slam, grand_slam_holder, consecutive_grand_slam_on_same_event, grand_slam_on_same_event)
VALUES
(8, 8, 2, 4, 1, 0.5);

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
-- Alt. Finals
('L', 'F',  4),
('L', 'SF', 2),
('L', 'QF', 1),
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
-- World Team Cup
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

DELETE FROM records_goat_points;
INSERT INTO records_goat_points
(record_id, rank, goat_points)
VALUES
-- Titles
('Titles', 1, 4),
('Titles', 2, 2),
('Titles', 3, 1),
('GrandSlamTitles', 1, 8),
('GrandSlamTitles', 2, 5),
('GrandSlamTitles', 3, 3),
('GrandSlamTitles', 4, 2),
('GrandSlamTitles', 5, 1),
('TourFinalsTitles', 1, 4),
('TourFinalsTitles', 2, 2),
('TourFinalsTitles', 3, 1),
('AltFinalsTitles', 1, 2),
('AltFinalsTitles', 2, 1),
('MastersTitles', 1, 4),
('MastersTitles', 2, 2),
('MastersTitles', 3, 1),
('OlympicsTitles', 1, 2),
('OlympicsTitles', 2, 1),
('BigTitles', 1, 4),
('BigTitles', 2, 2),
('BigTitles', 3, 1),
('HardTitles', 1, 2),
('HardTitles', 2, 1),
('ClayTitles', 1, 2),
('ClayTitles', 2, 1),
('GrassTitles', 1, 2),
('GrassTitles', 2, 1),
('CarpetTitles', 1, 2),
('CarpetTitles', 2, 1),
('SeasonTitles', 1, 2),
('SeasonTitles', 2, 1),
('SeasonGrandSlamTitles', 1, 4),
('SeasonGrandSlamTitles', 2, 2),
('SeasonGrandSlamTitles', 3, 1),
('SeasonMastersTitles', 1, 2),
('SeasonMastersTitles', 2, 1),
('SeasonBigTitles', 1, 2),
('SeasonBigTitles', 2, 1),
('TournamentTitles', 1, 2),
('TournamentTitles', 2, 1),
('TournamentGrandSlamTitles', 1, 4),
('TournamentGrandSlamTitles', 2, 2),
('TournamentGrandSlamTitles', 3, 1),
('TournamentMastersTitles', 1, 2),
('TournamentMastersTitles', 2, 1),
-- Finals
('Finals', 1, 1),
('GrandSlamFinals', 1, 2),
('GrandSlamFinals', 2, 1),
('TourFinalsFinals', 1, 1),
('MastersFinals', 1, 1),
('BigFinals', 1, 1),
('SeasonGrandSlamFinals', 1, 1),
('TournamentGrandSlamFinals', 1, 1),
-- Semi-Finals
('GrandSlamSemiFinals', 1, 1),
-- Title Winning Pct.
('TitleWinningPct', 1, 1),
('GrandSlamTitleWinningPct', 1, 2),
('GrandSlamTitleWinningPct', 2, 1),
('TourFinalsTitleWinningPct', 1, 1),
('MastersTitleWinningPct', 1, 1),
('BigTitleWinningPct', 1, 1),
-- Title Streaks
('TitleStreak', 1, 2),
('TitleStreak', 2, 1),
('GrandSlamTitleStreak', 1, 2),
('GrandSlamTitleStreak', 2, 1),
('TourFinalsTitleStreak', 1, 1),
('MastersTitleStreak', 1, 1),
('BigTitleStreak', 1, 2),
('BigTitleStreak', 2, 1),
-- Final Streaks
('FinalStreak', 1, 1),
('GrandSlamFinalStreak', 1, 1),
('BigFinalStreak', 1, 1),
-- Youngest/Oldest Champion
('YoungestTournamentChampion', 1, 1),
('YoungestGrandSlamChampion', 1, 2),
('YoungestGrandSlamChampion', 2, 1),
('YoungestTourFinalsChampion', 1, 1),
('YoungestMastersChampion', 1, 1),
('OldestTournamentChampion', 1, 1),
('OldestGrandSlamChampion', 1, 2),
('OldestGrandSlamChampion', 2, 1),
('OldestTourFinalsChampion', 1, 1),
('OldestMastersChampion', 1, 1),
-- Winning Streak
('WinningStreak', 1, 2),
('WinningStreak', 2, 1),
('GrandSlamWinningStreak', 1, 2),
('GrandSlamWinningStreak', 2, 1),
('TourFinalsWinningStreak', 1, 1),
('MastersWinningStreak', 1, 1),
('BigTournamentWinningStreak', 1, 2),
('BigTournamentWinningStreak', 2, 1),
('HardWinningStreak', 1, 1),
('ClayWinningStreak', 1, 1),
('GrassWinningStreak', 1, 1),
('CarpetWinningStreak', 1, 1),
('WinningStreakVsNo1', 1, 1),
('WinningStreakVsTop5', 1, 1),
('WinningStreakVsTop10', 1, 1),
-- Winning Pct
('SeasonWinningPct', 1, 2),
('SeasonWinningPct', 2, 1),
-- ATP Ranking
('WeeksAtATPNo1', 1, 4),
('WeeksAtATPNo1', 2, 2),
('WeeksAtATPNo1', 3, 1),
('WeeksAtATPTop2', 1, 2),
('WeeksAtATPTop2', 2, 1),
('WeeksAtATPTop3', 1, 2),
('WeeksAtATPTop3', 2, 1),
('WeeksAtATPTop5', 1, 1),
('WeeksAtATPTop10', 1, 1),
('ConsecutiveWeeksAtATPNo1', 1, 2),
('ConsecutiveWeeksAtATPNo1', 2, 1),
('ConsecutiveWeeksAtATPTop2', 1, 1),
('ConsecutiveWeeksAtATPTop3', 1, 1),
('EndsOfSeasonAtATPNo1', 1, 4),
('EndsOfSeasonAtATPNo1', 2, 2),
('EndsOfSeasonAtATPNo1', 3, 1),
('EndsOfSeasonAtATPTop2', 1, 2),
('EndsOfSeasonAtATPTop2', 2, 1),
('EndsOfSeasonAtATPTop3', 1, 2),
('EndsOfSeasonAtATPTop3', 2, 1),
('EndsOfSeasonAtATPTop5', 1, 1),
('EndsOfSeasonAtATPTop10', 1, 1),
('YoungestATPNo1', 1, 1),
('OldestATPNo1', 1, 1),
('ATPPoints', 1, 2),
('ATPPoints', 2, 1),
('ATPPointsNo1No2DifferencePct', 1, 1),
-- H2H
('H2HSeriesWinningPct', 1, 2),
('H2HSeriesWinningPct', 2, 1),
-- Titles Won W/O Losing Set
('TitlesWonWOLosingSet', 1, 1),
('GrandSlamTitlesWonWOLosingSet', 1, 2),
('GrandSlamTitlesWonWOLosingSet', 2, 1),
('TourFinalsTitlesWonWOLosingSet', 1, 1),
('MastersTitlesWonWOLosingSet', 1, 1),
-- Mean Opponent Elo Rating
('HighestOpponentEloRating', 1, 1),
('HighestGrandSlamOpponentEloRating', 1, 2),
('HighestGrandSlamOpponentEloRating', 2, 1),
('HighestTourFinalsOpponentEloRating', 1, 1),
('HighestMastersOpponentEloRating', 1, 1);

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
('altFinalsMatches',     'Alt. Tour Finals Matches', 10,  4),
('mastersMatches',       'Masters Matches',          50,  5),
('olympicsMatches',      'Olympics Matches',          5,  6),
('hardMatches',          'Hard Matches',            100,  7),
('clayMatches',          'Clay Matches',            100,  8),
('grassMatches',         'Grass Matches',            50,  9),
('carpetMatches',        'Carpet Matches',           50, 10),
('decidingSets',         'Deciding Sets',           100, 11),
('fifthSets',            'Fifth Sets',               20, 12),
('finals',               'Finals',                   20, 13),
('vsNo1',                'Vs No. 1',                 10, 14),
('vsTop5',               'Vs Top 5',                 20, 15),
('vsTop10',              'Vs Top 10',                20, 16),
('afterWinningFirstSet', 'After Winning First Set', 100, 17),
('afterLosingFirstSet',  'After Losing First Set',  100, 18),
('tieBreaks',            'Tie Breaks',              100, 19),
('decidingSetTBs',       'Deciding Set Tie Breaks',  10, 20);

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
('tourFinalsMatches', 1, 2),
('tourFinalsMatches', 2, 1),
('altFinalsMatches', 1, 1),
('mastersMatches', 1, 2),
('mastersMatches', 2, 1),
('olympicsMatches', 1, 1),
('hardMatches', 1, 2),
('hardMatches', 2, 1),
('clayMatches', 1, 2),
('clayMatches', 2, 1),
('grassMatches', 1, 2),
('grassMatches', 2, 1),
('carpetMatches', 1, 2),
('carpetMatches', 2, 1),
('decidingSets', 1, 2),
('decidingSets', 2, 1),
('fifthSets', 1, 2),
('fifthSets', 2, 1),
('finals', 1, 2),
('finals', 2, 1),
('vsNo1', 1, 4),
('vsNo1', 2, 2),
('vsNo1', 3, 1),
('vsTop5', 1, 4),
('vsTop5', 2, 2),
('vsTop5', 3, 1),
('vsTop10', 1, 4),
('vsTop10', 2, 2),
('vsTop10', 3, 1),
('afterWinningFirstSet', 1, 2),
('afterWinningFirstSet', 2, 1),
('afterLosingFirstSet', 1, 2),
('afterLosingFirstSet', 2, 1),
('tieBreaks', 1, 2),
('tieBreaks', 2, 1),
('decidingSetTBs', 1, 2),
('decidingSetTBs', 2, 1);

DELETE FROM statistics_category;
INSERT INTO statistics_category
(category_id, name, min_entries, sort_order)
VALUES
-- Serve
('acePct', 'Ace %', 10000, 1),
('doubleFaultPct', 'Double Fault %', 10000, 2),
('acesDfsRatio', 'Aces / DFs Ratio', 10000, 3),
('firstServePct', '1st Serve %', 10000, 4),
('firstServeWonPct', '1st Serve Won %', 10000, 5),
('secondServeWonPct', '2nd Serve Won %', 10000, 6),
('breakPointsSavedPct', 'Break Points Saved %', 10000, 7),
('servicePointsWonPct', 'Service Points Won %', 10000, 8),
('serviceGamesWonPct', 'Service Games Won %', 10000, 9),
-- Return
('firstServeReturnWonPct', '1st Serve Return Won %', 10000, 10),
('secondServeReturnWonPct', '2nd Serve Return Won %', 10000, 11),
('breakPointsPct', 'Break Points Won %', 10000, 12),
('returnPointsWonPct', 'Return Points Won %', 10000, 13),
('returnGamesWonPct', 'Return Games Won %', 10000, 14),
-- Total
('pointsDominanceRatio', 'Points Dominance Ratio', 10000, 15),
('gamesDominanceRatio', 'Games Dominance Ratio', 10000, 16),
('breakPointsRatio', 'Break Points Ratio', 10000, 17),
('overPerformingRatio', 'Over-Performing Ratio', 10000, 18),
('totalPointsWonPct', 'Total Points Won %', 10000, 19),
('totalGamesWonPct', 'Total Games Won %', 200, 20),
('setsWonPct', 'Sets Won %', 200, 21);

DELETE FROM statistics_goat_points;
INSERT INTO statistics_goat_points
(category_id, rank, goat_points)
VALUES
-- Serve
('acePct', 1, 1),
('doubleFaultPct', 1, 1),
('acesDfsRatio', 1, 1),
('firstServePct', 1, 1),
('firstServeWonPct', 1, 2),
('firstServeWonPct', 2, 1),
('secondServeWonPct', 1, 2),
('secondServeWonPct', 2, 1),
('breakPointsSavedPct', 1, 1),
('servicePointsWonPct', 1, 2),
('servicePointsWonPct', 2, 1),
('serviceGamesWonPct', 1, 2),
('serviceGamesWonPct', 2, 1),
-- Return
('firstServeReturnWonPct', 1, 2),
('firstServeReturnWonPct', 2, 1),
('secondServeReturnWonPct', 1, 2),
('secondServeReturnWonPct', 2, 1),
('breakPointsPct', 1, 1),
('returnPointsWonPct', 1, 2),
('returnPointsWonPct', 2, 1),
('returnGamesWonPct', 1, 2),
('returnGamesWonPct', 2, 1),
-- Total
('pointsDominanceRatio', 1, 1),
('gamesDominanceRatio', 1, 1),
('breakPointsRatio', 1, 1),
('overPerformingRatio', 1, 1),
('totalPointsWonPct', 1, 2),
('totalPointsWonPct', 2, 1),
('totalGamesWonPct', 1, 2),
('totalGamesWonPct', 2, 1),
('setsWonPct', 1, 2),
('setsWonPct', 2, 1);

COMMIT;