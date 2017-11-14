ALTER TABLE grand_slam_goat_points ADD COLUMN season_3_grand_slam INTEGER;

UPDATE grand_slam_goat_points SET season_3_grand_slam = 2;

ALTER TABLE grand_slam_goat_points ALTER COLUMN season_3_grand_slam SET NOT NULL;