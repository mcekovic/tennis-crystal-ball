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
-- ATP
('A', 'W',    500,  250,    2, FALSE),
('A', 'F',    300,  175,    1, FALSE),
('A', 'SF',   180,  110, NULL, FALSE),
('A', 'QF',    90,   60, NULL, FALSE),
('A', 'R16',   45,   25, NULL, FALSE),
('A', 'R32', NULL, NULL, NULL, FALSE),
-- Davis Cup
('D', 'W',     75, NULL, NULL, TRUE),
('D', 'F',     75, NULL,    1, TRUE),
('D', 'SF',    70, NULL, NULL, TRUE),
('D', 'QF',    65, NULL, NULL, TRUE),
('D', 'R16',   40, NULL, NULL, TRUE);

DELETE FROM year_end_rank_goat_points;
INSERT INTO year_end_rank_goat_points
(year_end_rank, goat_points)
VALUES
(1, 8),
(2, 5),
(3, 3),
(4, 2),
(5, 1);

DELETE FROM performance_goat_points;
INSERT INTO performance_goat_points
(category , rank, goat_points, sort_order)
VALUES
('matches', 1, 8, 1),
('matches', 2, 5, 1),
('matches', 3, 3, 1),
('matches', 4, 2, 1),
('matches', 5, 1, 1),
('grand_slam_matches', 1, 4, 2),
('grand_slam_matches', 2, 2, 2),
('grand_slam_matches', 3, 1, 2),
('masters_matches', 1, 4, 3),
('masters_matches', 2, 2, 3),
('masters_matches', 3, 1, 3),
('hard_matches', 1, 4, 4),
('hard_matches', 2, 2, 4),
('hard_matches', 3, 1, 4),
('clay_matches', 1, 4, 5),
('clay_matches', 2, 2, 5),
('clay_matches', 3, 1, 5),
('grass_matches', 1, 4, 6),
('grass_matches', 2, 2, 6),
('grass_matches', 3, 1, 6),
('carpet_matches', 1, 4, 7),
('carpet_matches', 2, 2, 7),
('carpet_matches', 3, 1, 7),
('deciding_sets', 1, 4, 8),
('deciding_sets', 2, 2, 8),
('deciding_sets', 3, 1, 8),
('fifth_sets', 1, 4, 9),
('fifth_sets', 2, 2, 9),
('fifth_sets', 3, 1, 9),
('finals', 1, 4, 10),
('finals', 2, 2, 10),
('finals', 3, 1, 10),
('vs_top10', 1, 8, 11),
('vs_top10', 2, 5, 11),
('vs_top10', 3, 3, 11),
('vs_top10', 4, 2, 11),
('vs_top10', 5, 1, 11),
('after_winning_first_set', 1, 4, 12),
('after_winning_first_set', 2, 2, 12),
('after_winning_first_set', 3, 1, 12),
('after_losing_first_set', 1, 4, 13),
('after_losing_first_set', 2, 2, 13),
('after_losing_first_set', 3, 1, 13),
('tie_breaks', 1, 4, 14),
('tie_breaks', 2, 2, 14),
('tie_breaks', 3, 1, 14);

COMMIT;

