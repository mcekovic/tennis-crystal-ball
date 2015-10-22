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

COMMIT;

