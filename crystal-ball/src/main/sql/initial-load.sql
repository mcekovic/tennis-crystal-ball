DELETE FROM tournament_rank_points;
INSERT INTO tournament_rank_points
(level, result, rank_points, rank_points_2008, goat_points)
VALUES
-- Grand Slam
('G', 'W',   2000, 1000,    8),
('G', 'F',   1200,  700,    4),
('G', 'SF',   720,  450,    2),
('G', 'QF',   360,  250,    1),
('G', 'R16',  180,  150, NULL),
('G', 'R32',   90,   75, NULL),
('G', 'R64',   45,   35, NULL),
('G', 'R128',  10,    5, NULL),
-- Tour Finals
('F', 'W',   NULL, NULL, NULL),
('F', 'F',    500,  250,    4),
('F', 'SF',   400,  200,    2),
('F', 'RR',   200,  100, NULL),
-- Masters
('M', 'W',   1000,  500,    4),
('M', 'F',    600,  350,    2),
('M', 'SF',   360,  225,    1),
('M', 'QF',   180,  125, NULL),
('M', 'R16',   90,   75, NULL),
('M', 'R32',   45,   35, NULL),
('M', 'R64',   10,    5, NULL),
-- Olympics
('O', 'W',    750,  400,    3),
('O', 'F',    450,  280,    2),
('O', 'BR',   340,  205,    1),
('O', 'SF',   270,  155, NULL),
('O', 'QF',   135,  100, NULL),
('O', 'R16',   70,   50, NULL),
('O', 'R32',   35,   25, NULL),
('O', 'R64',    5,    5, NULL),
-- ATP
('A', 'W',    500,  250,    2),
('A', 'F',    300,  175,    1),
('A', 'SF',   180,  110, NULL),
('A', 'QF',    90,   60, NULL),
('A', 'R16',   45,   25, NULL),
('A', 'R32', NULL, NULL, NULL),
-- Davis Cup
('D', 'W',     75, NULL, NULL),
('D', 'F',     75, NULL,    1),
('D', 'SF',    70, NULL, NULL),
('D', 'QF',    65, NULL, NULL),
('D', 'R16',   40, NULL, NULL);

COMMIT;

